# Analisi e Requisiti

## Requisiti funzionali

1. Autenticazione e autorizzazione
   - Login utente con credenziali.
   - Gestione ruoli RBAC: Admin/Proprietario, Cameriere, Cuoco/Cucina, Cliente.
   - Accesso differenziato alle funzionalità in base al ruolo.

2. Gestione menu
   - CRUD di piatti e categorie.
   - Gestione di prezzi, disponibilità e foto dei piatti.
   - Visualizzazione del menu digitale per utenti e clienti.

3. Gestione ordini e tavoli
   - Creazione, aggiornamento e consultazione degli ordini.
   - Associazione dell'ordine a tavolo e cameriere.
   - Aggiornamento dello stato dell’ordine e dello stato cucina.

4. Vista cucina in tempo reale
   - Notifica immediata degli ordini confermati.
   - Aggiornamenti live dello stato dei piatti.
   - Comunicazione bidirezionale sala-cucina tramite WebSocket/STOMP.

5. Prenotazioni
   - Gestione slot orari e coperti disponibili.
   - Creazione, modifica e conferma delle prenotazioni.
   - Possibilità di invio notifiche o conferme via email.

6. Conto e pagamento
   - Generazione del conto finale dell’ordine.
   - Supporto per split bill.
   - Esportazione del conto in PDF/Excel tramite Apache POI.

7. Dashboard analytics
   - Visualizzazione di revenue giornaliera/settimanale.
   - Grafici dei piatti più venduti.
   - Analisi di prenotazioni, coperti medi e dati di vendita.

8. Gestione staff
   - Creazione e modifica degli utenti.
   - Assegnazione dei ruoli e gestione dei turni.

9. QR code per ordinare
   - Generazione di QR code univoci per i tavoli.
   - Accesso al menu digitale via smartphone.
   - Invio ordini direttamente alla cucina dal dispositivo mobile.

10. Integrazione frontend-backend

- Frontend React come SPA con routing.
- Comunicazione tramite REST API per operazioni CRUD.
- Aggiornamenti live tramite WebSocket per ordini e stato cucina.

## Requisiti non funzionali

1. Sicurezza
   - Autenticazione JWT su tutte le richieste API.
   - Controllo di accesso basato su ruoli.
   - Protezione delle informazioni sensibili come password ed email.

2. Prestazioni
   - Aggiornamenti in tempo reale senza refresh di pagina.
   - Interfaccia reattiva e tempi di risposta contenuti.
   - API efficienti per operazioni CRUD e visualizzazione liste.

3. Affidabilità
   - Persistenza dati su database PostgreSQL.
   - Integrità referenziale di tavoli, ordini, prenotazioni e utenti.
   - Coerenza dei dati in caso di aggiornamenti concorrenti.

4. Usabilità
   - Interfaccia chiara e adeguata per ciascun ruolo.
   - Navigazione fluida grazie a React Router.
   - Dashboard analytics intuitiva e leggibile.

5. Manutenibilità
   - Architettura a tre livelli separati.
   - Backend modulare basato su Spring Boot.
   - Frontend React con componenti riutilizzabili.

6. Scalabilità
   - Containerizzazione con Docker e Docker Compose.
   - Possibile deployment in ambienti distribuiti.
   - Utilizzo di PostgreSQL come database scalabile.

7. Portabilità e deploy
   - Pipeline CI/CD con Jenkins.
   - Build e deploy automatizzabili.
   - Applicazione eseguibile in container Docker.

8. Testabilità
   - Copertura con test JUnit.
   - Possibilità di validare logica di business e API.
