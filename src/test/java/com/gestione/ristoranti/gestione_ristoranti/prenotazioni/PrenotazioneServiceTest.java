package com.gestione.ristoranti.gestione_ristoranti.prenotazioni;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ruolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto.PrenotazioneRequest;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.repository.PrenotazioneRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service.EmailService;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service.PrenotazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrenotazioneServiceTest {

    @Mock private PrenotazioneRepository prenotazioneRepository;
    @Mock private TavoloRepository tavoloRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private PrenotazioneService prenotazioneService;

    private Tavolo tavolo;
    private Utente utenteProprietario;

    @BeforeEach
    void setUp() {
        tavolo = new Tavolo();
        tavolo.setId(1L);
        tavolo.setNumero(4);

        utenteProprietario = mock(Utente.class);
        when(utenteProprietario.getEmail()).thenReturn("proprietario@test.com");
    }

    @Test
    void crea_conflittoOrario_lanceIllegalStateException() {
        PrenotazioneRequest request = mock(PrenotazioneRequest.class);
        when(request.getTavoloId()).thenReturn(1L);
        when(request.getData()).thenReturn(LocalDate.now());
        when(request.getOra()).thenReturn(LocalTime.of(20, 0));

        // Prenotazione esistente alla stessa ora → conflitto
        Prenotazione esistente = mock(Prenotazione.class);
        when(esistente.getId()).thenReturn(99L);
        when(esistente.getOra()).thenReturn(LocalTime.of(20, 0));

        when(utenteRepository.findByEmail("utente@test.com")).thenReturn(Optional.of(utenteProprietario));
        when(tavoloRepository.findById(1L)).thenReturn(Optional.of(tavolo));
        when(prenotazioneRepository.findByTavoloAndData(tavolo, LocalDate.now()))
                .thenReturn(List.of(esistente));

        assertThrows(IllegalStateException.class,
                () -> prenotazioneService.crea(request, "utente@test.com"));
        verify(prenotazioneRepository, never()).save(any());
    }

    @Test
    void crea_fasciaDiversa_nonConflitto_salvaCorrettamente() {
        PrenotazioneRequest request = mock(PrenotazioneRequest.class);
        when(request.getTavoloId()).thenReturn(1L);
        when(request.getData()).thenReturn(LocalDate.now());
        when(request.getOra()).thenReturn(LocalTime.of(20, 0));
        when(request.getCoperti()).thenReturn(2);

        // Prenotazione esistente a 3 ore di distanza → nessun conflitto
        Prenotazione esistente = mock(Prenotazione.class);
        when(esistente.getId()).thenReturn(99L);
        when(esistente.getOra()).thenReturn(LocalTime.of(23, 0));

        Utente richiedente = mock(Utente.class);
        when(utenteRepository.findByEmail("utente@test.com")).thenReturn(Optional.of(richiedente));
        when(tavoloRepository.findById(1L)).thenReturn(Optional.of(tavolo));
        when(prenotazioneRepository.findByTavoloAndData(tavolo, LocalDate.now()))
                .thenReturn(List.of(esistente));

        Prenotazione saved = mock(Prenotazione.class);
        when(prenotazioneRepository.save(any())).thenReturn(saved);

        prenotazioneService.crea(request, "utente@test.com");

        verify(prenotazioneRepository, times(1)).save(any());
        verify(emailService, times(1)).inviaConfermaPrenotazione(any());
    }

    @Test
    void cancella_nonProprietarioNonStaff_lanceSecurityException() {
        Ruolo ruoloCliente = mock(Ruolo.class);
        when(ruoloCliente.getNome()).thenReturn("CLIENTE");

        Utente altroUtente = mock(Utente.class);
        when(altroUtente.getEmail()).thenReturn("altro@test.com");
        when(altroUtente.getRuolo()).thenReturn(ruoloCliente);

        Prenotazione prenotazione = mock(Prenotazione.class);
        when(prenotazione.getUtente()).thenReturn(utenteProprietario);

        when(prenotazioneRepository.findById(5L)).thenReturn(Optional.of(prenotazione));
        when(utenteRepository.findByEmail("altro@test.com")).thenReturn(Optional.of(altroUtente));

        assertThrows(SecurityException.class,
                () -> prenotazioneService.cancella(5L, "altro@test.com"));
        verify(prenotazioneRepository, never()).delete(any());
    }

    @Test
    void cancella_staffPuoCancellarePrenotazioneAltrui() {
        Ruolo ruoloAdmin = mock(Ruolo.class);
        when(ruoloAdmin.getNome()).thenReturn("ADMIN");

        Utente admin = mock(Utente.class);
        when(admin.getEmail()).thenReturn("admin@test.com");
        when(admin.getRuolo()).thenReturn(ruoloAdmin);

        Prenotazione prenotazione = mock(Prenotazione.class);
        when(prenotazione.getUtente()).thenReturn(utenteProprietario);

        when(prenotazioneRepository.findById(5L)).thenReturn(Optional.of(prenotazione));
        when(utenteRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        prenotazioneService.cancella(5L, "admin@test.com");

        verify(prenotazioneRepository, times(1)).delete(prenotazione);
    }
}
