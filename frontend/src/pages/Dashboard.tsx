import { useAuth } from '@/context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import {
  UtensilsCrossed, ClipboardList, ChefHat, BarChart3,
  Users, QrCode, LogOut, Zap, CalendarDays,
} from 'lucide-react';
import AppSidebar from '@/components/layout/AppSidebar';
import { Role } from '@/types/auth';

const features = [
  { icon: ClipboardList,   title: 'Gestione Ordini',  description: 'Prendi e gestisci gli ordini al tavolo in tempo reale. Stato aggiornato istantaneamente per sala e cucina.' },
  { icon: UtensilsCrossed, title: 'Menu Digitale',    description: 'Crea e aggiorna il menu con categorie, descrizioni, prezzi e foto. Modifiche visibili subito ai clienti.' },
  { icon: ChefHat,         title: 'Vista Cucina',     description: 'Schermata dedicata ai cuochi con gli ordini in arrivo, stato di preparazione e segnalazione esaurimenti.' },
  { icon: QrCode,          title: 'Ordine da QR Code', description: 'I clienti scansionano il QR del tavolo e ordinano direttamente dal loro smartphone.' },
  { icon: BarChart3,       title: 'Analytics',        description: 'Dashboard con revenue, piatti più venduti e statistiche giornaliere per prendere decisioni informate.' },
  { icon: Users,           title: 'Gestione Staff',   description: 'Crea account per camerieri, cuochi e admin. Ogni ruolo vede solo ciò che gli serve.' },
];

const roles = [
  { label: 'Admin',     color: 'bg-indigo-600 text-white',   desc: 'Controllo completo su menu, staff e analytics.' },
  { label: 'Cameriere', color: 'bg-amber-600 text-white',    desc: 'Ordini, prenotazioni e generazione conto.' },
  { label: 'Cuoco',     color: 'bg-orange-600 text-white',   desc: 'Vista ordini live e aggiornamento stato piatti.' },
  { label: 'Cliente',   color: 'bg-zinc-700 text-zinc-200',  desc: 'Menu digitale e ordine da QR code al tavolo.' },
];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isCliente = user?.role === Role.CLIENTE;

  return (
    <div className="min-h-screen bg-zinc-950 font-sans">

      {/* Navbar */}
      <header className="sticky top-0 z-30 bg-zinc-950/90 backdrop-blur-md border-b border-zinc-800">
        <div className="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
          <AppSidebar />
          <div className="flex items-center gap-3">
            <span className="text-xs text-zinc-500 hidden sm:block font-light">{user?.sub}</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={logout}
              className="text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800 gap-1.5 text-xs rounded-lg"
            >
              <LogOut className="w-3.5 h-3.5" />
              Esci
            </Button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="relative overflow-hidden bg-zinc-950 border-b border-zinc-800">
        <div
          className="absolute inset-0 opacity-[0.04]"
          style={{
            backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
            backgroundSize: '28px 28px',
          }}
        />
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[300px] bg-indigo-600/10 blur-3xl rounded-full" />

        <div className="relative max-w-6xl mx-auto px-6 pt-24 pb-20 text-center">
          <div className="inline-flex items-center gap-2 bg-zinc-900 border border-zinc-800 text-zinc-400 text-xs font-medium px-3.5 py-1.5 rounded-full mb-8 tracking-wide uppercase">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            Sistema attivo
          </div>

          <h1 className="text-5xl sm:text-6xl font-light text-zinc-100 leading-[1.1] tracking-tight mb-6">
            {isCliente ? (
              <>Benvenuto da<br /><span className="text-indigo-400 font-extralight">Restora.</span></>
            ) : (
              <>Gestisci il tuo ristorante<br /><span className="text-zinc-600 font-extralight">in un unico posto.</span></>
            )}
          </h1>

          <p className="text-lg text-zinc-500 font-light max-w-xl mx-auto leading-relaxed">
            {isCliente
              ? 'Sfoglia il menu, prenota un tavolo o ordina direttamente dal tuo smartphone.'
              : 'Restora centralizza ordini, menu, cucina e analytics in una piattaforma pensata per ogni ruolo del tuo team.'}
          </p>

          {isCliente && (
            <div className="flex items-center justify-center gap-4 mt-10">
              <Button
                onClick={() => navigate('/prenotazione-online')}
                className="bg-indigo-600 hover:bg-indigo-500 text-white px-6 h-11 rounded-xl gap-2 shadow-lg shadow-indigo-900/40 text-sm font-medium"
              >
                <CalendarDays className="w-4 h-4" />
                Prenota un tavolo
              </Button>
              <Button
                variant="ghost"
                onClick={() => navigate('/menu')}
                className="text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800 px-6 h-11 rounded-xl text-sm border border-zinc-800"
              >
                Sfoglia il menu
              </Button>
            </div>
          )}
        </div>
      </section>

      {/* Features */}
      <section className="max-w-6xl mx-auto px-6 py-20">
        <div className="text-center mb-12">
          <div className="inline-flex items-center gap-2 text-indigo-500 mb-3">
            <Zap className="w-3.5 h-3.5" />
            <span className="text-xs tracking-[0.2em] uppercase font-medium">Funzionalità</span>
          </div>
          <p className="text-2xl font-light text-zinc-100 tracking-tight">Tutto ciò di cui hai bisogno</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {features.map(({ icon: Icon, title, description }) => (
            <div
              key={title}
              className="group bg-zinc-900 rounded-xl border border-zinc-800 p-6 flex flex-col gap-4 hover:border-indigo-800/60 hover:shadow-lg hover:shadow-indigo-950/50 transition-all duration-200"
            >
              <div className="w-10 h-10 rounded-xl bg-zinc-800 group-hover:bg-indigo-600/20 flex items-center justify-center transition-colors duration-200 border border-zinc-700 group-hover:border-indigo-700/50">
                <Icon className="w-5 h-5 text-zinc-400 group-hover:text-indigo-400 transition-colors duration-200" />
              </div>
              <div>
                <h3 className="text-sm font-semibold text-zinc-100 mb-1.5">{title}</h3>
                <p className="text-sm text-zinc-500 font-light leading-relaxed">{description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Ruoli */}
      <section className="border-y border-zinc-800 bg-zinc-900/50 py-20">
        <div className="max-w-6xl mx-auto px-6">
          <div className="text-center mb-12">
            <div className="inline-flex items-center gap-2 text-indigo-500 mb-3">
              <Users className="w-3.5 h-3.5" />
              <span className="text-xs tracking-[0.2em] uppercase font-medium">Team</span>
            </div>
            <p className="text-2xl font-light text-zinc-100 tracking-tight">Un ruolo per ogni membro</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {roles.map(({ label, color, desc }) => (
              <div key={label} className="rounded-xl border border-zinc-800 p-5 flex flex-col gap-3 bg-zinc-900 hover:border-zinc-700 transition-colors">
                <span className={`self-start text-xs font-semibold tracking-widest uppercase px-3 py-1.5 rounded-lg ${color}`}>
                  {label}
                </span>
                <p className="text-sm text-zinc-500 font-light leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="max-w-6xl mx-auto px-6 py-8 flex items-center justify-between">
        <span className="text-xs text-zinc-700 font-light">
          © {new Date().getFullYear()} Restora — Sistema di gestione ristorante
        </span>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-md bg-indigo-600 flex items-center justify-center">
            <UtensilsCrossed className="w-3 h-3 text-white" />
          </div>
          <span className="text-xs text-zinc-500 tracking-widest uppercase font-medium">Restora</span>
        </div>
      </footer>

    </div>
  );
}
