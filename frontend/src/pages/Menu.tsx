import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, RefreshCw, Search, Settings2, Tag, CalendarDays } from 'lucide-react';
import AppSidebar from '@/components/layout/AppSidebar';

import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { menuService } from '@/services/menuService';
import { Role } from '@/types/auth';
import type { Categoria, CategoriaPayload, Piatto, PiattoPayload } from '@/types/menu';
import CategoriaForm from '@/components/menu/CategoriaForm';
import PiattoCard from '@/components/menu/PiattoCard';
import PiattoForm from '@/components/menu/PiattoForm';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SkeletonCard } from '@/components/ui/skeleton';

export default function MenuPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const canEdit = user?.role === Role.ADMIN;
  const isCliente = user?.role === Role.CLIENTE;

  const [piatti, setPiatti] = useState<Piatto[]>([]);
  const [categorie, setCategorie] = useState<Categoria[]>([]);
  const [selectedCat, setSelectedCat] = useState<number | null>(null);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [piattoFormOpen, setPiattoFormOpen] = useState(false);
  const [editingPiatto, setEditingPiatto] = useState<Piatto | undefined>();
  const [categoriaFormOpen, setCategoriaFormOpen] = useState(false);
  const [editingCategoria, setEditingCategoria] = useState<Categoria | undefined>();

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [piattiData, categorieData] = await Promise.all([
        menuService.getPiatti(selectedCat ?? undefined),
        menuService.getCategorie(),
      ]);
      setPiatti(piattiData);
      setCategorie(categorieData);
    } catch {
      setError('Errore nel caricamento del menu. Riprova.');
    } finally {
      setLoading(false);
    }
  }, [selectedCat]);

  useEffect(() => { loadData(); }, [loadData]);

  const filtered = piatti.filter(p =>
    p.nome.toLowerCase().includes(search.toLowerCase()) ||
    p.descrizione?.toLowerCase().includes(search.toLowerCase())
  );

  // CRUD piatti
  const handleSavePiatto = async (data: PiattoPayload) => {
    if (editingPiatto) {
      await menuService.updatePiatto(editingPiatto.id, data);
      toast.success('Piatto aggiornato.');
    } else {
      await menuService.createPiatto(data);
      toast.success('Piatto creato.');
    }
    await loadData();
  };

  const handleDeletePiatto = async (id: number) => {
    if (!window.confirm('Eliminare questo piatto?')) return;
    try {
      await menuService.deletePiatto(id);
      await loadData();
      toast.success('Piatto eliminato.');
    } catch (err: unknown) {
      const data = (err as { response?: { data?: unknown } }).response?.data;
      const msg = typeof data === 'string' ? data
        : (data as { message?: string })?.message
        ?? 'Impossibile eliminare il piatto.';
      alert(msg);
    }
  };

  const openEditPiatto = (p: Piatto) => { setEditingPiatto(p); setPiattoFormOpen(true); };
  const openNewPiatto  = ()          => { setEditingPiatto(undefined); setPiattoFormOpen(true); };

  // CRUD categorie
  const handleSaveCategoria = async (data: CategoriaPayload) => {
    if (editingCategoria) {
      await menuService.updateCategoria(editingCategoria.id, data);
      toast.success('Categoria aggiornata.');
    } else {
      await menuService.createCategoria(data);
      toast.success('Categoria creata.');
    }
    await loadData();
  };

  const openEditCategoria = (c: Categoria) => { setEditingCategoria(c); setCategoriaFormOpen(true); };
  const openNewCategoria  = ()             => { setEditingCategoria(undefined); setCategoriaFormOpen(true); };

  return (
    <div className="min-h-screen bg-stone-50">
      {/* Header */}
      <div className="bg-white border-b border-stone-200 px-6 py-4 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <AppSidebar variant="light" compact />
          <h1 className="text-xl font-light tracking-widest uppercase text-stone-800">Menu</h1>
        </div>
        <div className="flex items-center gap-2">
        {isCliente && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => navigate('/prenotazioni')}
            className="text-xs tracking-widest uppercase"
          >
            <CalendarDays className="w-3.5 h-3.5 mr-1" />
            Prenotazioni
          </Button>
        )}
        {canEdit && (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={openNewCategoria}
              className="text-xs tracking-widest uppercase"
            >
              <Tag className="w-3.5 h-3.5 mr-1" />
              Categoria
            </Button>
            <Button
              size="sm"
              onClick={openNewPiatto}
              className="bg-stone-800 hover:bg-stone-700 text-stone-50 text-xs tracking-widest uppercase"
            >
              <Plus className="w-3.5 h-3.5 mr-1" />
              Piatto
            </Button>
          </div>
        )}
        </div>
      </div>

      <div className="px-6 py-5 flex flex-col gap-5 max-w-7xl mx-auto">
        {/* Filtri + ricerca */}
        <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
          <div className="flex items-center gap-2 flex-wrap">
            <button
              onClick={() => setSelectedCat(null)}
              className={`px-3 py-1 rounded-full text-xs font-medium tracking-widest uppercase transition-colors ${
                selectedCat === null
                  ? 'bg-stone-800 text-stone-50'
                  : 'bg-white border border-stone-200 text-stone-600 hover:bg-stone-100'
              }`}
            >
              Tutti
            </button>
            {categorie.map(cat => (
              <button
                key={cat.id}
                onClick={() => setSelectedCat(cat.id === selectedCat ? null : cat.id)}
                className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium tracking-widest uppercase transition-colors ${
                  selectedCat === cat.id
                    ? 'bg-stone-800 text-stone-50'
                    : 'bg-white border border-stone-200 text-stone-600 hover:bg-stone-100'
                }`}
              >
                {cat.nome}
                {canEdit && (
                  <span
                    role="button"
                    onClick={e => { e.stopPropagation(); openEditCategoria(cat); }}
                    className="opacity-50 hover:opacity-100"
                    title="Modifica categoria"
                  >
                    <Settings2 className="w-2.5 h-2.5" />
                  </span>
                )}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2 w-full sm:w-64">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-stone-400 pointer-events-none" />
              <Input
                placeholder="Cerca piatto…"
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="pl-8 bg-white border-stone-200 placeholder:text-stone-300 text-sm"
              />
            </div>
            <Button
              variant="outline"
              size="icon"
              onClick={loadData}
              disabled={loading}
              aria-label="Aggiorna"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </Button>
          </div>
        </div>

        {/* Errore */}
        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Griglia */}
        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <SkeletonCard key={i} className="h-64 bg-stone-200" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-stone-400">
            <p className="text-sm">Nessun piatto trovato.</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            {filtered.map(p => (
              <PiattoCard
                key={p.id}
                piatto={p}
                canEdit={canEdit}
                onEdit={openEditPiatto}
                onDelete={handleDeletePiatto}
              />
            ))}
          </div>
        )}

        {!loading && !error && (
          <p className="text-xs text-stone-400 text-right">
            {filtered.length} piatt{filtered.length === 1 ? 'o' : 'i'}
            {search && ` per "${search}"`}
          </p>
        )}
      </div>

      <PiattoForm
        open={piattoFormOpen}
        onClose={() => { setPiattoFormOpen(false); setEditingPiatto(undefined); }}
        onSave={handleSavePiatto}
        piatto={editingPiatto}
        categorie={categorie}
      />
      <CategoriaForm
        open={categoriaFormOpen}
        onClose={() => { setCategoriaFormOpen(false); setEditingCategoria(undefined); }}
        onSave={handleSaveCategoria}
        categoria={editingCategoria}
      />
    </div>
  );
}
