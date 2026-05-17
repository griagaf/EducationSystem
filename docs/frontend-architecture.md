# Frontend Architecture

## 1. Назначение frontend

Frontend - клиентская часть платформы, через которую пользователь:

- регистрируется и входит в систему;
- создает заметки;
- создает учебные цели;
- загружает учебные материалы;
- запускает генерацию roadmap;
- просматривает roadmap;
- изучает topics;
- проходит flashcards;
- работает с задачами;
- смотрит knowledge graph;
- отслеживает прогресс на dashboard.

Frontend не содержит бизнес-логику хранения данных и не обращается напрямую к AI-service или PostgreSQL. Все данные приходят из Spring Boot backend через REST API.

## 2. Стек

Основной стек:

- React;
- Vite;
- TypeScript;
- Tailwind CSS;
- обычные переиспользуемые компоненты;
- React Router;
- Axios.

## 3. UI-подход

Для базовой версии системы выбран вариант **обычных переиспользуемых компонентов**, без shadcn/ui.

Причины:

- меньше зависимостей;
- проще объяснить архитектуру на ревью;
- быстрее собрать минимально красивый интерфейс;
- компоненты полностью контролируются проектом;
- Tailwind CSS достаточно для базовой функциональности.

Если позже понадобится более богатая дизайн-система, shadcn/ui можно подключить без смены общей архитектуры frontend.

## 4. Структура проекта

```text
client/
  ├── src/
  │   ├── app/
  │   ├── pages/
  │   ├── components/
  │   ├── features/
  │   ├── services/
  │   ├── hooks/
  │   ├── types/
  │   └── utils/
```

### src/app

Содержит код уровня приложения:

- настройка router;
- корневой layout;
- protected routes;
- providers;
- глобальные стили;
- точка подключения приложения.

Рекомендуемые элементы:

- `App`;
- `router`;
- `ProtectedRoute`;
- `PublicRoute`;
- `AppLayout`;
- `AuthLayout`.

### src/pages

Содержит страницы приложения.

Страницы первой стабильной версии:

- `LoginPage`;
- `RegisterPage`;
- `DashboardPage`;
- `NotesPage`;
- `LearningGoalsPage`;
- `StudyMaterialsPage`;
- `TopicsPage`;
- `RoadmapDetailsPage`;
- `FlashcardsPage`;
- `KnowledgeGraphPage`;
- `TasksPage`.

Страница должна собирать готовые компоненты и вызывать feature-level hooks или services. Страница не должна содержать сложную бизнес-логику.

### src/components

Общие переиспользуемые UI-компоненты:

- `Button`;
- `Input`;
- `Textarea`;
- `Select`;
- `Card`;
- `Badge`;
- `ProgressBar`;
- `Modal`;
- `Spinner`;
- `EmptyState`;
- `ErrorState`;
- `PageHeader`;
- `Sidebar`;
- `TopBar`;
- `StatusBadge`.

Компоненты из `components` не должны знать детали конкретной бизнес-сущности, если это не layout-компоненты.

### src/features

Feature-модули по предметной области.

Рекомендуемые feature-модули:

```text
features/
  ├── auth/
  ├── dashboard/
  ├── notes/
  ├── learning/
  ├── study-materials/
  ├── topics/
  ├── roadmap/
  ├── flashcards/
  ├── knowledge-graph/
  └── tasks/
```

Каждый feature может содержать:

- feature components;
- hooks;
- local types;
- mappers;
- form helpers.

### src/services

Слой общения с backend API:

- общий Axios client;
- auth service;
- notes service;
- learning service;
- study materials service;
- topics service;
- roadmap service;
- flashcards service;
- knowledge graph service;
- tasks service;
- dashboard service.

Services не должны управлять UI-состоянием. Они только выполняют HTTP-запросы и возвращают данные.

### src/hooks

Общие React hooks:

- `useAuth`;
- `useCurrentUser`;
- `useApiError`;
- `useLoading`;
- `useDebounce`, если понадобится для поиска заметок.

Функциональные hooks могут находиться внутри `features`.

### src/types

Общие TypeScript-типы:

- `User`;
- `Note`;
- `LearningGoal`;
- `LearningGoalType`;
- `StudyMaterial`;
- `Topic`;
- `Roadmap`;
- `RoadmapStep`;
- `Task`;
- `Flashcard`;
- `FlashcardReview`;
- `KnowledgeNode`;
- `KnowledgeEdge`;
- `Dashboard`;
- `ApiError`;
- enum-like union types для статусов.

### src/utils

Вспомогательные функции:

