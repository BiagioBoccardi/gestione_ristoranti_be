import { useState } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Role } from '@/types/auth';
import {
  UtensilsCrossed, ClipboardList, ChefHat, BarChart3,
  Users, CalendarDays, FlaskConical, LogOut, X, Menu as MenuIcon,
  ShieldCheck, LayoutDashboard,
} from 'lucide-react';

interface NavItem {
  icon: React.ElementType;
  label: string;
  path: string;
  roles: string[] | null;
}

const navItems: NavItem[] = [
  { icon: LayoutDashboard,  label: 'Dashboard',         path: '/dashboard',           roles: null },
  { icon: UtensilsCrossed, label: 'Menu',              path: '/menu',                roles: null },
  { icon: ClipboardList,   label: 'Ordini',            path: '/ordini',              roles: [Role.ADMIN, Role.CAMERIERE] },
  { icon: ChefHat,         label: 'Cucina',            path: '/cucina',              roles: [Role.ADMIN, Role.CUOCO] },
  { icon: CalendarDays,    label: 'Prenotazioni',      path: '/prenotazioni',        roles: [Role.ADMIN, Role.CAMERIERE] },
  { icon: CalendarDays,    label: 'Prenota',           path: '/prenotazione-online', roles: [Role.CLIENTE] },
  { icon: BarChart3,       label: 'Analytics',         path: '/analytics',           roles: [Role.ADMIN] },
  { icon: FlaskConical,    label: 'Ricette',           path: '/ricette',             roles: [Role.ADMIN] },
  { icon: Users,           label: 'Staff',             path: '/staff',               roles: [Role.ADMIN] },
  { icon: ShieldCheck,     label: 'Pannello Admin',    path: '/admin',               roles: [Role.ADMIN] },
];

interface Props {
  variant?: 'dark' | 'light';
  compact?: boolean;
}

export default function AppSidebar({ variant = 'dark', compact = false }: Props) {
  const [open, setOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const visibleItems = navItems.filter(item =>
    item.roles === null || (user != null && item.roles.includes(user.role))
  );

  const location = useLocation();
  const close = () => setOpen(false);

  const iconClass  = variant === 'dark' ? 'text-zinc-600 group-hover:text-zinc-400' : 'text-stone-400 group-hover:text-stone-600';

  return (
    <>
      {/* Trigger */}
      {compact ? (
        <button
          onClick={() => setOpen(true)}
          className={`p-1.5 rounded-lg transition-colors group hover:bg-black/10 ${iconClass}`}
          aria-label="Apri navigazione"
        >
          <MenuIcon className="w-4 h-4" />
        </button>
      ) : (
        <button
          onClick={() => setOpen(true)}
          className="flex items-center gap-2 group"
          aria-label="Apri navigazione"
        >
          <div className="w-7 h-7 rounded-lg bg-indigo-600 flex items-center justify-center shadow-md shadow-indigo-900/40 group-hover:bg-indigo-500 transition-colors">
            <UtensilsCrossed className="w-3.5 h-3.5 text-white" />
          </div>
          <MenuIcon className={`w-3.5 h-3.5 ml-0.5 transition-colors ${iconClass}`} />
        </button>
      )}

      {createPortal(
        <>
          {/* Overlay */}
          {open && (
            <div
              className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm"
              onClick={close}
            />
          )}

          {/* Sidebar panel */}
          <aside
            style={{ gridTemplateRows: '3.5rem auto 1fr auto' }}
            className={`fixed inset-y-0 left-0 z-50 w-64 bg-zinc-900 border-r border-zinc-800 grid transition-transform duration-300 ease-in-out ${
              open ? 'translate-x-0' : '-translate-x-full'
            }`}
          >
            {/* Header */}
            <div className="flex items-center justify-between px-5 border-b border-zinc-800">
              <span className="text-sm font-semibold tracking-[0.15em] uppercase text-zinc-100">Navigazione</span>
              <button
                onClick={close}
                className="text-zinc-500 hover:text-zinc-200 transition-colors p-1 rounded-lg hover:bg-zinc-800"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* User info */}
            <div className="px-5 py-4 border-b border-zinc-800">
              <p className="text-xs text-zinc-500 truncate">{user?.sub}</p>
              <span className="mt-1 inline-flex items-center px-2 py-0.5 rounded-md bg-indigo-600/20 text-indigo-300 text-[10px] font-semibold tracking-wider uppercase border border-indigo-600/30">
                {user?.role?.replace('ROLE_', '')}
              </span>
            </div>

            {/* Nav */}
            <nav className="overflow-y-auto py-3 px-3 flex flex-col gap-0.5">
              {visibleItems.map(item => {
                const isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/');
                return (
                  <button
                    key={item.path}
                    type="button"
                    onClick={() => { navigate(item.path); close(); }}
                    className={`flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm transition-all text-left ${
                      isActive
                        ? 'bg-zinc-800 text-white'
                        : 'text-zinc-300 hover:text-white hover:bg-zinc-800'
                    }`}
                  >
                    <item.icon className={`w-4 h-4 flex-shrink-0 ${isActive ? 'text-indigo-400' : 'text-zinc-500'}`} />
                    {item.label}
                  </button>
                );
              })}
            </nav>

            {/* Logout */}
            <div className="px-3 py-4 border-t border-zinc-800">
              <button
                onClick={() => { logout(); close(); }}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-zinc-300 hover:text-red-400 hover:bg-red-950/30 transition-all"
              >
                <LogOut className="w-4 h-4 flex-shrink-0" />
                Esci
              </button>
            </div>
          </aside>
        </>,
        document.body
      )}
    </>
  );
}
