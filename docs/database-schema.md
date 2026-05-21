# Database Schema

## 1. Общие принципы

База данных: PostgreSQL.

Основные правила схемы:

- primary key для основных таблиц - UUID;
- все пользовательские данные связаны с `users.id`;
- даты создания и обновления хранятся в `created_at` и `updated_at`;
- бизнес-статусы хранятся как enum-like string values;
- entity не удаляются каскадно без явной необходимости;
- связи проектируются так, чтобы backend мог проверять принадлежность данных пользователю.

## 1.1. Стратегия UUID

- Все основные сущности используют UUID как primary key.
- UUID генерируется backend-приложением до сохранения entity.
- Внешние API принимают и возвращают UUID в строковом формате.
- Последовательные числовые id не используются, чтобы не раскрывать количество записей и порядок создания.

## 1.2. Стратегия audit fields

Для изменяемых сущностей используются:

- `created_at` - дата создания записи;
- `updated_at` - дата последнего изменения.

Правила:

- `created_at` устанавливается один раз при создании;
- `updated_at` обновляется backend-приложением при каждом изменении;
- для append-only сущностей, например `flashcard_reviews` и `ai_request_logs`, достаточно `created_at` или доменного timestamp вроде `reviewed_at`.

## 1.3. Стратегия nullable fields

- Обязательные бизнес-поля помечаются как not null.
- Nullable допускается только для опциональных связей и данных, которые появляются после обработки.
- Примеры допустимого nullable:
  - `notes.learning_goal_id`;
  - `topics.study_material_id`;
  - `roadmap_steps.topic_id`;
  - `tasks.roadmap_step_id`;
  - `tasks.topic_id`;
  - `study_materials.extracted_text`.

## 1.4. Стратегия каскадных операций

Каскадные удаления используются осторожно.

Рекомендуемая стратегия:

- удаление пользователя не является пользовательской функцией;
- цель лучше архивировать через `learning_goals.status = ARCHIVED`;
- удаление дочерних сущностей выполняется явно в слое сервисов;
- database cascade допустим только для строго зависимых технических записей после отдельного архитектурного решения.

Такой подход снижает риск случайной потери roadmap, topics, flashcards и истории review.

## 1.5. Стратегия индексов

Индексы добавляются для:

- запросов с проверкой пользователя по `user_id`;
- списков по `learning_goal_id`;
- фильтрации по status/type;
- сортировки по `created_at`, `updated_at`, `due_date`;
- graph traversal по `source_node_id` и `target_node_id`.

Unique constraints используются для:

- `users.email`;
- `user_profiles.user_id`;
- `roadmaps.learning_goal_id`;
- `roadmap_steps(roadmap_id, order_index)`.

## 2. Список таблиц

Таблицы:

1. `users`;
2. `user_profiles`;
3. `notes`;
4. `learning_goals`;
5. `study_materials`;
6. `topics`;
7. `roadmaps`;
8. `roadmap_steps`;
9. `tasks`;
10. `flashcards`;
11. `flashcard_reviews`;
12. `knowledge_nodes`;
13. `knowledge_edges`;
14. `ai_request_logs`.

## 3. Основные связи

```text
users 1:N notes
users 1:N learning_goals
learning_goals 1:N study_materials
learning_goals 1:N topics
learning_goals 1:1 roadmaps
roadmaps 1:N roadmap_steps
roadmap_steps 1:N tasks
topics 1:N tasks
topics 1:N flashcards
flashcards 1:N flashcard_reviews
learning_goals 1:N knowledge_nodes
knowledge_nodes 1:N knowledge_edges
users 1:N tasks
users 1:N ai_request_logs
users 1:1 user_profiles
```

Дополнительная полезная связь:

```text
learning_goals 1:N tasks
```

Она нужна для быстрого получения задач по учебной цели и расчета прогресса.

## 4. Статусы

### LearningGoal status

Допустимые значения:

- `ACTIVE`;
- `COMPLETED`;
- `ARCHIVED`.

### LearningGoal type

Допустимые значения:

- `SELF_STUDY`;
- `EXAM_PREPARATION`;
- `INTERVIEW_PREPARATION`;
- `TECHNOLOGY_LEARNING`.

