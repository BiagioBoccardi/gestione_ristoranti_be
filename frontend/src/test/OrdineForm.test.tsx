/// <reference types="vitest/globals" />
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OrdineForm from '@/components/ordini/OrdineForm';
import type { Piatto } from '@/types/menu';

const piatti: Piatto[] = [
  { id: 1, nome: 'Pizza Margherita', prezzo: 8.5, disponibile: true, categoriaId: 1, categoriaName: 'Pizze', immagineUrl: null, descrizione: '' },
  { id: 2, nome: 'Pasta al Ragù', prezzo: 9.0, disponibile: true, categoriaId: 2, categoriaName: 'Primi', immagineUrl: null, descrizione: '' },
];

describe('OrdineForm', () => {
  it('non si renderizza quando open=false', () => {
    render(
      <OrdineForm
        open={false}
        onClose={vi.fn()}
        onSave={vi.fn()}
        piatti={piatti}
      />
    );
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('si renderizza con il titolo "Nuovo Ordine" quando open=true senza ordine', () => {
    render(
      <OrdineForm
        open={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
        piatti={piatti}
      />
    );
    expect(screen.getByText(/nuovo ordine/i)).toBeTruthy();
  });

  it('mostra il titolo "Modifica Ordine" quando ordine è presente', () => {
    const ordine = {
      id: 10,
      tavoloId: 1,
      numeroTavolo: 3,
      stato: 'IN_ATTESA' as any,
      items: [{ id: 1, piatto: piatti[0], quantita: 2, prezzoUnitario: 8.5, note: '' }],
      totale: 17,
      creatoAt: '',
      nomeUtente: 'Mario',
    };

    render(
      <OrdineForm
        open={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
        ordine={ordine}
        tavoloId={1}
        piatti={piatti}
      />
    );
    expect(screen.getByText(/modifica ordine/i)).toBeTruthy();
  });

  it('chiama onClose al click su Annulla', async () => {
    const onClose = vi.fn();
    render(
      <OrdineForm
        open={true}
        onClose={onClose}
        onSave={vi.fn()}
        piatti={piatti}
        tavoloId={1}
      />
    );

    const annullaBtn = screen.getByRole('button', { name: /annulla/i });
    await userEvent.click(annullaBtn);

    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
