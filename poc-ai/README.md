# PoC AI — Restaurant AI Copilot

> **Stato:** documento di design (nessun codice implementato).
> **Obiettivo:** dimostrare l'applicazione dell'Intelligenza Artificiale al progetto
> *Gestione Ristoranti*, come richiesto dal requisito d'esame "PoC AI dimostrativo"
> (1/3 del voto). Il PoC è un **progetto separato** e non fa parte del deliverable
> principale.

Questo documento descrive **come verrebbe implementato** il PoC: architettura,
componenti, integrazione con le API esistenti, integrazione con il modello Claude,
le tre funzionalità dimostrative e le istruzioni d'uso. È pensato per essere
trasformato in codice in un secondo momento.

---

## 1. Idea in una riga

Un **copilot conversazionale** per il proprietario/manager del ristorante che,
usando un LLM (Claude) come motore di ragionamento e le **API REST già esistenti**
del backend Spring Boot come fonte di **dati reali**, è in grado di:

1. **Rispondere in linguaggio naturale** a domande sui dati del ristorante
   (food cost, revenue, piatti più venduti, prenotazioni) tramite *tool calling*.
2. **Generare descrizioni accattivanti** per i piatti del menu (prompt engineering).
3. **Prevedere la domanda** dei piatti nei prossimi giorni sulla serie storica
   degli ordini, con un commento esplicativo generato dall'LLM.

In un'unica demo il PoC copre tre delle "idee ammissibili" elencate nel `CLAUDE.md`.

---

## 2. Perché questo PoC

- **Si aggancia a dati reali.** Il backend espone già endpoint ricchi
  (`/api/analytics/*`, `/api/menu/*`, `/api/ordini/*`). Il PoC non inventa dati:
  interroga il sistema esistente. Questo rende la demo credibile e impressionante.
- **Riusa l'infrastruttura.** Autenticazione JWT, modello dati e logica di business
  sono già pronti: il PoC è un *layer* sottile sopra.
- **È modulare.** Le tre funzionalità sono indipendenti: in sede di demo se ne può
  mostrare anche solo una.

---

## 3. Architettura

```
┌────────────────────┐     HTTP/JSON      ┌─────────────────────────┐
│   Mini UI (demo)   │ ─────────────────► │   Servizio PoC AI       │
│  HTML + JS / React │ ◄───────────────── │   Python + FastAPI      │
└────────────────────┘                    │                         │
                                          │  ┌───────────────────┐  │
                                          │  │ Orchestratore LLM │  │
                                          │  │  (Claude API)     │  │
                                          │  └─────────┬─────────┘  │
                                          │            │ tool calls │
                                          │  ┌─────────▼─────────┐  │
                                          │  │  Backend Client   │  │
                                          │  │  (JWT + REST)     │  │
                                          │  └─────────┬─────────┘  │
                                          └────────────┼────────────┘
                                                       │ REST (Bearer JWT)
                                          ┌────────────▼────────────┐
                                          │  Backend Spring Boot    │
                                          │  /api/analytics, /menu  │
                                          │  /ordini  (dati reali)  │
                                          └────────────┬────────────┘
                                                       │
                                          ┌────────────▼────────────┐
                                          │      PostgreSQL         │
                                          └─────────────────────────┘
```

Il PoC **non accede mai direttamente al database**: passa sempre dalle API REST,
rispettando autenticazione e autorizzazioni esistenti.

---

## 4. Stack tecnologico del PoC

| Componente        | Tecnologia                          | Motivo |
| ----------------- | ----------------------------------- | ------ |
| Servizio          | **Python 3.11 + FastAPI**           | Standard per PoC AI/ML, async, OpenAPI auto |
| SDK LLM           | **`anthropic`** (SDK ufficiale)     | Tool calling, streaming, type-safe |
| Modello           | **`claude-sonnet-4-6`** (default)   | Ottimo rapporto qualità/costo/latenza per chat + tool use |
| HTTP client       | **`httpx`**                         | Chiamate async verso il backend Spring |
| Forecasting       | **`pandas` + statistica leggera**   | Media mobile / regressione su serie storica ordini |
| Config            | **`pydantic-settings` + `.env`**    | Gestione segreti e parametri |
| UI demo           | HTML + Fetch API (o piccola pagina React) | Una singola pagina per mostrare le 3 funzioni |
| Packaging         | **Dockerfile** (opzionale)          | Avvio riproducibile, coerente col resto del progetto |

> **Nota modelli Claude:** per la demo si usa `claude-sonnet-4-6` come default
> (bilanciato). Per risposte più sofisticate si può alzare a `claude-opus-4-8`;
> per ridurre costi/latenza su task semplici (es. descrizioni) si può usare
> `claude-haiku-4-5`. Il modello è configurabile via `.env`.

---

## 5. Struttura del progetto (prevista)

