# AI Service Architecture

## 1. Назначение AI-service

AI-service - отдельный Python FastAPI сервис для AI-операций.

Он отвечает только за генерацию и обработку AI-контента. Основная бизнес-логика остается в Spring Boot backend.

AI-service не должен:

- хранить пользователей;
- хранить учебные цели;
- хранить заметки;
- хранить задачи;
- работать напрямую с PostgreSQL основного приложения;
- выполнять авторизацию пользователей;
- рассчитывать прогресс;
- принимать решения о правах доступа.

Spring Boot backend является единственным потребителем AI-service в текущем релизе.

## 2. Базовая функциональность

AI-service выполняет три основные функции:

1. Генерация roadmap по учебной цели, topics и материалам.
2. Извлечение topics из текста пользовательских материалов.
3. Генерация flashcards по topics.
4. Возврат структурированного JSON для Spring Boot backend.

AI-service остается сервисом без состояния: он не хранит материалы, topics, flashcards и не обращается к PostgreSQL.

## 3. Будущие функции

После первой стабильной версии AI-service можно расширить:

- семантический поиск;
- embeddings;
- анализ заметок;
- рекомендации;
- извлечение knowledge graph.

Эти функции запланированы для следующих версий и не должны усложнять текущую версию сервиса.

## 4. Стек

Основной стек AI-service:

- Python 3.12;
- FastAPI;
- Pydantic;
- OpenAI-compatible API;
- httpx;
- python-dotenv;
- pytest для тестов.

### Назначение технологий

- FastAPI - HTTP API сервиса.
- Pydantic - валидация request и response schemas.
- OpenAI-compatible API - подключение к LLM-провайдеру через совместимый интерфейс.
- httpx - HTTP-клиент для обращения к внешнему AI API.
- python-dotenv - загрузка переменных окружения при локальной разработке.

## 5. Структура проекта

```text
ai-service/
  ├── app/
  │   ├── main.py
  │   ├── api/
  │   ├── services/
  │   ├── schemas/
  │   ├── clients/
  │   └── core/
  ├── tests/
  ├── requirements.txt
  └── Dockerfile
```

### app/main.py

Точка входа FastAPI приложения.

Отвечает за:

- создание FastAPI app;
- подключение routers;
- подключение exception handlers;
- health endpoint, если нужен Docker Compose.

### app/api

HTTP endpoints сервиса.

Рекомендуемые файлы:

- `roadmap_api` - endpoint генерации roadmap;
- `topics_api` - endpoint извлечения topics;
- `flashcards_api` - endpoint генерации flashcards;
- `health_api` - проверка состояния сервиса.

API слой не содержит AI-логику. Он принимает request, вызывает service и возвращает response.

### app/services

Слой AI-логики.

Рекомендуемые services:

- `RoadmapGenerationService` - координация генерации roadmap;
- `RoadmapPromptService` - подготовка prompt для AI;
- `RoadmapValidationService` - проверка структуры AI-ответа;
- `TopicExtractionService` - извлечение topics из текста материала;
- `FlashcardGenerationService` - генерация flashcards по topics;
- `GracefulDegradationRoadmapService` - резервная генерация roadmap при недоступности внешнего AI API.

### app/schemas

Pydantic schemas для входных и выходных данных.

Рекомендуемые schemas:

- `GenerateRoadmapRequest`;
- `GenerateRoadmapResponse`;
- `ExtractTopicsRequest`;
- `ExtractTopicsResponse`;
- `GenerateFlashcardsRequest`;
- `GenerateFlashcardsResponse`;
- `RoadmapStepSchema`;
- `RoadmapTaskSchema`;
- `TopicSchema`;
- `FlashcardSchema`;
- `ErrorResponse`.

### app/clients

Клиенты внешних сервисов.

Рекомендуемые clients:

- `OpenAICompatibleClient` - клиент для LLM-провайдера;
- `HttpClientFactory` или общий httpx client, если понадобится.

Клиент отвечает только за сетевой вызов и базовую обработку ответа провайдера. Он не должен знать бизнес-смысл roadmap.

### app/core

Общая конфигурация и инфраструктура сервиса.

Рекомендуемый состав:

- настройки приложения;
- переменные окружения;
- logging;
- exception classes;
- error handlers;
- timeout configuration.

### tests

Тесты AI-service.

Минимальные тесты базовой функциональности:

- валидация request schema;
- валидация response schema;
- успешная генерация roadmap через механизм graceful degradation;
- извлечение topics из текста;
- генерация flashcards по topics;
- обработка timeout;
- обработка пустого AI-ответа;
- обработка некорректного AI JSON.

