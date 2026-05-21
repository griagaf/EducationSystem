import React from 'react';
import ReactDOM from 'react-dom/client';

import './styles.css';

function App() {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-900">
      <section className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center px-6 py-12">
        <div className="max-w-3xl">
          <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">
            AI Personal Learning Platform
          </p>
          <h1 className="mt-4 text-4xl font-bold tracking-tight sm:text-5xl">
            Базовая инфраструктура платформы готова
          </h1>
          <p className="mt-6 text-lg leading-8 text-slate-600">
            React frontend подключен к Vite и Tailwind CSS. Бизнес-сценарии,
            авторизация и AI-функции будут добавляться следующими этапами.
          </p>
          <div className="mt-8 grid gap-4 sm:grid-cols-3">
            <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div className="text-sm font-medium text-slate-500">Backend</div>
              <div className="mt-2 text-2xl font-semibold">/api/v1</div>
            </div>
            <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div className="text-sm font-medium text-slate-500">AI-service</div>
              <div className="mt-2 text-2xl font-semibold">/health</div>
            </div>
            <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div className="text-sm font-medium text-slate-500">Database</div>
              <div className="mt-2 text-2xl font-semibold">PostgreSQL</div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