- форматирование дат;
- форматирование процентов;
- работа со статусами;
- class name helpers;
- простые validators.

## 5. Страницы первой стабильной версии

### Login

Маршрут:

- `/login`

Функции:

- форма email/password;
- отправка login request;
- сохранение JWT;
- переход на dashboard после успешного входа;
- показ ошибки при неверных credentials.

### Register

Маршрут:

- `/register`

Функции:

- форма display name/email/password;
- отправка register request;
- сохранение JWT;
- переход на dashboard после успешной регистрации;
- ссылка на login.

### Dashboard

Маршрут:

- `/dashboard`

Функции:

- общий прогресс;
- количество активных целей;
- количество завершенных задач;
- карточки целей;
- ближайшие задачи;
- краткая статистика заметок.

Dashboard - стартовая страница после входа.

### Notes

Маршрут:

- `/notes`

Функции:

- список заметок;
- создание заметки;
- редактирование заметки;
- удаление заметки;
- опциональная привязка заметки к учебной цели;
- empty state, если заметок нет.

### Learning Goals

Маршрут:

- `/goals`

Функции:

- список учебных целей;
- создание цели;
- выбор типа цели;
- отображение статуса цели;
- отображение mastery по topics;
- отображение прогресса;
- переход к деталям roadmap.

### Study Materials

Маршрут:

- `/goals/:goalId/materials`

Функции:

- загрузка PDF, DOCX и TXT;
- отображение статуса обработки;
- запуск извлечения topics;
- показ ошибок extraction.

### Topics

Маршрут:

- `/goals/:goalId/topics`

Функции:

- список topics цели;
- mastery score по каждой теме;
- difficulty level;
- переход к связанным задачам и flashcards.

### Roadmap Details

Маршрут:

- `/goals/:goalId`

Функции:

- информация о цели;
- кнопка генерации roadmap;
- состояние загрузки во время генерации;
- список этапов roadmap;
- задачи, связанные с этапами;
- topics, связанные с этапами;
- progress bar цели;
- изменение статусов задач.

### Flashcards

Маршрут:

- `/goals/:goalId/flashcards`

Функции:

- генерация flashcards по topics;
- просмотр вопроса и ответа;
- сохранение review result;
- обновление mastery score topic.

### Knowledge Graph

Маршрут:

- `/goals/:goalId/knowledge-graph`

Функции:

- visualization-ready graph по цели;
- отображение nodes и edges;
- переход от topic node к задачам и flashcards.

### Tasks

Маршрут:

- `/tasks`

Функции:

- общий список задач пользователя или список задач по целям;
- фильтрация по статусу;
- быстрый переход к цели;
- изменение статуса задачи.

В базовой версии системы задачи также отображаются на странице `Roadmap Details`, потому что основной сценарий связан с roadmap.

## 6. Хранение JWT

В текущем релизе JWT хранится в `localStorage`.

Правила:

- после login/register frontend сохраняет `accessToken`;
- Axios client добавляет token в заголовок `Authorization`;
- при logout token удаляется;
- при `401 Unauthorized` token удаляется и пользователь отправляется на `/login`.

Формат заголовка:

```text
Authorization: Bearer <accessToken>
```

Ограничение текущего релиза:

- `localStorage` проще для разработки и ревью;
- для production лучше рассмотреть httpOnly cookies и refresh token flow.

## 7. Ограничение доступа к маршрутам

Маршруты делятся на public и protected.

Public routes:

- `/login`;
- `/register`.

Protected routes:

- `/dashboard`;
- `/notes`;
- `/goals`;
- `/goals/:goalId`;
- `/tasks`.

Правила:

- если пользователь без token открывает protected route, он перенаправляется на `/login`;
- если пользователь с token открывает `/login` или `/register`, он перенаправляется на `/dashboard`;
- при старте приложения frontend может запросить `/api/v1/auth/me`, чтобы проверить token;
- если backend вернул `401`, пользователь считается неавторизованным.

## 8. Общение с backend

Frontend общается только со Spring Boot backend.

Базовый API URL задается через environment variable:

```text
VITE_API_BASE_URL
```

Рекомендуемый backend prefix:

```text
/api/v1
```

### Services

Рекомендуемые frontend services:

- `authService`;
- `notesService`;
- `learningService`;
- `studyMaterialsService`;
- `topicsService`;
- `roadmapService`;
- `flashcardsService`;
- `knowledgeGraphService`;
- `tasksService`;
- `dashboardService`.

### Обработка ошибок API

