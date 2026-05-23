# AI Personal Learning Platform

## 1. Обзор проекта

AI Personal Learning Platform - веб-платформа для персонального обучения, управления знаниями и отслеживания прогресса.

Система помогает пользователю создать учебную цель, загрузить материалы, выделить темы, получить roadmap, автоматически сформировать задачи, пройти flashcards для самопроверки и отслеживать уровень освоения тем.

Проект ориентирован на первую стабильную версию реальной платформы: архитектура остается практичной, расширяемой и реализуемой одним разработчиком без микросервисной избыточности.

## 2. Цели системы

- хранить пользовательские заметки и учебные цели;
- поддерживать разные типы обучения через единую сущность `LearningGoal`;
- строить roadmap вокруг `Topic`;
- преобразовывать roadmap в задачи;
- поддерживать загрузку учебных материалов;
- генерировать flashcards для самопроверки;
- обновлять `masteryScore` по объяснимым правилам;
- показывать прогресс на dashboard;
- сохранять четкие границы между frontend, backend, AI-service и database.

## 3. Базовая функциональность

- регистрация и вход пользователей;
- JWT-аутентификация;
- личные заметки;
- учебные цели типов `SELF_STUDY`, `EXAM_PREPARATION`, `INTERVIEW_PREPARATION`, `TECHNOLOGY_LEARNING`;
- загрузка PDF, DOCX и TXT материалов;
- извлечение текста из материалов в упрощенном виде;
- выделение topics через AI-service;
- AI-генерация roadmap;
- автоматическое создание tasks;
- flashcards и история прохождения;
- расчет progress и `masteryScore`;
- минимальный knowledge graph;
- dashboard с агрегированной статистикой;
- журнал AI-запросов для диагностики.

## 4. Основной пользовательский сценарий

1. Пользователь регистрируется или входит в систему.
2. Пользователь создает `LearningGoal`.
3. Пользователь добавляет описание цели или загружает учебные материалы.
4. Backend извлекает текст и вызывает AI-service для выделения topics.
5. Backend создает roadmap и roadmap steps.
6. Backend создает tasks и связывает их с steps и topics.
7. Пользователь выполняет задачи и проходит flashcards.
8. Backend обновляет progress и `masteryScore`.
9. Dashboard показывает состояние обучения, ближайшие задачи и слабые темы.

## 5. Обзор архитектуры

```text
Browser
  |
  v
React Frontend
  |
  | REST /api/v1
  v
Spring Boot Backend
  |                 |
  | SQL/JPA         | Внутренний HTTP /api/ai
  v                 v
PostgreSQL        FastAPI AI-service
```

Ключевые границы:

- frontend обращается только к backend;
- backend владеет бизнес-логикой, проверкой доступа и координацией сценариев;
- AI-service выполняет AI-операции без хранения состояния и без доступа к БД;
- PostgreSQL является единственным постоянным хранилищем;
- Docker Compose используется для локального запуска и интеграции компонентов.

## 6. Технологический стек

- Frontend: React, Vite, TypeScript, Tailwind CSS
- Backend: Java 21, Spring Boot 3.x, Gradle, Flyway
- AI-service: Python 3.12, FastAPI, Pydantic, httpx
- Database: PostgreSQL
- Authentication: JWT
- Deployment: Docker Compose

## 7. Структура репозитория

```text
EducationSystem/
  ├── server/
  ├── client/
  ├── ai-service/
  ├── docs/
  │   └── diagrams/
  ├── docker-compose.yml
  └── README.md
```

## 8. Индекс документации

Архитектура:

- [Архитектура системы](docs/architecture.md)
- [Архитектура backend](docs/backend-architecture.md)
- [Архитектура AI-service](docs/ai-service-architecture.md)
- [Архитектура frontend](docs/frontend-architecture.md)
- [Развертывание](docs/deployment.md)
- [Наблюдаемость](docs/observability.md)
- [Безопасность](docs/security.md)

Инженерные политики:

- [Соглашения API](docs/api-conventions.md)
- [Соглашения по коду](docs/coding-conventions.md)
- [Политика транзакций](docs/transaction-policy.md)
- [Асинхронная обработка](docs/async-processing.md)
- [Хранение файлов](docs/file-storage.md)

Контракты и данные:

- [Спецификация API](docs/api-spec.md)
- [Схема базы данных](docs/database-schema.md)

Предметная область:

- [Модель обучения](docs/learning-model.md)
- [Flashcards](docs/flashcards.md)
- [Knowledge Graph](docs/knowledge-graph.md)
- [Пользовательские сценарии](docs/user-flows.md)

Планирование и ревью:

- [План реализации](docs/implementation-plan.md)
- [Критерии готовности](docs/definition-of-done.md)
- [Финальный архитектурный обзор](docs/final-review.md)

