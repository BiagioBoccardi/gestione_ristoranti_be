# Gemini

## Introduzione

In questo documento le fasi procedurali per ogni requisito, divise in task con tabelle di fase per backend e frontend. Ogni requisito funzionale e non funzionale è trattato come task indipendente.

L'architettura del progetto è a microservizi: ogni task corrisponde a un servizio specifico o a una responsabilità funzionale distinta all'interno del sistema.

---

## Task 1: Autenticazione e autorizzazione

### Backend

| Titolo                | Fase       | Descrizione                                                              |
| --------------------- | ---------- | ------------------------------------------------------------------------ |
| Modello utente        | Completato | Definire entità Utente, Ruolo, permessi e relazioni con tavoli/ordini.   |
| API di autenticazione | Completato | Creare endpoint REST per login, registrazione e refresh token.           |
| Sicurezza JWT         | Completato | Configurare filtro JWT, generazione token e validazione delle richieste. |
| Autorizzazione RBAC   | Completato | Verificare i controlli di accesso sui servizi in base al ruolo.          |

### Frontend

| Titolo                  | Fase       | Descrizione                                                              |
| ----------------------- | ---------- | ------------------------------------------------------------------------ |
| Schermata login         | Completato | Progettare la UI per l'accesso utente con form e messaggi di errore.     |
| Gestione token          | Completato | Salvare JWT in memoria sicura e includerlo nelle chiamate REST.          |
| Accesso basato su ruolo | Completato | Mostrare/nascondere funzionalità in base al ruolo utente.                |
| Navigazione sicura      | Completato | Controllare redirect e restrizioni di pagina per utenti non autorizzati. |

---

## Task 2: Gestione menu

### Backend

| Titolo                   | Fase       | Descrizione                                                                         |
| ------------------------ | ---------- | ----------------------------------------------------------------------------------- |
| Modello Piatto/Categoria | Completato | Definire entità, attributi e relazioni JPA per menu.                                |
| Servizi CRUD menu        | Completato | Realizzare servizi per creare, leggere, aggiornare e cancellare piatti e categorie. |
| Validazione dati         | Completato | Aggiungere controlli su prezzi, disponibilità e formato delle foto.                 |
| API menu                 | Completato | Verificare endpoint REST per operazioni CRUD.                                       |

### Frontend

| Titolo               | Fase       | Descrizione                                                   |
| -------------------- | ---------- | ------------------------------------------------------------- |
| Pagina menu          | Completato | Creare lista menu, filtro categoria e visualizzazione piatti. |
| Moduli di modifica   | Completato | Costruire form per aggiungere/modificare piatti e categorie.  |
| Upload immagini      | Completato | Gestire upload e anteprima delle foto dei piatti.             |
| Visualizzazione dati | Completato | Confermare che i cambi menu siano visibili in tempo reale.    |

---

## Task 3: Gestione ordini e tavoli

### Backend

| Titolo                | Fase        | Descrizione                                                                       |
| --------------------- | ----------- | --------------------------------------------------------------------------------- |
| Modello Ordine/Tavolo | Completato  | Definire entità Ordine, Tavolo, Relazione con Utente e OrdineItem.                |
| Servizi ordine        | Completato  | Implementare logica per creare, aggiornare, consultare ordini e associare tavoli. |
| Stato ordine/cucina   | Completato  | Gestire transizioni di stato e logica per aggiornamenti cucina.                   |
| API ordini            | Completato  | Verificare gli endpoint di creazione, aggiornamento e consultazione.              |

### Frontend

| Titolo             | Fase       | Descrizione                                                           |
| ------------------ | ---------- | --------------------------------------------------------------------- |
| Interfaccia ordine | Completato | Progettare schermate per inserire ordini e associare un tavolo.       |
| Stato ordini       | Completato | Mostrare stato corrente dell'ordine e aggiornamenti cucina.           |
| Dettaglio tavolo   | Completato | Visualizzare informazioni del tavolo e ordini correlati.              |
| Flusso ordine      | Completato | Controllare che il ciclo di creazione e modifica ordine sia corretto. |

