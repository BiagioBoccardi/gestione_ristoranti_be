# Analisi del Task 1: Autenticazione e autorizzazione

## Obiettivo

Definire le entità e le responsabilità necessarie per implementare il task di autenticazione e autorizzazione. L'analisi è basata sul modello dati principale descritto in `README.MD` e sul task specificato in `analisi e requisiti/gemini.md`.

## Riferimenti

- `README.MD` — sezione `Modello dati — entità principali`
- `analisi e requisiti/gemini.md` — `Task 1: Autenticazione e autorizzazione`

## Contesto del task

Il task copre:

- login utente con credenziali
- gestione ruoli RBAC: Admin/Proprietario, Cameriere, Cuoco/Cucina, Cliente
- accesso differenziato alle funzionalità in base al ruolo
- generazione e validazione JWT

## Entità necessarie

### 1. Utente

Questa è l'entità principale per l'autenticazione.

- id: identificatore univoco
- nome: nome completo dell'utente
- email: login e identificativo univoco per accesso
- password: hash della password
- ruolo: riferimento al ruolo assegnato

### 2. Ruolo

Questa entità rappresenta il profilo di autorizzazione dell'utente.

- id: identificatore univoco del ruolo
- nome: ad esempio `ADMIN`, `CAMERIERE`, `CUOCO`, `CLIENTE`
- descrizione: testo descrittivo del ruolo
- permessi (opzionale): elenco di privilegi associati al ruolo

### 3. Relazione Utente ↔ Ruolo

- relazione molti-a-uno: un utente ha un solo ruolo principale
- entità separata con associazione JPA

## Mappatura rispetto al modello dati README

Il `README.MD` elenca l'entità `Utente` con attributi fondamentali:

- id
- nome
- email
- password
- ruolo

Questo task estende la definizione chiarendo che il ruolo deve essere un oggetto esplicito per gestire RBAC in modo scalabile.

## Proposta di entità nel backend

### Utente

- `int id`
- `String id`
- `String nome`
- `String email`
- `String password`
- `Ruolo ruolo`
- `boolean attivo` (opzionale)
- `LocalDateTime ultimoAccesso` (opzionale)

### Ruolo

- `Int id`
- `String nome`
- `String descrizione`
- `Set<String> permessi` (opzionale)

## Responsabilità Backend

### Definire il modello dati

- Creare le entità JPA `Utente` e `Ruolo`
- Definire relazioni e vincoli di unicità su email
- Mappare le relazioni su database PostgreSQL

### Autenticazione

- Endpoint REST per login
- Gestione credenziali e confronto password hashed
- Emissione di token JWT con claims ruolo e id utente

### Autorizzazione

- Configurare filtri Spring Security per validare JWT
- Applicare regole RBAC alle API
- Garantire che ogni endpoint abbia il giusto livello di autorizzazione

### Sicurezza dei dati

- Hash sicuro per le password (BCrypt o equivalente)
- Protezione dei dati sensibili in API e log

## Responsabilità Frontend

### Schermata di login

- Form con email e password
- Gestione degli errori di accesso

### Gestione del token

- Memorizzazione sicura del JWT (preferibilmente in memoria o cookie sicuro)
- Inclusione del token nelle chiamate REST successive

### Autorizzazione UI

- Visualizzazione condizionale delle sezioni in base al ruolo
- Redirect verso pagine protette in caso di accesso non autorizzato

### Esperienza utente

- Messaggi chiari per login fallito e per accesso negato
- Possibilità di logout e reset dello stato di autenticazione

## Piano di azione dettagliato per il task

1. Analisi richieste e modello dati
   - Confermare entità `Utente` e `Ruolo`
   - Identificare attributi necessari per JWT e RBAC

2. Progettazione backend
   - Creare classi entità e repository JPA
   - Definire endpoint di autenticazione
   - Implementare servizio di generazione JWT

3. Configurazione sicurezza
   - Configurare Spring Security con JWT
   - Mappare ruoli su autorizzazioni REST

4. Progettazione frontend
   - Definire pagina login e flusso di autenticazione
   - Implementare storage e refresh token
   - Realizzare protezione delle rotte

5. Testing
   - Test unitari backend per autenticazione e autorizzazione
   - Test end-to-end del flusso login/ruoli
   - Verifica UI per accesso e restrizioni

## Nota sull'architettura a microservizi

Questo task corrisponde al microservizio di autenticazione/autorizzazione. Le entità `Utente` e `Ruolo` saranno gestite principalmente da questo servizio, mentre altri microservizi possono richiedere l'identità e i permessi tramite token JWT.

## Conclusione

Il task richiede un modello dati allineato con il `README.MD`, con entità `Utente` e `Ruolo` chiaramente definite. Il backend gestisce login, JWT e RBAC; il frontend gestisce login, token e accesso condizionale in base al ruolo.

## Piano di stesura del modello dati JPA/Hibernate

### Obiettivo

Definire le classi JPA per le entità `Utente` e `Ruolo`, con mapping delle relazioni e vincoli necessari per l'autenticazione e autorizzazione.

### Passaggi

1. Definire l'entità `Ruolo`
   - Classe `Ruolo` con campi `id`, `nome` e `descrizione`
   - Annotazioni JPA `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
   - Eventuale lista di permessi come `Set<String>` o entità separata se necessario

2. Definire l'entità `Utente`
   - Classe `Utente` con campi `id`, `nome`, `email`, `password`, `ruolo`
   - Vincolo di unicità su `email`
   - Mapping `@ManyToOne` verso `Ruolo`
   - Annotazioni JPA `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`

3. Configurare relazioni e validazioni
   - Relazione molti-a-uno `Utente` → `Ruolo`
   - Vincolo `nullable = false` su `email`, `password` e `ruolo`
   - Indici su `email` e stato utente se necessari

4. Creare repository JPA
   - `UtenteRepository` con metodi `findByEmail` e query su ruolo
   - `RuoloRepository` per gestione dei ruoli

5. Allineare il modello con il database PostgreSQL
   - Verificare i tipi SQL generati per stringhe e chiavi primarie
   - Gestire eventuale creazione automatica delle tabelle da Hibernate

### Esito atteso

- Modello JPA pronto per essere implementato nel backend Spring Boot
- Entità coerenti con i requisiti funzionali e il modello dati del README
- Repository JPA disponibili per l'implementazione dei servizi di autenticazione
