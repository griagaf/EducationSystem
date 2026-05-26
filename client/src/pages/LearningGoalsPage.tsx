import { useEffect, useState } from 'react';

import { Alert } from '../components/ui/Alert';
import { Card } from '../components/ui/Card';
import { Spinner } from '../components/ui/Spinner';
import { GoalCard } from '../features/learning/GoalCard';
import { GoalForm } from '../features/learning/GoalForm';
import { getApiErrorMessage } from '../services/apiClient';
import { learningService } from '../services/learningService';
import type { LearningGoal } from '../types/learning';

export function LearningGoalsPage() {
  const [goals, setGoals] = useState<LearningGoal[]>([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadGoals() {
      try {
        const data = await learningService.getGoals();
        if (active) {
          setGoals(data);
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

    loadGoals();

    return () => {
      active = false;
    };
  }, []);

  function handleCreated(goal: LearningGoal) {
    setGoals((currentGoals) => [goal, ...currentGoals]);
  }

  return (
    <div className="mx-auto grid max-w-7xl gap-6 lg:grid-cols-[minmax(0,1fr)_380px]">
      <section className="space-y-4">
        <div>
          <p className="text-sm font-semibold text-sky-700">Learning Goals</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-950">
            Учебные цели
          </h2>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Центральная сущность обучения: цели позже будут связываться с
            roadmap, задачами, материалами и темами.
          </p>
        </div>

        {error ? <Alert message={error} /> : null}

        {isLoading ? (
          <Card>
            <Spinner label="Загружаем цели" />
          </Card>
        ) : goals.length === 0 ? (
          <Card>
            <h3 className="text-lg font-semibold text-slate-950">
              Целей пока нет
            </h3>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Создайте первую цель, чтобы подготовить основу для roadmap и задач.
            </p>
          </Card>
        ) : (
          <div className="grid gap-4">
            {goals.map((goal) => (
              <GoalCard goal={goal} key={goal.id} />
            ))}
          </div>
        )}
      </section>

      <aside>
        <Card className="sticky top-6">
          <h3 className="text-lg font-semibold text-slate-950">Новая цель</h3>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            После создания откройте цель, чтобы сгенерировать roadmap и задачи.
          </p>
          <div className="mt-5">
            <GoalForm onCreated={handleCreated} />
          </div>
        </Card>
      </aside>
    </div>
  );
}
