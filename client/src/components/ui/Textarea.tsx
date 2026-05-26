import type { TextareaHTMLAttributes } from 'react';

import { cn } from '../../utils/cn';

type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  error?: string;
  label: string;
};

export function Textarea({ className, error, id, label, ...props }: TextareaProps) {
  const textareaId = id ?? props.name;

  return (
    <label className="block" htmlFor={textareaId}>
      <span className="text-sm font-medium text-slate-700">{label}</span>
      <textarea
        className={cn(
          'mt-2 min-h-28 w-full resize-y rounded-lg border bg-white px-3 py-3 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-sky-600 focus:ring-4 focus:ring-sky-100',
          error ? 'border-red-400' : 'border-slate-300',
          className,
        )}
        id={textareaId}
        {...props}
      />
      {error ? <span className="mt-2 block text-sm text-red-600">{error}</span> : null}
    </label>
  );
}