### RoadmapStep status

Допустимые значения:

- `NOT_STARTED`;
- `IN_PROGRESS`;
- `COMPLETED`.

### Task status

Допустимые значения:

- `TODO`;
- `IN_PROGRESS`;
- `DONE`;
- `CANCELLED`.

### Task priority

Допустимые значения:

- `LOW`;
- `MEDIUM`;
- `HIGH`.

### StudyMaterial processing status

Допустимые значения:

- `UPLOADED`;
- `TEXT_EXTRACTED`;
- `TOPICS_EXTRACTED`;
- `FAILED`.

### Difficulty level

Используется для topics и flashcards:

- `EASY`;
- `MEDIUM`;
- `HARD`.

### Flashcard review result

Допустимые значения:

- `KNOW`;
- `PARTIAL`;
- `DONT_KNOW`.

## 5. Таблица users

### Назначение

Хранит учетные записи пользователей.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `email` | `varchar(255)` | да | Email пользователя |
| `password_hash` | `varchar(255)` | да | Хэш пароля |
| `role` | `varchar(50)` | да | Роль пользователя, по умолчанию `USER` |
| `is_enabled` | `boolean` | да | Активен ли аккаунт |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `users 1:1 user_profiles`;
- `users 1:N notes`;
- `users 1:N learning_goals`;
- `users 1:N tasks`;
- `users 1:N ai_request_logs`.

### Ограничения

- `id` - primary key;
- `email` - unique;
- `email` - not null;
- `password_hash` - not null;
- `role` - not null;
- `is_enabled` - not null;
- `created_at` - not null;
- `updated_at` - not null.

### Индексы

- unique index по `email`;
- index по `is_enabled`, если нужны выборки активных пользователей.

## 6. Таблица user_profiles

### Назначение

Хранит профильные данные пользователя отдельно от учетных данных.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Ссылка на пользователя |
| `display_name` | `varchar(150)` | да | Отображаемое имя |
| `bio` | `text` | нет | Краткое описание пользователя |
| `timezone` | `varchar(100)` | нет | Часовой пояс |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `user_profiles.user_id -> users.id`;
- связь `users 1:1 user_profiles`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key;
- `user_id` - unique;
- `display_name` - not null;
- `created_at` - not null;
- `updated_at` - not null.

### Индексы

- unique index по `user_id`.

## 7. Таблица notes

### Назначение

Хранит личные заметки пользователя.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец заметки |
| `learning_goal_id` | `uuid` | нет | Связанная учебная цель |
| `title` | `varchar(255)` | да | Название заметки |
| `content` | `text` | да | Содержимое заметки |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `notes.user_id -> users.id`;
- `notes.learning_goal_id -> learning_goals.id`;
- связь `users 1:N notes`;
- опциональная связь `learning_goals 1:N notes`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, nullable;
- `title` - not null;
- `content` - not null;
- `created_at` - not null;
- `updated_at` - not null.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `user_id, updated_at`;
- index по `user_id, created_at`.

## 8. Таблица learning_goals

### Назначение

Хранит учебные цели пользователя.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец цели |
| `title` | `varchar(255)` | да | Название цели |
| `description` | `text` | да | Описание цели |
| `type` | `varchar(50)` | да | Тип цели |
| `status` | `varchar(50)` | да | Статус цели |
| `target_date` | `date` | нет | Желаемая дата завершения |
| `duration_weeks` | `integer` | нет | Планируемая длительность в неделях |
| `estimated_duration` | `varchar(100)` | нет | Человекочитаемая оценка длительности |
| `progress_percent` | `integer` | да | Прогресс от 0 до 100 |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `learning_goals.user_id -> users.id`;
- связь `users 1:N learning_goals`;
- связь `learning_goals 1:N study_materials`;
- связь `learning_goals 1:N topics`;
- связь `learning_goals 1:1 roadmaps`;
- связь `learning_goals 1:N flashcards`;
- связь `learning_goals 1:N knowledge_nodes`;
- связь `learning_goals 1:N tasks`;
- опциональная связь `learning_goals 1:N notes`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `title` - not null;
- `description` - not null;
- `type` - not null;
- `type` должен быть одним из `SELF_STUDY`, `EXAM_PREPARATION`, `INTERVIEW_PREPARATION`, `TECHNOLOGY_LEARNING`;
- `status` - not null;
- `status` должен быть одним из `ACTIVE`, `COMPLETED`, `ARCHIVED`;
- `duration_weeks` должен быть null или больше 0;
- `progress_percent` - not null;
- `progress_percent` должен быть от 0 до 100;
- `created_at` - not null;
- `updated_at` - not null.

