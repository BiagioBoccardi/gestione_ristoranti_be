import { useCallback, useEffect, useState } from 'react';
import { RefreshCw, Wifi, WifiOff } from 'lucide-react';
import AppSidebar from '@/components/layout/AppSidebar';
import OrdineCard from '@/components/ordini/OrdineCard';
import { useKitchenSocket } from '@/hooks/useKitchenSocket';
import { ordineService } from '@/services/ordineService';
import type { Ordine, StatoOrdine } from '@/types/ordine';

const STATI_ATTIVI: StatoOrdine[] = ['IN_ATTESA', 'IN_PREPARAZIONE', 'PRONTO'];

const COLONNE: { stato: StatoOrdine; label: string; accent: string }[] = [
  { stato: 'IN_ATTESA',       label: 'In attesa',       accent: 'border-stone-300' },
  { stato: 'IN_PREPARAZIONE', label: 'In preparazione', accent: 'border-amber-400'  },
  { stato: 'PRONTO',          label: 'Pronto',          accent: 'border-green-400'  },
];

export default function CucinaPage() {
  const [ordini, setOrdini]   = useState<Ordine[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);

  const { connected, lastEvent } = useKitchenSocket();

  const loadOrdini = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await ordineService.getOrdini();
      setOrdini(data.filter(o => STATI_ATTIVI.includes(o.stato)));
    } catch {
      setError('Impossibile caricare gli ordini.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadOrdini(); }, [loadOrdini]);

  useEffect(() => {
    if (!lastEvent) return;
    const { ordineId, statoNuovo } = lastEvent;

    if (!STATI_ATTIVI.includes(statoNuovo)) {
      setOrdini(prev => prev.filter(o => o.id !== ordineId));
      return;
    }

    setOrdini(prev => {
      const exists = prev.some(o => o.id === ordineId);
      if (exists) {
        return prev.map(o => o.id === ordineId ? { ...o, stato: statoNuovo } : o);
      }
      // Ordine non ancora in lista: lo recuperiamo via REST
      ordineService.getOrdine(ordineId).then(ordine => {
        setOrdini(current =>
          current.some(o => o.id === ordine.id)
            ? current
            : [...current, ordine]
        );
      });
      return prev;
    });
  }, [lastEvent]);

  async function handleCambiaStato(id: number, stato: StatoOrdine) {
    try {
      await ordineService.updateStatoOrdine(id, stato);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Impossibile aggiornare lo stato.');
    }
  }

  return (
    <div className="min-h-screen bg-stone-50">
      <div className="border-b border-stone-200 bg-white px-6 py-4">
        <div className="flex items-center justify-between max-w-7xl mx-auto">
          <div className="flex items-center gap-2">
            <AppSidebar variant="light" compact />
            <h1 className="text-sm font-medium tracking-widest uppercase text-stone-800">
              Vista Cucina
            </h1>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={loadOrdini}
              disabled={loading}
              aria-label="Aggiorna"
              className="text-stone-400 hover:text-stone-700 p-1 rounded transition-colors disabled:opacity-40"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <div className="flex items-center gap-1.5 text-xs text-stone-500">
              {connected ? (
                <><Wifi className="w-3.5 h-3.5 text-green-500" /><span>Live</span></>
              ) : (
                <><WifiOff className="w-3.5 h-3.5 text-red-400" /><span>Connessione...</span></>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto p-6">
        {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

        <div className="grid grid-cols-3 gap-4">
          {COLONNE.map(({ stato, label, accent }) => {
            const colOrdini = ordini.filter(o => o.stato === stato);
            return (
              <div key={stato} className="flex flex-col gap-3">
                <div className={`flex items-center justify-between border-b-2 ${accent} pb-2`}>
                  <span className="text-xs font-medium tracking-widest uppercase text-stone-500">
                    {label}
                  </span>
                  <span className="text-xs font-medium text-stone-400 bg-stone-100 rounded-full px-2 py-0.5">
                    {colOrdini.length}
                  </span>
                </div>

                {loading && colOrdini.length === 0 && (
                  <div className="flex flex-col gap-2">
                    {[1, 2].map(i => (
                      <div key={i} className="h-28 rounded-xl bg-stone-100 animate-pulse" />
                    ))}
                  </div>
                )}

                {!loading && colOrdini.length === 0 && (
                  <p className="text-xs text-stone-400 text-center py-4">Nessun ordine</p>
                )}

                {colOrdini.map(o => (
                  <OrdineCard
                    key={o.id}
                    ordine={o}
                    onEdit={() => {}}
                    onCambiaStato={handleCambiaStato}
                    canEdit={false}
                  />
                ))}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
