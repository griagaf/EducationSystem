import { Badge } from '../../components/ui/Badge';
import type { TaskStatus } from '../../types/tasks';
import { taskStatusLabels } from './taskLabels';

type TaskStatusBadgeProps = {
  status: TaskStatus;
};

const tones: Record<TaskStatus, 'neutral' | 'green' | 'blue' | 'slate'> = {
  TODO: 'neutral',
  IN_PROGRESS: 'blue',
  DONE: 'green',
  CANCELLED: 'slate',
};

export function TaskStatusBadge({ status }: TaskStatusBadgeProps) {
  return <Badge tone={tones[status]}>{taskStatusLabels[status]}</Badge>;
}
