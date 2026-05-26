# Learning Model

## 1. Назначение

Learning model описывает, как платформа представляет обучение пользователя: цели, материалы, темы, roadmap, задачи, карточки и уровень освоения.

Центральная сущность системы - `LearningGoal`. Все остальные учебные сущности связаны с целью напрямую или через topics.

Экзаменационная подготовка не является отдельной подсистемой. Это один из типов учебной цели.

## 2. LearningGoal Types

`LearningGoal.type` определяет контекст обучения:

- `SELF_STUDY` - самостоятельное изучение темы;
- `EXAM_PREPARATION` - подготовка к экзамену;
- `INTERVIEW_PREPARATION` - подготовка к собеседованию;
- `TECHNOLOGY_LEARNING` - изучение технологии или инструмента.

Тип цели влияет на prompt для AI-service, формулировку roadmap и характер задач, но не создает отдельную архитектуру.

## 3. Основные сущности

```text
LearningGoal
  ├── StudyMaterial
  ├── Topic
  ├── Roadmap
  │   └── RoadmapStep
  ├── Task
  ├── Flashcard
  │   └── FlashcardReview
  └── KnowledgeGraph
      ├── KnowledgeNode
      └── KnowledgeEdge
```

## 4. Study Materials

`StudyMaterial` хранит пользовательские материалы, связанные с целью.

Поддерживаемые форматы текущей реализации:

- TXT.

Ограничения:

- PDF и DOCX запланированы для следующих версий;
- OCR не поддерживается;
- изображения не поддерживаются;
- extraction может быть упрощенным;
- AI-service не хранит материалы и не имеет доступа к файлам.

## 5. Topic

`Topic` - ключевая учебная сущность.

Topic содержит:

- `title`;
- `description`;
- `mastery_score`;
- `difficulty_level`.

Roadmap строится вокруг topics. Tasks, flashcards и knowledge graph также связываются с topics.

## 6. Mastery Score

`mastery_score` показывает уровень освоения темы от 0 до 100.

Источники изменения score:

- выполнение задач;
- прохождение flashcards;
- завершение roadmap steps.

Логика должна быть простой и объяснимой. Сложная ML-модель оценки знаний не используется.

Базовые правила:

- `Task DONE` по topic повышает score;
- `FlashcardReview KNOW` повышает score;
- `FlashcardReview PARTIAL` слегка повышает или сохраняет score;
- `FlashcardReview DONT_KNOW` снижает или не повышает score;
- completion roadmap step повышает score по связанному topic.

Score всегда ограничивается диапазоном 0-100.

## 7. Roadmap

Roadmap остается планом движения к цели, но теперь каждый roadmap step может быть связан с topic.

Roadmap generation использует:

- данные `LearningGoal`;
- extracted topics;
- summary пользовательских материалов;
- user level.

## 8. Progress Tracking

Progress tracking состоит из двух уровней:

- goal progress - общий прогресс цели по задачам и roadmap;
- topic mastery - уровень освоения отдельных тем.

Dashboard должен показывать оба уровня без смешивания источников истины:

- progressPercent хранится на goal;
- masteryScore хранится на topic.

## 9. Границы первой стабильной версии

Входит:

- типы целей;
- материалы;
- topics;
- roadmap на основе topics;
- flashcards;
- review history;
- explainable mastery score;
- минимальный knowledge graph.

Запланировано позже:

- embeddings;
- семантический поиск;
- адаптивный AI-наставник;
- сложная рекомендательная модель;
- векторная БД.