---

## Task 4: Vista cucina in tempo reale

### Backend

| Titolo                   | Fase       | Descrizione                                                         |
| ------------------------ | ---------- | ------------------------------------------------------------------- |
| Configurazione WebSocket | Completato | Definire canali STOMP per ordini e aggiornamenti cucina.            |
| Broker messaggi          | Completato | Configurare il broker e i topic per invio/ricezione in tempo reale. |
| Eventi ordine            | Completato | Emettere aggiornamenti quando un ordine cambia stato.               |
| Test realtime            | Completato | Verificare la ricezione push e la sincronizzazione dello stato.     |

### Frontend

| Titolo                 | Fase       | Descrizione                                                  |
| ---------------------- | ---------- | ------------------------------------------------------------ |
| Connessione WebSocket  | Completato | Stabilire la connessione STOMP dal client React.             |
| Lista ordini live      | Completato | Aggiornare la vista cucina automaticamente con nuovi ordini. |
| Notifiche cambio stato | Completato | Mostrare aggiornamenti quando lo stato dei piatti cambia.    |
| Esperienza utente      | Completato | Verificare aggiornamenti live senza refresh.                 |

---

## Task 5: Prenotazioni

### Backend

| Titolo                  | Fase       | Descrizione                                               |
| ----------------------- | ---------- | --------------------------------------------------------- |
| Modello Prenotazione    | Completato | Definire entità con data, ora, coperti, tavolo e cliente. |
| Servizi di prenotazione | Completato | Creare logica per booking, modifica e conferma.           |
| Notifiche email         | Completato | Configurare invio email di conferma prenotazione.         |
| API prenotazioni        | Completato | Verificare endpoint di gestione prenotazioni.             |

### Frontend

| Titolo                 | Fase       | Descrizione                                                   |
| ---------------------- | ---------- | ------------------------------------------------------------- |
| Schermata prenotazioni | Completato | Creare UI per scegliere data, ora e numero di coperti.        |
| Modifica prenotazioni  | Completato | Consentire aggiornamento e cancellazione delle prenotazioni.  |
| Conferma visiva        | Completato | Visualizzare stato di conferma e dettagli della prenotazione. |
| Validazione input      | Completato | Controllare la correttezza dei campi e messaggi di errore.    |

---

## Task 6: Conto e pagamento

### Backend

| Titolo            | Fase       | Descrizione                                                 |
| ----------------- | ---------- | ----------------------------------------------------------- |
| Modello Conto     | Completato | Definire entità Conto con totale, stato pagamento e metodo. |
| Logica split bill | Completato | Gestire calcolo quote parziali e importi per ogni ordine.   |
| Export PDF/Excel  | Completato | Generare esportazioni con Apache POI per conti e report.    |
| API conto         | Completato | Verificare generazione e download dei documenti.            |

### Frontend

| Titolo                | Fase       | Descrizione                                            |
| --------------------- | ---------- | ------------------------------------------------------ |
| Visualizzazione conto | Completato | Progettare la UI del conto finale e dettaglio prezzi.  |
| Split bill UI         | Completato | Consentire divisione del conto tra partecipanti.       |
| Download documenti    | Completato | Abilitare export PDF/Excel dal client.                 |
| Verifica pagamento    | Completato | Controllare coerenza importi e funzionalità di export. |

---

## Task 7: Dashboard analytics

### Backend

| Titolo           | Fase       | Descrizione                                          |
| ---------------- | ---------- | ---------------------------------------------------- |
| Raccolta dati    | Completato | Definire metriche di revenue, ordini e coperti medi. |
| Report aggregati | Completato | Calcolare statistiche giornaliere e settimanali.     |
| API analytics    | Completato | Esporre endpoint per dati dashboard.                 |
| Validazione dati | Completato | Verificare correttezza delle metriche e dei grafici. |

### Frontend

