import type { ReactNode } from 'react';

import { cn } from '../../utils/cn';

type BadgeProps = {
  children: ReactNode;
  tone?: 'neutral' | 'green' | 'blue' | 'slate';
};

const tones = {
  neutral: 'border-slate-200 bg-slate-50 text-slate-700',
  green: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  blue: 'border-sky-200 bg-sky-50 text-sky-700',
  slate: 'border-slate-300 bg-white text-slate-600',
};

export function Badge({ children, tone = 'neutral' }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold',
        tones[tone],
      )}
    >
      {children}
    </span>
  );
}
