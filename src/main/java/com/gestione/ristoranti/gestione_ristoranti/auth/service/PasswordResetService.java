package com.gestione.ristoranti.gestione_ristoranti.auth.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.PasswordResetToken;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.PasswordResetTokenRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UtenteRepository utenteRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public PasswordResetService(UtenteRepository utenteRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void richiediReset(String email) {
        // Risposta identica indipendentemente dall'email per evitare user enumeration
        utenteRepository.findByEmail(email).ifPresent(utente -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token, utente, LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);

            String resetLink = baseUrl + "/reset-password?token=" + token;
            emailService.inviaResetPassword(utente.getEmail(), utente.getNome(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String token, String nuovaPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token non valido o scaduto."));

        if (resetToken.isUsato()) {
            throw new IllegalArgumentException("Questo link è già stato utilizzato.");
        }

        if (resetToken.getScadenza().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Il link di reset è scaduto. Richiedine uno nuovo.");
        }

        Utente utente = resetToken.getUtente();
        utente.setPassword(passwordEncoder.encode(nuovaPassword));
        utenteRepository.save(utente);

        resetToken.setUsato(true);
        tokenRepository.save(resetToken);
    }
}
