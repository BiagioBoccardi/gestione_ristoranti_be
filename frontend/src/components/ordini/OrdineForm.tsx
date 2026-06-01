import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import type { Ordine, OrdinePayload, OrdineUpdatePayload, Tavolo } from '@/types/ordine';
import type { Piatto } from '@/types/menu';

interface ItemDraft {
  piattoId: number | '';
  quantita: number;
  note: string;
}

/**
 * Dialog per creare o modificare un ordine.
 * In modalità creazione mostra un selettore tavolo (se `tavoli` è fornito).
 * In modalità modifica (`ordine` presente) precarica gli item esistenti.
 * Chiama `onSave` con il payload corretto (OrdinePayload o OrdineUpdatePayload)
 * e chiude il dialog al completamento.
 */
interface OrdineFormProps {
  open: boolean;
  onClose: () => void;
  /** Callback invocata al salvataggio; sollevare eccezione per mostrare errore */
  onSave: (data: OrdinePayload | OrdineUpdatePayload) => Promise<void>;
  /** Se presente, il form entra in modalità modifica */
  ordine?: Ordine;
  tavoloId?: number;
  piatti: Piatto[];
  /** Se fornito, mostra il selettore tavolo (utile per ADMIN/CAMERIERE) */
  tavoli?: Tavolo[];
}

function emptyItem(): ItemDraft {
  return { piattoId: '', quantita: 1, note: '' };
}

export default function OrdineForm({ open, onClose, onSave, ordine, tavoloId, piatti, tavoli }: OrdineFormProps) {
  const [selectedTavoloId, setSelectedTavoloId] = useState<number | ''>(tavoloId ?? '');
  const [items, setItems] = useState<ItemDraft[]>([emptyItem()]);
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isModifica = !!ordine;

  useEffect(() => {
    if (!open) return;
    setError(null);
    if (ordine) {
      setItems(
        ordine.items.map(i => ({
          piattoId: i.piatto?.id ?? '',
          quantita: i.quantita,
          note: i.note ?? '',
        }))
      );
      setNote('');
      setSelectedTavoloId(tavoloId ?? '');
    } else {
      setItems([emptyItem()]);
      setNote('');
      setSelectedTavoloId(tavoloId ?? '');
    }
  }, [open, ordine, tavoloId]);

  function setItem(index: number, patch: Partial<ItemDraft>) {
    setItems(prev => prev.map((it, i) => (i === index ? { ...it, ...patch } : it)));
  }

  function addItem() {
    setItems(prev => [...prev, emptyItem()]);
  }

  function removeItem(index: number) {
    setItems(prev => prev.filter((_, i) => i !== index));
  }

  function getPiatto(id: number | ''): Piatto | undefined {
    if (id === '') return undefined;
    return piatti.find(p => p.id === id);
  }

  const totaleCalcolato = items.reduce((sum, it) => {
    const p = getPiatto(it.piattoId);
    return sum + (p ? p.prezzo * it.quantita : 0);
  }, 0);

  const isValid =
    items.length > 0 &&
    items.every(it => it.piattoId !== '' && it.quantita > 0) &&
    (isModifica || selectedTavoloId !== '');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!isValid) return;
    setLoading(true);
    setError(null);
    try {
      const itemsPayload = items.map(it => ({
        piattoId: it.piattoId as number,
        quantita: it.quantita,
        note: it.note.trim() || undefined,
      }));

      if (isModifica) {
        await onSave({ items: itemsPayload, note: note.trim() || undefined } as OrdineUpdatePayload);
      } else {
        await onSave({
          tavoloId: selectedTavoloId as number,
          items: itemsPayload,
          note: note.trim() || undefined,
        } as OrdinePayload);
      }
      onClose();
    } catch {
      setError('Errore nel salvataggio. Riprova.');
    } finally {
      setLoading(false);
    }
  }

  const selectClass =
    'flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs focus-visible:outline-none focus-visible:border-ring disabled:cursor-not-allowed disabled:opacity-50';

  return (
    <Dialog.Root open={open} onOpenChange={o => !o && onClose()}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle>{isModifica ? `Modifica ordine #${ordine!.id}` : 'Nuovo ordine'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">

          {/* Selezione tavolo (solo in creazione se non pre-impostato) */}
          {!isModifica && !tavoloId && tavoli && (
            <div className="flex flex-col gap-1.5">
              <Label className="text-xs tracking-widest uppercase text-stone-500 font-medium">
                Tavolo *
              </Label>
              <select
                value={selectedTavoloId}
                onChange={e => setSelectedTavoloId(Number(e.target.value))}
                required
                disabled={loading}
                className={selectClass}
              >
                <option value="">Seleziona tavolo…</option>
                {tavoli.map(t => (
                  <option key={t.id} value={t.id}>
                    Tavolo {t.numero} — {t.stato}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Lista items */}
          <div className="flex flex-col gap-2">
            <Label className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Piatti *
            </Label>

            {items.map((item, idx) => (
              <div key={idx} className="flex gap-2 items-start">
                {/* Selezione piatto */}
                <select
                  value={item.piattoId}
                  onChange={e => setItem(idx, { piattoId: Number(e.target.value) })}
                  required
                  disabled={loading}
                  className={`${selectClass} flex-1`}
                >
                  <option value="">Scegli piatto…</option>
                  {piatti.filter(p => p.disponibile).map(p => (
                    <option key={p.id} value={p.id}>
                      {p.nome} — {p.prezzo.toFixed(2)} €
                    </option>
                  ))}
                </select>

                {/* Quantità */}
                <Input
                  type="number"
                  min={1}
                  value={item.quantita}
                  onChange={e => setItem(idx, { quantita: Math.max(1, parseInt(e.target.value) || 1) })}
                  disabled={loading}
                  className="w-16 shrink-0"
                />

                {/* Note item */}
                <Input
                  placeholder="Note"
                  value={item.note}
                  onChange={e => setItem(idx, { note: e.target.value })}
                  disabled={loading}
                  className="w-28 shrink-0"
                />

                {/* Rimuovi */}
                {items.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeItem(idx)}
                    disabled={loading}
                    className="mt-2 text-stone-400 hover:text-red-500 transition-colors shrink-0"
                    aria-label="Rimuovi piatto"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            ))}

            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={addItem}
              disabled={loading}
              className="self-start gap-1.5 text-xs"
            >
              <Plus className="w-3.5 h-3.5" />
              Aggiungi piatto
            </Button>
          </div>

          {/* Note ordine */}
          <div className="flex flex-col gap-1.5">
            <Label className="text-xs tracking-widest uppercase text-stone-500 font-medium">
              Note generali
            </Label>
            <Textarea
              value={note}
              onChange={e => setNote(e.target.value)}
              placeholder="Allergie, preferenze, richieste speciali…"
              disabled={loading}
            />
          </div>

          {/* Totale calcolato */}
          <div className="flex justify-between items-center bg-stone-50 rounded-lg px-4 py-2.5">
            <span className="text-xs font-medium tracking-widest uppercase text-stone-400">Totale stimato</span>
            <span className="text-sm font-medium text-stone-800">{totaleCalcolato.toFixed(2)} €</span>
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
              Annulla
            </Button>
            <Button
              type="submit"
              disabled={loading || !isValid}
              className="bg-stone-800 hover:bg-stone-700 text-stone-50"
            >
              {loading ? 'Salvataggio…' : 'Salva ordine'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog.Root>
  );
}