Диаграммы:

- [Обзор архитектуры](docs/diagrams/architecture-overview.mmd)
- [Модули backend](docs/diagrams/backend-module-diagram.mmd)
- [ER-диаграмма базы данных](docs/diagrams/database-er-diagram.mmd)
- [Последовательность генерации roadmap](docs/diagrams/roadmap-generation-sequence.mmd)
- [Последовательность прохождения flashcard](docs/diagrams/flashcard-review-sequence.mmd)
- [Последовательность сценария обучения](docs/diagrams/learning-flow-sequence.mmd)

## 9. Быстрый старт

Локальный запуск рассчитан на Docker Compose:

```text
docker compose up --build
```

Проверки после запуска:

- backend health endpoint отвечает: `GET http://localhost:8080/api/v1/health`;
- AI-service health endpoint отвечает: `GET http://localhost:8000/health`;
- AI-service roadmap endpoint в режиме graceful degradation отвечает: `POST http://localhost:8000/api/ai/generate-roadmap`;
- frontend открывается: `http://localhost:3000`.

Доступные backend endpoints текущего этапа:

- `POST http://localhost:8080/api/v1/auth/register`;
- `POST http://localhost:8080/api/v1/auth/login`;
- `GET http://localhost:8080/api/v1/auth/me`.
- `GET http://localhost:8080/api/v1/goals`;
- `POST http://localhost:8080/api/v1/goals`;
- `GET http://localhost:8080/api/v1/goals/{goalId}`;
- `PUT http://localhost:8080/api/v1/goals/{goalId}`;
- `DELETE http://localhost:8080/api/v1/goals/{goalId}`.
- `POST http://localhost:8080/api/v1/goals/{goalId}/generate-roadmap`;
- `GET http://localhost:8080/api/v1/goals/{goalId}/roadmap`;
- `GET http://localhost:8080/api/v1/roadmaps/{roadmapId}`.
- `GET http://localhost:8080/api/v1/tasks`;
- `POST http://localhost:8080/api/v1/tasks`;
- `GET http://localhost:8080/api/v1/tasks/{taskId}`;
- `PUT http://localhost:8080/api/v1/tasks/{taskId}`;
- `DELETE http://localhost:8080/api/v1/tasks/{taskId}`;
- `PATCH http://localhost:8080/api/v1/tasks/{taskId}/status`.

`GET /api/v1/auth/me`, Learning Goals endpoints, Roadmap endpoints и Tasks endpoints требуют заголовок `Authorization: Bearer <accessToken>`.

Доступные frontend routes текущего этапа:

- `http://localhost:3000/login`;
- `http://localhost:3000/register`;
- `http://localhost:3000/dashboard`.
- `http://localhost:3000/goals`;
- `http://localhost:3000/goals/{goalId}`.
- `http://localhost:3000/tasks`.

Frontend поддерживает login, register, logout, хранение JWT и protected routes.
Frontend поддерживает создание и просмотр учебных целей.
AI-service поддерживает генерацию structured roadmap через внутренний endpoint.
Backend поддерживает генерацию и сохранение roadmap, topics и roadmap steps через AI-service.
Backend автоматически создает tasks из roadmap и поддерживает ручное управление задачами.
Frontend поддерживает генерацию roadmap со страницы цели, отображение этапов roadmap, список задач, фильтрацию по статусу и изменение статуса задачи.
Заметки, flashcards, материалы и dashboard data будут добавляться следующими этапами.

## 10. Переменные окружения

Минимальные группы настроек:

- backend: `JWT_SECRET`, `JWT_ACCESS_TOKEN_TTL`, параметры подключения к PostgreSQL, URL AI-service;
- PostgreSQL: database name, user, password;
- AI-service: API key AI-провайдера, model name, timeout;
- frontend: базовый URL backend API.

Реальные значения секретов не должны храниться в репозитории. Для разработки допускается `.env.example` без чувствительных данных.

## 11. Границы системы

В текущий объем не входят:

- микросервисная декомпозиция backend;
- CQRS;
- event sourcing;
- Kubernetes;
- отдельная векторная БД;
- сложная ML-модель оценки знаний;
- совместная работа в реальном времени;
- адаптивный AI-наставник;
- мобильное приложение.

Эти ограничения являются осознанными: система должна оставаться понятной, реализуемой и пригодной для постепенного развития.

## 12. Запланированные расширения

- механизм refresh token;
- более надежное извлечение текста из файлов;
- пагинация для всех растущих списков;
- метрики и мониторинг;
- embeddings и семантический поиск;
- расширенная аналитика `masteryScore`;
- перенос файлов в объектное хранилище при росте нагрузки.
