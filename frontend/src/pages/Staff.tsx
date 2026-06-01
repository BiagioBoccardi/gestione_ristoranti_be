import { useEffect, useState } from 'react';
import { Clock, Pencil, Plus, Trash2, UserPlus, Users } from 'lucide-react';
import { Button } from '@/components/ui/button';
import AppSidebar from '@/components/layout/AppSidebar';
import { useToast } from '@/hooks/use-toast';
import { SkeletonRow } from '@/components/ui/skeleton';
import { staffService } from '@/services/staffService';
import type { StaffMembro, TurnoItem } from '@/types/staff';
import UtenteForm from '@/components/staff/UtenteForm';
import TurnoForm from '@/components/staff/TurnoForm';

type Tab = 'staff' | 'turni';

const RUOLO_BADGE: Record<string, string> = {
  ADMIN:     'bg-indigo-600/20 text-indigo-300 border-indigo-600/30',
  CAMERIERE: 'bg-emerald-600/20 text-emerald-300 border-emerald-600/30',
  CUOCO:     'bg-amber-600/20 text-amber-300 border-amber-600/30',
  CLIENTE:   'bg-zinc-700 text-zinc-400 border-zinc-600',
};

const STATO_BADGE: Record<string, string> = {
  PIANIFICATO: 'bg-zinc-700 text-zinc-300 border-zinc-600',
  IN_CORSO:    'bg-blue-600/20 text-blue-300 border-blue-600/30',
  COMPLETATO:  'bg-emerald-600/20 text-emerald-300 border-emerald-600/30',
};

