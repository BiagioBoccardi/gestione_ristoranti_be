import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '@/services/authService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { UtensilsCrossed, Loader2, Eye, EyeOff, ArrowLeft, CheckCircle2, AlertCircle } from 'lucide-react';

export default function ResetPasswordPage() {
  const [searchParams]          = useSearchParams();
  const navigate                = useNavigate();
  const token                   = searchParams.get('token') ?? '';

  const [password, setPassword] = useState('');
  const [confirm,  setConfirm]  = useState('');
  const [showPwd,  setShowPwd]  = useState(false);
  const [loading,  setLoading]  = useState(false);
  const [done,     setDone]     = useState(false);
  const [error,    setError]    = useState('');

  const mismatch = confirm.length > 0 && password !== confirm;

  const onSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (mismatch) return;
    setLoading(true);
    setError('');
    try {
      await authService.resetPassword(token, password);
      setDone(true);
      setTimeout(() => navigate('/login'), 3000);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setError(e.response?.data?.message ?? 'Token non valido o scaduto.');
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-zinc-950 px-6">
        <div className="w-full max-w-sm text-center flex flex-col items-center gap-4">
          <AlertCircle className="w-10 h-10 text-red-400" />
          <p className="text-zinc-300 text-sm">Link non valido o mancante.</p>
          <Link to="/login" className="text-xs text-indigo-400 hover:text-indigo-300 transition-colors flex items-center gap-1.5">
            <ArrowLeft className="w-3.5 h-3.5" /> Torna al login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-zinc-950 px-6">
      <div className="w-full max-w-sm">

        {/* Brand */}
        <div className="flex items-center gap-2.5 mb-10">
          <div className="w-8 h-8 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-900/50">
            <UtensilsCrossed className="w-3.5 h-3.5 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-[0.2em] uppercase text-zinc-100">Restora</span>
        </div>

        {done ? (
          <div className="flex flex-col gap-5">
            <div className="w-12 h-12 rounded-2xl bg-emerald-600/20 border border-emerald-600/30 flex items-center justify-center">
              <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h1 className="text-xl font-semibold text-zinc-100 mb-2">Password aggiornata</h1>
              <p className="text-sm text-zinc-400 font-light leading-relaxed">
                La tua password è stata reimpostata con successo.
                Verrai reindirizzato al login tra pochi secondi.
              </p>
            </div>
            <Link to="/login" className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 transition-colors">
              <ArrowLeft className="w-3.5 h-3.5" /> Vai al login
            </Link>
          </div>
        ) : (
          <>
            <div className="mb-8">
              <h1 className="text-2xl font-semibold text-zinc-100 tracking-tight mb-1.5">
                Nuova password
              </h1>
              <p className="text-sm text-zinc-500 font-light">
                Scegli una nuova password per il tuo account.
              </p>
            </div>

            <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="password" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                  Nuova password
                </Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPwd ? 'text' : 'password'}
                    autoComplete="new-password"
                    placeholder="••••••••"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    disabled={loading}
                    required
                    minLength={6}
                    className="h-11 pr-10 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPwd(v => !v)}
                    aria-label={showPwd ? 'Nascondi password' : 'Mostra password'}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors"
                  >
                    {showPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
                <p className="text-xs text-zinc-600">Minimo 6 caratteri</p>
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="confirm" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                  Conferma password
                </Label>
                <Input
                  id="confirm"
                  type={showPwd ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={confirm}
                  onChange={e => setConfirm(e.target.value)}
                  disabled={loading}
                  required
                  className={`h-11 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg ${mismatch ? 'border-red-700 focus-visible:ring-red-600' : ''}`}
                />
                {mismatch && (
                  <p className="text-xs text-red-400">Le password non coincidono</p>
                )}
              </div>

              {error && (
                <p className="text-sm text-red-400 bg-red-950/40 border border-red-800/50 rounded-lg px-3 py-2.5">
                  {error}
                </p>
              )}

              <Button
                type="submit"
                disabled={loading || !password || !confirm || mismatch}
                className="w-full bg-indigo-600 hover:bg-indigo-500 text-white tracking-widest uppercase text-xs font-semibold h-11 rounded-lg"
              >
                {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                {loading ? 'Salvataggio…' : 'Reimposta password'}
              </Button>

              <Link
                to="/login"
                className="flex items-center justify-center gap-1.5 text-xs text-zinc-500 hover:text-zinc-300 transition-colors"
              >
                <ArrowLeft className="w-3.5 h-3.5" /> Torna al login
              </Link>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
