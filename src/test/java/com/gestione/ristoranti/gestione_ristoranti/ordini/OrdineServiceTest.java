package com.gestione.ristoranti.gestione_ristoranti.ordini;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineEventPublisher;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdineServiceTest {

    @Mock private OrdineRepository ordineRepository;
    @Mock private TavoloRepository tavoloRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private PiattoRepository piattoRepository;
    @Mock private OrdineEventPublisher ordineEventPublisher;

    @InjectMocks
    private OrdineService ordineService;

    private Tavolo tavolo;
    private Ordine ordine;

    @BeforeEach
    void setUp() {
        tavolo = new Tavolo();
        tavolo.setId(1L);
        tavolo.setNumero(3);
        tavolo.setStato(StatoTavolo.OCCUPATO);

        Utente utente = new Utente();
        utente.setId(2L);

        ordine = new Ordine(tavolo, utente, StatoOrdine.IN_ATTESA, LocalDateTime.now());
        ordine.setId(10L);
    }

    @Test
    void delete_ordineInAttesa_eliminaCorrettamente() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        ordineService.delete(10L);

        verify(ordineRepository, times(1)).delete(ordine);
    }

    @Test
    void delete_ordineInPreparazione_lanceConflictException() {
        ordine.setStato(StatoOrdine.IN_PREPARAZIONE);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        assertThrows(ConflictException.class, () -> ordineService.delete(10L));
        verify(ordineRepository, never()).delete(any());
    }

    @Test
    void delete_ordinePronto_lanceConflictException() {
        ordine.setStato(StatoOrdine.PRONTO);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        assertThrows(ConflictException.class, () -> ordineService.delete(10L));
    }

    @Test
    void delete_ordineConsegnato_lanceConflictException() {
        ordine.setStato(StatoOrdine.CONSEGNATO);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        assertThrows(ConflictException.class, () -> ordineService.delete(10L));
    }

    @Test
    void aggiornaStato_transizioneNonValida_saltaStato_lanceException() {
        // IN_ATTESA → PRONTO non è una transizione valida (si salta IN_PREPARAZIONE)
        ordine.setStato(StatoOrdine.IN_ATTESA);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));

        var request = new com.gestione.ristoranti.gestione_ristoranti.ordini.dto.CambioStatoRequest();
        request.setNuovoStato(StatoOrdine.PRONTO);

        assertThrows(IllegalStateException.class,
                () -> ordineService.aggiornaStato(10L, request));
        verify(ordineEventPublisher, never()).pubblicaStatoOrdine(any());
    }
}
