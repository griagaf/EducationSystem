# Backend Architecture

## 1. Используемый стек

Backend реализуется как модульный монолит на Java и Spring Boot.

Основной стек:

- Java 21;
- Spring Boot 3.x;
- Spring Web;
- Spring Безопасность;
- Spring Data JPA;
- PostgreSQL;
- JWT authentication;
- Bean Validation;
- Flyway для миграций базы данных;
- Gradle для сборки.

## 2. Lombok

Для базовой версии системы выбран вариант **без Lombok**.

Причины:

- меньше скрытой логики в entity и DTO;
- проще читать код на ревью;
- проще отлаживать equals, constructors и accessors;
- меньше зависимости от IDE-плагинов;
- лучше видно границы между entity, DTO и бизнес-логикой.

Минус подхода - больше шаблонного кода. Для базовой версии системы это приемлемо, потому что прозрачность архитектуры важнее сокращения нескольких строк.

## 3. Общая структура пакетов

Базовый пакет:

```text
com.example.aiplatform
  ├── auth
  ├── user
  ├── notes
  ├── learning
  ├── tasks
  ├── ai
  ├── dashboard
  ├── common
  └── config
```

Каждый бизнес-модуль содержит свой набор слоев:

```text
module
  ├── controller
  ├── dto
  ├── entity
  ├── repository
  └── service
```

Если модулю не нужны все слои, лишние пакеты не создаются.

## 4. Общие правила backend

### Controller

Controller отвечает только за HTTP-слой:

- принимает request;
- запускает validation;
- получает текущего пользователя из security context;
- вызывает service;
- возвращает response DTO.

Controller не содержит бизнес-логику, SQL-запросы и расчеты прогресса.

### Service

Service содержит бизнес-логику:

- проверка прав пользователя;
- создание и изменение сущностей;
- генерация задач из roadmap;
- расчет прогресса;
- вызов AI-service;
- координация работы нескольких repositories.

Если операция меняет данные, она выполняется внутри transaction.

### Repository

Repository работает с базой данных:

- Spring Data JPA repositories;
- поиск entity по id и owner;
- выборки для dashboard;
- сохранение и удаление entity.

Repository не содержит бизнес-правила.

### DTO

DTO используются для API:

- request DTO для входящих данных;
- response DTO для исходящих данных;
- отдельные DTO для интеграции с AI-service.

Entity не возвращаются напрямую наружу.

### Entity

Entity отражают структуру хранения данных:

- JPA annotations;
- связи между таблицами;
- технические поля `id`, `createdAt`, `updatedAt`;
- минимальная доменная логика, если она относится только к самой entity.

Entity не должны зависеть от controllers и API DTO.

## 5. Модуль auth

### Назначение

Модуль `auth` отвечает за регистрацию, вход и выдачу JWT.

### Entities

Отдельной auth entity в базовой версии системы не требуется. Для учетных данных используется `User` из модуля `user`.

### Repositories

Используется `UserRepository` из модуля `user`.

### Services

Основные services:

- `AuthService` - регистрация и login;
- `JwtService` - создание и проверка JWT;
- `PasswordService` или `PasswordEncoder` - хэширование и проверка пароля.

### Controllers

Основные controllers:

- `AuthController`.

### DTO

Request DTO:

- `RegisterRequest`;
- `LoginRequest`.

Response DTO:

- `AuthResponse`;
- `CurrentUserResponse`.

### Основные endpoints

- `POST /api/v1/auth/register` - регистрация пользователя;
- `POST /api/v1/auth/login` - вход пользователя;
- `GET /api/v1/auth/me` - данные текущего пользователя.

## 6. Модуль user

### Назначение

Модуль `user` хранит пользователей и предоставляет информацию о текущем пользователе для других модулей.

### Entities

- `User`;
- `UserProfile`.

Основные поля:

`User`:

- `id`;
- `email`;
- `passwordHash`;
- `role`;
- `enabled`;
- `createdAt`;
- `updatedAt`.

`UserProfile`:

- `id`;
- `user`;
- `displayName`;
- `bio`;
- `timezone`;
- `createdAt`;
- `updatedAt`.

### Repositories

- `UserRepository`;
- `UserProfileRepository`.

Основные методы:

- поиск по email;
- проверка существования email;
- поиск по id.
- поиск профиля по user id.

### Services

- `UserService` - получение пользователя, проверка существования;
- `CurrentUserService` - получение текущего пользователя из security context.

