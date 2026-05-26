import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Spinner } from '../components/ui/Spinner';
import { GoalStatusBadge } from '../features/learning/GoalStatusBadge';
import { learningGoalTypeLabels } from '../features/learning/learningLabels';
import { TaskStatusBadge } from '../features/tasks/TaskStatusBadge';
import { taskPriorityLabels } from '../features/tasks/taskLabels';
import { useAuth } from '../hooks/useAuth';
import { getApiErrorMessage } from '../services/apiClient';
import { dashboardService } from '../services/dashboardService';
import type { DashboardSummary } from '../types/dashboard';

export function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadSummary() {
      setIsLoading(true);
      setError('');

      try {
        const loadedSummary = await dashboardService.getSummary();
        if (isMounted) {
          setSummary(loadedSummary);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(getApiErrorMessage(requestError));
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadSummary();

    return () => {
      isMounted = false;
    };
  }, []);

  if (isLoading) {
    return (
      <div className="mx-auto max-w-6xl">
        <Card>
          <Spinner label="Загружаем dashboard" />
        </Card>
      </div>
    );
  }

  if (error || !summary) {
    return (
      <div className="mx-auto max-w-6xl space-y-4">
        <Alert message={error || 'Не удалось загрузить dashboard'} />
        <Button onClick={() => window.location.reload()} variant="secondary">
          Обновить
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-semibold text-sky-700">Dashboard</p>
            <h2 className="mt-3 text-2xl font-semibold text-slate-950">
              Прогресс обучения, {user?.displayName}
            </h2>
          </div>
          <Link to="/goals">
            <Button>Открыть цели</Button>
          </Link>
        </div>

        <div className="mt-6">
          <ProgressBar value={summary.taskCompletionPercent} />
          <div className="mt-2 text-sm text-slate-600">
            Выполнено {summary.completedTasks} из {summary.totalTasks} задач
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Всего целей" value={summary.totalGoals} />
        <MetricCard label="Активные цели" value={summary.activeGoals} />
        <MetricCard label="Прогресс задач" value={`${summary.taskCompletionPercent}%`} />
        <MetricCard label="Средний mastery" value={`${summary.averageMasteryScore}%`} />
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <Card>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h3 className="text-lg font-semibold text-slate-950">Недавние цели</h3>
            <Badge tone="slate">{summary.recentGoals.length}</Badge>
          </div>

          {summary.recentGoals.length === 0 ? (
            <EmptyBlock message="Учебные цели пока не созданы." />
          ) : (
            <div className="mt-5 grid gap-3">
              {summary.recentGoals.map((goal) => (
                <Link
                  className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 transition hover:border-sky-200 hover:bg-sky-50"
                  key={goal.id}
                  to={`/goals/${goal.id}`}
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="font-semibold text-slate-950">{goal.title}</div>
                      <div className="mt-1 text-sm text-slate-600">
                        {learningGoalTypeLabels[goal.type]}
                      </div>
                    </div>
                    <GoalStatusBadge status={goal.status} />
                  </div>
                  <div className="mt-3">
                    <ProgressBar value={goal.progressPercent} />
                  </div>
                </Link>
              ))}
            </div>
          )}
        </Card>

        <Card>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h3 className="text-lg font-semibold text-slate-950">Ближайшие задачи</h3>
            <Badge tone="slate">{summary.upcomingTasks.length}</Badge>
          </div>

          {summary.upcomingTasks.length === 0 ? (
            <EmptyBlock message="Нет задач в работе или к выполнению." />
          ) : (
            <div className="mt-5 grid gap-3">
              {summary.upcomingTasks.map((task) => (
                <div
                  className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3"
                  key={task.id}
                >
                  <div className="flex flex-wrap items-center gap-2">
                    <TaskStatusBadge status={task.status} />
                    <Badge tone={task.priority === 'HIGH' ? 'blue' : 'slate'}>
                      {taskPriorityLabels[task.priority]}
                    </Badge>
                  </div>
                  <div className="mt-2 font-semibold text-slate-950">{task.title}</div>
                  <div className="mt-1 text-sm text-slate-600">
                    {task.learningGoalTitle}
                    {task.dueDate ? ` · ${task.dueDate}` : ''}
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </section>

      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h3 className="text-lg font-semibold text-slate-950">Темы для повторения</h3>
          <Badge tone="slate">{summary.weakTopics.length}</Badge>
        </div>

        {summary.weakTopics.length === 0 ? (
          <EmptyBlock message="Тем с низким mastery score пока нет." />
        ) : (
          <div className="mt-5 grid gap-3 md:grid-cols-2">
            {summary.weakTopics.map((topic) => (
              <div
                className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3"
                key={topic.id}
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0 font-semibold text-slate-950">{topic.title}</div>
                  <Badge tone={topic.masteryScore < 30 ? 'neutral' : 'slate'}>
                    {topic.difficultyLevel}
                  </Badge>
                </div>
                <div className="mt-3">
                  <ProgressBar value={topic.masteryScore} />
                </div>
                <div className="mt-2 text-sm text-slate-600">
                  Mastery {topic.masteryScore}%
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

type MetricCardProps = {
  label: string;
  value: number | string;
};

function MetricCard({ label, value }: MetricCardProps) {
  return (
    <Card>
      <div className="text-sm font-medium text-slate-500">{label}</div>
      <div className="mt-3 text-3xl font-semibold text-slate-950">{value}</div>
    </Card>
  );
}

type ProgressBarProps = {
  value: number;
};

function ProgressBar({ value }: ProgressBarProps) {
  const safeValue = Math.max(0, Math.min(100, value));

  return (
    <div className="h-2 overflow-hidden rounded-full bg-slate-100">
      <div
        className="h-full rounded-full bg-sky-700 transition-all"
        style={{ width: `${safeValue}%` }}
      />
    </div>
  );
}

type EmptyBlockProps = {
  message: string;
};

function EmptyBlock({ message }: EmptyBlockProps) {
  return (
    <div className="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-5 text-sm text-slate-600">
      {message}
    </div>
  );
}
