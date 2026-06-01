package com.gestione.ristoranti.gestione_ristoranti.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;

    public JwtUtils(@Value("${app.jwt.secret}") String jwtSecret,
                    @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT scaduto: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformato: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("Firma JWT non valida: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Validazione JWT fallita [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
        return false;
    }
}
