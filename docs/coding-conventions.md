# Соглашения по коду

## 1. Общие правила

- Код следует существующей модульной архитектуре.
- Бизнес-логика находится в слое сервисов.
- Controller не содержит бизнес-логику.
- Repository работает только с БД.
- DTO используются для границы API.
- Entity не возвращаются напрямую наружу.

## 2. Backend

### Именование пакетов

Базовый пакет:

```text
com.example.aiplatform
```

Модули:

- `auth`;
- `user`;
- `notes`;
- `learning`;
- `tasks`;
- `ai`;
- `dashboard`;
- `common`;
- `config`.

Внутри модуля:

```text
controller
dto
entity
repository
service
mapper
```

### Именование DTO

- Request DTO: `CreateLearningGoalRequest`, `UpdateTaskStatusRequest`.
- Response DTO: `LearningGoalResponse`, `DashboardSummaryResponse`.
- Internal AI DTO: `AiGenerateRoadmapRequest`, `AiTopicResponse`.

### Именование сервисов

- Основной service модуля: `LearningGoalService`, `TaskService`.
- Orchestration service: `RoadmapGenerationService`.
- Calculation service: `MasteryService`, `GoalProgressService`.
- Integration service/client: `AiRoadmapClient`, `AiFlashcardClient`.

### Именование контроллеров

- `AuthController`;
- `NoteController`;
- `LearningGoalController`;
- `RoadmapController`;
- `TaskController`;
- `FlashcardController`;
- `DashboardController`.

Controller принимает DTO, вызывает service и возвращает DTO.

### Именование исключений

- `ResourceNotFoundException`;
- `AccessDeniedException`;
- `ValidationException`;
- `AiServiceException`;
- `InvalidAiResponseException`.

Исключения преобразуются в единый API error response через global exception handler.

### Именование mapper-классов

- `NoteMapper`;
- `LearningGoalMapper`;
- `RoadmapMapper`;
- `TaskMapper`;
- `FlashcardMapper`.

Mapper не должен выполнять бизнес-логику.

### Именование repository-классов

- `UserRepository`;
- `LearningGoalRepository`;
- `TopicRepository`;
- `TaskRepository`.

Repository методы для пользовательских данных должны учитывать owner:

```text
findByIdAndOwnerId
findAllByOwnerId
```

### Границы транзакций

- Transaction ставится на методах сервисов.
- Операции чтения могут быть read-only.
- Операции генерации roadmap, topics и flashcards должны сохранять связанные сущности атомарно.
- Внешний вызов AI-service не должен выполняться внутри длинной транзакции, если это можно разделить безопасно.

### Правила валидации

- Валидация выполняется на request DTO.
- Entity защищает инварианты данных.
- Значения enum валидируются до выполнения бизнес-логики.
- Проверка принадлежности данных пользователю выполняется в слое сервисов.

## 3. Frontend

### Именование компонентов

- Components: `GoalCard`, `TaskItem`, `FlashcardDeck`.
- Pages: `DashboardPage`, `LearningGoalsPage`, `RoadmapDetailsPage`.
- Layouts: `AppLayout`, `AuthLayout`.

### Именование hooks

- Общие hooks: `useAuth`, `useApiError`.
- Feature hooks: `useGoals`, `useFlashcards`, `useDashboardSummary`.

### Именование сервисов

- `authService`;
- `notesService`;
- `learningService`;
- `tasksService`;
- `flashcardsService`;
- `dashboardService`.

Services выполняют HTTP-запросы и не содержат UI-логику.

### Folder Structure

```text
src/
  app/
  pages/
  components/
  features/
  services/
  hooks/
  types/
  utils/
```

Функциональные компоненты хранятся внутри `features`.

## 4. AI-service

### Schema Naming

- `GenerateRoadmapRequest`;
- `GenerateRoadmapResponse`;
- `ExtractTopicsRequest`;
- `ExtractTopicsResponse`;
- `GenerateFlashcardsRequest`;
- `GenerateFlashcardsResponse`.

### Именование сервисов

- `RoadmapGenerationService`;
- `TopicExtractionService`;
- `FlashcardGenerationService`;
- `RoadmapValidationService`.

### Endpoint Naming

AI-service endpoints:

- `POST /api/ai/generate-roadmap`;
- `POST /api/ai/extract-topics`;
- `POST /api/ai/generate-flashcards`.

### Response Contracts

- AI-service возвращает только structured JSON.
- AI-service не возвращает markdown как основной формат.
- AI-service не хранит данные.
- Backend валидирует AI response перед сохранением.


