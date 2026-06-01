package com.gestione.ristoranti.gestione_ristoranti.conti;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Conto;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.SplitBillResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.repository.ContoRepository;
import com.gestione.ristoranti.gestione_ristoranti.conti.service.ContoService;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContoServiceTest {

    @Mock private ContoRepository contoRepository;
    @Mock private OrdineRepository ordineRepository;

    @InjectMocks
    private ContoService contoService;

    @Test
    void apriConto_ordineNonConsegnato_lanceConflictException() {
        Ordine ordine = mock(Ordine.class);
        when(ordine.getStato()).thenReturn(StatoOrdine.IN_ATTESA);

        when(contoRepository.existsByOrdineId(1L)).thenReturn(false);
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));

        assertThrows(ConflictException.class, () -> contoService.apriConto(1L));
        verify(contoRepository, never()).save(any());
    }

    @Test
    void apriConto_contoEsistente_restituisceContoSenzaDuplicati() {
        Conto esistente = mock(Conto.class);
        Ordine ordine = mock(Ordine.class);
        when(esistente.getOrdine()).thenReturn(ordine);
        when(ordine.getItems()).thenReturn(java.util.List.of());
        when(ordine.getTavolo()).thenReturn(mock(com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo.class));

        when(contoRepository.existsByOrdineId(1L)).thenReturn(true);
        when(contoRepository.findByOrdineId(1L)).thenReturn(Optional.of(esistente));

        contoService.apriConto(1L);

        // Non deve creare un nuovo conto
        verify(contoRepository, never()).save(any());
    }

    @Test
    void pagaConto_contoGiaPagato_lanceConflictException() {
        Conto conto = mock(Conto.class);
        when(conto.getPagato()).thenReturn(true);
        when(contoRepository.findById(1L)).thenReturn(Optional.of(conto));

        var request = mock(com.gestione.ristoranti.gestione_ristoranti.conti.dto.PagaContoRequest.class);

        assertThrows(ConflictException.class, () -> contoService.pagaConto(1L, request));
        verify(contoRepository, never()).save(any());
    }

    @Test
    void calcolaSplit_trePersone_calcolaQuotaCorretta() {
        Conto conto = mock(Conto.class);
        when(conto.getTotale()).thenReturn(new BigDecimal("30.00"));
        when(contoRepository.findById(1L)).thenReturn(Optional.of(conto));

        SplitBillResponse result = contoService.calcolaSplit(1L, 3);

        assertThat(result.getQuotaPerPersona()).isEqualByComparingTo("10.00");
        assertThat(result.getNPersone()).isEqualTo(3);
        assertThat(result.getTotale()).isEqualByComparingTo("30.00");
    }

    @Test
    void calcolaSplit_arrotondamento_calcolaCorrettamente() {
        Conto conto = mock(Conto.class);
        when(conto.getTotale()).thenReturn(new BigDecimal("10.00"));
        when(contoRepository.findById(1L)).thenReturn(Optional.of(conto));

        SplitBillResponse result = contoService.calcolaSplit(1L, 3);

        // 10 / 3 = 3.33 (HALF_UP)
        assertThat(result.getQuotaPerPersona()).isEqualByComparingTo("3.33");
    }

    @Test
    void calcolaSplit_zeroPersone_lanceIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> contoService.calcolaSplit(1L, 0));
        verify(contoRepository, never()).findById(any());
    }
}