```
poc-ai/
├── README.md                 # questo documento
├── .env.example              # variabili d'ambiente (chiavi, URL backend)
├── requirements.txt          # anthropic, fastapi, uvicorn, httpx, pandas, pydantic-settings
├── Dockerfile                # opzionale, per avvio containerizzato
├── app/
│   ├── main.py               # entrypoint FastAPI, definizione endpoint
│   ├── config.py             # caricamento .env (API key, backend URL, credenziali)
│   ├── backend_client.py     # login + chiamate REST al backend Spring (httpx)
│   ├── llm.py                # wrapper Claude API + definizione tool + loop tool-use
│   ├── tools.py              # mappa "tool LLM" → funzioni del backend_client
│   ├── features/
│   │   ├── chat.py           # Feature 1: Q&A conversazionale sui dati
│   │   ├── descrizioni.py    # Feature 2: generazione descrizioni piatti
│   │   └── forecast.py       # Feature 3: previsione domanda piatti
│   └── prompts/
│       ├── system_chat.txt   # system prompt del copilot
│       └── descrizione.txt   # template prompt per descrizioni
└── ui/
    └── index.html            # mini interfaccia di demo (3 sezioni)
```

---

## 6. Integrazione con il backend esistente

Il `backend_client.py` effettua il login una volta e riusa il JWT (Bearer token)
per tutte le chiamate successive. Gli endpoint REST già disponibili e utili al PoC:

| Endpoint backend                         | Uso nel PoC |
| ---------------------------------------- | ----------- |
| `POST /api/auth/login`                   | Ottiene il JWT di servizio |
| `GET  /api/analytics/kpi?da=&a=`         | Revenue totale, ordini, valore medio, food cost medio |
| `GET  /api/analytics/revenue/giornaliera`| Serie storica revenue (chat + forecast) |
| `GET  /api/analytics/top-piatti`         | Piatti più venduti |
| `GET  /api/analytics/food-cost`          | Food cost per piatto + giudizio |
| `GET  /api/menu/piatti`                  | Elenco piatti (per generare descrizioni) |
| `GET  /api/ordini`                       | Serie storica ordini (per forecast) |

> Gli endpoint esatti vanno verificati sui controller (`AnalyticsController`,
> `MenuController`, `OrdineController`) prima di scrivere il client. La tabella
> riflette le funzionalità esposte dai servizi attuali.

---

## 7. Feature 1 — Chat conversazionale sui dati reali (Tool Calling)

L'utente scrive una domanda in linguaggio naturale; Claude decide **quali dati
recuperare** chiamando dei *tool* che il PoC espone, poi formula la risposta.

**Esempi di domande:**
- *"Quali piatti hanno un food cost critico?"*
- *"Qual è stato il revenue degli ultimi 7 giorni?"*
- *"Quali sono i 3 piatti più venduti e quanto hanno reso?"*

**Tool esposti al modello** (descritti in `tools.py`, eseguiti dal `backend_client`):

| Tool                  | Parametri        | Ritorna |
| --------------------- | ---------------- | ------- |
| `get_kpi`             | `da`, `a` (date) | KPI del periodo |
| `get_revenue_giornaliera` | `giorni`     | serie revenue |
| `get_top_piatti`      | `limit`          | classifica piatti |
| `get_food_cost`       | —                | food cost per piatto |

**Flusso (tool-use loop):**

1. Si invia il messaggio utente a Claude con la lista dei `tools` e un *system prompt*
   che spiega il dominio (ristorante, KPI disponibili, lingua italiana).
2. Se la risposta ha `stop_reason == "tool_use"`, il PoC esegue il tool richiesto
   chiamando il backend reale e rimanda il risultato come `tool_result`.
3. Si ripete finché Claude produce la risposta finale in testo naturale.

```python
# Pseudocodice del loop (app/llm.py)
messages = [{"role": "user", "content": domanda}]
while True:
    resp = client.messages.create(
        model=settings.model,
        system=system_prompt,
        tools=TOOLS,
        messages=messages,
        max_tokens=1024,
    )
    if resp.stop_reason != "tool_use":
        return testo(resp)                       # risposta finale
    messages.append({"role": "assistant", "content": resp.content})
    results = [esegui_tool(b) for b in blocchi_tool(resp)]   # chiama il backend
    messages.append({"role": "user", "content": results})
```

Questo dimostra l'**agentività**: l'LLM non riceve i dati già pronti, ma decide
autonomamente cosa interrogare.

---

## 8. Feature 2 — Generazione descrizioni piatti (Prompt Engineering)

Dato un piatto del menu (nome, categoria, ingredienti/ricetta, prezzo), il PoC
genera una descrizione accattivante e coerente con il tono del locale.

**Flusso:**
1. Recupera il piatto da `GET /api/menu/piatti` (e la ricetta se disponibile).
2. Compone il prompt da `prompts/descrizione.txt` con i dati del piatto e parametri
   di stile (tono: elegante / informale; lunghezza; lingua).
