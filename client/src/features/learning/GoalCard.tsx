import { Link } from 'react-router-dom';

import { Card } from '../../components/ui/Card';
import type { LearningGoal } from '../../types/learning';
import { GoalStatusBadge } from './GoalStatusBadge';
import { learningGoalTypeLabels } from './learningLabels';

type GoalCardProps = {
  goal: LearningGoal;
};

export function GoalCard({ goal }: GoalCardProps) {
  return (
    <Card className="transition hover:border-sky-200 hover:shadow-md">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <Link
            className="text-lg font-semibold text-slate-950 hover:text-sky-700"
            to={`/goals/${goal.id}`}
          >
            {goal.title}
          </Link>
          <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">
            {goal.description}
          </p>
        </div>
        <GoalStatusBadge status={goal.status} />
      </div>
      <div className="mt-4 flex flex-wrap gap-2 text-xs font-medium text-slate-500">
        <span>{learningGoalTypeLabels[goal.type]}</span>
        {goal.targetDate ? <span>Target: {goal.targetDate}</span> : null}
        {goal.durationWeeks ? <span>{goal.durationWeeks} weeks</span> : null}
      </div>
      <div className="mt-5">
        <div className="mb-2 flex justify-between text-xs font-medium text-slate-500">
          <span>Progress</span>
          <span>{goal.progressPercent}%</span>
        </div>
        <div className="h-2 rounded-full bg-slate-100">
          <div
            className="h-2 rounded-full bg-sky-700"
            style={{ width: `${goal.progressPercent}%` }}
          />
        </div>
      </div>
    </Card>
  );
}
