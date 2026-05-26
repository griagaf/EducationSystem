import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Card } from '../components/ui/Card';
import { Spinner } from '../components/ui/Spinner';
import { TaskStatusBadge } from '../features/tasks/TaskStatusBadge';
import { taskPriorityLabels, taskStatusLabels } from '../features/tasks/taskLabels';
import { getApiErrorMessage } from '../services/apiClient';
import { taskService } from '../services/taskService';
import type { Task, TaskStatus } from '../types/tasks';

const statusOptions: Array<TaskStatus | 'ALL'> = [
  'ALL',
  'TODO',
  'IN_PROGRESS',
  'DONE',
  'CANCELLED',
];

export function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'ALL'>('ALL');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [updatingTaskId, setUpdatingTaskId] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    async function loadTasks() {
      setIsLoading(true);
      setError('');

      try {
        const data = await taskService.getTasks({
          status: statusFilter === 'ALL' ? undefined : statusFilter,
        });
        if (active) {
          setTasks(data);
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

    loadTasks();

    return () => {
      active = false;
    };
  }, [statusFilter]);

  async function handleStatusChange(taskId: string, status: TaskStatus) {
    setUpdatingTaskId(taskId);
    setError('');

    try {
      const updatedTask = await taskService.changeStatus(taskId, status);
      setTasks((currentTasks) =>
        currentTasks.map((task) => (task.id === taskId ? updatedTask : task)),
      );
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setUpdatingTaskId(null);
    }
  }

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-sky-700">Tasks</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-950">Задачи</h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
            Задачи создаются вручную или автоматически после генерации roadmap.
          </p>
        </div>

        <label className="block min-w-48">
          <span className="text-xs font-semibold uppercase text-slate-500">
            Статус
          </span>
          <select
            className="mt-2 h-10 w-full rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-800 shadow-sm focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-100"
            onChange={(event) => setStatusFilter(event.target.value as TaskStatus | 'ALL')}
            value={statusFilter}
          >
            {statusOptions.map((status) => (
              <option key={status} value={status}>
                {status === 'ALL' ? 'Все статусы' : taskStatusLabels[status]}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error ? <Alert message={error} /> : null}

      {isLoading ? (
        <Card>
          <Spinner label="Загружаем задачи" />
        </Card>
      ) : tasks.length === 0 ? (
        <Card>
          <h3 className="text-lg font-semibold text-slate-950">Задач пока нет</h3>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            После генерации roadmap система создаст задачи по этапам обучения.
          </p>
        </Card>
      ) : (
        <div className="grid gap-4">
          {tasks.map((task) => (
            <Card key={task.id}>
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <TaskStatusBadge status={task.status} />
                    <Badge tone={task.priority === 'HIGH' ? 'blue' : 'slate'}>
                      {taskPriorityLabels[task.priority]}
                    </Badge>
                  </div>
                  <h3 className="mt-3 text-lg font-semibold text-slate-950">
                    {task.title}
                  </h3>
                  {task.description ? (
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                      {task.description}
                    </p>
                  ) : null}
                  <div className="mt-3 flex flex-wrap gap-3 text-xs text-slate-500">
                    <Link
                      className="font-semibold text-sky-700 hover:text-sky-900"
                      to={`/goals/${task.learningGoalId}`}
                    >
                      {task.learningGoalTitle}
                    </Link>
                    {task.dueDate ? <span>До {task.dueDate}</span> : null}
                  </div>
                </div>

                <select
                  className="h-10 rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-800 shadow-sm focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-100 disabled:bg-slate-100"
                  disabled={updatingTaskId === task.id}
                  onChange={(event) =>
                    handleStatusChange(task.id, event.target.value as TaskStatus)
                  }
                  value={task.status}
                >
                  {statusOptions
                    .filter((status): status is TaskStatus => status !== 'ALL')
                    .map((status) => (
                      <option key={status} value={status}>
                        {taskStatusLabels[status]}
                      </option>
                    ))}
                </select>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
