# Flashcards

## 1. Назначение

Flashcards используются для самопроверки и регулярной оценки освоения topics.

Карточки не являются отдельной учебной системой. Они связаны с `LearningGoal` и `Topic`.

## 2. Сущности

### Flashcard

Поля:

- `id`;
- `userId`;
- `learningGoalId`;
- `topicId`;
- `question`;
- `answer`;
- `difficulty`;
- `createdAt`;
- `updatedAt`.

Difficulty:

- `EASY`;
- `MEDIUM`;
- `HARD`.

### FlashcardReview

Поля:

- `id`;
- `userId`;
- `flashcardId`;
- `result`;
- `reviewedAt`.

Review result:

- `KNOW`;
- `PARTIAL`;
- `DONT_KNOW`.

## 3. Генерация карточек

Поток:

1. Пользователь выбирает цель или topics.
2. Backend проверяет принадлежность данных пользователю.
3. Backend отправляет выбранную тему в AI-service. Для нескольких topics backend выполняет отдельный вызов по каждой теме.
4. AI-service вызывает `POST /api/ai/generate-flashcards`.
5. AI-service возвращает structured JSON.
6. Backend валидирует ответ.
7. Backend сохраняет flashcards.

AI-service не хранит карточки и не обращается к базе.

## 4. Review Flow

1. Пользователь открывает карточку.
2. Пользователь отвечает самостоятельно.
3. Пользователь выбирает результат: `KNOW`, `PARTIAL`, `DONT_KNOW`.
4. Backend сохраняет `FlashcardReview`.
5. Backend обновляет `Topic.mastery_score`.

## 5. Mastery Update

Базовая explainable logic:

- `KNOW`: увеличить mastery score;
- `PARTIAL`: небольшое увеличение или сохранение;
- `DONT_KNOW`: небольшое снижение или сохранение;
- score ограничивается диапазоном 0-100.

Конкретные коэффициенты должны быть заданы в backend service и покрыты тестами.

## 6. API

Backend endpoints:

- `POST /api/v1/topics/{id}/generate-flashcards`;
- `GET /api/v1/topics/{id}/flashcards`;
- `GET /api/v1/flashcards/{id}`;
- `POST /api/v1/flashcards/{id}/review`.

AI-service endpoint:

- `POST /api/ai/generate-flashcards`.

Request AI-service:

```json
{
  "goal_title": "Изучить Java Spring Boot",
  "topic_title": "Spring IoC",
  "topic_description": "Dependency injection basics",
  "difficulty_level": "MEDIUM",
  "count": 5
}
```

Response AI-service:

```json
{
  "flashcards": [
    {
      "question": "What is dependency injection?",
      "answer": "A pattern where dependencies are provided from outside the object.",
      "difficulty": "MEDIUM"
    }
  ]
}
```

## 7. Ограничения

Не реализуется:

- планировщик интервальных повторений;
- адаптивный AI-наставник;
- ML-оценивание ответов;
- совместное прохождение карточек в реальном времени;
- проверка голосовых ответов.


