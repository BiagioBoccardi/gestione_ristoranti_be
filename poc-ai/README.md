# PoC AI — Ristorante Assistente Intelligente

Chatbot conversazionale che usa **Claude** (Anthropic) con **tool-calling** per interrogare in tempo reale i dati del backend Spring Boot del ristorante.

## Architettura

```
Browser → FastAPI (Python) → Claude API (tool-calling) → Backend Spring Boot
```

Quando l'utente pone una domanda, Claude decide autonomamente quali tool invocare (KPI, top piatti, ordini), ottiene i dati reali e formula una risposta in linguaggio naturale.

## Tool disponibili

| Tool | Endpoint backend | Descrizione |
|------|-----------------|-------------|
| `get_kpi` | `GET /api/analytics/kpi` | Revenue, ordini, coperti medi |
| `get_top_piatti` | `GET /api/analytics/top-piatti` | Classifica piatti più venduti |
| `get_ordini_recenti` | `GET /api/ordini` | Ordini recenti con filtro stato |

## Avvio

### Prerequisiti

- Python 3.11+
- Backend Spring Boot in esecuzione (es. `docker compose up`)
- API Key Anthropic (https://console.anthropic.com)

### Configurazione

```bash
cd poc-ai
cp .env.example .env
# Compila ANTHROPIC_API_KEY, ADMIN_EMAIL, ADMIN_PASSWORD nel file .env
```

### Installazione ed esecuzione

```bash
pip install -r requirements.txt
# Carica variabili d'ambiente
set ANTHROPIC_API_KEY=sk-ant-...   # Windows
export ANTHROPIC_API_KEY=sk-ant-...  # Linux/Mac
set ADMIN_EMAIL=admin@example.com
set ADMIN_PASSWORD=la_tua_password

uvicorn main:app --reload --port 8001
```

Apri http://localhost:8001 nel browser.

### Demo con Docker Compose attivo

Se il backend gira su `http://localhost/api` (docker compose up):

```bash
set BACKEND_URL=http://localhost/api
uvicorn main:app --port 8001
```

## Esempi di domande

- "Quali sono i KPI del ristorante?"
- "Quali sono i 5 piatti più venduti?"
- "Ci sono ordini in attesa di preparazione?"
- "Dammi un riassunto delle performance di oggi"
