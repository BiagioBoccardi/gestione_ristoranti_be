import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import type { RoleType } from '@/types/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: RoleType[];
}

export default function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  // Se l'utente non è loggato, reindirizza al login salvando la pagina che cercava di visitare
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Se la rotta richiede ruoli specifici e l'utente non li ha, reindirizza
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Se passa tutti i controlli, renderizza il componente richiesto
  return <>{children}</>;
}