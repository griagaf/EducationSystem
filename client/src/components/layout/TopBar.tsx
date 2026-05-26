import { useNavigate } from 'react-router-dom';

import { Button } from '../ui/Button';
import { useAuth } from '../../hooks/useAuth';

type TopBarProps = {
  title: string;
};

export function TopBar({ title }: TopBarProps) {
  const navigate = useNavigate();
  const { logout, user } = useAuth();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="border-b border-slate-200 bg-white px-4 py-4 sm:px-6 lg:px-8">
      <div className="flex items-center justify-between gap-4">
        <div className="min-w-0">
          <h1 className="text-xl font-semibold text-slate-950">{title}</h1>
          <p className="mt-1 truncate text-sm text-slate-500">
            {user?.displayName} · {user?.email}
          </p>
        </div>
        <Button onClick={handleLogout} variant="secondary">
          Logout
        </Button>
      </div>
    </header>
  );
}
