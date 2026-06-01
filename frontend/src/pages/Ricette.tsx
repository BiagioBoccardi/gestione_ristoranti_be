import { useEffect, useState } from 'react';
import { Plus, Trash2, ChefHat, FlaskConical, Pencil, X, Check } from 'lucide-react';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/layout/AppSidebar';
import { Input } from '@/components/ui/input';
import { menuService } from '@/services/menuService';
import { ricetteService, type Ingrediente, type Ricetta } from '@/services/ricetteService';

interface Piatto { id: number; nome: string; prezzo: number; }

function fmt(n: number) {
  return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(n);
}

function giudizioColor(fc: number) {
  if (fc <= 25) return 'text-emerald-400';
  if (fc <= 32) return 'text-blue-400';
  if (fc <= 40) return 'text-amber-400';
  return 'text-red-400';
}

export default function RicettePage() {
  const [ingredienti,     setIngredienti]     = useState<Ingrediente[]>([]);
  const [piatti,          setPiatti]          = useState<Piatto[]>([]);
  const [piattoSel,       setPiattoSel]       = useState<Piatto | null>(null);
  const [ricetta,         setRicetta]         = useState<Ricetta | null>(null);
  const [loadingRicetta,  setLoadingRicetta]  = useState(false);

  // Form ingrediente
  const [ingForm,    setIngForm]    = useState({ nome: '', unitaMisura: '', costoPerUnita: '' });
  const [editIngId,  setEditIngId]  = useState<number | null>(null);
  const [showIngForm, setShowIngForm] = useState(false);

  // Form voce ricetta
  const [voceForm, setVoceForm] = useState({ ingredienteId: '', quantita: '', percentualeScarto: '0' });
  const [addingVoce, setAddingVoce] = useState(false);

  useEffect(() => {
    ricetteService.getIngredienti().then(setIngredienti);
    menuService.getPiatti().then(list => setPiatti(
      list.map((p: { id: number; nome: string; prezzo: number }) => ({ id: p.id, nome: p.nome, prezzo: p.prezzo }))
    ));
  }, []);

  const loadRicetta = async (p: Piatto) => {
    setPiattoSel(p);
    setLoadingRicetta(true);
    try {
      const r = await ricetteService.getRicetta(p.id);
      setRicetta(r);
    } catch {
      setRicetta({ piattoId: p.id, nomePiatto: p.nome, prezzoVendita: p.prezzo, voci: [], costoTotale: 0, foodCostPercentuale: 0 });
    } finally {
      setLoadingRicetta(false);
    }
  };

  // ── Ingredienti CRUD ──────────────────────────────────────────────────────

  const openNewIng = () => {
    setIngForm({ nome: '', unitaMisura: '', costoPerUnita: '' });
    setEditIngId(null);
    setShowIngForm(true);
  };

  const openEditIng = (i: Ingrediente) => {
    setIngForm({ nome: i.nome, unitaMisura: i.unitaMisura, costoPerUnita: String(i.costoPerUnita) });
    setEditIngId(i.id);
    setShowIngForm(true);
  };

  const saveIngrediente = async () => {
    const body = { nome: ingForm.nome, unitaMisura: ingForm.unitaMisura, costoPerUnita: parseFloat(ingForm.costoPerUnita) };
    if (editIngId) {
      const updated = await ricetteService.aggiornaIngrediente(editIngId, body);
      setIngredienti(prev => prev.map(i => i.id === editIngId ? updated : i));
    } else {
      const created = await ricetteService.creaIngrediente(body);
      setIngredienti(prev => [...prev, created]);
    }
    setShowIngForm(false);
  };

  const deleteIngrediente = async (id: number) => {
    if (!window.confirm('Eliminare questo ingrediente?')) return;
    await ricetteService.eliminaIngrediente(id);
    setIngredienti(prev => prev.filter(i => i.id !== id));
  };

  // ── Voci ricetta ──────────────────────────────────────────────────────────

  const addVoce = async () => {
    if (!piattoSel || !voceForm.ingredienteId) return;
    const r = await ricetteService.aggiungiVoce(piattoSel.id, {
      ingredienteId: parseInt(voceForm.ingredienteId),
      quantita: parseFloat(voceForm.quantita) || 0,
      percentualeScarto: parseFloat(voceForm.percentualeScarto) || 0,
    });
    setRicetta(r);
    setVoceForm({ ingredienteId: '', quantita: '', percentualeScarto: '0' });
    setAddingVoce(false);
  };

  const deleteVoce = async (voceId: number) => {
    if (!piattoSel) return;
    await ricetteService.eliminaVoce(piattoSel.id, voceId);
    setRicetta(prev => prev ? { ...prev, voci: prev.voci.filter(v => v.id !== voceId) } : null);
  };

  return (
    <div className="min-h-screen bg-zinc-950">

      {/* Header */}
      <div className="bg-zinc-950 border-b border-zinc-800 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AppSidebar compact />
            <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center shadow-md shadow-indigo-900/50">
              <FlaskConical className="w-4 h-4 text-white" />
            </div>
            <div>
              <h1 className="text-base font-semibold tracking-tight text-zinc-100">Ricette & Ingredienti</h1>
              <p className="text-xs text-zinc-500 font-light">Gestione food cost per piatto</p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* ── Ingredienti ─────────────────────────────────────────────────── */}
        <div className="space-y-4">
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden">
            <div className="px-5 py-4 border-b border-zinc-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <FlaskConical className="w-4 h-4 text-zinc-400" />
                <p className="text-sm font-semibold text-zinc-100">Ingredienti</p>
                <span className="text-xs text-zinc-600 font-light">({ingredienti.length})</span>
              </div>
              <Button
                onClick={openNewIng}
                className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs h-8 px-3 gap-1.5 rounded-lg shadow-sm"
              >
                <Plus className="w-3.5 h-3.5" />
                Nuovo
              </Button>
            </div>

            {/* Form ingrediente */}
            {showIngForm && (
              <div className="px-5 py-4 border-b border-zinc-800 bg-zinc-800/40">
                <p className="text-xs text-zinc-400 uppercase tracking-widest font-semibold mb-3">
                  {editIngId ? 'Modifica ingrediente' : 'Nuovo ingrediente'}
                </p>
                <div className="grid grid-cols-3 gap-2 mb-3">
                  <Input
                    placeholder="Nome"
                    value={ingForm.nome}
                    onChange={e => setIngForm(f => ({ ...f, nome: e.target.value }))}
                    className="bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 text-sm h-9 rounded-lg col-span-1"
                  />
                  <Input
                    placeholder="Unità (kg, L…)"
                    value={ingForm.unitaMisura}
                    onChange={e => setIngForm(f => ({ ...f, unitaMisura: e.target.value }))}
                    className="bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 text-sm h-9 rounded-lg"
                  />
                  <Input
                    type="number"
                    placeholder="€/unità"
                    value={ingForm.costoPerUnita}
                    onChange={e => setIngForm(f => ({ ...f, costoPerUnita: e.target.value }))}
                    className="bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 text-sm h-9 rounded-lg"
                  />
                </div>
                <div className="flex gap-2">
                  <Button
                    onClick={saveIngrediente}
                    disabled={!ingForm.nome || !ingForm.unitaMisura || !ingForm.costoPerUnita}
                    className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs h-8 px-3 gap-1.5 rounded-lg"
                  >
                    <Check className="w-3.5 h-3.5" />
                    Salva
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => setShowIngForm(false)}
                    className="border-zinc-700 text-zinc-400 hover:bg-zinc-800 text-xs h-8 px-3 rounded-lg"
                  >
                    <X className="w-3.5 h-3.5" />
                  </Button>
                </div>
              </div>
            )}

            {ingredienti.length === 0 ? (
              <div className="flex items-center justify-center h-32 text-zinc-600 text-sm">
                Nessun ingrediente — creane uno
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-zinc-950/50 border-b border-zinc-800">
                    <th className="px-5 py-2.5 text-left text-xs text-zinc-500 uppercase tracking-wider font-semibold">Nome</th>
                    <th className="px-5 py-2.5 text-left text-xs text-zinc-500 uppercase tracking-wider font-semibold">Unità</th>
                    <th className="px-5 py-2.5 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Costo/u</th>
                    <th className="px-2 py-2.5" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800/50">
                  {ingredienti.map(i => (
                    <tr key={i.id} className="hover:bg-zinc-800/30 transition-colors">
                      <td className="px-5 py-3 text-zinc-200 font-medium">{i.nome}</td>
                      <td className="px-5 py-3 text-zinc-500 text-xs uppercase">{i.unitaMisura}</td>
                      <td className="px-5 py-3 text-right text-zinc-400 tabular-nums">{fmt(i.costoPerUnita)}</td>
                      <td className="px-3 py-3 text-right">
                        <div className="flex gap-1.5 justify-end">
                          <button
                            onClick={() => openEditIng(i)}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium bg-zinc-800 text-zinc-300 hover:bg-indigo-600/20 hover:text-indigo-300 border border-zinc-700 hover:border-indigo-700 transition-all"
                          >
                            <Pencil className="w-3 h-3" />
                            Modifica
                          </button>
                          <button
                            onClick={() => deleteIngrediente(i.id)}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium bg-zinc-800 text-zinc-300 hover:bg-red-600/20 hover:text-red-400 border border-zinc-700 hover:border-red-800 transition-all"
                          >
                            <Trash2 className="w-3 h-3" />
                            Elimina
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* ── Ricette per piatto ────────────────────────────────────────────── */}
        <div className="space-y-4">

          {/* Selezione piatto */}
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden">
            <div className="px-5 py-4 border-b border-zinc-800 flex items-center gap-2">
              <ChefHat className="w-4 h-4 text-zinc-400" />
              <p className="text-sm font-semibold text-zinc-100">Seleziona piatto</p>
            </div>
            <div className="p-4 flex flex-wrap gap-2 max-h-48 overflow-y-auto">
              {piatti.map(p => (
                <button
                  key={p.id}
                  onClick={() => loadRicetta(p)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all border ${
                    piattoSel?.id === p.id
                      ? 'bg-indigo-600 border-indigo-500 text-white'
                      : 'bg-zinc-800 border-zinc-700 text-zinc-300 hover:border-zinc-600'
                  }`}
                >
                  {p.nome}
                </button>
              ))}
            </div>
          </div>

          {/* Ricetta del piatto selezionato */}
          {piattoSel && (
            <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden">
              <div className="px-5 py-4 border-b border-zinc-800 flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-zinc-100">{piattoSel.nome}</p>
                  <p className="text-xs text-zinc-500 font-light mt-0.5">Prezzo vendita: {fmt(piattoSel.prezzo)}</p>
                </div>
                {ricetta && ricetta.voci.length > 0 && (
                  <div className="text-right">
                    <p className="text-xs text-zinc-500">Costo: <span className="text-zinc-300 font-medium">{fmt(ricetta.costoTotale)}</span></p>
                    <p className={`text-xs font-semibold ${giudizioColor(ricetta.foodCostPercentuale)}`}>
                      Food cost: {ricetta.foodCostPercentuale.toFixed(1)}%
                    </p>
                  </div>
                )}
              </div>

              {loadingRicetta ? (
                <div className="flex items-center justify-center h-24">
                  <div className="w-6 h-6 border-2 border-zinc-800 border-t-indigo-500 rounded-full animate-spin" />
                </div>
              ) : (
                <>
                  {ricetta && ricetta.voci.length > 0 && (
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="bg-zinc-950/50 border-b border-zinc-800">
                          <th className="px-5 py-2.5 text-left text-xs text-zinc-500 uppercase tracking-wider font-semibold">Ingrediente</th>
                          <th className="px-4 py-2.5 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Qtà</th>
                          <th className="px-4 py-2.5 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Scarto%</th>
                          <th className="px-4 py-2.5 text-right text-xs text-zinc-500 uppercase tracking-wider font-semibold">Costo</th>
                          <th className="px-3 py-2.5" />
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-zinc-800/50">
                        {ricetta.voci.map(v => (
                          <tr key={v.id} className="hover:bg-zinc-800/30 transition-colors">
                            <td className="px-5 py-3 text-zinc-200">
                              {v.nomeIngrediente}
                              <span className="ml-1.5 text-xs text-zinc-600">{v.unitaMisura}</span>
                            </td>
                            <td className="px-4 py-3 text-right text-zinc-400 tabular-nums">{v.quantita}</td>
                            <td className="px-4 py-3 text-right text-zinc-500 tabular-nums">{v.percentualeScarto}%</td>
                            <td className="px-4 py-3 text-right text-zinc-300 font-medium tabular-nums">{fmt(v.costoVoce)}</td>
                            <td className="px-3 py-3">
                              <button onClick={() => deleteVoce(v.id)} className="p-1.5 text-zinc-600 hover:text-red-400 hover:bg-zinc-800 rounded-lg transition-colors">
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}

                  {/* Form aggiungi voce */}
                  <div className="px-5 py-4 border-t border-zinc-800">
                    {!addingVoce ? (
                      <button
                        onClick={() => setAddingVoce(true)}
                        className="flex items-center gap-2 text-xs text-indigo-400 hover:text-indigo-300 transition-colors font-medium"
                      >
                        <Plus className="w-3.5 h-3.5" />
                        Aggiungi ingrediente alla ricetta
                      </button>
                    ) : (
                      <div className="space-y-3">
                        <p className="text-xs text-zinc-400 uppercase tracking-widest font-semibold">Nuovo ingrediente</p>
                        <div className="grid grid-cols-3 gap-2">
                          <select
                            value={voceForm.ingredienteId}
                            onChange={e => setVoceForm(f => ({ ...f, ingredienteId: e.target.value }))}
                            className="col-span-3 bg-zinc-800 border border-zinc-700 text-zinc-200 text-sm h-9 rounded-lg px-3 focus:outline-none focus:border-indigo-500"
                          >
                            <option value="">Seleziona ingrediente…</option>
                            {ingredienti.map(i => (
                              <option key={i.id} value={i.id}>{i.nome} ({i.unitaMisura})</option>
                            ))}
                          </select>
                          <Input
                            type="number"
                            placeholder="Quantità"
                            value={voceForm.quantita}
                            onChange={e => setVoceForm(f => ({ ...f, quantita: e.target.value }))}
                            className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 text-sm h-9 rounded-lg"
                          />
                          <Input
                            type="number"
                            placeholder="Scarto %"
                            value={voceForm.percentualeScarto}
                            onChange={e => setVoceForm(f => ({ ...f, percentualeScarto: e.target.value }))}
                            className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 text-sm h-9 rounded-lg"
                          />
                          <div className="flex gap-2">
                            <Button
                              onClick={addVoce}
                              disabled={!voceForm.ingredienteId || !voceForm.quantita}
                              className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white text-xs h-9 rounded-lg"
                            >
                              <Check className="w-3.5 h-3.5" />
                            </Button>
                            <Button
                              variant="outline"
                              onClick={() => setAddingVoce(false)}
                              className="flex-1 border-zinc-700 text-zinc-400 hover:bg-zinc-800 text-xs h-9 rounded-lg"
                            >
                              <X className="w-3.5 h-3.5" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