| Titolo                 | Fase       | Descrizione                                        |
| ---------------------- | ---------- | -------------------------------------------------- |
| Layout dashboard       | Completato | Creare pannello con grafici e indicatori chiave.   |
| Grafici Chart.js       | Completato | Integrare chart per revenue e piatti più venduti.  |
| Aggiornamento dinamico | Completato | Aggiornare dati al refresh o in tempo reale.       |
| Controlli visuali      | Completato | Controllare leggibilità e accuratezza dei grafici. |

---

## Task 8: Gestione staff

### Backend

| Titolo              | Fase       | Descrizione                                  |
| ------------------- | ---------- | -------------------------------------------- |
| Modello staff       | Completato | Definire entità Utente con ruolo e turni.    |
| Servizi utenti      | Completato | Realizzare CRUD per la gestione dello staff. |
| Ruoli e permessi    | Completato | Associare ruoli alle funzioni disponibili.   |
| Test autorizzazione | Completato | Verificare ruoli admin e personale.          |

### Frontend

| Titolo          | Fase       | Descrizione                                                       |
| --------------- | ---------- | ----------------------------------------------------------------- |
| Schermata staff | Completato | Creare interfaccia per creare e modificare utenti.                |
| Gestione turni  | Completato | Aggiungere campi per i turni e la gestione ruoli.                 |
| Restrizioni UI  | Completato | Nascondere funzioni non accessibili ai ruoli non amministrativi.  |
| Verifica ruoli  | Completato | Controllare che le operazioni staff siano limitate correttamente. |

---

## Task 9: QR code per ordinare

### Backend

| Titolo              | Fase       | Descrizione                                                                   |
| ------------------- | ---------- | ----------------------------------------------------------------------------- |
| Generazione QR      | Completato | Creare servizio che genera QR code univoci per tavolo.                        |
| Collegamento tavolo | Completato | Associare QR code alla risorsa tavolo nel database.                           |
| Endpoint menu QR    | Completato | Fornire il menu digitale tramite REST API per il QR.                          |
| Sicurezza QR        | Completato | Verificare che il QR apra il menu corretto e non dia accessi non autorizzati. |

### Frontend

| Titolo                | Fase       | Descrizione                                                            |
| --------------------- | ---------- | ---------------------------------------------------------------------- |
| Pagina menu QR        | Completato | Progettare vista mobile responsive per il menu.                        |
| Accesso da smartphone | Completato | Gestire apertura del menu a partire dal QR scan.                       |
| Invio ordine mobile   | Completato | Permettere l’invio dell’ordine dal device al backend.                  |
| Verifica flusso       | Completato | Controllare che il QR porti al menu corretto e ordini siano trasmessi. |

---

## Task 10: Integrazione frontend-backend

### Backend

| Titolo             | Fase            | Descrizione                                                            |
| ------------------ | --------------- | ---------------------------------------------------------------------- |
| API REST           | Implementazione | Realizzare endpoint per operazioni CRUD di tutte le entità principali. |
| Documentazione API | Implementazione | Descrivere le API e i formati di richiesta/risposta.                   |
| Gestione errori    | Implementazione | Standardizzare risposte di errore e codici HTTP.                       |
| Test end-to-end    | Test            | Verificare comunicazione corretta tra client e server.                 |

### Frontend

| Titolo                | Fase            | Descrizione                                                         |
| --------------------- | --------------- | ------------------------------------------------------------------- |
| Consumo API           | Implementazione | Integrare le chiamate REST nel client React.                        |
| Routing SPA           | Implementazione | Configurare React Router per navigazione e protezione delle rotte.  |
| Gestione errori       | Implementazione | Mostrare messaggi appropriati per errori backend.                   |
| Verifica integrazione | Test            | Controllare che le operazioni frontend/back-end funzionino insieme. |

---
## Task 11: Configurazione Docker

### Backend

