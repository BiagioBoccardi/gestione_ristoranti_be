import { useCallback, useEffect, useState } from 'react';
import { RefreshCw, X, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import OrdineCard from '@/components/ordini/OrdineCard';
import { tavoloService } from '@/services/tavoloService';
import { ordineService } from '@/services/ordineService';
import type { Ordine, StatoOrdine, Tavolo } from '@/types/ordine';

const STATI_ATTIVI: StatoOrdine[] = ['IN_ATTESA', 'IN_PREPARAZIONE', 'PRONTO'];

interface TavoloDettaglioPanelProps {
  tavolo: Tavolo;
  onClose: () => void;
  onNuovoOrdine: () => void;
  onEditOrdine: (ordine: Ordine) => void;
  canEdit: boolean;
}

export default function TavoloDettaglioPanel({
  tavolo,
  onClose,
  onNuovoOrdine,
  onEditOrdine,
  canEdit,
}: TavoloDettaglioPanelProps) {
  const [ordini, setOrdini] = useState<Ordine[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadOrdini = useCallback(async () => {
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
  }, [tavolo.id]);

  useEffect(() => {
    loadOrdini();
  }, [loadOrdini]);

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
    <div className="flex flex-col h-full bg-white rounded-xl border border-stone-200 overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-stone-100">
        <h2 className="text-sm font-medium tracking-widest uppercase text-stone-700">
          Tavolo {tavolo.numero}
        </h2>
        <div className="flex items-center gap-1">
          <button
            onClick={loadOrdini}
            disabled={loading}
            className="text-stone-400 hover:text-stone-700 p-1 rounded transition-colors disabled:opacity-40"
            aria-label="Aggiorna ordini"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={onClose}
            className="text-stone-400 hover:text-stone-700 p-1 rounded transition-colors"
            aria-label="Chiudi pannello"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Body */}
      <div className="flex-1 overflow-y-auto px-4 py-3 flex flex-col gap-3">
        {error && <p className="text-sm text-red-600">{error}</p>}

        {loading && ordini.length === 0 && (
          <div className="flex flex-col gap-2">
            {[1, 2].map(i => (
              <div key={i} className="h-24 rounded-xl bg-stone-100 animate-pulse" />
            ))}
          </div>
        )}

        {!loading && ordini.length === 0 && (
          <p className="text-sm text-stone-400 text-center py-6">Nessun ordine per questo tavolo.</p>
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
      </div>

      {/* Footer */}
      {canEdit && (
        <div className="border-t border-stone-100 px-4 py-3">
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
  );
}
