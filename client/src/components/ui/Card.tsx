import type { HTMLAttributes, ReactNode } from 'react';

import { cn } from '../../utils/cn';

type CardProps = HTMLAttributes<HTMLDivElement> & {
  children: ReactNode;
};

export function Card({ children, className, ...props }: CardProps) {
  return (
    <div
      className={cn('rounded-lg border border-slate-200 bg-white p-5 shadow-sm', className)}
      {...props}
    >
      {children}
    </div>
  );
}
