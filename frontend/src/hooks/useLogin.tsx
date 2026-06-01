import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { authService } from '@/services/authService';
import { Role } from '@/types/auth';

const ROLE_HOME: Record<string, string> = {
  [Role.ADMIN]:     '/dashboard',
  [Role.CAMERIERE]: '/ordini',
  [Role.CUOCO]:     '/cucina',
  [Role.CLIENTE]:   '/menu',
};

/**
 * Hook che gestisce il flusso di login: chiama l'API, salva il JWT in AuthContext,
 * decodifica il ruolo dal payload del token e reindirizza l'utente alla pagina
 * di destinazione corretta per il suo ruolo (o alla pagina da cui proveniva).
 *
 * @returns `handleLogin(email, password)` — funzione asincrona da invocare al submit;
 *          `loading` — true durante la chiamata API;
 *          `error` — messaggio di errore localizzato, o null se nessun errore
 */
export function useLogin() {
  const { login } = useAuth();
  const navigate  = useNavigate();
  const location  = useLocation();

  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState<string | null>(null);

  const handleLogin = async (email: string, password: string) => {
    setLoading(true);
    setError(null);
    try {
      const { token } = await authService.login(email, password);
      login(token); // salva in localStorage e aggiorna AuthContext

      const role = JSON.parse(atob(token.split('.')[1])).role as string;
      const home = ROLE_HOME[role] ?? '/dashboard';
      const from = (location.state as { from?: { pathname: string } })?.from?.pathname;
      // Usa 'from' solo se è una pagina specifica (non il fallback generico /dashboard)
      navigate(from && from !== '/dashboard' ? from : home, { replace: true });
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status: number; data?: { message?: string } } };
      const msg =
        axiosErr.response?.status === 401
          ? 'Email o password non corretti.'
          : axiosErr.response?.data?.message ?? 'Errore di connessione. Riprova.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return { handleLogin, loading, error };
}