### App Gestione Ristorante

Progetto Finale — Documentazione Tecnica

### Descrizione del progetto

L'applicazione è una piattaforma web full-stack per la gestione completa di un ristorante. Permette di gestire ordini in tempo reale tra sala e cucina, prenotazioni dei tavoli, menu digitale con aggiornamento dinamico e una dashboard analytics per il proprietario. Il sistema supporta ruoli distinti con interfacce dedicate per ogni tipo di utente.

### Obiettivi

Realizzare un'applicazione web completa con backend REST e front-end reattivo.
Implementare l'autenticazione e l'autorizzazione basata su ruoli (RBAC).
Dimostrare l'uso di WebSocket per la comunicazione in tempo reale.
Applicare pratiche DevOps con pipeline CI/CD tramite Jenkins e Docker.
Produrre una codebase testata con copertura JUnit significativa.

### Microservizi 

Modulo
Funzionalità principali
Gestione menu
Piatti, categorie, prezzi, disponibilità e foto
Ordini & tavoli
Presa ordini, stato cucina, gestione tavoli in sala
Vista cucina (live)
Coda ordini in tempo reale via WebSocket, aggiornamento stato
Prenotazioni
Slot orari, coperti disponibili, conferma via email
Conto & pagamento
Generazione conto, split bill, export PDF
Dashboard analytics
Revenue giornaliera, piatti più venduti, coperti medi
Gestione staff
Creazione utenti, assegnazione ruoli, turni

### Ruoli utente

Ruolo
Accesso e funzionalità
Admin / Proprietario
Gestione completa: menu, staff, analytics, configurazione sistema
Cameriere
Presa ordini, gestione prenotazioni, generazione conto
Cuoco / Cucina
Vista ordini live, aggiornamento stato piatti, segnalazione esaurimento
Cliente (opzionale)
Prenotazione online, menu digitale, ordine da QR code al tavolo

### Stack tecnologico

| Area     | Tecnologia              | Uso principale                                         |
| -------- | ----------------------- | ------------------------------------------------------ |
| Backend  | Spring Boot             | REST API e logica di business                          |
| Backend  | Spring Security + JWT   | Autenticazione e gestione ruoli                        |
| Backend  | WebSocket (STOMP)       | Comunicazione in tempo reale sala ↔ cucina             |
| Backend  | JPA / Hibernate         | ORM per accesso al database                            |
| Backend  | Apache POI              | Export Excel e PDF per report e conti                  |
| Frontend | React + React Router    | SPA con navigazione multi-pagina                       |
| Frontend | Chart.js                | Grafici analytics: revenue, ordini, piatti più venduti |
| Frontend | WebSocket client        | Ricezione aggiornamenti live nella vista cucina        |
| Database | PostgreSQL              | Database relazionale principale                        |
| DevOps   | Jenkins                 | Pipeline CI/CD: build, test, Docker build, deploy      |
| DevOps   | Docker + Docker Compose | Containerizzazione di backend, frontend e database     |

### Architettura del sistema

Il sistema segue un'architettura a tre livelli:

| Livello      | Tecnologia  | Responsabilità                                         |
| ------------ | ----------- | ------------------------------------------------------ |
| Front-end    | React       | Interfaccia utente, routing, comunicazione con API     |
| Applicazione | Spring Boot | REST API, business logic, WebSocket broker, sicurezza  |
| Dati         | PostgreSQL  | Persistenza, integrità referenziale, query ottimizzate |

La comunicazione tra frontend e backend avviene tramite REST API per le operazioni CRUD e WebSocket per gli aggiornamenti in tempo reale degli ordini. L'autenticazione è gestita tramite token JWT inclusi in ogni richiesta HTTP.

### Modello dati — entità principali

**UTENTE**

| Colonna  | Tipo                              |
| -------- | --------------------------------- |
| id       | INT                               |
| nome     | VARCHAR(100)                      |
| email    | VARCHAR(150)                      |
| password | VARCHAR(255)                      |
| ruolo    | ENUM('ADMIN','STAFF','USER')      |

**TAVOLO**

| Colonna | Tipo                                    |
| ------- | --------------------------------------- |
| id      | INT                                     |
| numero  | INT                                     |
| coperti | INT                                     |
| stato   | ENUM('LIBERO','OCCUPATO','RISERVATO')   |

**CATEGORIA**

| Colonna     | Tipo         |
| ----------- | ------------ |
| id          | INT          |
| nome        | VARCHAR(100) |
| descrizione | TEXT         |

**PIATTO**

| Colonna      | Tipo            |
| ------------ | --------------- |
| id           | INT             |
| categoria_id | INT             |
| nome         | VARCHAR(150)    |
| prezzo       | DECIMAL(10,2)   |
| disponibile  | BOOLEAN         |
| foto         | VARCHAR(255)    |

**PRENOTAZIONE**

| Colonna    | Tipo   |
| ---------- | ------ |
| id         | INT    |
| tavolo_id  | INT    |
| utente_id  | INT    |
| data       | DATE   |
| ora        | TIME   |
| coperti    | INT    |
| note       | TEXT   |

**ORDINE**

| Colonna   | Tipo                                                          |
| --------- | ------------------------------------------------------------- |
| id        | INT                                                           |
| tavolo_id | INT                                                           |
| utente_id | INT                                                           |
| stato     | ENUM('IN_ATTESA','IN_PREPARAZIONE','PRONTO','CONSEGNATO')     |
| creato_il | DATETIME                                                      |

**ORDINE_ITEM**

| Colonna   | Tipo |
| --------- | ---- |
| id        | INT  |
| ordine_id | INT  |
| piatto_id | INT  |
| quantita  | INT  |
| note      | TEXT |

**CONTO**

| Colonna   | Tipo                                   |
| --------- | -------------------------------------- |
| id        | INT                                    |
| ordine_id | INT                                    |
| totale    | DECIMAL(10,2)                          |
| pagato    | BOOLEAN                                |
| metodo    | ENUM('CONTANTI','CARTA','BONIFICO')    |

### Funzionalità di rilievo

### Vista cucina in tempo reale
Tramite WebSocket (protocollo STOMP su SockJS), ogni ordine confermato da un cameriere viene trasmesso istantaneamente alla schermata della cucina senza necessità di aggiornare la pagina. Il cuoco può aggiornare lo stato di ogni piatto (in preparazione / pronto) e il cameriere viene notificato in automatico.

 ### QR code per ordinare al tavolo
Ogni tavolo ha un QR code univoco generato dal sistema. Il cliente lo scansiona con lo smartphone, accede al menu digitale e compone il proprio ordine senza installare alcuna app. L'ordine arriva direttamente in cucina.

### Dashboard analytics
L'admin ha accesso a una dashboard con grafici Chart.js che mostrano: revenue giornaliera/settimanale, i piatti più ordinati, il numero medio di coperti per fascia oraria e l'andamento delle prenotazioni.
