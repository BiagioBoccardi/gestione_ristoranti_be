import { useEffect, useState } from 'react';
import AppSidebar from '@/components/layout/AppSidebar';
import { adminService } from '@/services/adminService';
import { staffService } from '@/services/staffService';
import type { UtenteAdmin } from '@/services/adminService';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/context/AuthContext';
import {
  Users, Trash2, RefreshCw, Plus, X, Loader2, LogOut, ShieldCheck,
} from 'lucide-react';

const RUOLI = ['ADMIN', 'CAMERIERE', 'CUOCO', 'CLIENTE'];

const BADGE: Record<string, string> = {
  ADMIN:     'bg-indigo-600/20 text-indigo-300 border-indigo-600/30',
  CAMERIERE: 'bg-amber-600/20 text-amber-300 border-amber-600/30',
  CUOCO:     'bg-orange-600/20 text-orange-300 border-orange-600/30',
  CLIENTE:   'bg-zinc-700/40 text-zinc-300 border-zinc-600/30',
};

interface CreaForm {
  nome: string;
  email: string;
  password: string;
  ruolo: string;
}

export default function AdminPage() {
  const { user, logout } = useAuth();
  const toast = useToast();

  const [utenti, setUtenti] = useState<UtenteAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState<number | null>(null);
  const [deleting, setDeleting] = useState<number | null>(null);
  const [showCrea, setShowCrea] = useState(false);
  const [creaLoading, setCreaLoading] = useState(false);
  const [form, setForm] = useState<CreaForm>({ nome: '', email: '', password: '', ruolo: 'CAMERIERE' });

  const carica = async () => {
    setLoading(true);
    try {
      setUtenti(await adminService.getUtenti());
    } catch {
      toast.error('Impossibile caricare gli utenti.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { carica(); }, []);

  const cambiaRuolo = async (id: number, nuovoRuolo: string) => {
    setUpdating(id);
    try {
      const aggiornato = await adminService.aggiornaRuolo(id, nuovoRuolo);
      setUtenti(prev => prev.map(u => u.id === id ? aggiornato : u));
      toast.success('Ruolo aggiornato.');
    } catch {
      toast.error('Errore aggiornamento ruolo.');
    } finally {
      setUpdating(null);
    }
  };

  const elimina = async (id: number) => {
    if (!confirm('Eliminare questo utente?')) return;
    setDeleting(id);
    try {
      await adminService.eliminaUtente(id);
      setUtenti(prev => prev.filter(u => u.id !== id));
      toast.success('Utente eliminato.');
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      toast.error(e.response?.data?.message ?? 'Errore eliminazione utente.');
    } finally {
      setDeleting(null);
    }
  };

  const creaUtente = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    setCreaLoading(true);
    try {
      await staffService.creaUtente({
        nome: form.nome,
        email: form.email,
        password: form.password,
        ruolo: form.ruolo,
      });
      toast.success(`Utente ${form.email} creato.`);
      setShowCrea(false);
      setForm({ nome: '', email: '', password: '', ruolo: 'CAMERIERE' });
      await carica();
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      toast.error(e.response?.data?.message ?? 'Errore creazione utente.');
    } finally {
      setCreaLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-zinc-950 font-sans">
      {/* Navbar */}
      <header className="sticky top-0 z-30 bg-zinc-950/90 backdrop-blur-md border-b border-zinc-800">
        <div className="max-w-5xl mx-auto px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <AppSidebar />
            <span className="text-xs text-zinc-600 hidden sm:block">/ Pannello Admin</span>
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

      <main className="max-w-5xl mx-auto px-6 py-10">
        {/* Title row */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-600/20 flex items-center justify-center border border-indigo-600/30">
              <ShieldCheck className="w-4.5 h-4.5 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-lg font-semibold text-zinc-100">Gestione Utenti</h1>
              <p className="text-xs text-zinc-500">{utenti.length} utenti registrati</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm" onClick={carica} disabled={loading}
              className="text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800 rounded-lg">
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </Button>
            <Button size="sm" onClick={() => setShowCrea(true)}
              className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs rounded-lg gap-1.5">
              <Plus className="w-3.5 h-3.5" /> Nuovo Utente
            </Button>
          </div>
        </div>

        {/* Modal crea utente */}
        {showCrea && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
            <div className="bg-zinc-900 border border-zinc-700 rounded-2xl p-7 w-full max-w-md shadow-2xl">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-base font-semibold text-zinc-100">Nuovo Utente</h2>
                <button onClick={() => setShowCrea(false)} className="text-zinc-500 hover:text-zinc-200 transition-colors">
                  <X className="w-4 h-4" />
                </button>
              </div>
              <form onSubmit={creaUtente} className="flex flex-col gap-4">
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Nome</Label>
                  <Input value={form.nome} onChange={e => setForm(f => ({ ...f, nome: e.target.value }))}
                    placeholder="Mario Rossi" required
                    className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 rounded-lg" />
                </div>
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Email</Label>
                  <Input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                    placeholder="mario@restora.it" required
                    className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 rounded-lg" />
                </div>
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Password</Label>
                  <Input type="password" value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                    placeholder="••••••••" required minLength={6}
                    className="bg-zinc-800 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 rounded-lg" />
                </div>
                <div>
                  <Label className="text-xs text-zinc-400 uppercase tracking-widest mb-1.5 block">Ruolo</Label>
                  <select value={form.ruolo} onChange={e => setForm(f => ({ ...f, ruolo: e.target.value }))}
                    className="w-full h-10 px-3 rounded-lg bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500">
                    {RUOLI.map(r => <option key={r} value={r}>{r}</option>)}
                  </select>
                </div>
                <div className="flex gap-3 mt-2">
                  <Button type="button" variant="ghost" onClick={() => setShowCrea(false)}
                    className="flex-1 border border-zinc-700 text-zinc-400 hover:text-zinc-200 rounded-lg">
                    Annulla
                  </Button>
                  <Button type="submit" disabled={creaLoading}
                    className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg">
                    {creaLoading && <Loader2 className="w-3.5 h-3.5 mr-2 animate-spin" />}
                    Crea
                  </Button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Tabella utenti */}
        <div className="bg-zinc-900 border border-zinc-800 rounded-xl overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center h-40 gap-2 text-zinc-500">
              <Loader2 className="w-4 h-4 animate-spin" />
              <span className="text-sm">Caricamento…</span>
            </div>
          ) : utenti.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-40 gap-2 text-zinc-600">
              <Users className="w-6 h-6" />
              <span className="text-sm">Nessun utente trovato</span>
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-zinc-800 text-zinc-500 text-xs uppercase tracking-widest">
                  <th className="px-5 py-3.5 text-left font-medium">Nome</th>
                  <th className="px-5 py-3.5 text-left font-medium">Email</th>
                  <th className="px-5 py-3.5 text-left font-medium">Ruolo</th>
                  <th className="px-5 py-3.5 text-right font-medium">Azioni</th>
                </tr>
              </thead>
              <tbody>
                {utenti.map((u, i) => (
                  <tr key={u.id}
                    className={`border-b border-zinc-800/50 hover:bg-zinc-800/30 transition-colors ${i === utenti.length - 1 ? 'border-b-0' : ''}`}>
                    <td className="px-5 py-4 text-zinc-100 font-medium">{u.nome}</td>
                    <td className="px-5 py-4 text-zinc-400 font-light">{u.email}</td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-semibold tracking-wider uppercase border ${BADGE[u.ruolo] ?? 'bg-zinc-700 text-zinc-300 border-zinc-600'}`}>
                          {u.ruolo}
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center justify-end gap-2">
                        <select
                          value={u.ruolo}
                          disabled={updating === u.id}
                          onChange={e => cambiaRuolo(u.id, e.target.value)}
                          className="h-8 px-2 rounded-lg bg-zinc-800 border border-zinc-700 text-zinc-300 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 disabled:opacity-50"
                        >
                          {RUOLI.map(r => <option key={r} value={r}>{r}</option>)}
                        </select>
                        {updating === u.id && <Loader2 className="w-3.5 h-3.5 text-indigo-400 animate-spin" />}
                        <button
                          onClick={() => elimina(u.id)}
                          disabled={deleting === u.id || u.email === user?.sub}
                          className="p-1.5 rounded-lg text-zinc-600 hover:text-red-400 hover:bg-red-950/30 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                          title={u.email === user?.sub ? 'Non puoi eliminare te stesso' : 'Elimina utente'}
                        >
                          {deleting === u.id
                            ? <Loader2 className="w-3.5 h-3.5 animate-spin" />
                            : <Trash2 className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <p className="text-xs text-zinc-700 mt-6 text-center">
          Puoi modificare il ruolo direttamente dal menu a tendina — la modifica è immediata.
        </p>
      </main>
    </div>
  );
}