Backend возвращает ошибки в едином формате:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {}
}
```

Frontend должен:

- показывать короткое сообщение пользователю;
- подсвечивать ошибки форм;
- показывать retry action для генерации roadmap;
- отправлять пользователя на login при `401`;
- показывать not found state при `404`.

## 9. Необходимые компоненты

### Layout

- `AppLayout` - layout для авторизованной части;
- `AuthLayout` - layout для login/register;
- `Sidebar` - основная навигация;
- `TopBar` - заголовок страницы и действия пользователя.

### Auth

- `LoginForm`;
- `RegisterForm`;
- `LogoutButton`.

### Dashboard

- `StatsCard`;
- `GoalProgressCard`;
- `UpcomingTasksList`;
- `OverallProgress`;

### Notes

- `NotesList`;
- `NoteCard`;
- `NoteEditor`;
- `CreateNoteModal` или отдельная форма;
- `NoteGoalSelect`.

### Learning Goals

- `GoalCard`;
- `GoalForm`;
- `GoalTypeSelect`;
- `GoalStatusBadge`;
- `GoalProgressBar`;
- `GoalsList`.

### Study Materials

- `MaterialUpload`;
- `MaterialList`;
- `MaterialProcessingStatus`;
- `ExtractTopicsButton`.

### Topics

- `TopicList`;
- `TopicCard`;
- `MasteryBadge`;
- `DifficultyBadge`.

### Roadmap

- `RoadmapTimeline`;
- `RoadmapStepCard`;
- `GenerateRoadmapButton`;
- `RoadmapLoadingState`;
- `RoadmapEmptyState`.

### Flashcards

- `FlashcardDeck`;
- `FlashcardCard`;
- `FlashcardReviewControls`;
- `GenerateFlashcardsButton`.

### Knowledge Graph

- `KnowledgeGraphView`;
- `KnowledgeNodeDetails`;
- `KnowledgeGraphLegend`.

### Tasks

- `TaskList`;
- `TaskItem`;
- `TaskStatusBadge`;
- `TaskStatusSelect`;
- `CreateTaskForm`;
- `TaskFilters`.

### Shared

- `Button`;
- `Input`;
- `Textarea`;
- `Select`;
- `Card`;
- `Badge`;
- `ProgressBar`;
- `Spinner`;
- `EmptyState`;
- `ErrorState`;
- `ConfirmDialog`.

## 10. Минимальный дизайн

### Общий стиль

- светлая тема;
- спокойная рабочая цветовая палитра;
- белый или почти белый фон;
- нейтральные карточки;
- акцентный цвет для основных действий;
- понятные hover/focus states.

### Sidebar

Sidebar используется как основная навигация в protected area.

Пункты:

- Dashboard;
- Learning Goals;
- Notes;
- Tasks;
- Logout.

На мобильных экранах sidebar может превращаться в верхнее меню или drawer.

### Карточки

Карточки используются для:

- целей;
- статистики dashboard;
- заметок;
- этапов roadmap;
- задач, если выбран card layout.

Карточка должна содержать только одну смысловую единицу.

### Dashboard

Dashboard должен показывать прогресс без перегрузки:

- верхний ряд статистики;
- общий progress bar;
- список активных целей;
- ближайшие задачи.

### Таблицы и списки

Для базовой функциональности предпочтительны списки и компактные карточки.

Таблица уместна для:

- общего списка задач;
- списка заметок, если заметок много.

### Статусы задач

Статусы задач:

- `TODO`;
- `IN_PROGRESS`;
- `DONE`;
- `CANCELLED`.

Визуальное отображение:

- `TODO` - нейтральный badge;
- `IN_PROGRESS` - акцентный badge;
- `DONE` - зеленый badge или состояние завершения;
- `CANCELLED` - приглушенный badge.

Для статусов целей:

- `ACTIVE`;
- `COMPLETED`;
- `ARCHIVED`.

## 11. Состояния экранов

Каждая основная страница должна иметь состояния:

- loading;
- empty;
- error;
- success;
- unauthorized.

Для генерации roadmap обязательно отдельное состояние loading, потому что AI-операция может занимать несколько секунд.

## 12. Границы ответственности frontend

Frontend отвечает за:

- отображение данных;
- формы;
- клиентскую навигацию;
- хранение token в клиентском приложении;
- вызов backend API;
- локальные UI-состояния;
- базовую клиентскую валидацию.

Frontend не отвечает за:

- проверку прав доступа на уровне данных;
- хранение бизнес-данных;
- генерацию roadmap;
- расчет прогресса как источник истины;
- прямой доступ к AI-service;
- прямой доступ к PostgreSQL.

Прогресс может отображаться на frontend, но источником истины остается backend.







