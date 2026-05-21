import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Spinner } from '../components/ui/Spinner';
import { GoalStatusBadge } from '../features/learning/GoalStatusBadge';
import { learningGoalTypeLabels } from '../features/learning/learningLabels';
import { TaskStatusBadge } from '../features/tasks/TaskStatusBadge';
import { taskPriorityLabels } from '../features/tasks/taskLabels';
import { getApiErrorMessage } from '../services/apiClient';
import { learningService } from '../services/learningService';
import { taskService } from '../services/taskService';
import type { LearningGoal } from '../types/learning';
import type { Task } from '../types/tasks';

export function LearningGoalDetailsPage() {
  const { goalId } = useParams();
  const [goal, setGoal] = useState<LearningGoal | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadGoal() {
      if (!goalId) {
        setError('Goal id is missing');
        setIsLoading(false);
        return;
      }

      try {
        const [goalData, taskData] = await Promise.all([
          learningService.getGoal(goalId),
          taskService.getTasks({ learningGoalId: goalId }),
        ]);
        if (active) {
          setGoal(goalData);
          setTasks(taskData);
        }
      } catch (requestError) {
        if (active) {
          setError(getApiErrorMessage(requestError));
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    loadGoal();

    return () => {
      active = false;
    };
  }, [goalId]);

  if (isLoading) {
    return (
      <div className="mx-auto max-w-4xl">
        <Card>
          <Spinner label="Загружаем цель" />
        </Card>
      </div>
    );
  }

  if (error || !goal) {
    return (
      <div className="mx-auto max-w-4xl space-y-4">
        {error ? <Alert message={error} /> : null}
        <Link to="/goals">
          <Button variant="secondary">Вернуться к целям</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <Link to="/goals">
        <Button variant="ghost">Back to goals</Button>
      </Link>

      <Card>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="text-sm font-semibold text-sky-700">Learning Goal</p>
            <h2 className="mt-2 text-3xl font-semibold text-slate-950">
              {goal.title}
            </h2>
          </div>
          <GoalStatusBadge status={goal.status} />
        </div>

        <p className="mt-5 max-w-3xl text-sm leading-6 text-slate-600">
          {goal.description}
        </p>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <InfoItem label="Type" value={learningGoalTypeLabels[goal.type]} />
          <InfoItem label="Target date" value={goal.targetDate ?? 'Not set'} />
          <InfoItem
            label="Duration"
            value={goal.durationWeeks ? `${goal.durationWeeks} weeks` : 'Not set'}
          />
          <InfoItem label="Progress" value={`${goal.progressPercent}%`} />
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-slate-950">Roadmap</h3>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Roadmap отображается после генерации через backend API.
            </p>
          </div>
          <Badge tone="slate">API ready</Badge>
        </div>
      </Card>

      <Card>
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-slate-950">Связанные задачи</h3>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Задачи появляются после генерации roadmap или ручного создания.
            </p>
          </div>
          <Link to="/tasks">
            <Button variant="secondary">Открыть все задачи</Button>
          </Link>
        </div>

        {tasks.length === 0 ? (
          <div className="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-5 text-sm text-slate-600">
            Для этой цели пока нет задач.
          </div>
        ) : (
          <div className="mt-5 divide-y divide-slate-200">
            {tasks.slice(0, 5).map((task) => (
              <div
                className="flex flex-wrap items-center justify-between gap-3 py-3"
                key={task.id}
              >
                <div className="min-w-0">
                  <div className="font-semibold text-slate-950">{task.title}</div>
                  <div className="mt-1 text-xs text-slate-500">
                    Приоритет: {taskPriorityLabels[task.priority]}
                  </div>
                </div>
                <TaskStatusBadge status={task.status} />
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

type InfoItemProps = {
  label: string;
  value: string;
};

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
      <div className="text-xs font-semibold uppercase text-slate-500">{label}</div>
      <div className="mt-2 text-sm font-semibold text-slate-950">{value}</div>
    </div>
  );
}
