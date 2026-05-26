import { useEffect, useMemo, useState } from 'react';

import { Alert } from '../../components/ui/Alert';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Spinner } from '../../components/ui/Spinner';
import { getApiErrorMessage } from '../../services/apiClient';
import { flashcardService } from '../../services/flashcardService';
import type { Flashcard, FlashcardReviewResult, Topic } from '../../types/learning';
import { cn } from '../../utils/cn';
import { difficultyLabels, flashcardReviewLabels } from './flashcardLabels';

type FlashcardPanelProps = {
  topic: Topic;
  onClose: () => void;
  onMasteryScoreChange: (topicId: string, masteryScore: number) => void;
};

export function FlashcardPanel({
  topic,
  onClose,
  onMasteryScoreChange,
}: FlashcardPanelProps) {
  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [reviewedCardIds, setReviewedCardIds] = useState<Set<string>>(new Set());
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isAnswerVisible, setIsAnswerVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [reviewingResult, setReviewingResult] = useState<FlashcardReviewResult | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let isMounted = true;

    async function loadFlashcards() {
      setIsLoading(true);
      setError('');
      setReviewedCardIds(new Set());
      setCurrentIndex(0);
      setIsAnswerVisible(false);

      try {
        const loadedFlashcards = await flashcardService.getByTopic(topic.id);
        if (isMounted) {
          setFlashcards(loadedFlashcards);
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

    loadFlashcards();

    return () => {
      isMounted = false;
    };
  }, [topic.id]);

  const currentFlashcard = flashcards[currentIndex] ?? null;
  const reviewedCount = reviewedCardIds.size;
  const progressPercent = flashcards.length
    ? Math.round((reviewedCount / flashcards.length) * 100)
    : 0;
  const isSessionComplete = flashcards.length > 0 && reviewedCount === flashcards.length;

  const sortedFlashcards = useMemo(() => flashcards, [flashcards]);

  async function handleGenerate() {
    setIsGenerating(true);
    setError('');

    try {
      await flashcardService.generate(topic.id, { count: 5 });
      const refreshedFlashcards = await flashcardService.getByTopic(topic.id);
      setFlashcards(refreshedFlashcards);
      setReviewedCardIds(new Set());
      setCurrentIndex(0);
      setIsAnswerVisible(false);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setIsGenerating(false);
    }
  }

  async function handleReview(result: FlashcardReviewResult) {
    if (!currentFlashcard) {
      return;
    }

    setReviewingResult(result);
    setError('');

    try {
      const reviewResponse = await flashcardService.review(currentFlashcard.id, result);
      onMasteryScoreChange(topic.id, reviewResponse.topicMasteryScore);

      const nextReviewedCardIds = new Set(reviewedCardIds);
      nextReviewedCardIds.add(currentFlashcard.id);
      setReviewedCardIds(nextReviewedCardIds);
      setIsAnswerVisible(false);

      const nextIndex = flashcards.findIndex(
        (flashcard, index) => index > currentIndex && !nextReviewedCardIds.has(flashcard.id),
      );
      if (nextIndex >= 0) {
        setCurrentIndex(nextIndex);
        return;
      }

      const firstUnreviewedIndex = flashcards.findIndex(
        (flashcard) => !nextReviewedCardIds.has(flashcard.id),
      );
      if (firstUnreviewedIndex >= 0) {
        setCurrentIndex(firstUnreviewedIndex);
      }
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setReviewingResult(null);
    }
  }

  function handleRestartSession() {
    setReviewedCardIds(new Set());
    setCurrentIndex(0);
    setIsAnswerVisible(false);
  }

  return (
    <div className="fixed inset-0 z-40 bg-slate-950/30 px-4 py-6 backdrop-blur-sm">
      <div className="mx-auto flex max-h-[calc(100vh-3rem)] max-w-4xl flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-xl">
        <div className="flex flex-wrap items-start justify-between gap-4 border-b border-slate-200 px-5 py-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <Badge tone="blue">Карточки</Badge>
              <Badge tone={topic.masteryScore >= 70 ? 'green' : 'neutral'}>
                Mastery {topic.masteryScore}%
              </Badge>
              <Badge tone="slate">{difficultyLabels[topic.difficultyLevel]}</Badge>
            </div>
            <h3 className="mt-3 text-xl font-semibold text-slate-950">{topic.title}</h3>
          </div>
          <Button onClick={onClose} variant="ghost">
            Закрыть
          </Button>
        </div>

        <div className="overflow-y-auto px-5 py-5">
          {error ? (
            <div className="mb-4">
              <Alert message={error} />
            </div>
          ) : null}

          {isLoading ? (
            <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-8">
              <Spinner label="Загружаем карточки" />
            </div>
          ) : null}

          {!isLoading && flashcards.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50 px-4 py-6">
              <div className="text-sm font-semibold text-slate-950">Карточек пока нет</div>
              <div className="mt-4">
                <Button disabled={isGenerating} onClick={handleGenerate}>
                  {isGenerating ? 'Генерируем...' : 'Сгенерировать карточки'}
                </Button>
              </div>
            </div>
          ) : null}

          {!isLoading && flashcards.length > 0 ? (
            <div className="space-y-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="text-sm font-semibold text-slate-950">
                  {isSessionComplete
                    ? 'Сессия завершена'
                    : `Карточка ${currentIndex + 1} из ${flashcards.length}`}
                </div>
                <Button disabled={isGenerating} onClick={handleGenerate} variant="secondary">
                  {isGenerating ? 'Генерируем...' : 'Сгенерировать еще'}
                </Button>
              </div>

              <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full bg-sky-700 transition-all"
                  style={{ width: `${progressPercent}%` }}
                />
              </div>

              {isSessionComplete ? (
                <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-5">
                  <div className="text-sm font-semibold text-emerald-800">
                    Все карточки в этой сессии пройдены.
                  </div>
                  <div className="mt-4">
                    <Button onClick={handleRestartSession} variant="secondary">
                      Пройти снова
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-5">
                  <div className="flex flex-wrap items-center gap-2">
                    {currentFlashcard ? (
                      <Badge tone="slate">{difficultyLabels[currentFlashcard.difficulty]}</Badge>
                    ) : null}
                  </div>

                  <div className="mt-4 text-lg font-semibold leading-7 text-slate-950">
                    {currentFlashcard?.question}
                  </div>

                  {isAnswerVisible ? (
                    <div className="mt-5 rounded-lg border border-slate-200 bg-white px-4 py-4 text-sm leading-6 text-slate-700">
                      {currentFlashcard?.answer}
                    </div>
                  ) : null}

                  <div className="mt-5 flex flex-wrap gap-3">
                    {!isAnswerVisible ? (
                      <Button onClick={() => setIsAnswerVisible(true)}>
                        Показать ответ
                      </Button>
                    ) : (
                      (Object.keys(flashcardReviewLabels) as FlashcardReviewResult[]).map(
                        (result) => (
                          <Button
                            className="min-w-28"
                            disabled={reviewingResult !== null}
                            key={result}
                            onClick={() => handleReview(result)}
                            variant={result === 'KNOW' ? 'primary' : 'secondary'}
                          >
                            {reviewingResult === result
                              ? 'Сохраняем...'
                              : flashcardReviewLabels[result]}
                          </Button>
                        ),
                      )
                    )}
                  </div>
                </div>
              )}

              <div className="grid gap-2">
                {sortedFlashcards.map((flashcard, index) => {
                  const isActive = index === currentIndex && !isSessionComplete;
                  const isReviewed = reviewedCardIds.has(flashcard.id);

                  return (
                    <button
                      className={cn(
                        'flex min-h-12 items-center justify-between gap-3 rounded-lg border px-3 py-2 text-left text-sm transition',
                        isActive
                          ? 'border-sky-300 bg-sky-50 text-sky-900'
                          : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50',
                      )}
                      key={flashcard.id}
                      onClick={() => {
                        setCurrentIndex(index);
                        setIsAnswerVisible(false);
                      }}
                      type="button"
                    >
                      <span className="min-w-0 truncate">{flashcard.question}</span>
                      <Badge tone={isReviewed ? 'green' : 'slate'}>
                        {isReviewed ? 'Review' : `${index + 1}`}
                      </Badge>
                    </button>
                  );
                })}
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