| Titolo                  | Fase            | Descrizione                                                                            |
| ----------------------- | --------------- | -------------------------------------------------------------------------------------- |
| Dockerfile backend      | Completato | Creare Dockerfile multi-stage per l'applicazione Spring Boot.                          |
| docker-compose          | Completato | Definire docker-compose.yml con backend, frontend e PostgreSQL.                        |
| Variabili d'ambiente    | Completato | Configurare .env e profili Spring per ambienti dev/prod.                               |
| Rete e volumi           | Completato | Configurare rete interna Docker e volumi persistenti per il database.                  |
| Test container          | Completato | Verificare che tutti i servizi si avviino e comunichino correttamente via docker-compose. |

### Frontend

| Titolo                  | Fase       | Descrizione                                                                  |
| ----------------------- | ---------- | ---------------------------------------------------------------------------- |
| Dockerfile frontend     | Completato | Creare Dockerfile multi-stage con build Vite e serve con Nginx.              |
| Configurazione Nginx    | Completato | Configurare nginx.conf per SPA React con proxy verso il backend.             |
| Variabili build         | Completato | Gestire VITE_API_URL e variabili d'ambiente per ambienti diversi.            |
| Test integrazione       | Completato | Verificare che il frontend containerizzato comunichi correttamente col backend. |

## Task N1: Sicurezza

### Backend

| Titolo            | Fase            | Descrizione                                                           |
| ----------------- | --------------- | --------------------------------------------------------------------- |
| JWT               | Completato | Configurare emissione e validazione JWT per tutte le richieste.       |
| Controllo accessi | Completato | Applicare filtri e autorizzazioni sui servizi.                        |
| Protezione dati   | Completato | Crittografare password e proteggere i dati sensibili.                 |
| Audit             | Completato | Verificare che solo utenti autorizzati possano accedere alle risorse. |

### Frontend

| Titolo             | Fase       | Descrizione                                                  |
| ------------------ | ---------- | ------------------------------------------------------------ |
| UI accesso         | Completato | Proteggere schermate riservate e redirect su logout.         |
| Storage token      | Completato | Gestire i token in modo sicuro nel client.                   |
| Validazione client | Completato | Validare input prima di inviare richieste.                   |
| Test sicurezza     | Completato | Verificare protezione delle pagine e gestione degli accessi. |

---

## Task N2: Prestazioni

### Backend

| Titolo               | Fase            | Descrizione                                    |
| -------------------- | --------------- | ---------------------------------------------- |
| Ottimizzazione query | Implementazione | Migliorare query JPA per liste e report.       |
| Caching              | Implementazione | Applicare cache sui dati frequentemente letti. |
| API rapide           | Implementazione | Ridurre payload e tempo di risposta API.       |
| Monitoraggio         | Test            | Valutare latenza e carico sulle chiamate.      |

### Frontend

| Titolo               | Fase            | Descrizione                                    |
| -------------------- | --------------- | ---------------------------------------------- |
| Rendering efficiente | Implementazione | Minimizzare re-render e componenti pesanti.    |
| Lazy loading         | Implementazione | Caricare risorse e componenti on demand.       |
| Interazione fluida   | Implementazione | Ottimizzare form e grafici per mobile.         |
| Test prestazioni     | Test            | Valutare tempi di caricamento e reattività UI. |

---

## Task N3: Affidabilità

### Backend

| Titolo               | Fase            | Descrizione                                             |
| -------------------- | --------------- | ------------------------------------------------------- |
| Progettazione DB     | Analisi         | Definire schema con relazioni e vincoli referenziali.   |
| Gestione transazioni | Implementazione | Usare transazioni per operazioni critiche.              |
| Backup e recovery    | Implementazione | Pianificare salvataggio e ripristino dei dati.          |
| Test integrità       | Test            | Verificare consistenza dei dati su operazioni multiple. |

### Frontend

| Titolo            | Fase            | Descrizione                                           |
| ----------------- | --------------- | ----------------------------------------------------- |
| Gestione errori   | Implementazione | Gestire fallback e messaggi quando le API falliscono. |
| Stato stabile     | Implementazione | Conservare lo stato applicazione coerente tra pagine. |
| Retry             | Implementazione | Aggiungere retry su chiamate API intermittenti.       |
| Test affidabilità | Test            | Controllare resilienza UI a errori backend.           |

---

## Task N4: Usabilità