## 6. Endpoints Базовая функциональность

### POST /api/ai/generate-roadmap

Endpoint генерирует roadmap по учебной цели, topics и материалам.

### POST /api/ai/extract-topics

Endpoint извлекает topics из текста учебного материала.

### POST /api/ai/generate-flashcards

Endpoint генерирует flashcards по topics.

Потребитель endpoint: Spring Boot backend.

Frontend не вызывает этот endpoint напрямую.

## 7. Формат входных данных

Request содержит:

- `goal_title` - краткое название учебной цели;
- `goal_description` - подробное описание цели;
- `goal_type` - тип цели;
- `duration_weeks` - желаемая длительность обучения в неделях;
- `user_level` - уровень пользователя.
- `topics` - существующие темы, если они уже извлечены;
- `materials_summary` - краткая выжимка из материалов.

Пример значений:

```json
{
  "goal_title": "Изучить Java Spring Boot",
  "goal_description": "Хочу изучить Java Spring Boot за 3 месяца и сделать pet project",
  "goal_type": "TECHNOLOGY_LEARNING",
  "duration_weeks": 12,
  "user_level": "beginner",
  "topics": [
    {
      "title": "Spring IoC",
      "description": "Dependency injection basics"
    }
  ],
  "materials_summary": "Extracted text summary"
}
```

### Правила валидации request

- `goal_title` обязателен и не должен быть пустым;
- `goal_description` обязателен и не должен быть пустым;
- `goal_type` должен быть одним из `SELF_STUDY`, `EXAM_PREPARATION`, `INTERVIEW_PREPARATION`, `TECHNOLOGY_LEARNING`;
- `duration_weeks` должен быть положительным числом;
- `duration_weeks` для базовой версии системы рекомендуется ограничить диапазоном 1-52;
- `user_level` должен принимать одно из значений:
  - `beginner`;
  - `intermediate`;
  - `advanced`.

## 8. Формат ответа

Response содержит:

- `roadmap_title` - название roadmap;
- `roadmap_description` - краткое описание roadmap;
- `steps[]` - этапы roadmap;
- `tasks[]` - задачи для автоматического создания в backend.
- для `extract-topics`: `topics[]`;
- для `generate-flashcards`: `flashcards[]`.

Пример структуры:

```json
{
  "roadmap_title": "Java Spring Boot за 12 недель",
  "roadmap_description": "План изучения Spring Boot от базовых концепций до pet project",
  "steps": [
    {
      "step_number": 1,
      "topic_title": "Java backend basics",
      "title": "Основы Java backend",
      "description": "Повторить HTTP, REST, SQL и базовые принципы backend-разработки",
      "duration_weeks": 2
    }
  ],
  "tasks": [
    {
      "step_number": 1,
      "title": "Повторить HTTP методы",
      "description": "Разобрать GET, POST, PUT, PATCH, DELETE и основные status codes"
    }
  ]
}
```

### Правила response

- response должен быть валидным JSON;
- `roadmap_title` не должен быть пустым;
- `roadmap_description` не должен быть пустым;
- `steps` должен содержать минимум один этап;
- каждый step должен иметь `step_number`, `title`, `description`;
- `step_number` должен соответствовать существующему этапу;
- `tasks` должен содержать задачи, связанные с этапами;
- каждая task должна иметь `step_number`, `title`, `description`.

Spring Boot backend сохраняет roadmap, steps и tasks в свою базу данных.

Пример ответа `POST /api/ai/extract-topics`:

```json
{
  "topics": [
    {
      "title": "Spring IoC",
      "description": "Dependency injection basics",
      "difficulty_level": "MEDIUM"
    }
  ]
}
```

Пример ответа `POST /api/ai/generate-flashcards`:

```json
{
  "flashcards": [
    {
      "topic_id": "uuid",
      "question": "What is dependency injection?",
      "answer": "A pattern where dependencies are provided from outside the object.",
      "difficulty": "MEDIUM"
    }
  ]
}
```

## 9. Основной поток генерации

1. Spring Boot backend получает запрос пользователя на генерацию roadmap.
2. Backend проверяет JWT и права доступа к учебной цели.
3. Backend формирует request для AI-service.
4. Backend вызывает `POST /api/ai/generate-roadmap`.
5. AI-service валидирует request.
6. AI-service формирует prompt.
7. AI-service вызывает OpenAI-compatible API или механизм graceful degradation.
8. AI-service валидирует и нормализует результат.
9. AI-service возвращает структурированный JSON backend.
10. Backend сохраняет roadmap, topics bindings и создает задачи.

