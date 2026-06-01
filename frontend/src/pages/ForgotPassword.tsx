import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '@/services/authService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { UtensilsCrossed, Loader2, ArrowLeft, MailCheck } from 'lucide-react';

export default function ForgotPasswordPage() {
  const [email, setEmail]       = useState('');
  const [loading, setLoading]   = useState(false);
  const [sent, setSent]         = useState(false);
  const [error, setError]       = useState('');

  const onSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await authService.forgotPassword(email);
      setSent(true);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setError(e.response?.data?.message ?? 'Errore imprevisto. Riprova.');
    } finally {
      setLoading(false);
    }
  };

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

        {sent ? (
          /* Stato di successo */
          <div className="flex flex-col gap-5">
            <div className="w-12 h-12 rounded-2xl bg-emerald-600/20 border border-emerald-600/30 flex items-center justify-center">
              <MailCheck className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h1 className="text-xl font-semibold text-zinc-100 mb-2">Email inviata</h1>
              <p className="text-sm text-zinc-400 font-light leading-relaxed">
                Se <span className="text-zinc-200">{email}</span> è associata a un account,
                riceverai un link per reimpostare la password entro pochi minuti.
              </p>
              <p className="text-xs text-zinc-600 mt-3">
                Non trovi l'email? Controlla la cartella spam.
              </p>
            </div>
            <Link
              to="/login"
              className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" /> Torna al login
            </Link>
          </div>
        ) : (
          /* Form */
          <>
            <div className="mb-8">
              <h1 className="text-2xl font-semibold text-zinc-100 tracking-tight mb-1.5">
                Password dimenticata
              </h1>
              <p className="text-sm text-zinc-500 font-light">
                Inserisci la tua email e ti invieremo un link per reimpostarla.
              </p>
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
                  onChange={e => setEmail(e.target.value)}
                  disabled={loading}
                  required
                  className="h-11 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-600 focus-visible:ring-indigo-500 focus-visible:border-indigo-500 rounded-lg"
                />
              </div>

              {error && (
                <p className="text-sm text-red-400 bg-red-950/40 border border-red-800/50 rounded-lg px-3 py-2.5">
                  {error}
                </p>
              )}

              <Button
                type="submit"
                disabled={loading || !email}
                className="w-full bg-indigo-600 hover:bg-indigo-500 text-white tracking-widest uppercase text-xs font-semibold h-11 rounded-lg"
              >
                {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                {loading ? 'Invio in corso…' : 'Invia link di reset'}
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