### Индексы

- index по `user_id`;
- index по `user_id, status`;
- index по `user_id, type`;
- index по `user_id, created_at`;
- index по `target_date`.

## 9. Таблица study_materials

### Назначение

Хранит metadata файла и извлеченный текст пользовательских материалов, связанных с учебной целью.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец материала |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `file_name` | `varchar(255)` | да | Исходное имя файла |
| `content_type` | `varchar(100)` | да | MIME type или тип файла |
| `file_size` | `bigint` | да | Размер файла в байтах |
| `storage_key` | `varchar(500)` | да | Путь или ключ хранения файла |
| `extracted_text` | `text` | нет | Упрощенно извлеченный текст |
| `processing_status` | `varchar(50)` | да | Статус обработки |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `study_materials.user_id -> users.id`;
- `study_materials.learning_goal_id -> learning_goals.id`;
- связь `learning_goals 1:N study_materials`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, not null;
- `file_name` - not null;
- `content_type` - not null;
- `file_size` - not null, больше 0;
- `storage_key` - not null;
- `processing_status` должен быть одним из `UPLOADED`, `TEXT_EXTRACTED`, `TOPICS_EXTRACTED`, `FAILED`;
- поддерживаемые форматы первой версии: PDF, DOCX, TXT;
- OCR и изображения не поддерживаются.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `user_id, created_at`;
- index по `processing_status`.

### Жизненный цикл файла

1. Backend сохраняет файл в локальное хранилище.
2. Backend создает запись `study_materials` со статусом `UPLOADED`.
3. После успешного извлечения текста backend обновляет `extracted_text` и статус `TEXT_EXTRACTED`.
4. После выделения topics backend обновляет статус `TOPICS_EXTRACTED`.
5. При ошибке обработки backend сохраняет статус `FAILED` и пишет техническую причину в логи.

### Очистка файлов

- Файл не хранится в PostgreSQL, в таблице хранится только `storage_key`.
- Удаление материала должно проходить через backend service с проверкой пользователя.
- Если запись удалена, но файл остался на диске, он считается потерянным файлом и подлежит отдельной процедуре очистки.
- Автоматическое каскадное удаление файлов на уровне БД невозможно и не используется.

## 10. Таблица topics

### Назначение

Хранит темы обучения. Topic является ключевой сущностью персонализации: roadmap, tasks и flashcards связываются с темами.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец темы |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `study_material_id` | `uuid` | нет | Материал-источник |
| `title` | `varchar(255)` | да | Название темы |
| `description` | `text` | нет | Описание темы |
| `mastery_score` | `integer` | да | Уровень освоения 0-100 |
| `difficulty_level` | `varchar(50)` | да | Сложность темы |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `topics.user_id -> users.id`;
- `topics.learning_goal_id -> learning_goals.id`;
- `topics.study_material_id -> study_materials.id`;
- связь `learning_goals 1:N topics`;
- связь `study_materials 1:N topics`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, not null;
- `title` - not null;
- `mastery_score` должен быть от 0 до 100;
- `difficulty_level` должен быть одним из `EASY`, `MEDIUM`, `HARD`.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `study_material_id`;
- index по `learning_goal_id, mastery_score`;
- index по `learning_goal_id, difficulty_level`.

## 11. Таблица roadmaps

### Назначение

Хранит roadmap, сгенерированный для учебной цели.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `title` | `varchar(255)` | да | Название roadmap |
| `description` | `text` | да | Краткое описание roadmap |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `roadmaps.learning_goal_id -> learning_goals.id`;
- связь `learning_goals 1:1 roadmaps`;
- связь `roadmaps 1:N roadmap_steps`.

