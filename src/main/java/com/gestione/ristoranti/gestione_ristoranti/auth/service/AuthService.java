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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UtenteRepository utenteRepository;
    private final RuoloRepository ruoloRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UtenteRepository utenteRepository,
                       RuoloRepository ruoloRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.utenteRepository = utenteRepository;
        this.ruoloRepository = ruoloRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica un utente tramite email e password, generando un token JWT.
     *
     * @param email    email dell'utente
     * @param password password in chiaro
     * @return risposta con JWT, nome utente e ruolo
     * @throws org.springframework.security.authentication.BadCredentialsException se le credenziali sono errate
     */
    @Transactional(readOnly = true)
    public LoginResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        com.gestione.ristoranti.gestione_ristoranti.auth.security.UtenteDetails userDetails =
                (com.gestione.ristoranti.gestione_ristoranti.auth.security.UtenteDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);
        Utente utente = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato dopo autenticazione"));

        return new LoginResponse(jwt, utente.getNome(), utente.getRuolo().getNome());
    }

    /**
     * Registra un nuovo utente con ruolo CLIENTE.
     *
     * @param nome     nome visualizzato
     * @param email    email univoca
     * @param password password in chiaro (verrà codificata con BCrypt)
     * @return messaggio di conferma registrazione
     * @throws IllegalStateException se l'email è già registrata
     */
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
    public RegisterResponse creaUtente(String nome, String email, String password, String nomeRuolo) {
        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email già registrata: " + email);
        }

        Ruolo ruolo = ruoloRepository.findByNome(nomeRuolo.toUpperCase())
                .orElseThrow(() -> new IllegalStateException("Ruolo non trovato: " + nomeRuolo));

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(password));
        utente.setRuolo(ruolo);

        utenteRepository.save(utente);

        return new RegisterResponse("Utente creato con ruolo " + ruolo.getNome());
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
}