### Controllers

В базовой версии системы отдельный `UserController` не обязателен. Данные текущего пользователя можно отдавать через `AuthController`.

### DTO

- `UserResponse`;
- `CurrentUserResponse`.

### Основные endpoints

- `GET /api/v1/auth/me` - текущий пользователь.

## 7. Модуль notes

### Назначение

Модуль `notes` отвечает за личные заметки пользователя.

### Entities

- `Note`.

Основные поля:

- `id`;
- `owner`;
- `learningGoal`;
- `title`;
- `content`;
- `createdAt`;
- `updatedAt`.

### Repositories

- `NoteRepository`.

Основные методы:

- список заметок текущего пользователя;
- поиск заметки по id и owner;
- поиск заметок по goal id и owner;
- удаление заметки пользователя.

### Services

- `NoteService`.

Основная логика:

- создание заметки;
- обновление заметки;
- удаление заметки;
- проверка принадлежности заметки пользователю;
- опциональная привязка заметки к учебной цели.

### Controllers

- `NoteController`.

### DTO

Request DTO:

- `CreateNoteRequest`;
- `UpdateNoteRequest`.

Response DTO:

- `NoteResponse`;
- `NoteListItemResponse`.

### Основные endpoints

- `GET /api/v1/notes` - список заметок;
- `POST /api/v1/notes` - создать заметку;
- `GET /api/v1/notes/{noteId}` - получить заметку;
- `PUT /api/v1/notes/{noteId}` - обновить заметку;
- `DELETE /api/v1/notes/{noteId}` - удалить заметку.

## 8. Модуль learning

### Назначение

Модуль `learning` отвечает за учебные цели, материалы, темы, roadmap, flashcards и минимальный knowledge graph.

Экзаменационная подготовка не выделяется в отдельную систему. Она моделируется как один из типов `LearningGoal`.

### Entities

- `LearningGoal`;
- `StudyMaterial`;
- `Topic`;
- `Roadmap`;
- `RoadmapStep`;
- `Flashcard`;
- `FlashcardReview`;
- `KnowledgeNode`;
- `KnowledgeEdge`.

Основные поля `LearningGoal`:

- `id`;
- `owner`;
- `title`;
- `description`;
- `type`;
- `targetDate`;
- `estimatedDuration`;
- `status`;
- `progressPercent`;
- `createdAt`;
- `updatedAt`.

Типы `LearningGoal`:

- `SELF_STUDY`;
- `EXAM_PREPARATION`;
- `INTERVIEW_PREPARATION`;
- `TECHNOLOGY_LEARNING`.

Основные поля `StudyMaterial`:

- `id`;
- `owner`;
- `learningGoal`;
- `fileName`;
- `contentType`;
- `fileSize`;
- `storageKey`;
- `extractedText`;
- `processingStatus`;
- `createdAt`;
- `updatedAt`.

Основные поля `Topic`:

- `id`;
- `owner`;
- `learningGoal`;
- `studyMaterial`;
- `title`;
- `description`;
- `masteryScore`;
- `difficultyLevel`;
- `createdAt`;
- `updatedAt`.

Основные поля `Roadmap`:

- `id`;
- `goal`;
- `title`;
- `description`;
- `createdAt`;
- `updatedAt`.

Основные поля `RoadmapStep`:

- `id`;
- `roadmap`;
- `topic`;
- `orderIndex`;
- `title`;
- `description`;
- `estimatedDays`;
- `status`;
- `createdAt`;
- `updatedAt`.

Основные поля `Flashcard`:

- `id`;
- `owner`;
- `learningGoal`;
- `topic`;
- `question`;
- `answer`;
- `difficulty`;
- `createdAt`;
- `updatedAt`.

Основные поля `FlashcardReview`:

- `id`;
- `owner`;
- `flashcard`;
- `result`;
- `reviewedAt`.

Основные поля `KnowledgeNode`:

- `id`;
- `owner`;
- `learningGoal`;
- `nodeType`;
- `sourceType`;
- `sourceId`;
- `title`;
- `description`;
- `masteryScore`;
- `createdAt`;
- `updatedAt`.

Основные поля `KnowledgeEdge`:

- `id`;
- `owner`;
- `learningGoal`;
- `sourceNode`;
- `targetNode`;
- `edgeType`;
- `weight`;
- `createdAt`.

### Repositories

