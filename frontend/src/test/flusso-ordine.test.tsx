/// <reference types="vitest/globals" />
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { Ordine, StatoOrdine, Tavolo } from '@/types/ordine';
import type { Piatto } from '@/types/menu';

// ── Mock react-router-dom ─────────────────────────────────────────────────────

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...(actual as object), useNavigate: () => vi.fn() };
});

// ── Mock componenti UI ────────────────────────────────────────────────────────

vi.mock('@/components/ui/dialog', () => ({
  Dialog: {
    Root: ({ open, children }: { open: boolean; children: React.ReactNode; onOpenChange?: (o: boolean) => void }) =>
      open ? <>{children}</> : null,
  },
  DialogContent: ({ children }: { children: React.ReactNode }) =>
    <div role="dialog">{children}</div>,
  DialogHeader: ({ children }: { children: React.ReactNode }) =>
    <div>{children}</div>,
  DialogTitle: ({ children }: { children: React.ReactNode }) =>
    <h2>{children}</h2>,
  DialogFooter: ({ children }: { children: React.ReactNode }) =>
    <div>{children}</div>,
}));

vi.mock('@/components/ui/badge', () => ({
  Badge: ({ children, variant }: { children: React.ReactNode; variant?: string }) =>
    <span data-variant={variant}>{children}</span>,
}));

vi.mock('@/components/ui/button', () => ({
  Button: ({ children, onClick, disabled, type, className }: {
    children: React.ReactNode;
    onClick?: () => void;
    disabled?: boolean;
    type?: 'button' | 'submit' | 'reset';
    className?: string;
    variant?: string;
    size?: string;
  }) =>
    <button type={type ?? 'button'} onClick={onClick} disabled={disabled} className={className}>{children}</button>,
}));

vi.mock('@/components/ui/input', () => ({
  Input: (props: React.InputHTMLAttributes<HTMLInputElement>) => <input {...props} />,
}));

vi.mock('@/components/ui/label', () => ({
  Label: ({ children }: { children: React.ReactNode }) => <label>{children}</label>,
}));

vi.mock('@/components/ui/textarea', () => ({
  Textarea: (props: React.TextareaHTMLAttributes<HTMLTextAreaElement>) => <textarea {...props} />,
}));

// ── Mock servizi ──────────────────────────────────────────────────────────────

vi.mock('@/services/tavoloService', () => ({
  tavoloService: {
    getOrdiniByTavolo: vi.fn(),
  },
}));

vi.mock('@/services/ordineService', () => ({
  ordineService: {
    updateStatoOrdine: vi.fn(),
  },
}));

// ── Import componenti (dopo i mock) ───────────────────────────────────────────

import OrdineForm from '@/components/ordini/OrdineForm';
import OrdineCard from '@/components/ordini/OrdineCard';
import TavoloOrdiniModal from '@/components/tavoli/TavoloOrdiniModal';
import { tavoloService } from '@/services/tavoloService';
import { ordineService } from '@/services/ordineService';

// ── Dati di test ──────────────────────────────────────────────────────────────

const piatti: Piatto[] = [
  { id: 1, nome: 'Margherita', prezzo: 8.0, disponibile: true, categoria: { id: 1, nome: 'Pizze' } },
  { id: 2, nome: 'Tiramisù', prezzo: 5.0, disponibile: true, categoria: { id: 2, nome: 'Dessert' } },
];

const tavolo: Tavolo = { id: 1, numero: 5, capacita: 4, stato: 'LIBERO' };

const ordineBase: Ordine = {
  id: 10,
  tavoloId: 1,
  numeroTavolo: 5,
  stato: 'IN_ATTESA',
  items: [
    { id: 100, piatto: { id: 1, nome: 'Margherita', prezzo: 8.0 }, prezzoUnitario: 8.0, quantita: 2, note: '' },
  ],
  totale: 16.0,
  creatoAt: '2026-05-12T10:00:00Z',
};

// ── Test: OrdineForm ──────────────────────────────────────────────────────────