### Backend

| Titolo         | Fase       | Descrizione                                                |
| -------------- | ---------- | ---------------------------------------------------------- |
| Supporto API   | Completato | Fornire risposte chiare e strutturate per la UI.           |
| Documentazione | Completato | Documentare endpoint e formati per il frontend.            |
| Consistenza    | Completato | Mantenere formati dati coerenti.                           |
| Test UX        | Completato | Verificare che i dati soddisfino i requisiti di usabilità. |

### Frontend

| Titolo               | Fase       | Descrizione                                  |
| -------------------- | ---------- | -------------------------------------------- |
| UI coerente          | Completato | Creare layout intuitivo per ogni ruolo.      |
| Navigazione semplice | Completato | Organizzare percorsi e menu in modo chiaro.  |
| Feedback utente      | Completato | Mostrare conferme e errori leggibili.        |
| Test usabilità       | Completato | Verificare facilità d'uso con flussi tipici. |

---

## Task N5: Manutenibilità

### Backend

| Titolo                | Fase       | Descrizione                                    |
| --------------------- | ---------- | ---------------------------------------------- |
| Architettura modulare | Completato | Strutturare servizi separati per dominio.      |
| Codice pulito         | Completato | Seguire convenzioni Spring Boot e SOLID.       |
| Documentazione codice | Completato | Commentare e descrivere componenti.            |
| Test unitari          | Completato | Verificare logica di business in modo isolato. |

### Frontend

| Titolo                    | Fase       | Descrizione                                                  |
| ------------------------- | ---------- | ------------------------------------------------------------ |
| Componenti riutilizzabili | Completato | Creare componenti modulabili e configurabili.                |
| Struttura progetto        | Completato | Organizzare cartelle per pagine, componenti e servizi.       |
| Documentazione UI         | Completato | Aggiungere descrizioni e linee guida di utilizzo.            |
| Test di regressione       | Completato | Verificare che modifiche non rompano funzionalità esistenti. |

---

## Task N6: Scalabilità

### Backend

| Titolo             | Fase       | Descrizione                                             |
| ------------------ | ---------- | ------------------------------------------------------- |
| Containerizzazione | Completato | Preparare immagini Docker per il backend.               |
| Deploy scalabile   | Completato | Predisporre la configurazione per ambienti distribuiti. |
| Database scalabile | Completato | Ottimizzare PostgreSQL per carico crescente.            |
| Test carico        | Completato | Simulare carichi per valutare limite di scalabilità.    |

### Frontend

| Titolo            | Fase       | Descrizione                                    |
| ----------------- | ---------- | ---------------------------------------------- |
| Asset ottimizzati | Completato | Minimizzare bundle e usare caching.            |
| Distribuzione CDN | Completato | Pianificare deploy statico su hosting veloce.  |
| Resilienza UI     | Completato | Fare in modo che la UI regga traffico elevato. |
| Test carico       | Completato | Valutare comportamento UI sotto uso intensivo. |

---

## Task N7: Portabilità e deploy

### Backend

| Titolo              | Fase       | Descrizione                                            |
| ------------------- | ---------- | ------------------------------------------------------ |
| Jenkins pipeline    | Completato | Configurare pipeline CI/CD per build e deploy backend. |
| Build automatizzata | Completato | Creare script di build e packaging.                    |
| Deploy container    | Completato | Definire immagini Docker e Compose.                    |
| Test pipeline       | Completato | Verificare processi di build e deploy automatici.      |

### Frontend

| Titolo             | Fase       | Descrizione                                               |
| ------------------ | ---------- | --------------------------------------------------------- |
| Build frontend     | Completato | Configurare build React per produzione.                   |
| Deploy statico     | Completato | Preparare hosting per l'applicazione SPA.                 |
| Integrazione CI/CD | Completato | Collegare build frontend alla pipeline Jenkins.           |
| Test deploy        | Completato | Verificare che il deploy pubblico funzioni correttamente. |

---

## Task N8: Testabilità

### Backend

