import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Spinner } from '../components/ui/Spinner';
import { GoalStatusBadge } from '../features/learning/GoalStatusBadge';
import { learningGoalTypeLabels } from '../features/learning/learningLabels';
import { TaskStatusBadge } from '../features/tasks/TaskStatusBadge';
import { taskPriorityLabels, taskStatusLabels } from '../features/tasks/taskLabels';
import { getApiErrorMessage } from '../services/apiClient';
import { learningService } from '../services/learningService';
import { taskService } from '../services/taskService';
import type { LearningGoal, Roadmap, Topic } from '../types/learning';
import type { Task, TaskStatus } from '../types/tasks';

export function LearningGoalDetailsPage() {
  const { goalId } = useParams();
  const [goal, setGoal] = useState<LearningGoal | null>(null);
  const [roadmap, setRoadmap] = useState<Roadmap | null>(null);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [pageError, setPageError] = useState('');
  const [roadmapError, setRoadmapError] = useState('');
  const [taskError, setTaskError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [updatingTaskId, setUpdatingTaskId] = useState<string | null>(null);

  const loadGoalFlow = useCallback(async () => {
    if (!goalId) {
      setPageError('Goal id is missing');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setPageError('');
    setRoadmapError('');

    try {
      const goalData = await learningService.getGoal(goalId);
      const [roadmapData, taskData] = await Promise.all([
        learningService.getRoadmap(goalId),
        taskService.getTasks({ learningGoalId: goalId }),
      ]);

      setGoal(goalData);
      setRoadmap(roadmapData);
      setTasks(taskData);
    } catch (requestError) {
      setPageError(getApiErrorMessage(requestError));
    } finally {
      setIsLoading(false);
    }
  }, [goalId]);

  useEffect(() => {
    loadGoalFlow();
  }, [loadGoalFlow]);

  const topicsById = useMemo(() => {
    return new Map(topics.map((topic) => [topic.id, topic]));
  }, [topics]);

  const tasksByStepId = useMemo(() => {
    return tasks.reduce<Record<string, Task[]>>((accumulator, task) => {
      if (!task.roadmapStepId) {
        return accumulator;
      }
      accumulator[task.roadmapStepId] = [
        ...(accumulator[task.roadmapStepId] ?? []),
        task,
      ];
      return accumulator;
    }, {});
  }, [tasks]);

  async function handleGenerateRoadmap() {
    if (!goalId) {
      return;
    }

    setIsGenerating(true);
    setRoadmapError('');
    setTaskError('');

    try {
      const generated = await learningService.generateRoadmap(goalId, {
        userLevel: 'beginner',
        includeMaterials: false,
      });
      const [goalData, taskData] = await Promise.all([
        learningService.getGoal(goalId),
        taskService.getTasks({ learningGoalId: goalId }),
      ]);

      setGoal(goalData);
      setRoadmap(generated.roadmap);
      setTopics(generated.topics);
      setTasks(taskData);
    } catch (requestError) {
      setRoadmapError(getApiErrorMessage(requestError));
    } finally {
      setIsGenerating(false);
    }
  }

  async function handleTaskStatusChange(taskId: string, status: TaskStatus) {
    if (!goalId) {
      return;
    }

    setUpdatingTaskId(taskId);
    setTaskError('');

    try {
      const updatedTask = await taskService.changeStatus(taskId, status);
      const updatedGoal = await learningService.getGoal(goalId);

      setTasks((currentTasks) =>
        currentTasks.map((task) => (task.id === taskId ? updatedTask : task)),
      );
      setGoal(updatedGoal);
    } catch (requestError) {
      setTaskError(getApiErrorMessage(requestError));
    } finally {
      setUpdatingTaskId(null);
    }
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-4xl">
        <Card>
          <Spinner label="Загружаем цель" />
        </Card>
      </div>
    );
  }

  if (pageError || !goal) {
    return (
      <div className="mx-auto max-w-4xl space-y-4">
        {pageError ? <Alert message={pageError} /> : null}
        <Link to="/goals">
          <Button variant="secondary">Вернуться к целям</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <Link to="/goals">
        <Button variant="ghost">Вернуться к целям</Button>
      </Link>

      <Card>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="text-sm font-semibold text-sky-700">Учебная цель</p>
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
          <InfoItem label="Тип" value={learningGoalTypeLabels[goal.type]} />
          <InfoItem label="Целевая дата" value={goal.targetDate ?? 'Не указана'} />
          <InfoItem
            label="Длительность"
            value={goal.durationWeeks ? `${goal.durationWeeks} недель` : 'Не указана'}
          />
          <InfoItem label="Прогресс" value={`${goal.progressPercent}%`} />
        </div>

        <div className="mt-6">
          <div className="h-2 overflow-hidden rounded-full bg-slate-100">
            <div
              className="h-full rounded-full bg-sky-700 transition-all"
              style={{ width: `${goal.progressPercent}%` }}
            />
          </div>
        </div>
      </Card>

      <Card>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-slate-950">Roadmap</h3>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
              Сгенерируйте roadmap, чтобы получить этапы обучения и связанные задачи.
            </p>
          </div>

          {!roadmap ? (
            <Button disabled={isGenerating} onClick={handleGenerateRoadmap}>
              {isGenerating ? 'Генерируем...' : 'Сгенерировать roadmap'}
            </Button>
          ) : (
            <Badge tone="green">Roadmap создан</Badge>
          )}
        </div>

        {roadmapError ? (
          <div className="mt-4">
            <Alert message={roadmapError} />
          </div>
        ) : null}

        {isGenerating ? (
          <div className="mt-5 rounded-lg border border-sky-100 bg-sky-50 px-4 py-5">
            <Spinner label="Генерируем roadmap и создаем задачи" />
          </div>
        ) : null}

        {!roadmap && !isGenerating ? (
          <div className="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-5 text-sm text-slate-600">
            Roadmap пока не создан. После генерации здесь появятся этапы, темы и задачи.
          </div>
        ) : null}

        {roadmap ? (
          <RoadmapView
            onTaskStatusChange={handleTaskStatusChange}
            tasksByStepId={tasksByStepId}
            topicsById={topicsById}
            updatingTaskId={updatingTaskId}
            roadmap={roadmap}
          />
        ) : null}
      </Card>

      <Card>
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-slate-950">Все задачи цели</h3>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Эти задачи связаны с текущей целью и доступны в общем разделе Tasks.
            </p>
          </div>
          <Link to="/tasks">
            <Button variant="secondary">Открыть Tasks</Button>
          </Link>
        </div>

        {taskError ? (
          <div className="mt-4">
            <Alert message={taskError} />
          </div>
        ) : null}

        {tasks.length === 0 ? (
          <div className="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-5 text-sm text-slate-600">
            Для этой цели пока нет задач.
          </div>
        ) : (
          <div className="mt-5 grid gap-3">
            {tasks.map((task) => (
              <TaskRow
                key={task.id}
                onStatusChange={handleTaskStatusChange}
                task={task}
                updatingTaskId={updatingTaskId}
              />
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

type RoadmapViewProps = {
  roadmap: Roadmap;
  tasksByStepId: Record<string, Task[]>;
  topicsById: Map<string, Topic>;
  updatingTaskId: string | null;
  onTaskStatusChange: (taskId: string, status: TaskStatus) => void;
};

function RoadmapView({
  roadmap,
  tasksByStepId,
  topicsById,
  updatingTaskId,
  onTaskStatusChange,
}: RoadmapViewProps) {
  const sortedSteps = [...roadmap.steps].sort((left, right) => left.orderIndex - right.orderIndex);

  return (
    <div className="mt-6 space-y-5">
      <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-4">
        <h4 className="text-base font-semibold text-slate-950">{roadmap.title}</h4>
        <p className="mt-2 text-sm leading-6 text-slate-600">{roadmap.description}</p>
      </div>

      <div className="grid gap-4">
        {sortedSteps.map((step) => {
          const stepTasks = tasksByStepId[step.id] ?? [];
          const topic = step.topicId ? topicsById.get(step.topicId) : null;

          return (
            <div
              className="rounded-lg border border-slate-200 bg-white px-4 py-4"
              key={step.id}
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge tone="blue">Этап {step.orderIndex}</Badge>
                    <Badge tone="slate">{step.status}</Badge>
                    <Badge tone="neutral">{step.estimatedDays} дней</Badge>
                  </div>
                  <h4 className="mt-3 text-lg font-semibold text-slate-950">
                    {step.title}
                  </h4>
                </div>
              </div>

              <p className="mt-3 text-sm leading-6 text-slate-600">
                {step.description}
              </p>

              <div className="mt-4 flex flex-wrap gap-2">
                {step.topicId ? (
                  <Badge tone="neutral">
                    {topic?.title ?? `Topic ${step.topicId.slice(0, 8)}`}
                  </Badge>
                ) : (
                  <Badge tone="slate">Topic не указан</Badge>
                )}
              </div>

              <div className="mt-5">
                <div className="text-sm font-semibold text-slate-950">
                  Задачи этапа
                </div>
                {stepTasks.length === 0 ? (
                  <div className="mt-3 rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-4 text-sm text-slate-600">
                    Для этапа пока нет связанных задач.
                  </div>
                ) : (
                  <div className="mt-3 grid gap-3">
                    {stepTasks.map((task) => (
                      <TaskRow
                        compact
                        key={task.id}
                        onStatusChange={onTaskStatusChange}
                        task={task}
                        updatingTaskId={updatingTaskId}
                      />
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

type TaskRowProps = {
  task: Task;
  updatingTaskId: string | null;
  compact?: boolean;
  onStatusChange: (taskId: string, status: TaskStatus) => void;
};

function TaskRow({ task, updatingTaskId, compact = false, onStatusChange }: TaskRowProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <TaskStatusBadge status={task.status} />
          <Badge tone={task.priority === 'HIGH' ? 'blue' : 'slate'}>
            {taskPriorityLabels[task.priority]}
          </Badge>
        </div>
        <div className="mt-2 font-semibold text-slate-950">{task.title}</div>
        {!compact && task.description ? (
          <p className="mt-1 text-sm leading-6 text-slate-600">{task.description}</p>
        ) : null}
      </div>

      <select
        className="h-10 rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-800 shadow-sm focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-100 disabled:bg-slate-100"
        disabled={updatingTaskId === task.id}
        onChange={(event) => onStatusChange(task.id, event.target.value as TaskStatus)}
        value={task.status}
      >
        {Object.entries(taskStatusLabels).map(([status, label]) => (
          <option key={status} value={status}>
            {label}
          </option>
        ))}
      </select>
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