describe('OrdineForm – creazione e modifica ordine', () => {
  it('mostra "Nuovo ordine" in modalità creazione', () => {
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);
    expect(screen.getByText('Nuovo ordine')).toBeInTheDocument();
  });

  it('mostra "Modifica ordine #10" in modalità modifica', () => {
    render(
      <OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} ordine={ordineBase} />
    );
    expect(screen.getByText('Modifica ordine #10')).toBeInTheDocument();
  });

  it('il pulsante Salva è disabilitato senza un piatto selezionato', () => {
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);
    expect(screen.getByRole('button', { name: /salva ordine/i })).toBeDisabled();
  });

  it('il pulsante Salva si abilita dopo aver selezionato un piatto', async () => {
    const user = userEvent.setup();
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);

    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], '1');

    expect(screen.getByRole('button', { name: /salva ordine/i })).not.toBeDisabled();
  });

  it('chiama onSave con il payload corretto alla creazione', async () => {
    const user = userEvent.setup();
    const onSave = vi.fn().mockResolvedValue(undefined);
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={onSave} piatti={piatti} tavoloId={1} />);

    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], '1');
    await user.click(screen.getByRole('button', { name: /salva ordine/i }));

    expect(onSave).toHaveBeenCalledWith(
      expect.objectContaining({
        tavoloId: 1,
        items: [expect.objectContaining({ piattoId: 1, quantita: 1 })],
      })
    );
  });

  it('aggiunge e rimuove un piatto correttamente', async () => {
    const user = userEvent.setup();
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);

    await user.click(screen.getByRole('button', { name: /aggiungi piatto/i }));
    expect(screen.getAllByRole('combobox')).toHaveLength(2);

    await user.click(screen.getAllByRole('button', { name: /rimuovi piatto/i })[0]);
    expect(screen.getAllByRole('combobox')).toHaveLength(1);
  });

  it('mostra il totale calcolato in base ai piatti selezionati', async () => {
    const user = userEvent.setup();
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);

    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], '1'); // Margherita 8.00€ × 1

    expect(screen.getByText('8.00 €')).toBeInTheDocument();
  });

  it('mostra un messaggio di errore quando onSave fallisce', async () => {
    const user = userEvent.setup();
    const onSave = vi.fn().mockRejectedValue(new Error('Network error'));
    render(<OrdineForm open={true} onClose={vi.fn()} onSave={onSave} piatti={piatti} tavoloId={1} />);

    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], '1');
    await user.click(screen.getByRole('button', { name: /salva ordine/i }));

    expect(await screen.findByText(/errore nel salvataggio/i)).toBeInTheDocument();
  });

  it('non renderizza nulla quando open è false', () => {
    render(<OrdineForm open={false} onClose={vi.fn()} onSave={vi.fn()} piatti={piatti} tavoloId={1} />);
    expect(screen.queryByText('Nuovo ordine')).not.toBeInTheDocument();
  });
});

// ── Test: OrdineCard ──────────────────────────────────────────────────────────

