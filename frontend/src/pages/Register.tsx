import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '@/services/authService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Eye, EyeOff, UtensilsCrossed, AlertCircle, Loader2, CheckCircle } from 'lucide-react';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function RegisterPage() {
  const [nome,          setNome]          = useState('');
  const [email,         setEmail]         = useState('');
  const [password,      setPassword]      = useState('');
  const [showPwd,       setShowPwd]       = useState(false);
  const [loading,       setLoading]       = useState(false);
  const [error,         setError]         = useState<string | null>(null);
  const [success,       setSuccess]       = useState(false);
  const [nomeError,     setNomeError]     = useState('');
  const [emailError,    setEmailError]    = useState('');
  const [passwordError, setPasswordError] = useState('');
  const navigate = useNavigate();

  const validate = () => {
    const nErr = !nome.trim() ? 'Il nome è obbligatorio' : '';
    const eErr = !email ? "L'email è obbligatoria" : !EMAIL_RE.test(email) ? 'Formato email non valido' : '';
    const pErr = !password ? 'La password è obbligatoria' : password.length < 8 ? 'La password deve avere almeno 8 caratteri' : '';
    setNomeError(nErr);
    setEmailError(eErr);
    setPasswordError(pErr);
    return !nErr && !eErr && !pErr;
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    setError(null);
    try {
      await authService.register(nome, email, password);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message ?? 'Errore durante la registrazione. Riprova.');
    } finally {
      setLoading(false);
    }
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
        <div className="absolute -top-40 -right-40 w-[500px] h-[500px] rounded-full bg-indigo-600/15 blur-3xl" />

        <div className="relative flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-900/50">
            <UtensilsCrossed className="w-4 h-4 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-[0.2em] uppercase text-zinc-100">Restora</span>
        </div>

        <div className="relative">
          <p className="text-3xl font-light text-zinc-100 leading-snug mb-4">
            Entra a far parte<br />del tuo team<br />
            <span className="text-indigo-400">in pochi secondi.</span>
          </p>
          <p className="text-sm text-zinc-500 font-light leading-relaxed max-w-xs">
            Crea il tuo account e inizia a gestire ordini, cucina e menu dal primo accesso.
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
        <div className="absolute bottom-0 left-0 w-64 h-64 bg-indigo-600/5 rounded-full blur-3xl" />

        {/* Mobile brand */}
        <div className="lg:hidden flex flex-col items-center gap-2 mb-10">
          <div className="w-11 h-11 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-900/40">
            <UtensilsCrossed className="w-5 h-5 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-[0.2em] uppercase text-zinc-100">Restora</span>
        </div>

        <div className="w-full max-w-sm relative">
          <div className="mb-8">
            <h1 className="text-2xl font-semibold text-zinc-100 tracking-tight mb-1.5">Crea un account</h1>
            <p className="text-sm text-zinc-500 font-light">Inserisci i tuoi dati per registrarti</p>
          </div>

          <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="nome" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                Nome
              </Label>
              <Input
                id="nome"
                type="text"
                autoComplete="name"
                placeholder="Mario Rossi"
                value={nome}
                onChange={(e) => { setNome(e.target.value); setNomeError(''); }}
                disabled={loading || success}
                required
                className={`h-11 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg ${nomeError ? 'border-red-600' : ''}`}
              />
              {nomeError && <p className="text-xs text-red-400 mt-0.5">{nomeError}</p>}
            </div>

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
                disabled={loading || success}
                required
                className={`h-11 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg ${emailError ? 'border-red-600' : ''}`}
              />
              {emailError && <p className="text-xs text-red-400 mt-0.5">{emailError}</p>}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-xs tracking-widest uppercase text-zinc-400 font-semibold">
                Password
              </Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPwd ? 'text' : 'password'}
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => { setPassword(e.target.value); setPasswordError(''); }}
                  disabled={loading || success}
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
              {password.length > 0 && password.length < 8 && !passwordError && (
                <p className="text-xs text-zinc-500 mt-0.5">Almeno 8 caratteri ({password.length}/8)</p>
              )}
            </div>

            {error && (
              <Alert variant="destructive" className="py-2.5 border-red-800 bg-red-950/50 text-red-400 rounded-lg">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription className="text-sm">{error}</AlertDescription>
              </Alert>
            )}

            {success && (
              <Alert className="py-2.5 border-emerald-800 bg-emerald-950/50 text-emerald-400 rounded-lg">
                <CheckCircle className="h-4 w-4" />
                <AlertDescription className="text-sm">Registrazione completata! Reindirizzamento al login…</AlertDescription>
              </Alert>
            )}

            <Button
              type="submit"
              disabled={loading || success}
              className="w-full mt-1 bg-indigo-600 hover:bg-indigo-500 text-white tracking-widest uppercase text-xs font-semibold h-11 shadow-lg shadow-indigo-900/40 hover:shadow-indigo-900/60 transition-all rounded-lg"
            >
              {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {loading ? 'Registrazione in corso…' : 'Registrati'}
            </Button>

            <p className="text-center text-xs text-zinc-600 font-light">
              Hai già un account?{' '}
              <Link to="/login" className="text-indigo-400 hover:text-indigo-300 font-medium underline underline-offset-2 transition-colors">
                Accedi
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