- `LearningGoalRepository`;
- `StudyMaterialRepository`;
- `TopicRepository`;
- `RoadmapRepository`;
- `RoadmapStepRepository`;
- `FlashcardRepository`;
- `FlashcardReviewRepository`;
- `KnowledgeNodeRepository`;
- `KnowledgeEdgeRepository`.

Основные методы:

- поиск целей текущего пользователя;
- поиск цели по id и owner;
- поиск материалов по goal id и owner;
- поиск topics по goal id и owner;
- поиск roadmap по goal id;
- поиск этапов roadmap по порядку.

### Services

- `LearningGoalService`;
- `StudyMaterialService`;
- `TopicService`;
- `RoadmapService`;
- `RoadmapGenerationService`;
- `FlashcardService`;
- `MasteryService`;
- `KnowledgeGraphService`.

Основная логика:

- создание и обновление учебной цели;
- изменение статуса цели;
- загрузка PDF, DOCX и TXT материалов;
- упрощенное извлечение текста из материалов;
- извлечение topics через AI-service;
- получение страницы цели с roadmap и задачами;
- запуск генерации roadmap;
- сохранение roadmap;
- создание этапов roadmap;
- генерация и review flashcards;
- обновление `Topic.masteryScore` через explainable rules;
- построение visualization-ready knowledge graph;
- координация с `AiClient`;
- координация с `TaskService` для создания задач.

### Controllers

- `LearningGoalController`;
- `StudyMaterialController`;
- `TopicController`;
- `RoadmapController`;
- `FlashcardController`;
- `KnowledgeGraphController`.

### DTO

Request DTO:

- `CreateLearningGoalRequest`;
- `UpdateLearningGoalRequest`;
- `UpdateLearningGoalStatusRequest`;
- `GenerateRoadmapRequest`;
- `UploadStudyMaterialResponse`;
- `TopicResponse`;
- `GenerateFlashcardsRequest`;
- `FlashcardReviewRequest`;
- `KnowledgeGraphResponse`.

Response DTO:

- `LearningGoalResponse`;
- `LearningGoalDetailsResponse`;
- `StudyMaterialResponse`;
- `RoadmapResponse`;
- `RoadmapStepResponse`;
- `GenerateRoadmapResponse`;
- `FlashcardResponse`;
- `FlashcardReviewResponse`;
- `KnowledgeNodeResponse`;
- `KnowledgeEdgeResponse`.

### Основные endpoints

- `GET /api/v1/goals` - список учебных целей;
- `POST /api/v1/goals` - создать учебную цель;
- `GET /api/v1/goals/{goalId}` - детали цели, roadmap и задачи;
- `PUT /api/v1/goals/{goalId}` - обновить цель;
- `PATCH /api/v1/goals/{goalId}/status` - изменить статус цели;
- `DELETE /api/v1/goals/{goalId}` - удалить или архивировать цель;
- `POST /api/v1/goals/{goalId}/materials` - загрузить учебный материал;
- `GET /api/v1/goals/{goalId}/materials` - получить материалы цели;
- `POST /api/v1/materials/{materialId}/extract-topics` - извлечь topics из материала;
- `GET /api/v1/goals/{goalId}/topics` - получить topics цели;
- `POST /api/v1/goals/{goalId}/generate-roadmap` - сгенерировать roadmap;
- `GET /api/v1/goals/{goalId}/roadmap` - получить roadmap цели;
- `GET /api/v1/roadmaps/{roadmapId}` - получить roadmap по id;
- `POST /api/v1/goals/{goalId}/flashcards/generate` - сгенерировать flashcards;
- `GET /api/v1/goals/{goalId}/flashcards` - получить flashcards цели;
- `POST /api/v1/flashcards/{flashcardId}/reviews` - сохранить результат прохождения карточки;
- `GET /api/v1/goals/{goalId}/knowledge-graph` - получить knowledge graph цели.

## 9. Модуль tasks

### Назначение

Модуль `tasks` отвечает за задачи пользователя и расчет прогресса по цели.

### Entities

- `Task`.

Основные поля:

- `id`;
- `owner`;
- `learningGoal`;
- `roadmapStep`;
- `topic`;
- `title`;
- `description`;
- `status`;
- `priority`;
- `dueDate`;
- `createdAt`;
- `updatedAt`.

### Repositories

- `TaskRepository`.

Основные методы:

- список задач по goal id и owner;
- поиск задачи по id и owner;
- подсчет всех задач по цели;
- подсчет выполненных задач по цели;
- поиск ближайших задач для dashboard.