### Ограничения

- `id` - primary key;
- `learning_goal_id` - foreign key, not null;
- `learning_goal_id` - unique для связи 1:1;
- `title` - not null;
- `description` - not null;
- `created_at` - not null;
- `updated_at` - not null.

### Индексы

- unique index по `learning_goal_id`;
- index по `created_at`.

## 12. Таблица roadmap_steps

### Назначение

Хранит этапы roadmap.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `roadmap_id` | `uuid` | да | Roadmap |
| `topic_id` | `uuid` | нет | Связанная тема |
| `order_index` | `integer` | да | Порядковый номер этапа |
| `title` | `varchar(255)` | да | Название этапа |
| `description` | `text` | да | Описание этапа |
| `status` | `varchar(50)` | да | Статус этапа |
| `estimated_days` | `integer` | да | Оценка длительности этапа в днях |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `roadmap_steps.roadmap_id -> roadmaps.id`;
- `roadmap_steps.topic_id -> topics.id`;
- связь `roadmaps 1:N roadmap_steps`;
- связь `roadmap_steps 1:N tasks`;

### Ограничения

- `id` - primary key;
- `roadmap_id` - foreign key, not null;
- `topic_id` - foreign key, nullable;
- `order_index` - not null;
- `order_index` должен быть больше 0;
- `title` - not null;
- `description` - not null;
- `status` - not null;
- `status` должен быть одним из `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`;
- `estimated_days` должен быть больше 0;
- `created_at` - not null;
- `updated_at` - not null;
- `(roadmap_id, order_index)` - unique.

### Индексы

- index по `roadmap_id`;
- index по `topic_id`;
- unique index по `roadmap_id, order_index`;
- index по `roadmap_id, status`.

## 13. Таблица tasks

### Назначение

Хранит задачи пользователя.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец задачи |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `roadmap_step_id` | `uuid` | нет | Этап roadmap |
| `topic_id` | `uuid` | нет | Связанная тема |
| `title` | `varchar(255)` | да | Название задачи |
| `description` | `text` | нет | Описание задачи |
| `status` | `varchar(50)` | да | Статус задачи |
| `priority` | `varchar(50)` | да | Приоритет задачи |
| `due_date` | `date` | нет | Дедлайн |
| `completed_at` | `timestamp with time zone` | нет | Дата выполнения |
| `order_index` | `integer` | нет | Порядок отображения |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `tasks.user_id -> users.id`;
- `tasks.learning_goal_id -> learning_goals.id`;
- `tasks.roadmap_step_id -> roadmap_steps.id`;
- `tasks.topic_id -> topics.id`;
- связь `users 1:N tasks`;
- связь `learning_goals 1:N tasks`;
- связь `roadmap_steps 1:N tasks`.
- связь `topics 1:N tasks`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, not null;
- `roadmap_step_id` - foreign key, nullable;
- `topic_id` - foreign key, nullable;
- `title` - not null;
- `status` - not null;
- `status` должен быть одним из `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`;
- `priority` - not null;
- `priority` должен быть одним из `LOW`, `MEDIUM`, `HIGH`;
- `order_index` должен быть null или больше 0;
- `created_at` - not null;
- `updated_at` - not null;

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `roadmap_step_id`;
- index по `topic_id`;
- index по `user_id, status`;
- index по `learning_goal_id, status`;
- index по `user_id, due_date`;
- index по `user_id, priority`;
- index по `roadmap_step_id, order_index`.

## 14. Таблица flashcards

### Назначение

Хранит карточки для самопроверки, связанные с темами и учебными целями.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец карточки |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `topic_id` | `uuid` | да | Тема |
| `question` | `text` | да | Вопрос |
| `answer` | `text` | да | Ответ |
| `difficulty` | `varchar(50)` | да | Сложность |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `flashcards.user_id -> users.id`;
- `flashcards.learning_goal_id -> learning_goals.id`;
- `flashcards.topic_id -> topics.id`;
- связь `topics 1:N flashcards`;
- связь `flashcards 1:N flashcard_reviews`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, not null;
- `topic_id` - foreign key, not null;
- `question` - not null;
- `answer` - not null;
- `difficulty` должен быть одним из `EASY`, `MEDIUM`, `HARD`.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `topic_id`;
- index по `topic_id, difficulty`.

