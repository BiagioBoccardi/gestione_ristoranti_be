/// <reference types="vitest/globals" />
import type {} from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { Ordine, OrdineStatoEvent, StatoOrdine } from '@/types/ordine';

// ── Mock AppSidebar (evita dipendenza da AuthContext) ─────────────────────────

vi.mock('@/components/layout/AppSidebar', () => ({
  AppSidebar: () => null,
}));

// ── Mock hook WebSocket ───────────────────────────────────────────────────────

vi.mock('@/hooks/useKitchenSocket', () => ({
  useKitchenSocket: vi.fn(),
}));

// ── Mock servizio ordini ──────────────────────────────────────────────────────

vi.mock('@/services/ordineService', () => ({
  ordineService: {
    getOrdini: vi.fn(),
    updateStatoOrdine: vi.fn(),
  },
}));

// ── Mock OrdineCard (evita di ri-mockare tutti i componenti UI) ───────────────

vi.mock('@/components/ordini/OrdineCard', () => ({
  default: ({
    ordine,
    onCambiaStato,
  }: {
    ordine: Ordine;
    onCambiaStato: (id: number, stato: StatoOrdine) => Promise<void>;
    canEdit: boolean;
    onEdit: () => void;
  }) => (
    <div data-testid={`ordine-${ordine.id}`}>
      <span>Ordine #{ordine.id}</span>
      <span data-testid={`stato-${ordine.id}`}>{ordine.stato}</span>
      <button onClick={() => onCambiaStato(ordine.id, 'IN_PREPARAZIONE')}>Avanza</button>
    </div>
  ),
}));

// ── Import dopo i mock ────────────────────────────────────────────────────────

import CucinaPage from '@/pages/Cucina';
import { useKitchenSocket } from '@/hooks/useKitchenSocket';
import { ordineService } from '@/services/ordineService';

// ── Dati di test ──────────────────────────────────────────────────────────────

const ordineInAttesa: Ordine = {
  id: 10,
  tavoloId: 1,
  numeroTavolo: 5,
  stato: 'IN_ATTESA',
  items: [{ id: 100, piatto: { id: 1, nome: 'Margherita', prezzo: 8 }, prezzoUnitario: 8, quantita: 2 }],
  totale: 16,
  creatoAt: '2026-05-12T10:00:00',
};

const ordineInPreparazione: Ordine = {
  ...ordineInAttesa,
  id: 11,
  stato: 'IN_PREPARAZIONE',
};

function buildEvent(partial: Partial<OrdineStatoEvent>): OrdineStatoEvent {
  return {
    ordineId: 10,
    tavoloId: 1,
    numeroTavolo: 5,
    statoVecchio: 'IN_ATTESA',
    statoNuovo: 'IN_PREPARAZIONE',
    timestamp: '2026-05-12T10:05:00',
    ...partial,
  };
}

// ── Test ──────────────────────────────────────────────────────────────────────

describe('CucinaPage — vista cucina real-time', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useKitchenSocket).mockReturnValue({ connected: true, lastEvent: null });
    vi.mocked(ordineService.updateStatoOrdine).mockResolvedValue(ordineInAttesa);
  });

  it('carica gli ordini attivi all\'avvio tramite REST', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInAttesa]);

    render(<CucinaPage />);

    expect(await screen.findByTestId('ordine-10')).toBeInTheDocument();
    expect(ordineService.getOrdini).toHaveBeenCalledTimes(1);
  });

  it('mostra l\'indicatore "Live" quando il WebSocket è connesso', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([]);

    render(<CucinaPage />);

    expect(await screen.findByText('Live')).toBeInTheDocument();
  });

  it('mostra "Connessione..." quando il WebSocket non è connesso', async () => {
    vi.mocked(useKitchenSocket).mockReturnValue({ connected: false, lastEvent: null });
    vi.mocked(ordineService.getOrdini).mockResolvedValue([]);

    render(<CucinaPage />);

    expect(await screen.findByText('Connessione...')).toBeInTheDocument();
  });

  it('aggiorna lo stato dell\'ordine quando arriva un evento WebSocket', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInAttesa]);
    vi.mocked(useKitchenSocket).mockReturnValue({ connected: true, lastEvent: null });

    const { rerender } = render(<CucinaPage />);
    await screen.findByTestId('ordine-10');

    // Simula arrivo evento WebSocket
    vi.mocked(useKitchenSocket).mockReturnValue({
      connected: true,
      lastEvent: buildEvent({ ordineId: 10, statoVecchio: 'IN_ATTESA', statoNuovo: 'IN_PREPARAZIONE' }),
    });
    rerender(<CucinaPage />);

    await waitFor(() => {
      expect(screen.getByTestId('stato-10').textContent).toBe('IN_PREPARAZIONE');
    });
  });

  it('rimuove l\'ordine dalla lista quando diventa CONSEGNATO', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInAttesa]);
    vi.mocked(useKitchenSocket).mockReturnValue({ connected: true, lastEvent: null });

    const { rerender } = render(<CucinaPage />);
    await screen.findByTestId('ordine-10');

    vi.mocked(useKitchenSocket).mockReturnValue({
      connected: true,
      lastEvent: buildEvent({ ordineId: 10, statoVecchio: 'IN_ATTESA', statoNuovo: 'CONSEGNATO' }),
    });
    rerender(<CucinaPage />);

    await waitFor(() => {
      expect(screen.queryByTestId('ordine-10')).not.toBeInTheDocument();
    });
  });

  it('mostra "Nessun ordine" nelle colonne vuote dopo il caricamento', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInPreparazione]);

    render(<CucinaPage />);

    await screen.findByTestId('ordine-11');
    // Le colonne IN_ATTESA e PRONTO devono essere vuote
    const vuote = await screen.findAllByText('Nessun ordine');
    expect(vuote.length).toBeGreaterThanOrEqual(2);
  });

  it('raggruppa gli ordini nelle colonne corrette', async () => {
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInAttesa, ordineInPreparazione]);

    render(<CucinaPage />);

    expect(await screen.findByTestId('ordine-10')).toBeInTheDocument();
    expect(await screen.findByTestId('ordine-11')).toBeInTheDocument();
    expect(screen.getByTestId('stato-10').textContent).toBe('IN_ATTESA');
    expect(screen.getByTestId('stato-11').textContent).toBe('IN_PREPARAZIONE');
  });

  it('chiama updateStatoOrdine quando il cuoco avanza uno stato', async () => {
    const user = userEvent.setup();
    vi.mocked(ordineService.getOrdini).mockResolvedValue([ordineInAttesa]);

    render(<CucinaPage />);

    await screen.findByTestId('ordine-10');
    await user.click(screen.getByRole('button', { name: /avanza/i }));

    expect(ordineService.updateStatoOrdine).toHaveBeenCalledWith(10, 'IN_PREPARAZIONE');
  });

  it('mostra il messaggio di errore quando il caricamento fallisce', async () => {
    vi.mocked(ordineService.getOrdini).mockRejectedValue(new Error('Network error'));

    render(<CucinaPage />);

    expect(await screen.findByText(/impossibile caricare/i)).toBeInTheDocument();
  });
});