### Services

- `TaskService`;
- `GoalProgressService`.

Основная логика:

- создание задачи вручную;
- автоматическое создание задач из roadmap;
- обновление задачи;
- изменение статуса задачи;
- удаление задачи;
- пересчет прогресса цели;
- обновление mastery score связанной темы при выполнении задачи.

### Controllers

- `TaskController`.

### DTO

Request DTO:

- `CreateTaskRequest`;
- `UpdateTaskRequest`;
- `UpdateTaskStatusRequest`.

Response DTO:

- `TaskResponse`;
- `TaskListItemResponse`;
- `UpdateTaskStatusResponse`.

### Основные endpoints

- `GET /api/v1/tasks` - список задач текущего пользователя;
- `POST /api/v1/tasks` - создать задачу;
- `GET /api/v1/tasks/{taskId}` - получить задачу;
- `PUT /api/v1/tasks/{taskId}` - обновить задачу;
- `PATCH /api/v1/tasks/{taskId}/status` - изменить статус задачи;
- `DELETE /api/v1/tasks/{taskId}` - удалить задачу.

## 10. Модуль ai

### Назначение

Модуль `ai` инкапсулирует интеграцию backend с Python FastAPI AI-service.

### Entities

Entity в модуле `ai` не нужны. AI-service не хранит данные в backend напрямую.

### Repositories

Repositories в модуле `ai` не нужны.

### Services

- `AiRoadmapClient` - HTTP-клиент для AI-service;
- `AiTopicExtractionClient` - HTTP-клиент для извлечения topics;
- `AiFlashcardClient` - HTTP-клиент для генерации flashcards;
- `AiRoadmapMapper` - преобразование AI DTO в backend DTO;
- `AiServiceHealthService` - опциональная проверка доступности AI-service.

### Controllers

Отдельный public controller для AI в базовой версии системы не нужен. Генерация запускается через `RoadmapController`.

### DTO

Request DTO:

- `AiRoadmapGenerateRequest`;
- `AiRoadmapPreferences`;
- `AiExtractTopicsRequest`;
- `AiGenerateFlashcardsRequest`.

Response DTO:

- `AiRoadmapGenerateResponse`;
- `AiRoadmapStepResponse`;
- `AiTaskSuggestionResponse`;
- `AiTopicResponse`;
- `AiFlashcardResponse`.

### Основные endpoints

Backend не публикует отдельные AI endpoints для frontend.

Внутренний вызов backend -> AI-service:

- `POST /api/ai/generate-roadmap`;
- `POST /api/ai/extract-topics`;
- `POST /api/ai/generate-flashcards`.

## 11. Модуль dashboard

### Назначение

Модуль `dashboard` собирает агрегированные данные для главного экрана пользователя.

### Entities

Отдельные dashboard entities в базовой версии системы не нужны.

### Repositories

Используются repositories других модулей:

- `LearningGoalRepository`;
- `TaskRepository`;
- `NoteRepository`.

### Services

- `DashboardService`.

Основная логика:

- количество активных целей;
- количество завершенных целей;
- количество всех задач;
- количество выполненных задач;
- общий прогресс;
- average mastery score;
- слабые topics;
- список целей с прогрессом;
- ближайшие невыполненные задачи.

### Controllers

- `DashboardController`.

### DTO

Response DTO:

- `DashboardResponse`;
- `DashboardGoalResponse`;
- `DashboardTopicResponse`;
- `UpcomingTaskResponse`.

### Основные endpoints

- `GET /api/v1/dashboard/summary` - агрегированная статистика текущего пользователя.

## 12. Модуль common

### Назначение

Модуль `common` содержит общие классы, которые используются несколькими модулями.

### Состав

- базовые exception classes;
- единый формат ошибки API;
- global exception handler;
- DTO для пагинации, если понадобится;
- enum для общих статусов, если они действительно общие;
- базовые audit поля, если используется mapped superclass;
- mapper utilities.

### Основные правила

- `common` не должен превращаться в свалку;
- бизнес-логика конкретного модуля не переносится в `common`;
- в `common` попадает только то, что реально используется несколькими модулями.

## 13. Модуль config

### Назначение

Модуль `config` содержит конфигурацию приложения.

### Состав

- Spring Безопасность configuration;
- CORS configuration;
- JWT configuration;
- WebClient или RestClient configuration для AI-service;
- OpenAPI configuration, если используется;
- Jackson configuration, если нужна;
- application properties binding.

