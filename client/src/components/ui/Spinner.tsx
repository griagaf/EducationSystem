type SpinnerProps = {
  label?: string;
};

export function Spinner({ label = 'Загрузка' }: SpinnerProps) {
  return (
    <div className="inline-flex items-center gap-3 text-sm font-medium text-slate-600">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-sky-700" />
      {label}
    </div>
  );
}
