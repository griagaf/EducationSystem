import type { ReactNode } from 'react';

type AuthLayoutProps = {
  children: ReactNode;
  title: string;
  subtitle: string;
};

export function AuthLayout({ children, subtitle, title }: AuthLayoutProps) {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-950">
      <div className="mx-auto flex min-h-screen w-full max-w-6xl items-center justify-center px-4 py-8">
        <section className="grid w-full overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm lg:grid-cols-[0.95fr_1.05fr]">
          <div className="hidden border-r border-slate-200 bg-slate-950 p-10 text-white lg:flex lg:flex-col lg:justify-between">
            <div>
              <div className="text-sm font-semibold text-sky-300">
                AI Personal Learning Platform
              </div>
              <h1 className="mt-8 text-3xl font-semibold leading-tight">
                Персональное обучение, материалы и прогресс в одной рабочей среде.
              </h1>
            </div>
            <div className="rounded-xl border border-white/10 bg-white/5 p-5">
              <div className="text-sm font-medium text-slate-200">
                Backend API
              </div>
              <div className="mt-2 text-sm text-slate-400">/api/v1/auth</div>
            </div>
          </div>
          <div className="px-5 py-8 sm:px-8 lg:px-12 lg:py-14">
            <div className="mx-auto w-full max-w-md">
              <div className="mb-8">
                <p className="text-sm font-semibold text-sky-700">
                  AI Personal Learning Platform
                </p>
                <h2 className="mt-3 text-2xl font-semibold text-slate-950">
                  {title}
                </h2>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  {subtitle}
                </p>
              </div>
              {children}
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
