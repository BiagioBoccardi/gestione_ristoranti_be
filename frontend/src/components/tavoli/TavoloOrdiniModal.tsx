import { useCallback, useEffect, useState } from 'react';
import { RefreshCw, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import OrdineCard from '@/components/ordini/OrdineCard';
import { useKitchenSocket } from '@/hooks/useKitchenSocket';
import { tavoloService } from '@/services/tavoloService';
import { ordineService } from '@/services/ordineService';
import type { Ordine, StatoOrdine, Tavolo } from '@/types/ordine';

const STATI_ATTIVI: StatoOrdine[] = ['IN_ATTESA', 'IN_PREPARAZIONE', 'PRONTO'];

interface Props {
  tavolo: Tavolo | null;
  open: boolean;
  onClose: () => void;
  onNuovoOrdine: () => void;
  onEditOrdine: (ordine: Ordine) => void;
  canEdit: boolean;
  refreshKey: number;
}

export default function TavoloOrdiniModal({
  tavolo,
  open,
  onClose,
  onNuovoOrdine,
  onEditOrdine,
  canEdit,
  refreshKey,
}: Props) {
  const [ordini, setOrdini] = useState<Ordine[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { lastEvent } = useKitchenSocket();

  const loadOrdini = useCallback(async () => {
    if (!tavolo) return;
    setLoading(true);
    setError(null);
    try {
      const data = await tavoloService.getOrdiniByTavolo(tavolo.id);
      setOrdini(data);
    } catch {
      setError('Impossibile caricare gli ordini.');
    } finally {
      setLoading(false);
    }
  }, [tavolo?.id]);

  useEffect(() => {
    if (open) loadOrdini();
    else setOrdini([]);
  }, [open, loadOrdini, refreshKey]);

  useEffect(() => {
    if (!lastEvent || !open || !tavolo) return;
    if (lastEvent.tavoloId === tavolo.id) loadOrdini();
  }, [lastEvent]);

  async function handleCambiaStato(id: number, stato: StatoOrdine) {
    try {
      await ordineService.updateStatoOrdine(id, stato);
      await loadOrdini();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Impossibile aggiornare lo stato.');
    }
  }

  const ordiniAttivi = ordini.filter(o => STATI_ATTIVI.includes(o.stato));
  const ordiniStorico = ordini.filter(o => !STATI_ATTIVI.includes(o.stato));

  return (
    <Dialog.Root open={open} onOpenChange={(o: boolean) => { if (!o) onClose(); }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between pr-4">
            <span>Tavolo {tavolo?.numero}</span>
            <button
              onClick={loadOrdini}
              disabled={loading}
              className="text-stone-400 hover:text-stone-700 p-1 rounded transition-colors disabled:opacity-40"
              aria-label="Aggiorna ordini"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-3">
          {error && <p className="text-sm text-red-600">{error}</p>}

          {loading && ordini.length === 0 && (
            <div className="flex flex-col gap-2">
              {[1, 2].map(i => (
                <div key={i} className="h-24 rounded-xl bg-stone-100 animate-pulse" />
              ))}
            </div>
          )}

          {!loading && ordini.length === 0 && (
            <p className="text-sm text-stone-400 text-center py-6">
              Nessun ordine per questo tavolo.
            </p>
          )}

          {ordiniAttivi.length > 0 && (
            <div className="flex flex-col gap-2">
              <p className="text-xs tracking-widest uppercase text-stone-400 font-medium">Attivi</p>
              {ordiniAttivi.map(o => (
                <OrdineCard
                  key={o.id}
                  ordine={o}
                  onEdit={onEditOrdine}
                  onCambiaStato={handleCambiaStato}
                  canEdit={canEdit}
                />
              ))}
            </div>
          )}

          {ordiniStorico.length > 0 && (
            <div className="flex flex-col gap-2">
              <p className="text-xs tracking-widest uppercase text-stone-400 font-medium">Storico</p>
              {ordiniStorico.map(o => (
                <OrdineCard
                  key={o.id}
                  ordine={o}
                  onEdit={onEditOrdine}
                  onCambiaStato={handleCambiaStato}
                  canEdit={canEdit}
                />
              ))}
            </div>
          )}

          {canEdit && (
            <div className="border-t border-stone-100 pt-3 mt-1">
              <Button
                onClick={onNuovoOrdine}
                className="w-full bg-stone-800 hover:bg-stone-700 text-stone-50 tracking-widest uppercase text-xs gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                Nuovo ordine
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog.Root>
  );
}
