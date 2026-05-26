import { Navigate, Outlet, useLocation } from 'react-router-dom';

import { Spinner } from '../../components/ui/Spinner';
import { useAuth } from '../../hooks/useAuth';

export function ProtectedRoute() {
  const location = useLocation();
  const { status, user } = useAuth();

  if (status === 'loading') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <Spinner label="Проверяем сессию" />
      </div>
    );
  }

  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  return <Outlet />;
}
