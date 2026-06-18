package com.gestione.ristoranti.gestione_ristoranti.auth.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.api.LoginResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.RegisterResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.UtenteResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ruolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.RuoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.security.JwtUtils;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private static final String CHARS_TEMP_PWD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UtenteRepository utenteRepository;
    private final RuoloRepository ruoloRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UtenteRepository utenteRepository,
                       RuoloRepository ruoloRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.utenteRepository = utenteRepository;
        this.ruoloRepository = ruoloRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato dopo autenticazione"));

        if (utente.isPrimoAccesso()) {
            return inviaOtpPrimoAccesso(utente);
        }

        String jwt = jwtUtils.generateJwtToken(authentication);
        return new LoginResponse(jwt, utente.getNome(), utente.getRuolo().getNome());
    }

    @Transactional
    public RegisterResponse register(String nome, String email, String password) {
        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email già registrata: " + email);
        }

        Ruolo ruolo = ruoloRepository.findByNome("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Ruolo CLIENTE non trovato. popolare i ruoli iniziali."));

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(password));
        utente.setRuolo(ruolo);

        utenteRepository.save(utente);

        return new RegisterResponse("Utente registrato con successo");
    }

    @Transactional
    public RegisterResponse creaUtente(String nome, String email, String nomeRuolo) {
        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email già registrata: " + email);
        }

        Ruolo ruolo = ruoloRepository.findByNome(nomeRuolo.toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Ruolo non trovato: " + nomeRuolo));

        String passwordTemporanea = generaPasswordCasuale(12);

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(passwordTemporanea));
        utente.setRuolo(ruolo);
        utente.setPrimoAccesso(true);

        utenteRepository.save(utente);

        emailService.inviaBenvenutoStaff(email, nome, passwordTemporanea);

        return new RegisterResponse("Utente creato con ruolo " + ruolo.getNome());
    }

    @Transactional
    public LoginResponse verificaPrimoAccesso(String email, String codice) {
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        if (!utente.isPrimoAccesso()) {
            throw new IllegalStateException("Nessun primo accesso in attesa per questo account");
        }
        if (utente.getCodiceVerifica() == null || utente.getScadenzaCodice() == null) {
            throw new IllegalStateException("Codice non ancora generato. Effettua prima il login.");
        }
        if (LocalDateTime.now().isAfter(utente.getScadenzaCodice())) {
            throw new IllegalStateException("Il codice è scaduto. Effettua nuovamente il login per ricevere un nuovo codice.");
        }
        if (!utente.getCodiceVerifica().equals(codice)) {
            throw new IllegalStateException("Codice non valido.");
        }

        utente.setPrimoAccesso(false);
        utente.setCodiceVerifica(null);
        utente.setScadenzaCodice(null);
        utenteRepository.save(utente);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + utente.getRuolo().getNome())));
        String jwt = jwtUtils.generateJwtToken(auth);
        return new LoginResponse(jwt, utente.getNome(), utente.getRuolo().getNome());
    }

    @Transactional(readOnly = true)
    public List<UtenteResponse> getUtenti() {
        return utenteRepository.findAll().stream()
                .map(UtenteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtenteResponse getUtenteById(Long id) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));
        return UtenteResponse.from(utente);
    }

    @Transactional
    public UtenteResponse aggiornaRuolo(Long id, String nomeRuolo) {
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));

        Ruolo ruolo = ruoloRepository.findByNome(nomeRuolo.toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Ruolo non trovato: " + nomeRuolo));

        utente.setRuolo(ruolo);
        utenteRepository.save(utente);

        return UtenteResponse.from(utente);
    }

    @Transactional
    public void eliminaUtente(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utente non trovato con id: " + id);
        }
        utenteRepository.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private LoginResponse inviaOtpPrimoAccesso(Utente utente) {
        String codice = String.format("%06d", RANDOM.nextInt(1_000_000));
        utente.setCodiceVerifica(codice);
        utente.setScadenzaCodice(LocalDateTime.now().plusMinutes(15));
        utenteRepository.save(utente);
        emailService.inviaCodiceVerificaPrimoAccesso(utente.getEmail(), utente.getNome(), codice);
        return LoginResponse.forPrimoAccesso(utente.getEmail());
    }

    private String generaPasswordCasuale(int lunghezza) {
        StringBuilder sb = new StringBuilder(lunghezza);
        for (int i = 0; i < lunghezza; i++) {
            sb.append(CHARS_TEMP_PWD.charAt(RANDOM.nextInt(CHARS_TEMP_PWD.length())));
        }
        return sb.toString();
    }
}