describe('OrdineCard – visualizzazione e interazioni', () => {
  it('mostra ID ordine, nome piatto e totale', () => {
    render(<OrdineCard ordine={ordineBase} onEdit={vi.fn()} onCambiaStato={vi.fn()} canEdit={true} />);
    expect(screen.getByText(/ordine #10/i)).toBeInTheDocument();
    expect(screen.getByText('Margherita')).toBeInTheDocument();
    // 16.00 € compare sia nell'item sia nella riga totale
    expect(screen.getAllByText('16.00 €').length).toBeGreaterThanOrEqual(1);
  });

  it('mostra il badge con lo stato corrente', () => {
    render(<OrdineCard ordine={ordineBase} onEdit={vi.fn()} onCambiaStato={vi.fn()} canEdit={true} />);
    // "In attesa" compare sia nel badge (span[data-variant]) sia come option nel select
    expect(screen.getByText('In attesa', { selector: 'span[data-variant]' })).toBeInTheDocument();
  });

  it('mostra il pulsante Modifica quando canEdit è true', () => {
    render(<OrdineCard ordine={ordineBase} onEdit={vi.fn()} onCambiaStato={vi.fn()} canEdit={true} />);
    expect(screen.getByRole('button', { name: /modifica/i })).toBeInTheDocument();
  });

  it('nasconde il pulsante Modifica quando canEdit è false', () => {
    render(<OrdineCard ordine={ordineBase} onEdit={vi.fn()} onCambiaStato={vi.fn()} canEdit={false} />);
    expect(screen.queryByRole('button', { name: /modifica/i })).not.toBeInTheDocument();
  });

  it('chiama onEdit con l\'ordine al click del pulsante Modifica', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(<OrdineCard ordine={ordineBase} onEdit={onEdit} onCambiaStato={vi.fn()} canEdit={true} />);

    await user.click(screen.getByRole('button', { name: /modifica/i }));
    expect(onEdit).toHaveBeenCalledWith(ordineBase);
  });

  it('chiama onCambiaStato con il nuovo stato selezionato', async () => {
    const user = userEvent.setup();
    const onCambiaStato = vi.fn().mockResolvedValue(undefined);
    render(<OrdineCard ordine={ordineBase} onEdit={vi.fn()} onCambiaStato={onCambiaStato} canEdit={true} />);

    await user.selectOptions(screen.getByRole('combobox'), 'IN_PREPARAZIONE');
    expect(onCambiaStato).toHaveBeenCalledWith(10, 'IN_PREPARAZIONE');
  });

  it('mostra quantità e note del piatto nell\'item', () => {
    const ordineConNote: Ordine = {
      ...ordineBase,
      items: [{ id: 101, piatto: { id: 1, nome: 'Margherita', prezzo: 8.0 }, prezzoUnitario: 8.0, quantita: 3, note: 'senza cipolla' }],
    };
    render(<OrdineCard ordine={ordineConNote} onEdit={vi.fn()} onCambiaStato={vi.fn()} canEdit={true} />);
    expect(screen.getByText(/3×/)).toBeInTheDocument();
    expect(screen.getByText(/senza cipolla/i)).toBeInTheDocument();
  });
});

// ── Test: TavoloOrdiniModal – flusso ordine completo ─────────────────────────

describe('TavoloOrdiniModal – flusso ordine completo', () => {
  const defaultProps = {
    tavolo,
    open: true,
    onClose: vi.fn(),
    onNuovoOrdine: vi.fn(),
    onEditOrdine: vi.fn(),
    canEdit: true,
    refreshKey: 0,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(ordineService.updateStatoOrdine).mockResolvedValue(ordineBase);
  });

  it('carica e mostra gli ordini quando la modale si apre', async () => {
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([ordineBase]);
    render(<TavoloOrdiniModal {...defaultProps} />);

    await waitFor(() => expect(tavoloService.getOrdiniByTavolo).toHaveBeenCalledWith(1));
    expect(await screen.findByText(/ordine #10/i)).toBeInTheDocument();
  });

  it('separa gli ordini attivi da quelli nello storico', async () => {
    const ordineConsegnato: Ordine = { ...ordineBase, id: 11, stato: 'CONSEGNATO' as StatoOrdine };
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([ordineBase, ordineConsegnato]);
    render(<TavoloOrdiniModal {...defaultProps} />);

    expect(await screen.findByText('Attivi')).toBeInTheDocument();
    expect(await screen.findByText('Storico')).toBeInTheDocument();
  });

  it('mostra il messaggio vuoto quando non ci sono ordini', async () => {
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([]);
    render(<TavoloOrdiniModal {...defaultProps} />);

    expect(await screen.findByText(/nessun ordine/i)).toBeInTheDocument();
  });

  it('mostra un errore quando il caricamento fallisce', async () => {
    vi.mocked(tavoloService.getOrdiniByTavolo).mockRejectedValue(new Error('Network error'));
    render(<TavoloOrdiniModal {...defaultProps} />);

    expect(await screen.findByText(/impossibile caricare/i)).toBeInTheDocument();
  });

  it('chiama onNuovoOrdine al click del pulsante "Nuovo ordine"', async () => {
    const user = userEvent.setup();
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([]);
    const onNuovoOrdine = vi.fn();
    render(<TavoloOrdiniModal {...defaultProps} onNuovoOrdine={onNuovoOrdine} />);

    await screen.findByText(/nessun ordine/i);
    await user.click(screen.getByRole('button', { name: /nuovo ordine/i }));
    expect(onNuovoOrdine).toHaveBeenCalledTimes(1);
  });

  it('chiama onEditOrdine quando si clicca Modifica su un ordine', async () => {
    const user = userEvent.setup();
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([ordineBase]);
    const onEditOrdine = vi.fn();
    render(<TavoloOrdiniModal {...defaultProps} onEditOrdine={onEditOrdine} />);

    await screen.findByText(/ordine #10/i);
    await user.click(screen.getByRole('button', { name: /modifica/i }));
    expect(onEditOrdine).toHaveBeenCalledWith(ordineBase);
  });

  it('non mostra contenuto quando la modale è chiusa', () => {
    vi.mocked(tavoloService.getOrdiniByTavolo).mockResolvedValue([]);
    render(<TavoloOrdiniModal {...defaultProps} open={false} />);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});
