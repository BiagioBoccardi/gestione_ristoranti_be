import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLogin } from '@/hooks/useLogin';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Eye, EyeOff, UtensilsCrossed, AlertCircle, Loader2 } from 'lucide-react';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function LoginPage() {
  const [email,         setEmail]         = useState('');
  const [password,      setPassword]      = useState('');
  const [showPwd,       setShowPwd]       = useState(false);
  const [emailError,    setEmailError]    = useState('');
  const [passwordError, setPasswordError] = useState('');
  const { handleLogin, loading, error } = useLogin();

  const onSubmit = (e: { preventDefault(): void }) => {
    e.preventDefault();
    const eErr = !email
      ? "L'email è obbligatoria"
      : !EMAIL_RE.test(email)
        ? 'Formato email non valido'
        : '';
    const pErr = !password ? 'La password è obbligatoria' : '';
    setEmailError(eErr);
    setPasswordError(pErr);
    if (!eErr && !pErr) handleLogin(email, password);
  };

  return (
    <div className="min-h-screen flex bg-zinc-950">

      {/* Left panel */}
      <div className="hidden lg:flex flex-col justify-between w-[42%] bg-gradient-to-br from-indigo-950 to-zinc-900 p-12 relative overflow-hidden border-r border-zinc-800">
        <div
          className="absolute inset-0 opacity-[0.06]"
          style={{
            backgroundImage: 'radial-gradient(circle, #ffffff 1px, transparent 1px)',
            backgroundSize: '28px 28px',
          }}
        />
        <div className="absolute -bottom-40 -left-40 w-[500px] h-[500px] rounded-full bg-indigo-600/20 blur-3xl" />

        <div className="relative flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-900/50">
            <UtensilsCrossed className="w-4 h-4 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-[0.2em] uppercase text-zinc-100">Restora</span>
        </div>

        <div className="relative">
          <p className="text-3xl font-light text-zinc-100 leading-snug mb-4">
            Gestisci il tuo<br />ristorante con<br />
            <span className="text-indigo-400">semplicità.</span>
          </p>
          <p className="text-sm text-zinc-500 font-light leading-relaxed max-w-xs">
            Ordini, menu, cucina e analytics in un'unica piattaforma pensata per il tuo team.
          </p>
        </div>

        <div className="relative flex gap-4">
          {['Admin', 'Cameriere', 'Cuoco', 'Cliente'].map(r => (
            <span key={r} className="text-xs text-zinc-600 tracking-wide font-medium">{r}</span>
          ))}
        </div>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex flex-col items-center justify-center px-8 py-12 relative">
        <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-600/5 rounded-full blur-3xl" />

        {/* Mobile brand */}
        <div className="lg:hidden flex flex-col items-center gap-2 mb-10">
          <div className="w-11 h-11 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-900/40">
            <UtensilsCrossed className="w-5 h-5 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-[0.2em] uppercase text-zinc-100">Restora</span>
        </div>

        <div className="w-full max-w-sm relative">
          <div className="mb-8">
            <h1 className="text-2xl font-semibold text-zinc-100 tracking-tight mb-1.5">Accedi</h1>
            <p className="text-sm text-zinc-500 font-light">Inserisci le tue credenziali per continuare</p>
          </div>

          <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                Email
              </Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="nome@ristorante.it"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setEmailError(''); }}
                disabled={loading}
                required
                className={`h-11 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg ${emailError ? 'border-red-600' : ''}`}
              />
              {emailError && <p className="text-xs text-red-400 mt-0.5">{emailError}</p>}
            </div>

            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <Label htmlFor="password" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                  Password
                </Label>
                <Link
                  to="/forgot-password"
                  className="text-xs text-zinc-500 hover:text-indigo-400 transition-colors"
                >
                  Hai dimenticato la password?
                </Link>
              </div>
              <div className="relative">
                <Input
                  id="password"
                  type={showPwd ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => { setPassword(e.target.value); setPasswordError(''); }}
                  disabled={loading}
                  required
                  className={`h-11 pr-10 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg ${passwordError ? 'border-red-600' : ''}`}
                />
                <button
                  type="button"
                  onClick={() => setShowPwd((v) => !v)}
                  aria-label={showPwd ? 'Nascondi password' : 'Mostra password'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors"
                >
                  {showPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {passwordError && <p className="text-xs text-red-400 mt-0.5">{passwordError}</p>}
            </div>

            {error && (
              <Alert variant="destructive" className="py-2.5 border-red-800 bg-red-950/50 text-red-400 rounded-lg">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription className="text-sm">{error}</AlertDescription>
              </Alert>
            )}

            <Button
              type="submit"
              disabled={loading}
              className="w-full mt-1 bg-indigo-600 hover:bg-indigo-500 text-white tracking-widest uppercase text-xs font-semibold h-11 shadow-lg shadow-indigo-900/40 hover:shadow-indigo-900/60 transition-all rounded-lg"
            >
              {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {loading ? 'Accesso in corso…' : 'Accedi'}
            </Button>

            <p className="text-center text-xs text-zinc-600 font-light">
              Non hai un account?{' '}
              <Link to="/register" className="text-indigo-400 hover:text-indigo-300 font-medium underline underline-offset-2 transition-colors">
                Registrati
              </Link>
            </p>

          </form>
        </div>

        <p className="absolute bottom-6 text-xs text-zinc-700 font-light">
          © {new Date().getFullYear()} Restora
        </p>
      </div>
    </div>
  );
}
