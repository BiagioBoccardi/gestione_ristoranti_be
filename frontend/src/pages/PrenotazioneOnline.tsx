import { useEffect, useState } from 'react';
import AppSidebar from '@/components/layout/AppSidebar';
import { prenotazioneService } from '@/services/prenotazioneService';
import { tavoloService } from '@/services/tavoloService';
import type { Prenotazione } from '@/types/prenotazione';
import type { Tavolo } from '@/types/ordine';
import { useToast } from '@/context/ToastContext';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import {
  CalendarDays, Plus, Trash2, Loader2, LogOut, Clock, Users,
} from 'lucide-react';

interface PrenotazioneForm {
  tavoloId: number;
  data: string;
  ora: string;
  coperti: number;
  note: string;
}

const EMPTY_FORM: PrenotazioneForm = {
  tavoloId: 0,
  data: '',
  ora: '20:00',
  coperti: 2,
  note: '',
};

function formatData(d: string) {
  return new Date(d).toLocaleDateString('it-IT', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
  });
}

function formatOra(o: string) {
  return o.substring(0, 5);
}

export default function PrenotazioneOnlinePage() {
  const { user, logout } = useAuth();
  const toast = useToast();

  const [prenotazioni, setPrenotazioni] = useState<Prenotazione[]>([]);
  const [tavoli, setTavoli] = useState<Tavolo[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [cancellando, setCancellando] = useState<number | null>(null);
  const [form, setForm] = useState<PrenotazioneForm>(EMPTY_FORM);

  const caricaDati = async () => {
    setLoading(true);
    try {
      const [p, t] = await Promise.all([
        prenotazioneService.getMie(),
        tavoloService.getTavoli(),
      ]);
      setPrenotazioni(p);
      setTavoli(t);
      if (t.length > 0 && form.tavoloId === 0) {
        setForm(f => ({ ...f, tavoloId: t[0].id }));
      }
    } catch {
      toast.error('Impossibile caricare le prenotazioni.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { caricaDati(); }, []);

  const apriForm = () => {
    setForm({ ...EMPTY_FORM, tavoloId: tavoli[0]?.id ?? 0 });
    setShowForm(true);
  };

  const salva = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (!form.tavoloId) { toast.error('Seleziona un tavolo.'); return; }
    setSaving(true);
    try {
      await prenotazioneService.crea({
        tavoloId: form.tavoloId,
        data: form.data,
        ora: form.ora + ':00',
        coperti: form.coperti,
        note: form.note || undefined,
      });
      toast.success('Prenotazione confermata!');
      setShowForm(false);
      await caricaDati();
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      toast.error(e.response?.data?.message ?? 'Errore nella prenotazione.');
    } finally {
      setSaving(false);
    }
  };

  const cancella = async (id: number) => {
    if (!confirm('Annullare questa prenotazione?')) return;
    setCancellando(id);
    try {
      await prenotazioneService.cancella(id);
      setPrenotazioni(prev => prev.filter(p => p.id !== id));
      toast.success('Prenotazione annullata.');
    } catch {
      toast.error('Impossibile annullare la prenotazione.');
    } finally {
      setCancellando(null);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  const prossime = prenotazioni.filter(p => p.data >= today);
  const passate  = prenotazioni.filter(p => p.data < today);

  return (
    <div className="min-h-screen bg-zinc-950 font-sans">
      {/* Navbar */}
      <header className="sticky top-0 z-30 bg-zinc-950/90 backdrop-blur-md border-b border-zinc-800">
        <div className="max-w-3xl mx-auto px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AppSidebar />
            <span className="text-xs text-zinc-600 hidden sm:block">/ Le mie prenotazioni</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-zinc-500 hidden sm:block">{user?.sub}</span>
            <Button variant="ghost" size="sm" onClick={logout}
              className="text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800 gap-1.5 text-xs rounded-lg">
              <LogOut className="w-3.5 h-3.5" /> Esci
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-10">
        {/* Hero */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-600/20 flex items-center justify-center border border-indigo-600/30">
              <CalendarDays className="w-4 h-4 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-lg font-semibold text-zinc-100">Prenota un tavolo</h1>
              <p className="text-xs text-zinc-500">Gestisci le tue prenotazioni al ristorante</p>
            </div>
          </div>
          <Button size="sm" onClick={apriForm}
            className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs rounded-lg gap-1.5">
            <Plus className="w-3.5 h-3.5" /> Nuova Prenotazione
          </Button>
        </div>

        {/* Modal form */}
        <Dialog.Root open={showForm} onOpenChange={(o: boolean) => { if (!o) setShowForm(false); }}>
          <DialogContent className="bg-zinc-900 border border-zinc-700 text-zinc-100 max-w-md">
            <DialogHeader>
              <DialogTitle className="text-base font-semibold text-zinc-100">Nuova prenotazione</DialogTitle>
            </DialogHeader>
            <form onSubmit={salva} className="flex flex-col gap-4">
              <div>
                <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Tavolo</Label>
                <select
                  value={form.tavoloId}
                  onChange={e => setForm(f => ({ ...f, tavoloId: Number(e.target.value) }))}
                  required
                  className="w-full h-10 px-3 rounded-lg bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  {tavoli.map(t => (
                    <option key={t.id} value={t.id}>
                      Tavolo {t.numero} ({t.capacita} posti)
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Data</Label>
                  <Input
                    type="date"
                    value={form.data}
                    min={today}
                    onChange={e => setForm(f => ({ ...f, data: e.target.value }))}
                    required
                    className="bg-zinc-800 border-zinc-700 text-zinc-100 rounded-lg [color-scheme:dark]"
                  />
                </div>
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Ora</Label>
                  <Input
                    type="time"
                    value={form.ora}
                    onChange={e => setForm(f => ({ ...f, ora: e.target.value }))}
                    required
                    className="bg-zinc-800 border-zinc-700 text-zinc-100 rounded-lg [color-scheme:dark]"
                  />
                </div>
              </div>
              <div>
                <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Coperti</Label>
                <Input
                  type="number"
                  min={1}
                  max={20}
                  value={form.coperti}
                  onChange={e => setForm(f => ({ ...f, coperti: Number(e.target.value) }))}
                  required
                  className="bg-zinc-800 border-zinc-700 text-zinc-100 rounded-lg"
                />
              </div>
              <div>
                <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Note (opzionale)</Label>
                <Input
                  value={form.note}
                  onChange={e => setForm(f => ({ ...f, note: e.target.value }))}
                  placeholder="Es. allergie, richieste speciali…"
                  className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 rounded-lg"
                />
              </div>
              <div className="flex gap-3 mt-2">
                <Button type="button" variant="ghost" onClick={() => setShowForm(false)}
                  className="flex-1 border border-zinc-700 text-zinc-400 hover:text-zinc-200 rounded-lg">
                  Annulla
                </Button>
                <Button type="submit" disabled={saving}
                  className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg">
                  {saving && <Loader2 className="w-3.5 h-3.5 mr-2 animate-spin" />}
                  Conferma
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog.Root>

        {loading ? (
          <div className="flex items-center justify-center h-40 gap-2 text-zinc-500">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span className="text-sm">Caricamento…</span>
          </div>
        ) : (
          <>
            {/* Prossime prenotazioni */}
            <section className="mb-10">
              <h2 className="text-xs text-zinc-500 uppercase tracking-[0.2em] font-medium mb-4">
                Prossime prenotazioni
              </h2>
              {prossime.length === 0 ? (
                <div className="bg-zinc-900 border border-zinc-800 rounded-xl p-8 flex flex-col items-center gap-3 text-zinc-600">
                  <CalendarDays className="w-8 h-8 opacity-30" />
                  <p className="text-sm font-light">Nessuna prenotazione futura</p>
                  <button onClick={apriForm} className="text-xs text-indigo-400 hover:text-indigo-300 underline underline-offset-2 transition-colors">
                    Prenota ora
                  </button>
                </div>
              ) : (
                <div className="flex flex-col gap-3">
                  {prossime.map(p => (
                    <div key={p.id} className="bg-zinc-900 border border-zinc-800 rounded-xl p-5 flex items-center justify-between hover:border-zinc-700 transition-colors">
                      <div className="flex flex-col gap-1.5">
                        <p className="text-sm font-semibold text-zinc-100 capitalize">{formatData(p.data)}</p>
                        <div className="flex items-center gap-3 text-xs text-zinc-400">
                          <span className="flex items-center gap-1">
                            <Clock className="w-3 h-3" /> {formatOra(p.ora)}
                          </span>
                          <span className="flex items-center gap-1">
                            <Users className="w-3 h-3" /> {p.coperti} persone
                          </span>
                          <span className="text-zinc-600">Tavolo {p.numeroTavolo}</span>
                        </div>
                        {p.note && <p className="text-xs text-zinc-600 italic">{p.note}</p>}
                      </div>
                      <button
                        onClick={() => cancella(p.id)}
                        disabled={cancellando === p.id}
                        className="p-2 rounded-lg text-zinc-600 hover:text-red-400 hover:bg-red-950/30 transition-colors disabled:opacity-50"
                        title="Annulla prenotazione"
                      >
                        {cancellando === p.id
                          ? <Loader2 className="w-4 h-4 animate-spin" />
                          : <Trash2 className="w-4 h-4" />}
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* Storico */}
            {passate.length > 0 && (
              <section>
                <h2 className="text-xs text-zinc-500 uppercase tracking-[0.2em] font-medium mb-4">
                  Storico
                </h2>
                <div className="flex flex-col gap-2">
                  {passate.map(p => (
                    <div key={p.id} className="bg-zinc-900/50 border border-zinc-800/50 rounded-xl px-5 py-4 flex items-center justify-between opacity-50">
                      <div className="flex flex-col gap-1">
                        <p className="text-sm font-medium text-zinc-300 capitalize">{formatData(p.data)}</p>
                        <div className="flex items-center gap-3 text-xs text-zinc-500">
                          <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {formatOra(p.ora)}</span>
                          <span className="flex items-center gap-1"><Users className="w-3 h-3" /> {p.coperti}</span>
                          <span>Tavolo {p.numeroTavolo}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  );
}
