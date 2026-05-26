import { Card } from '../components/ui/Card';

type PlaceholderPageProps = {
  title: string;
};

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <div className="mx-auto max-w-4xl">
      <Card>
        <p className="text-sm font-semibold text-sky-700">{title}</p>
        <h2 className="mt-3 text-xl font-semibold text-slate-950">
          Раздел ожидает подключения backend API.
        </h2>
        <p className="mt-2 text-sm leading-6 text-slate-600">
          Маршрут защищен JWT и использует общую оболочку приложения.
        </p>
      </Card>
    </div>
  );
}