## 14. Безопасность

### JWT authentication

В текущем релизе используется JWT-based authentication:

1. Пользователь регистрируется или входит.
2. Backend проверяет credentials.
3. Backend выдает access token.
4. Frontend отправляет token в заголовке:

```text
Authorization: Bearer <token>
```

5. Backend проверяет token на каждом защищенном запросе.

### Доступ только к своим данным

Все пользовательские сущности должны быть связаны с владельцем:

- `Note.owner`;
- `LearningGoal.owner`;
- `StudyMaterial.owner`;
- `Topic.owner`;
- `Task.owner`;
- `Flashcard.owner`;
- `FlashcardReview.owner`;
- `KnowledgeNode.owner`;
- `KnowledgeEdge.owner`;
- через `LearningGoal.owner` для `Roadmap`;
- через `Roadmap.goal.owner` для `RoadmapStep`.

Правило доступа:

```text
currentUser.id must match entity.owner.id
```

Для связанных сущностей проверка выполняется через корневую пользовательскую сущность. Например, доступ к roadmap проверяется через цель, которой принадлежит roadmap.

### Protected endpoints

Все endpoints защищены авторизацией, кроме:

- `POST /api/v1/auth/register`;
- `POST /api/v1/auth/login`;
- health endpoints, если они нужны для Docker Compose.

### Пароли

Правила хранения паролей:

- пароль не хранится в открытом виде;
- хранится только password hash;
- используется BCrypt или другой надежный password encoder из Spring Безопасность;
- password hash никогда не возвращается в API.

### Ошибки безопасности

Рекомендуемые ответы:

- `401 Unauthorized` - пользователь не аутентифицирован;
- `403 Forbidden` - пользователь пытается получить чужие данные;
- `404 Not Found` - сущность не найдена или не принадлежит пользователю.

Для базовой версии системы допустимо возвращать `404 Not Found` вместо `403 Forbidden` при доступе к чужой сущности, чтобы не раскрывать факт ее существования.

## 15. Транзакции

Транзакции нужны для операций, которые меняют несколько сущностей:

- регистрация пользователя;
- генерация roadmap;
- сохранение roadmap и steps;
- автоматическое создание задач;
- изменение статуса задачи и пересчет прогресса;
- удаление цели вместе с roadmap и задачами.

Read-only операции должны быть помечены как read-only transaction на уровне service, если это принято в проекте.

## 16. Минимальный набор backend endpoints

Auth:

- `POST /api/v1/auth/register`;
- `POST /api/v1/auth/login`;
- `GET /api/v1/auth/me`.

Notes:

- `GET /api/v1/notes`;
- `POST /api/v1/notes`;
- `GET /api/v1/notes/{noteId}`;
- `PUT /api/v1/notes/{noteId}`;
- `DELETE /api/v1/notes/{noteId}`.

Learning:

- `GET /api/v1/goals`;
- `POST /api/v1/goals`;
- `GET /api/v1/goals/{goalId}`;
- `PUT /api/v1/goals/{goalId}`;
- `PATCH /api/v1/goals/{goalId}/status`;
- `DELETE /api/v1/goals/{goalId}`;
- `POST /api/v1/goals/{goalId}/materials`;
- `GET /api/v1/goals/{goalId}/materials`;
- `POST /api/v1/materials/{materialId}/extract-topics`;
- `GET /api/v1/goals/{goalId}/topics`;
- `POST /api/v1/goals/{goalId}/generate-roadmap`;
- `GET /api/v1/goals/{goalId}/roadmap`;
- `GET /api/v1/roadmaps/{roadmapId}`.

Flashcards:

- `POST /api/v1/goals/{goalId}/flashcards/generate`;
- `GET /api/v1/goals/{goalId}/flashcards`;
- `POST /api/v1/flashcards/{flashcardId}/reviews`.

Knowledge Graph:

- `GET /api/v1/goals/{goalId}/knowledge-graph`.

Tasks:

- `GET /api/v1/tasks`;
- `POST /api/v1/tasks`;
- `GET /api/v1/tasks/{taskId}`;
- `PUT /api/v1/tasks/{taskId}`;
- `PATCH /api/v1/tasks/{taskId}/status`;
- `DELETE /api/v1/tasks/{taskId}`.

Dashboard:

- `GET /api/v1/dashboard/summary`.








