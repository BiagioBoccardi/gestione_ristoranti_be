package com.gestione.ristoranti.gestione_ristoranti.auth.security;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UtenteDetails implements UserDetails {

    private final Utente utente;

    public UtenteDetails(Utente utente) {
        this.utente = utente;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().getNome()));
    }

    @Override
    public String getPassword() {
        return utente.getPassword();
    }

    @Override
    public String getUsername() {
        return utente.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return utente.getId();
    }

    public String getNome() {
        return utente.getNome();
    }
}
