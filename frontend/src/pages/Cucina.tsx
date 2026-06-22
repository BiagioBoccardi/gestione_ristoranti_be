import { useCallback, useEffect, useState } from 'react';
import { RefreshCw, Wifi, WifiOff, BookOpen, X } from 'lucide-react';
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

interface PiattoRiepilogo {
  nome: string;
  quantita: number;
}

function buildRiepilogoGiornaliero(ordini: Ordine[]): PiattoRiepilogo[] {
  const oggi = new Date().toISOString().split('T')[0];
  const mappa = new Map<string, number>();
  for (const o of ordini) {
    const dataOrdine = o.creatoAt?.split('T')[0];
    if (dataOrdine !== oggi) continue;
    for (const item of o.items) {
      const nome = item.piatto?.nome ?? 'Piatto sconosciuto';
      mappa.set(nome, (mappa.get(nome) ?? 0) + item.quantita);
    }
  }
  return Array.from(mappa.entries())
    .map(([nome, quantita]) => ({ nome, quantita }))
    .sort((a, b) => b.quantita - a.quantita);
}

export default function CucinaPage() {
  const [ordini, setOrdini]             = useState<Ordine[]>([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState<string | null>(null);
  const [showRiepilogo, setShowRiepilogo] = useState(false);
  const [riepilogoItems, setRiepilogoItems] = useState<PiattoRiepilogo[]>([]);
  const [loadingRiepilogo, setLoadingRiepilogo] = useState(false);

  const { connected, lastEvent } = useKitchenSocket();

  const loadOrdini = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await ordineService.getOrdini({});
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
      if (STATI_ATTIVI.includes(stato)) {
        setOrdini(prev => prev.map(o => o.id === id ? { ...o, stato } : o));
      } else {
        setOrdini(prev => prev.filter(o => o.id !== id));
      }
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? 'Impossibile aggiornare lo stato.');
    }
  }

  async function apriRiepilogo() {
    setShowRiepilogo(true);
    setLoadingRiepilogo(true);
    try {
      const tutti = await ordineService.getOrdini({ stato: 'CONSEGNATO' });
      setRiepilogoItems(buildRiepilogoGiornaliero(tutti));
    } catch {
      setRiepilogoItems([]);
    } finally {
      setLoadingRiepilogo(false);
    }
  }

  const totalePortate = riepilogoItems.reduce((s, p) => s + p.quantita, 0);

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
              onClick={apriRiepilogo}
              className="flex items-center gap-1.5 text-xs font-medium tracking-widest uppercase text-stone-600 hover:text-stone-900 border border-stone-200 rounded-lg px-3 py-1.5 hover:bg-stone-50 transition-colors"
            >
              <BookOpen className="w-3.5 h-3.5" />
              Chiusura giornata
            </button>
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

      {/* Modale chiusura giornata */}
      {showRiepilogo && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="bg-white rounded-2xl border border-stone-200 shadow-xl w-full max-w-md mx-4 flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-stone-100">
              <div>
                <h2 className="text-sm font-semibold tracking-widest uppercase text-stone-800">Chiusura giornata</h2>
                <p className="text-xs text-stone-400 mt-0.5">
                  {new Date().toLocaleDateString('it-IT', { weekday: 'long', day: '2-digit', month: 'long' })}
                </p>
              </div>
              <button
                onClick={() => setShowRiepilogo(false)}
                className="text-stone-400 hover:text-stone-600 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="overflow-y-auto flex-1 px-6 py-4">
              {loadingRiepilogo ? (
                <div className="flex flex-col gap-2">
                  {[1, 2, 3, 4].map(i => (
                    <div key={i} className="h-10 rounded-lg bg-stone-100 animate-pulse" />
                  ))}
                </div>
              ) : riepilogoItems.length === 0 ? (
                <p className="text-sm text-stone-400 text-center py-8">Nessun ordine consegnato oggi.</p>
              ) : (
                <div className="flex flex-col gap-1">
                  {riepilogoItems.map(({ nome, quantita }) => (
                    <div key={nome} className="flex items-center justify-between py-2.5 border-b border-stone-50 last:border-0">
                      <span className="text-sm text-stone-700">{nome}</span>
                      <span className="text-sm font-semibold text-stone-900 tabular-nums bg-stone-100 rounded-full px-2.5 py-0.5">
                        ×{quantita}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {!loadingRiepilogo && riepilogoItems.length > 0 && (
              <div className="flex items-center justify-between px-6 py-4 border-t border-stone-100 bg-stone-50 rounded-b-2xl">
                <span className="text-xs font-medium tracking-widest uppercase text-stone-500">Totale portate</span>
                <span className="text-sm font-bold text-stone-900 tabular-nums">{totalePortate}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