function fmtDatetime(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function StaffPage() {
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState<Tab>('staff');
  const [staff, setStaff] = useState<StaffMembro[]>([]);
  const [turni, setTurni] = useState<TurnoItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [utenteFormOpen, setUtenteFormOpen] = useState(false);
  const [turnoFormOpen, setTurnoFormOpen] = useState(false);
  const [selectedUtente, setSelectedUtente] = useState<StaffMembro | null>(null);
  const [selectedTurno, setSelectedTurno] = useState<TurnoItem | null>(null);

  useEffect(() => { loadAll(); }, []);

  async function loadAll() {
    setLoading(true);
    setError(null);
    try {
      const [staffData, turniData] = await Promise.all([
        staffService.getStaff(),
        staffService.getTurni(),
      ]);
      setStaff(staffData);
      setTurni(turniData);
    } catch {
      setError('Errore nel caricamento dei dati. Riprova.');
    } finally {
      setLoading(false);
    }
  }

  function openNuovoUtente() {
    setSelectedUtente(null);
    setUtenteFormOpen(true);
  }

  function openModificaUtente(u: StaffMembro) {
    setSelectedUtente(u);
    setUtenteFormOpen(true);
  }

  async function eliminaUtente(id: number) {
    if (!window.confirm('Eliminare questo membro dello staff?')) return;
    try {
      await staffService.eliminaStaff(id);
      setStaff(prev => prev.filter(u => u.id !== id));
      setTurni(prev => prev.filter(t => t.utenteId !== id));
      toast({ title: 'Membro eliminato', description: 'Il membro dello staff è stato rimosso.' });
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      alert(err?.response?.data?.message ?? "Errore durante l'eliminazione");
    }
  }

  function openNuovoTurno() {
    setSelectedTurno(null);
    setTurnoFormOpen(true);
  }

  function openModificaTurno(t: TurnoItem) {
    setSelectedTurno(t);
    setTurnoFormOpen(true);
  }

  async function eliminaTurno(id: number) {
    if (!window.confirm('Eliminare questo turno?')) return;
    try {
      await staffService.eliminaTurno(id);
      setTurni(prev => prev.filter(t => t.id !== id));
      toast({ title: 'Turno eliminato', description: 'Il turno è stato rimosso.' });
    } catch {
      alert('Errore durante l\'eliminazione del turno');
    }
  }

  return (
    <div className="min-h-screen bg-zinc-950 font-sans">

      {/* Header */}
      <header className="sticky top-0 z-40 bg-zinc-950/90 backdrop-blur-md border-b border-zinc-800">
        <div className="max-w-6xl mx-auto px-6 h-14 flex items-center gap-4">
          <AppSidebar compact />
          <div className="flex items-center gap-2 flex-1">
            <div className="w-7 h-7 rounded-lg bg-indigo-600/20 border border-indigo-600/30 flex items-center justify-center">
              <Users className="w-3.5 h-3.5 text-indigo-400" />
            </div>
            <span className="text-sm font-semibold text-zinc-100 tracking-wide">Gestione Staff</span>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-8">

        {error && (
          <div className="mb-6 text-sm text-red-400 bg-red-950/40 border border-red-800/50 rounded-xl px-4 py-3">
            {error}
          </div>
        )}

        {/* Tabs */}
        <div className="flex items-center gap-1 mb-6 bg-zinc-900 border border-zinc-800 rounded-xl p-1 w-fit">
          <button
            onClick={() => setActiveTab('staff')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold tracking-wider uppercase transition-all ${
              activeTab === 'staff'
                ? 'bg-zinc-800 text-zinc-100 shadow-sm'
                : 'text-zinc-500 hover:text-zinc-300'
            }`}
          >
            <Users className="w-3.5 h-3.5" />
            Membri Staff
            <span className="ml-1 px-1.5 py-0.5 rounded-md bg-zinc-700 text-zinc-300 text-[10px]">
              {staff.length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('turni')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold tracking-wider uppercase transition-all ${
              activeTab === 'turni'
                ? 'bg-zinc-800 text-zinc-100 shadow-sm'
                : 'text-zinc-500 hover:text-zinc-300'
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            Turni
            <span className="ml-1 px-1.5 py-0.5 rounded-md bg-zinc-700 text-zinc-300 text-[10px]">
              {turni.length}
            </span>
          </button>
        </div>

        {/* ── Tab Membri Staff ───────────────────────────────────────────────── */}
        {activeTab === 'staff' && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-zinc-500">Gestisci i membri del team e i loro ruoli.</p>
              <Button
                onClick={openNuovoUtente}
                className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs tracking-wider uppercase h-9 px-4 gap-2 rounded-lg shadow-sm shadow-indigo-900/30"
              >
                <UserPlus className="w-3.5 h-3.5" />
                Nuovo membro
              </Button>
            </div>

            <div className="rounded-xl border border-zinc-800 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-zinc-800 bg-zinc-900/60">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Nome</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Email</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Ruolo</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Turni</th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Azioni</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800">
                  {loading ? (
                    Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={5} />)
                  ) : staff.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-zinc-600 text-sm">
                        Nessun membro staff. Crea il primo membro.
                      </td>
                    </tr>
                  ) : staff.map(u => (
                    <tr key={u.id} className="hover:bg-zinc-900/40 transition-colors">
                      <td className="px-5 py-4 text-zinc-100 font-medium">{u.nome}</td>
                      <td className="px-5 py-4 text-zinc-400">{u.email}</td>
                      <td className="px-5 py-4">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold border tracking-wider ${RUOLO_BADGE[u.ruolo] ?? 'bg-zinc-700 text-zinc-400 border-zinc-600'}`}>
                          {u.ruolo}
                        </span>
                      </td>
                      <td className="px-5 py-4 text-zinc-400">{u.nrTurni}</td>
                      <td className="px-5 py-4">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => openModificaUtente(u)}
                            className="text-zinc-500 hover:text-zinc-100 hover:bg-zinc-800 h-8 w-8 p-0 rounded-lg"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => eliminaUtente(u.id)}
                            className="text-zinc-600 hover:text-red-400 hover:bg-red-950/30 h-8 w-8 p-0 rounded-lg"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── Tab Turni ─────────────────────────────────────────────────────── */}
        {activeTab === 'turni' && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-zinc-500">Pianifica e gestisci i turni del personale.</p>
              <Button
                onClick={openNuovoTurno}
                className="bg-indigo-600 hover:bg-indigo-500 text-white text-xs tracking-wider uppercase h-9 px-4 gap-2 rounded-lg shadow-sm shadow-indigo-900/30"
              >
                <Plus className="w-3.5 h-3.5" />
                Nuovo turno
              </Button>
            </div>

            <div className="rounded-xl border border-zinc-800 overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-zinc-800 bg-zinc-900/60">
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Membro</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Inizio</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Fine</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Stato</th>
                    <th className="text-left px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Note</th>
                    <th className="text-right px-5 py-3.5 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Azioni</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800">
                  {loading ? (
                    Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={6} />)
                  ) : turni.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-5 py-12 text-center text-zinc-600 text-sm">
                        Nessun turno registrato. Crea il primo turno.
                      </td>
                    </tr>
                  ) : turni.map(t => (
                    <tr key={t.id} className="hover:bg-zinc-900/40 transition-colors">
                      <td className="px-5 py-4 text-zinc-100 font-medium">{t.utenteNome}</td>
                      <td className="px-5 py-4 text-zinc-400">{fmtDatetime(t.dataInizio)}</td>
                      <td className="px-5 py-4 text-zinc-400">{fmtDatetime(t.dataFine)}</td>
                      <td className="px-5 py-4">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold border tracking-wider ${STATO_BADGE[t.stato] ?? 'bg-zinc-700 text-zinc-400 border-zinc-600'}`}>
                          {t.stato.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="px-5 py-4 text-zinc-500 max-w-[180px] truncate">{t.note ?? '—'}</td>
                      <td className="px-5 py-4">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => openModificaTurno(t)}
                            className="text-zinc-500 hover:text-zinc-100 hover:bg-zinc-800 h-8 w-8 p-0 rounded-lg"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => eliminaTurno(t.id)}
                            className="text-zinc-600 hover:text-red-400 hover:bg-red-950/30 h-8 w-8 p-0 rounded-lg"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      <UtenteForm
        open={utenteFormOpen}
        onClose={() => setUtenteFormOpen(false)}
        onSaved={() => { loadAll(); toast({ title: selectedUtente ? 'Membro aggiornato' : 'Membro creato', description: selectedUtente ? 'Le modifiche sono state salvate.' : 'Il nuovo membro è stato aggiunto.' }); }}
        utente={selectedUtente}
      />
      <TurnoForm
        open={turnoFormOpen}
        onClose={() => setTurnoFormOpen(false)}
        onSaved={() => { loadAll(); toast({ title: selectedTurno ? 'Turno aggiornato' : 'Turno creato', description: selectedTurno ? 'Le modifiche sono state salvate.' : 'Il nuovo turno è stato aggiunto.' }); }}
        staff={staff}
        turno={selectedTurno}
      />
    </div>
  );
}
