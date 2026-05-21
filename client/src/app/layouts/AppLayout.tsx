import { Outlet, useLocation } from 'react-router-dom';

import { Sidebar } from '../../components/layout/Sidebar';
import { TopBar } from '../../components/layout/TopBar';

const titles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/goals': 'Learning Goals',
  '/notes': 'Notes',
  '/tasks': 'Tasks',
};

export function AppLayout() {
  const location = useLocation();
  const title = titles[location.pathname] ?? 'Workspace';

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <div className="flex min-h-screen">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <TopBar title={title} />
          <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}
