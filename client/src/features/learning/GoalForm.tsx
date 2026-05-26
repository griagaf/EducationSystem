import { useState, type FormEvent } from 'react';

import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { getApiErrorMessage } from '../../services/apiClient';
import { learningService } from '../../services/learningService';
import type {
  LearningGoal,
  LearningGoalCreateRequest,
  LearningGoalType,
} from '../../types/learning';
import { learningGoalTypeLabels } from './learningLabels';

const goalTypes = Object.keys(learningGoalTypeLabels) as LearningGoalType[];

type GoalFormProps = {
  onCreated: (goal: LearningGoal) => void;
};

export function GoalForm({ onCreated }: GoalFormProps) {
  const [form, setForm] = useState<LearningGoalCreateRequest>({
    title: '',
    description: '',
    type: 'TECHNOLOGY_LEARNING',
    targetDate: '',
    durationWeeks: 12,
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const createdGoal = await learningService.createGoal({
        ...form,
        targetDate: form.targetDate || null,
        durationWeeks: form.durationWeeks ? Number(form.durationWeeks) : null,
      });
      onCreated(createdGoal);
      setForm({
        title: '',
        description: '',
        type: 'TECHNOLOGY_LEARNING',
        targetDate: '',
        durationWeeks: 12,
      });
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit}>
      {error ? <Alert message={error} /> : null}
      <Input
        label="Название цели"
        name="title"
        onChange={(event) => setForm({ ...form, title: event.target.value })}
        placeholder="Изучить Java Spring Boot"
        required
        value={form.title}
      />
      <Textarea
        label="Описание"
        name="description"
        onChange={(event) => setForm({ ...form, description: event.target.value })}
        placeholder="Что именно нужно изучить и зачем"
        required
        value={form.description}
      />
      <label className="block">
        <span className="text-sm font-medium text-slate-700">Тип цели</span>
        <select
          className="mt-2 h-11 w-full rounded-lg border border-slate-300 bg-white px-3 text-sm text-slate-950 outline-none transition focus:border-sky-600 focus:ring-4 focus:ring-sky-100"
          onChange={(event) =>
            setForm({ ...form, type: event.target.value as LearningGoalType })
          }
          value={form.type}
        >
          {goalTypes.map((type) => (
            <option key={type} value={type}>
              {learningGoalTypeLabels[type]}
            </option>
          ))}
        </select>
      </label>
      <div className="grid gap-4 sm:grid-cols-2">
        <Input
          label="Целевая дата"
          name="targetDate"
          onChange={(event) => setForm({ ...form, targetDate: event.target.value })}
          type="date"
          value={form.targetDate ?? ''}
        />
        <Input
          label="Длительность, недель"
          max={52}
          min={1}
          name="durationWeeks"
          onChange={(event) =>
            setForm({ ...form, durationWeeks: Number(event.target.value) })
          }
          type="number"
          value={form.durationWeeks ?? ''}
        />
      </div>
      <Button className="w-full" disabled={isSubmitting} type="submit">
        {isSubmitting ? 'Создаем...' : 'Создать цель'}
      </Button>
    </form>
  );
}
