import { NavLink } from 'react-router-dom';

import { cn } from '../../utils/cn';

const navItems = [
  { label: 'Dashboard', to: '/dashboard' },
  { label: 'Learning Goals', to: '/goals' },
  { label: 'Notes', to: '/notes' },
  { label: 'Tasks', to: '/tasks' },
];

export function Sidebar() {
  return (
    <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white px-4 py-5 lg:block">
      <div className="mb-8 px-2">
        <div className="text-sm font-semibold text-sky-700">AI Learning</div>
        <div className="mt-1 text-xs text-slate-500">Personal workspace</div>
      </div>

      <nav className="space-y-1">
        {navItems.map((item) => (
          <NavLink
            className={({ isActive }) =>
              cn(
                'block rounded-lg px-3 py-2 text-sm font-medium transition',
                isActive
                  ? 'bg-sky-50 text-sky-800'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950',
              )
            }
            key={item.to}
            to={item.to}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
