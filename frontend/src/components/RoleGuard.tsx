import type { ReactNode } from 'react';
import { useAuth } from '@/context/AuthContext';
import type { RoleType } from '@/types/auth';

interface RoleGuardProps {
  roles: RoleType[];
  children: ReactNode;
  fallback?: ReactNode;
}

/**
 * Mostra i children solo se l'utente ha almeno uno dei ruoli richiesti.
 *
 * Uso:
 *   <RoleGuard roles={[Role.ADMIN]}>
 *     <PulsanteEliminazione />
 *   </RoleGuard>
 *
 *   <RoleGuard roles={[Role.ADMIN]} fallback={<p>Solo admin</p>}>
 *     <PannelloAdmin />
 *   </RoleGuard>
 */
export default function RoleGuard({ roles, children, fallback = null }: RoleGuardProps) {
  const { user } = useAuth();

  if (!user || !roles.includes(user.role)) return <>{fallback}</>;
  return <>{children}</>;
}