import { createContext, useContext, useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { CheckCircle, XCircle, Info, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
  id: string;
  type: ToastType;
  message: string;
}

interface ToastContextType {
  showToast: (type: ToastType, message: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

const STYLES: Record<ToastType, string> = {
  success: 'bg-emerald-950/95 border-emerald-700/60 text-emerald-100',
  error:   'bg-red-950/95 border-red-700/60 text-red-100',
  info:    'bg-zinc-800/95 border-zinc-600/60 text-zinc-100',
};

const ICONS = {
  success: CheckCircle,
  error:   XCircle,
  info:    Info,
};

const ICON_COLORS: Record<ToastType, string> = {
  success: 'text-emerald-400',
  error:   'text-red-400',
  info:    'text-zinc-400',
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const showToast = useCallback((type: ToastType, message: string) => {
    const id = crypto.randomUUID();
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4500);
  }, []);

  const dismiss = (id: string) => setToasts(prev => prev.filter(t => t.id !== id));

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-5 right-5 z-[200] flex flex-col gap-2 w-80 pointer-events-none">
        {toasts.map(t => {
          const Icon = ICONS[t.type];
          return (
            <div
              key={t.id}
              className={`flex items-start gap-3 px-4 py-3.5 rounded-xl border backdrop-blur-sm shadow-2xl pointer-events-auto ${STYLES[t.type]}`}
            >
              <Icon className={`w-4 h-4 mt-0.5 shrink-0 ${ICON_COLORS[t.type]}`} />
              <p className="text-sm font-light leading-relaxed flex-1">{t.message}</p>
              <button
                onClick={() => dismiss(t.id)}
                className="opacity-40 hover:opacity-100 transition-opacity shrink-0 mt-0.5"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast deve essere usato dentro ToastProvider');
  const { showToast } = ctx;
  return {
    success: (msg: string) => showToast('success', msg),
    error:   (msg: string) => showToast('error', msg),
    info:    (msg: string) => showToast('info', msg),
  };
}
