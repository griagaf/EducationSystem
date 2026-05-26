import { Badge } from '../../components/ui/Badge';
import type { LearningGoalStatus } from '../../types/learning';
import { learningGoalStatusLabels } from './learningLabels';

type GoalStatusBadgeProps = {
  status: LearningGoalStatus;
};

export function GoalStatusBadge({ status }: GoalStatusBadgeProps) {
  const tone = status === 'COMPLETED' ? 'green' : status === 'ACTIVE' ? 'blue' : 'slate';

  return <Badge tone={tone}>{learningGoalStatusLabels[status]}</Badge>;
}