## 10. OpenAI-compatible API

AI-service должен быть отвязан от конкретного LLM-провайдера.

Настройки через переменные окружения:

- `AI_API_BASE_URL`;
- `AI_API_KEY`;
- `AI_MODEL`;
- `AI_TIMEOUT_SECONDS`;
- `AI_MAX_RETRIES`;
- `AI_GRACEFUL_DEGRADATION_ENABLED`.

Если внешний AI API недоступен или не настроен, для базовой версии системы используется механизм graceful degradation.

## 11. Механизм graceful degradation

Механизм graceful degradation поддерживает устойчивость работы при недоступности внешнего AI API.

Он используется, если:

- нет API key;
- AI API недоступен;
- включен режим `AI_GRACEFUL_DEGRADATION_ENABLED`;
- внешний AI API вернул некорректный ответ.

Механизм graceful degradation возвращает резервный, но валидный roadmap. Это позволяет backend и frontend сохранять основной пользовательский сценарий независимо от внешнего AI-провайдера.

## 12. Обработка ошибок

AI-service должен возвращать контролируемые ошибки в JSON.

Базовый формат ошибки:

```json
{
  "code": "AI_TIMEOUT",
  "message": "AI request timed out"
}
```

### AI недоступен

Причины:

- внешний AI API не отвечает;
- ошибка сети;
- неверный базовый URL API;
- провайдер вернул 5xx.

Рекомендуемое поведение:

- если graceful degradation включен, вернуть резервный roadmap;
- если graceful degradation отключен, вернуть ошибку `AI_UNAVAILABLE`;
- HTTP status: `503 Service Unavailable`.

### Некорректный ответ AI

Причины:

- AI вернул не JSON;
- JSON не соответствует schema;
- отсутствуют обязательные поля;
- steps или tasks пустые;
- task ссылается на несуществующий step.

Рекомендуемое поведение:

- попытаться нормализовать ответ, если это безопасно;
- если нормализация невозможна и graceful degradation включен, вернуть резервный roadmap;
- если graceful degradation отключен, вернуть ошибку `INVALID_AI_RESPONSE`;
- HTTP status: `502 Bad Gateway`.

### Timeout

Причины:

- превышен `AI_TIMEOUT_SECONDS`;
- зависание внешнего API;
- долгий ответ LLM.

Рекомендуемое поведение:

- отменить внешний запрос;
- если graceful degradation включен, вернуть резервный roadmap;
- если graceful degradation отключен, вернуть ошибку `AI_TIMEOUT`;
- HTTP status: `504 Gateway Timeout`.

### Пустой результат

Причины:

- AI вернул пустой content;
- AI вернул пустые массивы;
- prompt не дал полезного результата.

Рекомендуемое поведение:

- считать ответ невалидным;
- если graceful degradation включен, вернуть резервный roadmap;
- если graceful degradation отключен, вернуть ошибку `EMPTY_AI_RESULT`;
- HTTP status: `502 Bad Gateway`.

## 13. Безопасность

AI-service в текущем релизе не выполняет пользовательскую авторизацию.

Минимальные правила:

- сервис не должен быть доступен публично в production;
- frontend не должен знать URL AI-service;
- AI-service вызывается только backend;
- API key внешнего AI-провайдера хранится только в переменных окружения;
- секреты не попадают в logs;
- request и response logs не должны содержать чувствительные данные.

В Docker Compose AI-service должен быть доступен backend по внутреннему сетевому alias.

## 14. Границы ответственности

AI-service отвечает за:

- подготовку prompt;
- вызов AI-провайдера;
- генерацию по механизму graceful degradation;
- валидацию AI-ответа;
- возврат структурированного roadmap JSON;
- возврат структурированного topics JSON;
- возврат structured flashcards JSON.

Spring Boot backend отвечает за:

- пользователей;
- авторизацию;
- учебные цели;
- учебные материалы;
- topics;
- flashcards и reviews;
- сохранение roadmap;
- создание задач;
- расчет mastery score;
- построение knowledge graph;
- расчет прогресса;
- dashboard;
- проверку доступа.

## 15. Что запланировано для следующих версий AI-service

Для следующих версий AI-service запланированы:

- семантический поиск;
- embeddings;
- анализ заметок;
- рекомендации;
- извлечение knowledge graph;
- хранение истории генераций;
- очередь задач;
- стриминг ответа;
- fine-tuning;
- собственная ML-модель.