## 15. Таблица flashcard_reviews

### Назначение

Хранит историю прохождения карточек пользователем.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Пользователь |
| `flashcard_id` | `uuid` | да | Карточка |
| `result` | `varchar(50)` | да | Результат review |
| `reviewed_at` | `timestamp with time zone` | да | Дата review |

### Связи

- `flashcard_reviews.user_id -> users.id`;
- `flashcard_reviews.flashcard_id -> flashcards.id`;
- связь `flashcards 1:N flashcard_reviews`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `flashcard_id` - foreign key, not null;
- `result` должен быть одним из `KNOW`, `PARTIAL`, `DONT_KNOW`;
- `reviewed_at` - not null.

### Индексы

- index по `user_id`;
- index по `flashcard_id`;
- index по `user_id, reviewed_at`.

## 16. Таблица knowledge_nodes

### Назначение

Хранит visualization-ready узлы knowledge graph.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `node_type` | `varchar(50)` | да | Тип узла |
| `source_type` | `varchar(50)` | да | Источник узла |
| `source_id` | `uuid` | нет | Id исходной сущности |
| `title` | `varchar(255)` | да | Название |
| `description` | `text` | нет | Описание |
| `mastery_score` | `integer` | нет | Уровень освоения, если применимо |
| `created_at` | `timestamp with time zone` | да | Дата создания |
| `updated_at` | `timestamp with time zone` | да | Дата обновления |

### Связи

- `knowledge_nodes.user_id -> users.id`;
- `knowledge_nodes.learning_goal_id -> learning_goals.id`;
- связь `learning_goals 1:N knowledge_nodes`.

### Ограничения

- `node_type` должен быть одним из `GOAL`, `TOPIC`, `ROADMAP_STEP`, `TASK`;
- `source_type` должен быть одним из `LEARNING_GOAL`, `TOPIC`, `ROADMAP_STEP`, `TASK`;
- `mastery_score` должен быть null или от 0 до 100.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `node_type`;
- index по `source_type, source_id`.

## 17. Таблица knowledge_edges

### Назначение

Хранит связи между узлами knowledge graph.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Владелец |
| `learning_goal_id` | `uuid` | да | Учебная цель |
| `source_node_id` | `uuid` | да | Исходный узел |
| `target_node_id` | `uuid` | да | Целевой узел |
| `edge_type` | `varchar(50)` | да | Тип связи |
| `weight` | `numeric(5,2)` | нет | Вес связи |
| `created_at` | `timestamp with time zone` | да | Дата создания |

### Связи

- `knowledge_edges.user_id -> users.id`;
- `knowledge_edges.learning_goal_id -> learning_goals.id`;
- `knowledge_edges.source_node_id -> knowledge_nodes.id`;
- `knowledge_edges.target_node_id -> knowledge_nodes.id`.

### Ограничения

- `edge_type` должен быть одним из `CONTAINS`, `PREREQUISITE_OF`, `RELATED_TO`, `GENERATED_TASK`, `COVERS_TOPIC`;
- `weight` должен быть null или больше 0.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `source_node_id`;
- index по `target_node_id`;
- index по `edge_type`.

## 18. Таблица ai_request_logs

### Назначение

Хранит технический журнал AI-запросов пользователя.

Эта таблица нужна для:

- диагностики ошибок AI-service;
- анализа количества AI-запросов;
- отображения истории генераций в будущих версиях;
- диагностики текущего релиза.

В текущем релизе таблица не является источником бизнес-данных. Roadmap и задачи хранятся отдельно.

### Поля

