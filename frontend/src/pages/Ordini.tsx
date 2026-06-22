import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { RefreshCw, LogOut, UtensilsCrossed, QrCode } from 'lucide-react';
import AppSidebar from '@/components/layout/AppSidebar';

import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { SkeletonCard } from '@/components/ui/skeleton';
import TavoloCard from '@/components/tavoli/TavoloCard';
import TavoloOrdiniModal from '@/components/tavoli/TavoloOrdiniModal';
import TavoloQrModal from '@/components/tavoli/TavoloQrModal';
import OrdineForm from '@/components/ordini/OrdineForm';
import { tavoloService } from '@/services/tavoloService';
import { ordineService } from '@/services/ordineService';
import { menuService } from '@/services/menuService';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth';
import type { Tavolo, Ordine, OrdinePayload, OrdineUpdatePayload } from '@/types/ordine';
import type { Piatto } from '@/types/menu';

const POLL_INTERVAL = 30_000;

export default function OrdiniPage() {
  const { user, logout } = useAuth();
  const { toast } = useToast();
  const { tavoloId: tavoloIdParam } = useParams<{ tavoloId?: string }>();

  const [tavoli, setTavoli] = useState<Tavolo[]>([]);
  const [piatti, setPiatti] = useState<Piatto[]>([]);
  const [selectedTavoloId, setSelectedTavoloId] = useState<number | null>(
    tavoloIdParam ? parseInt(tavoloIdParam) : null
  );
  const [loadingTavoli, setLoadingTavoli] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [ordineFormOpen, setOrdineFormOpen] = useState(false);
  const [editingOrdine, setEditingOrdine] = useState<Ordine | undefined>();
  const [panelRefreshKey, setPanelRefreshKey] = useState(0);

  const canEdit = user?.role === Role.ADMIN || user?.role === Role.CAMERIERE;
  const isAdmin = user?.role === Role.ADMIN;

  const [qrTavoloId, setQrTavoloId] = useState<number | null>(null);
  const qrTavolo = tavoli.find(t => t.id === qrTavoloId) ?? null;

  const loadTavoli = useCallback(async () => {
    setLoadingTavoli(true);
    setError(null);
    try {
      const data = await tavoloService.getTavoli();
      setTavoli(data);
    } catch {
      setError('Impossibile caricare i tavoli.');
    } finally {
      setLoadingTavoli(false);
    }
  }, []);

  useEffect(() => {
    loadTavoli();
    menuService.getPiatti().then(setPiatti).catch(() => {});

    const id = setInterval(loadTavoli, POLL_INTERVAL);
    return () => clearInterval(id);
  }, [loadTavoli]);

  function handleSelectTavolo(id: number) {
    setSelectedTavoloId(id);
  }

  function handleNuovoOrdine() {
    setEditingOrdine(undefined);
    setOrdineFormOpen(true);
  }

  function handleEditOrdine(ordine: Ordine) {
    setEditingOrdine(ordine);
    setOrdineFormOpen(true);
  }

  async function handleSaveOrdine(data: OrdinePayload | OrdineUpdatePayload) {
    if (editingOrdine) {
      await ordineService.updateOrdine(editingOrdine.id, data as OrdineUpdatePayload);
      toast({ title: 'Ordine aggiornato', description: 'Le modifiche sono state salvate.' });
    } else {
      await ordineService.createOrdine(data as OrdinePayload);
      toast({ title: 'Ordine creato', description: "L'ordine è stato inviato alla cucina." });
    }
    await loadTavoli();
    setPanelRefreshKey(k => k + 1);
  }

  const selectedTavolo = tavoli.find(t => t.id === selectedTavoloId) ?? null;

  return (
    <div className="min-h-screen bg-stone-50 font-sans">

      {/* Navbar */}
      <header className="sticky top-0 z-50 bg-white/80 backdrop-blur border-b border-stone-100">
        <div className="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AppSidebar variant="light" />
            <span className="text-stone-300 mx-1">·</span>
            <span className="text-xs tracking-widest uppercase text-stone-400">Ordini</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-stone-400 hidden sm:block">{user?.sub}</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={logout}
              className="text-stone-500 hover:text-stone-800 gap-1.5 text-xs"
            >
              <LogOut className="w-3.5 h-3.5" />
              Esci
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-6 py-8">

        {/* Intestazione sezione */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="text-xs tracking-[0.2em] uppercase text-stone-400 font-medium mb-1">
              Gestione
            </p>
            <h1 className="text-2xl font-light text-stone-900">Mappa Tavoli</h1>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={loadTavoli}
              disabled={loadingTavoli}
              className="text-stone-500 hover:text-stone-800 gap-1.5 text-xs"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loadingTavoli ? 'animate-spin' : ''}`} />
              Aggiorna
            </Button>
          </div>
        </div>

        {error && (
          <p className="text-sm text-red-600 mb-4">{error}</p>
        )}

        {/* Mappa tavoli */}
        {loadingTavoli && tavoli.length === 0 ? (
          <div className="grid grid-cols-3 sm:grid-cols-4 lg:grid-cols-5 gap-3">
            {Array.from({ length: 10 }).map((_, i) => (
              <SkeletonCard key={i} className="h-28 bg-stone-100" />
            ))}
          </div>
        ) : tavoli.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-stone-400">
            <UtensilsCrossed className="w-8 h-8 mb-3 opacity-40" />
            <p className="text-sm">Nessun tavolo trovato.</p>
          </div>
        ) : (
          <div className="grid grid-cols-3 sm:grid-cols-4 lg:grid-cols-5 gap-3">
            {tavoli.map(t => (
              <div key={t.id} className="relative group">
                <TavoloCard
                  tavolo={t}
                  selected={t.id === selectedTavoloId}
                  onClick={handleSelectTavolo}
                />
                {isAdmin && (
                  <button
                    onClick={e => { e.stopPropagation(); setQrTavoloId(t.id); }}
                    title="Mostra QR code"
                    className="absolute top-1.5 right-1.5 p-1 rounded-md bg-white/80 text-stone-400 hover:text-stone-700 hover:bg-white opacity-0 group-hover:opacity-100 transition-all shadow-sm"
                  >
                    <QrCode className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modale dettaglio tavolo */}
      <TavoloOrdiniModal
        tavolo={selectedTavolo}
        open={selectedTavoloId !== null && !ordineFormOpen}
        onClose={() => setSelectedTavoloId(null)}
        onNuovoOrdine={handleNuovoOrdine}
        onEditOrdine={handleEditOrdine}
        canEdit={canEdit}
        refreshKey={panelRefreshKey}
        onTavoloUpdated={loadTavoli}
      />

      {/* Modale QR tavolo */}
      {qrTavoloId !== null && qrTavolo !== null && (
        <TavoloQrModal
          tavoloId={qrTavoloId}
          numeroTavolo={qrTavolo.numero}
          onClose={() => setQrTavoloId(null)}
        />
      )}

      {/* Dialog crea/modifica ordine */}
      <OrdineForm
        open={ordineFormOpen}
        onClose={() => { setOrdineFormOpen(false); setEditingOrdine(undefined); }}
        onSave={handleSaveOrdine}
        ordine={editingOrdine}
        tavoloId={selectedTavoloId ?? undefined}
        piatti={piatti}
        tavoli={canEdit ? tavoli : undefined}
      />
    </div>
  );
}
