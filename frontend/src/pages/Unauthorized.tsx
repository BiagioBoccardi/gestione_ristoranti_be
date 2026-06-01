import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function Unauthorized() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen space-y-4">
      <h1 className="text-4xl font-bold text-red-600">403 - Accesso Negato</h1>
      <p className="text-slate-600">Non hai i permessi necessari per visualizzare questa pagina.</p>
      <Link to="/dashboard">
        <Button>Torna alla Dashboard</Button>
      </Link>
    </div>
  );
}