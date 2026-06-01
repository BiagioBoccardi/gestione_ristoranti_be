package com.gestione.ristoranti.gestione_ristoranti.auth.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}
