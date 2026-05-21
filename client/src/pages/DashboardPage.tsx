import { Card } from '../components/ui/Card';
import { useAuth } from '../hooks/useAuth';

const nextSteps = [
  'Подключить заметки',
  'Добавить учебные цели',
  'Подключить задачи и прогресс',
];

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <p className="text-sm font-semibold text-sky-700">Workspace</p>
        <h2 className="mt-3 text-2xl font-semibold text-slate-950">
          Добро пожаловать, {user?.displayName}
        </h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
          Авторизация подключена. Данные dashboard появятся после реализации
          учебных целей, задач и прогресса.
        </p>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <Card>
          <div className="text-sm font-medium text-slate-500">Auth status</div>
          <div className="mt-3 text-2xl font-semibold text-emerald-700">
            Active
          </div>
        </Card>
        <Card>
          <div className="text-sm font-medium text-slate-500">API prefix</div>
          <div className="mt-3 text-2xl font-semibold text-slate-950">/api/v1</div>
        </Card>
        <Card>
          <div className="text-sm font-medium text-slate-500">Session</div>
          <div className="mt-3 text-2xl font-semibold text-slate-950">JWT</div>
        </Card>
      </section>

      <Card>
        <h3 className="text-lg font-semibold text-slate-950">Следующие модули</h3>
        <ul className="mt-4 grid gap-3 md:grid-cols-3">
          {nextSteps.map((step) => (
            <li
              className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700"
              key={step}
            >
              {step}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
}