3. Chiama Claude (anche `claude-haiku-4-5` per velocità) e restituisce 1–3 varianti.

**Valore dimostrato:** prompt engineering applicato a un caso d'uso concreto del
dominio, con output direttamente riutilizzabile nel menu digitale / QR.

---

## 9. Feature 3 — Previsione domanda piatti (Serie storica + LLM)

Stima quanti ordini/coperti aspettarsi nei prossimi N giorni, basandosi sulla
serie storica degli ordini.

**Approccio (semplice e spiegabile, adatto a un PoC):**
1. Estrae la serie storica da `GET /api/ordini` / `GET /api/analytics/revenue/giornaliera`.
2. Con `pandas` calcola un *baseline* statistico: media mobile + componente
   settimanale (es. il sabato vende più del lunedì → fattore per giorno della settimana).
3. Produce la previsione numerica per i prossimi giorni.
4. Passa numeri e contesto a Claude che genera un **commento operativo**
   (*"Sabato previsto +30% di coperti: aumenta le scorte dei 3 piatti top e prevedi
   personale extra in cucina"*).

> È un forecasting **euristico/leggero**, non un modello ML addestrato: appropriato
> per un PoC dimostrativo e completamente trasparente. In una versione evoluta si
> potrebbe sostituire con Prophet/ARIMA senza cambiare l'interfaccia.

---

## 10. Mini UI di demo

Una singola pagina (`ui/index.html`) con tre sezioni:
1. **Chat** — casella di testo + area risposte (chiama `POST /chat`).
2. **Descrizioni** — selezione piatto + bottone "Genera" (chiama `POST /descrizione`).
3. **Forecast** — selettore "prossimi N giorni" + grafico/tabella (chiama `POST /forecast`).

Endpoint FastAPI esposti dal PoC:

| Metodo | Path           | Feature |
| ------ | -------------- | ------- |
| `POST` | `/chat`        | Feature 1 |
| `POST` | `/descrizione` | Feature 2 |
| `POST` | `/forecast`    | Feature 3 |
| `GET`  | `/health`      | health check |

---

## 11. Configurazione (`.env.example`)

```dotenv
# Chiave API Claude (da console.anthropic.com)
ANTHROPIC_API_KEY=sk-ant-...

# Modello da usare (default bilanciato)
CLAUDE_MODEL=claude-sonnet-4-6

# Backend Spring Boot da interrogare
BACKEND_BASE_URL=http://localhost:8080

# Credenziali di servizio per ottenere il JWT
BACKEND_USER=admin@ristorante.local
BACKEND_PASSWORD=cambia_questa_password

# Porta del servizio PoC
POC_PORT=8000
```

---

## 12. Come si avvierebbe (una volta implementato)

```bash
cd poc-ai
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env        # compilare ANTHROPIC_API_KEY e credenziali backend
uvicorn app.main:app --reload --port 8000
# poi aprire ui/index.html nel browser
```

Oppure (opzionale) con Docker:

```bash
cd poc-ai
docker build -t poc-ai .
docker run --env-file .env -p 8000:8000 poc-ai
```

Prerequisito: il backend Spring Boot deve essere in esecuzione e raggiungibile
all'`BACKEND_BASE_URL` indicato.

---

## 13. Roadmap implementativa

| # | Fase | Output |
| - | ---- | ------ |
| 1 | Scaffolding `poc-ai/` (FastAPI, requirements, config, .env) | progetto avviabile |
| 2 | `backend_client.py`: login JWT + chiamate analytics/menu/ordini | accesso ai dati reali |
| 3 | `llm.py` + `tools.py`: integrazione Claude con tool-use loop | Feature 1 (chat) |
| 4 | `features/descrizioni.py` + prompt | Feature 2 (descrizioni) |
| 5 | `features/forecast.py` (pandas) + commento LLM | Feature 3 (forecast) |
| 6 | `ui/index.html` con le 3 sezioni | demo cliccabile |
| 7 | Dockerfile + rifinitura README/diagrammi | pacchetto consegnabile |

---

## 14. Note per l'esame

- Il PoC è **separato** dal deliverable principale: non altera backend, frontend
  né la pipeline CI/CD esistenti (coerente col vincolo del `CLAUDE.md`).
- Dimostra **tre** applicazioni dell'IA (chat agentica, generazione testo,
  previsione) su **dati reali** del progetto.
- È **trasparente e spiegabile**: il forecasting è euristico e le risposte chat
  sono tracciabili ai tool/endpoint chiamati.
- Costi contenuti: modelli configurabili (Haiku/Sonnet/Opus) in base al task.

---

## 15. Possibili estensioni future

- Sostituire il forecasting euristico con un modello ML (Prophet/ARIMA).
- Aggiungere **classificazione NLP dei feedback** clienti (4ª idea del `CLAUDE.md`).
- Integrare il copilot direttamente nella dashboard React esistente.
- Aggiungere *prompt caching* del system prompt per ridurre i costi delle chat.