| Поле | Тип | Обязательное | Описание |
|---|---|---:|---|
| `id` | `uuid` | да | Primary key |
| `user_id` | `uuid` | да | Пользователь, инициировавший запрос |
| `learning_goal_id` | `uuid` | нет | Цель, для которой выполнялся запрос |
| `request_type` | `varchar(100)` | да | Тип AI-запроса |
| `provider` | `varchar(100)` | нет | AI-провайдер |
| `model` | `varchar(100)` | нет | AI-модель |
| `status` | `varchar(50)` | да | Статус запроса |
| `prompt_preview` | `text` | нет | Сокращенная версия prompt |
| `response_preview` | `text` | нет | Сокращенная версия ответа |
| `error_message` | `text` | нет | Сообщение об ошибке |
| `duration_ms` | `integer` | нет | Длительность запроса |
| `created_at` | `timestamp with time zone` | да | Дата создания |

### Связи

- `ai_request_logs.user_id -> users.id`;
- `ai_request_logs.learning_goal_id -> learning_goals.id`;
- связь `users 1:N ai_request_logs`;
- опциональная связь `learning_goals 1:N ai_request_logs`.

### Ограничения

- `id` - primary key;
- `user_id` - foreign key, not null;
- `learning_goal_id` - foreign key, nullable;
- `request_type` - not null;
- `status` - not null;
- `duration_ms` должен быть null или больше либо равен 0;
- `created_at` - not null.

Рекомендуемые значения `request_type`:

- `ROADMAP_GENERATION`.
- `TOPIC_EXTRACTION`;
- `FLASHCARD_GENERATION`.

Рекомендуемые значения `status`:

- `SUCCESS`;
- `FAILED`;
- `GRACEFUL_DEGRADATION_USED`;
- `TIMEOUT`.

### Индексы

- index по `user_id`;
- index по `learning_goal_id`;
- index по `user_id, created_at`;
- index по `request_type`;
- index по `status`.

## 19. Принадлежность данных и безопасность

Backend должен проверять, что пользователь работает только со своими данными.

Прямая принадлежность пользователю:

- `notes.user_id`;
- `learning_goals.user_id`;
- `study_materials.user_id`;
- `topics.user_id`;
- `tasks.user_id`;
- `flashcards.user_id`;
- `flashcard_reviews.user_id`;
- `knowledge_nodes.user_id`;
- `knowledge_edges.user_id`;
- `ai_request_logs.user_id`;
- `user_profiles.user_id`.

Косвенная принадлежность пользователю:

- `roadmaps` через `learning_goals.user_id`;
- `roadmap_steps` через `roadmaps -> learning_goals -> users`.

Для чтения и изменения сущностей backend должен использовать запросы с учетом `user_id` или проверять принадлежность через связанную корневую сущность.

## 20. Удаление данных

Для базовой версии системы применяется простая стратегия:

- удаление пользователя не является пользовательской функцией;
- удаление заметки выполняется физически;
- удаление задачи выполняется физически;
- удаление цели может быть заменено на статус `ARCHIVED`;
- удаление материала должно явно обрабатывать связанные topics и flashcards;
- при удалении roadmap его steps и связанные generated tasks должны обрабатываться явно.

Рекомендуемый подход для целей:

- вместо удаления использовать `learning_goals.status = ARCHIVED`;
- это безопаснее для истории прогресса и связанных данных.

## 21. Будущие расширения

### embeddings

В будущих версиях можно добавить embedding-поля:

- `notes.embedding`;
- `learning_goals.embedding`;
- `study_materials.embedding`;
- `topics.embedding`;
- `roadmap_steps.embedding`;
- `tasks.embedding`.

Эти поля позволят искать похожие заметки, цели и этапы roadmap.

### pgvector

Для семантического поиска можно подключить PostgreSQL extension `pgvector`.

Будущие возможности:

- поиск по векторной близости;
- поиск похожих заметок;
- подбор связанных материалов;
- AI context retrieval для ответов помощника.

### Knowledge graph extensions

`knowledge_nodes` и `knowledge_edges` уже входят в базовую модель как visualization-ready структура. В следующих версиях их можно расширить:

- embedding для узлов;
- дополнительные типы связей;
- graph analytics;
- импорт связей из внешних источников.

### Расширение AI logs

В будущих версиях `ai_request_logs` можно расширить:

- token usage;
- cost;
- raw provider response;
- metadata запроса;
- trace id;
- prompt version;
- response quality score.








