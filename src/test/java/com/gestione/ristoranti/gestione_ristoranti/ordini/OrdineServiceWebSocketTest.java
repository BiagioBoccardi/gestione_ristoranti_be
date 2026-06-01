package com.gestione.ristoranti.gestione_ristoranti.ordini;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.CambioStatoRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineStatoEvent;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineEventPublisher;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdineServiceWebSocketTest {

    @Mock private OrdineRepository ordineRepository;
    @Mock private TavoloRepository tavoloRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private PiattoRepository piattoRepository;
    @Mock private OrdineEventPublisher ordineEventPublisher;

    @InjectMocks
    private OrdineService ordineService;

    private Ordine ordine;
    private Tavolo tavolo;

    @BeforeEach
    void setUp() {
        tavolo = new Tavolo();
        tavolo.setId(1L);
        tavolo.setNumero(5);
        tavolo.setStato(StatoTavolo.OCCUPATO);

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario Rossi");

        ordine = new Ordine(tavolo, utente, StatoOrdine.IN_ATTESA, LocalDateTime.now());
        ordine.setId(10L);
    }

    @Test
    void aggiornaStato_pubblicaEventoWebSocketDopoIlSalvataggio() {
        CambioStatoRequest request = new CambioStatoRequest();
        request.setNuovoStato(StatoOrdine.IN_PREPARAZIONE);

        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        ordineService.aggiornaStato(10L, request);

        ArgumentCaptor<OrdineStatoEvent> captor = ArgumentCaptor.forClass(OrdineStatoEvent.class);
        verify(ordineEventPublisher, times(1)).pubblicaStatoOrdine(captor.capture());

        OrdineStatoEvent event = captor.getValue();
        assertThat(event.ordineId()).isEqualTo(10L);
        assertThat(event.tavoloId()).isEqualTo(1L);
        assertThat(event.numeroTavolo()).isEqualTo(5);
        assertThat(event.statoVecchio()).isEqualTo(StatoOrdine.IN_ATTESA);
        assertThat(event.statoNuovo()).isEqualTo(StatoOrdine.IN_PREPARAZIONE);
        assertThat(event.timestamp()).isNotNull();
    }

    @Test
    void aggiornaStato_nonPubblicaEventoSeTransizioneNonValida() {
        CambioStatoRequest request = new CambioStatoRequest();
        request.setNuovoStato(StatoOrdine.CONSEGNATO); // salto vietato da IN_ATTESA

        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        assertThrows(IllegalStateException.class,
                () -> ordineService.aggiornaStato(10L, request));

        verify(ordineEventPublisher, never()).pubblicaStatoOrdine(any());
    }

    @Test
    void aggiornaStato_pubblicaEventoCorrettoDaInPreparazioneAPronto() {
        ordine.setStato(StatoOrdine.IN_PREPARAZIONE);

        CambioStatoRequest request = new CambioStatoRequest();
        request.setNuovoStato(StatoOrdine.PRONTO);

        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        ordineService.aggiornaStato(10L, request);

        ArgumentCaptor<OrdineStatoEvent> captor = ArgumentCaptor.forClass(OrdineStatoEvent.class);
        verify(ordineEventPublisher).pubblicaStatoOrdine(captor.capture());

        OrdineStatoEvent event = captor.getValue();
        assertThat(event.statoVecchio()).isEqualTo(StatoOrdine.IN_PREPARAZIONE);
        assertThat(event.statoNuovo()).isEqualTo(StatoOrdine.PRONTO);
    }
}