| Titolo                | Fase            | Descrizione                                     |
| --------------------- | --------------- | ----------------------------------------------- |
| Test JUnit            | Implementazione | Scrivere test unitari per servizi e repository. |
| Test integrazione     | Implementazione | Verificare integrazione tra API e database.     |
| Copertura test        | Monitoraggio    | Misurare copertura del codice backend.          |
| Esecuzione automatica | Test            | Integrare test nella pipeline CI.               |

### Frontend

| Titolo                | Fase            | Descrizione                                   |
| --------------------- | --------------- | --------------------------------------------- |
| Test componenti       | Implementazione | Scrivere test per componenti React.           |
| Test end-to-end       | Implementazione | Eseguire test completi dei flussi utente.     |
| Monitoraggio coverage | Monitoraggio    | Valutare copertura dei test frontend.         |
| Pipeline test         | Test            | Eseguire i test automatici durante il deploy. |

---

## DevOps — Requisiti Esame

### Criteri di valutazione

| Componente | Peso | Requisiti |
| ---------- | ---- | --------- |
| CI/CD pipeline | 2/3 del voto | Minimo 5 pt → massimo 7 pt |
| PoC AI dimostrativo | 1/3 del voto | Progetto separato ammesso |

**Punteggi pipeline CI/CD:**
- **5 pt (minimo):** Git + Snyk + test del codice
- **7 pt (massimo completo):** + SonarQube + Observability (Prometheus + Grafana)

---

### Pipeline Jenkins — Stage implementati

| Stage | Tool | Stato |
| ----- | ---- | ----- |
| Checkout | Git (scm) | Completato |
| Build & Test Backend | Maven + JUnit + JaCoCo | Completato |
| Security Scan | Snyk (backend pom.xml + frontend npm) | Completato |
| Static Analysis | SonarQube + Quality Gate | Completato |
| Build & Test Frontend | npm + Vitest | Completato |
| Docker Build | docker compose build | Completato |
| Push to Docker Hub | docker push (backend + frontend :latest + :buildNumber) | Completato |
| Deploy | docker compose up -d | Completato |
| Smoke Test | curl health + API + frontend | Completato |

---

### Credenziali Jenkins richieste

Prima dell'esame configurare in **Manage Jenkins → Credentials**:

| Credential ID | Tipo | Valore |
| ------------- | ---- | ------ |
| `postgres-user` | Secret text | username PostgreSQL |
| `postgres-password` | Secret text | password PostgreSQL |
| `jwt-secret` | Secret text | JWT secret key |
| `mail-password` | Secret text | Gmail App Password |
| `snyk-token` | Secret text | token da app.snyk.io |
| `dockerhub-credentials` | Username+Password | credenziali Docker Hub |
| `dockerhub-username` | Secret text | username Docker Hub |
| `SonarQube` | (server config) | in Manage Jenkins → Configure System |

---

### Observability — URL di accesso

| Servizio | URL | Credenziali |
| -------- | --- | ----------- |
| Applicazione | http://localhost | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / valore di GRAFANA_PASSWORD |
| Metrics endpoint | http://localhost/api/actuator/prometheus | — |
| Health endpoint | http://localhost/api/actuator/health | — |

---

### Procedura esame (push → pipeline → demo)

1. Impostare `DOCKER_HUB_USERNAME` nel file `.env`
2. Effettuare un commit e push su `main`
3. La pipeline Jenkins si avvia automaticamente (webhook SCM o polling)
4. Al termine della pipeline le immagini sono su Docker Hub
5. Per avviare l'applicazione su qualsiasi macchina: `docker compose up`

---

### PoC AI — Nota

Per il voto pieno (1/3) è richiesto un PoC dimostrativo di applicazione dell'IA al progetto.
Il PoC può essere un **progetto separato** e non deve essere incluso nel repository principale.

Idee ammissibili:
- Chatbot per suggerimenti menu (LLM via API)
- Previsione domanda piatti tramite ML (serie storica ordini)
- Classificazione automatica feedback clienti (NLP)
- Generazione automatica descrizioni piatti (prompt engineering)

---

