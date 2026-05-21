# План реализации

## 1. Настройка проекта

### Цель

Создать базовую структуру репозитория для frontend, backend, AI-service и документации.

### Задачи

- Создать директории `server`, `client`, `ai-service`.
- Зафиксировать базовые README-инструкции для каждого компонента.
- Выбрать единый стиль именования переменных окружения.
- Подготовить `.gitignore` для Java, Node, Python, IDE и Docker artifacts.
- Проверить, что документация в корне проекта согласована с будущей структурой.

### Результат

В репозитории есть понятная структура проекта, готовая к реализации компонентов базовой функциональности.

### Критерии готовности

- Директории компонентов созданы.
- Spring Boot backend расположен в `server`.
- Документация не противоречит структуре проекта.
- В репозитории нет сгенерированных build artifacts.
- Понятно, где будет находиться каждый сервис.

### Что проверить вручную

- Открыть репозиторий и убедиться, что структура читается без пояснений.
- Проверить, что документация ссылается на актуальные компоненты.
- Проверить, что будущие секреты не попадают в git.

### Какие тесты написать

На этом этапе автоматические тесты не обязательны.

## 2. Backend Foundation

### Цель

Создать базовое Spring Boot приложение и подготовить фундамент для модульного монолита.

### Задачи

- Создать Spring Boot проект на Java 21, Spring Boot 3.x и Gradle.
- Подключить Flyway для миграций базы данных.
- Подключить Spring Web, Spring Security, Spring Data JPA, Validation, PostgreSQL driver.
- Создать базовый пакет `com.example.aiplatform`.
- Создать пакеты `auth`, `user`, `notes`, `learning`, `tasks`, `ai`, `dashboard`, `common`, `config`.
- Настроить подключение к PostgreSQL через переменные окружения.
- Подготовить базовую обработку ошибок.
- Добавить health endpoint, если нужен для Docker Compose.
- Подготовить миграции базы данных.

### Результат

Backend запускается, подключается к PostgreSQL и имеет базовую структуру модулей.

### Критерии готовности

- Backend стартует без ошибок.
- Приложение видит PostgreSQL.
- Есть единый формат ошибок API.
- Созданы первые миграции для базовой схемы или подготовлена инфраструктура миграций.
- Пакеты соответствуют `BACKEND_ARCHITECTURE.md`.

### Что проверить вручную

- Запустить backend локально.
- Проверить health endpoint.
- Проверить подключение к базе.
- Проверить, что при ошибке API возвращается JSON, а не stack trace.

### Какие тесты написать

- Context load test для Spring Boot.
- Тест конфигурации подключения к базе в test profile.
- Тест global exception handler на базовую ошибку.

## 3. Auth Module

### Цель

Реализовать регистрацию, вход и JWT-авторизацию API.

### Задачи

- Создать entity `User` и `UserProfile`.
- Создать repositories для пользователей.
- Реализовать регистрацию пользователя.
- Реализовать login.
- Добавить password hashing.
- Реализовать генерацию JWT.
- Реализовать JWT filter.
- Настроить защищенные endpoints.
- Добавить endpoint текущего пользователя.

### Результат

Пользователь может зарегистрироваться, войти и обращаться к защищенным endpoints с JWT.

### Критерии готовности

- `POST /api/v1/auth/register` создает пользователя.
- `POST /api/v1/auth/login` возвращает JWT.
- `GET /api/v1/auth/me` возвращает текущего пользователя.
- Пароль хранится только как hash.
- Без JWT защищенные endpoints возвращают `401`.
- С невалидным JWT защищенные endpoints возвращают `401`.

### Что проверить вручную

- Зарегистрировать нового пользователя.
- Попробовать зарегистрировать тот же email повторно.
- Войти с правильным паролем.
- Войти с неправильным паролем.
- Вызвать `/api/v1/auth/me` с JWT и без JWT.

### Какие тесты написать

- Unit tests для `AuthService`.
- Unit tests для `JwtService`.
- Integration tests для register/login/me.
- Безопасность tests для доступа без JWT.

## 4. Notes Module

### Цель

Реализовать CRUD заметок пользователя.

### Задачи

- Создать entity `Note`.
- Создать `NoteRepository`.
- Реализовать `NoteService`.
- Реализовать DTO для создания, обновления и ответа.
- Реализовать `NoteController`.
- Добавить проверку принадлежности данных пользователю по `user_id`.
- Добавить опциональную привязку заметки к учебной цели.

### Результат

Пользователь может создавать, читать, обновлять и удалять свои заметки.

### Критерии готовности

- `GET /api/v1/notes` возвращает только заметки текущего пользователя.
- `POST /api/v1/notes` создает заметку.
- `GET /api/v1/notes/{id}` возвращает только свою заметку.
- `PUT /api/v1/notes/{id}` обновляет только свою заметку.
- `DELETE /api/v1/notes/{id}` удаляет только свою заметку.
- Entity не возвращаются напрямую наружу.

### Что проверить вручную

- Создать заметку.
- Обновить заметку.
- Удалить заметку.
- Создать двух пользователей и проверить, что один не видит заметки другого.
- Проверить empty state на уровне API: пустой список заметок.

### Какие тесты написать

- Unit tests для `NoteService`.
- Repository tests для поиска заметок по owner.
- Integration tests для CRUD notes.
- Безопасность tests на запрет доступа к чужой заметке.

## 5. Learning Goals Module

### Цель

Реализовать учебные цели пользователя.

### Задачи

- Создать entity `LearningGoal`.
- Создать enum статусов цели.
- Создать `LearningGoalRepository`.
- Реализовать `LearningGoalService`.
- Реализовать DTO.
- Реализовать `LearningGoalController`.
- Добавить проверку принадлежности данных пользователю.
- Подготовить связь цели с roadmap, notes и tasks.
- Добавить типы целей: `SELF_STUDY`, `EXAM_PREPARATION`, `INTERVIEW_PREPARATION`, `TECHNOLOGY_LEARNING`.
- Добавить поля `type`, `targetDate`, `estimatedDuration`.

### Результат

Пользователь может управлять своими учебными целями.

### Критерии готовности

- `GET /api/v1/goals` возвращает только цели текущего пользователя.
- `POST /api/v1/goals` создает цель со статусом `ACTIVE`.
- `GET /api/v1/goals/{id}` возвращает детали своей цели.
- `PUT /api/v1/goals/{id}` обновляет свою цель.
- `DELETE /api/v1/goals/{id}` удаляет или архивирует свою цель.
- Начальный `progressPercent` равен `0`.

### Что проверить вручную

- Создать цель "Изучить Java Spring Boot".
- Обновить описание цели.
- Архивировать или удалить цель.
- Проверить, что цель другого пользователя недоступна.
- Проверить список целей при отсутствии данных.
- Создать цель типа `EXAM_PREPARATION` и убедиться, что она использует общую модель `LearningGoal`, а не отдельную подсистему экзаменов.

### Какие тесты написать

- Unit tests для `LearningGoalService`.
- Repository tests для поиска целей по owner.
- Integration tests для CRUD goals.
- Tests для статусов и progress initialization.

## 6. Tasks Module

### Цель

Реализовать задачи, статусы, приоритеты и расчет прогресса цели.

### Задачи

- Создать entity `Task`.
- Создать enum статусов задачи.
- Создать enum приоритетов задачи.
- Создать `TaskRepository`.
- Реализовать `TaskService`.
- Реализовать `GoalProgressService`.
- Реализовать DTO.
- Реализовать `TaskController`.
- Добавить ручное создание задачи.
- Добавить изменение статуса задачи.
- Добавить пересчет прогресса цели.

### Результат

Пользователь может управлять задачами, а прогресс цели пересчитывается при изменениях.

### Критерии готовности

- `GET /api/v1/tasks` возвращает задачи текущего пользователя.
- `POST /api/v1/tasks` создает задачу.
- `PUT /api/v1/tasks/{id}` обновляет задачу.
- `DELETE /api/v1/tasks/{id}` удаляет задачу.
- `PATCH /api/v1/tasks/{id}/status` меняет статус.
- При статусе `DONE` прогресс цели увеличивается.
- Пользователь не может изменить чужую задачу.

### Что проверить вручную

- Создать задачу для цели.
- Изменить статус на `IN_PROGRESS`.
- Изменить статус на `DONE`.
- Проверить изменение progress percent у цели.
- Удалить задачу и проверить пересчет прогресса.

### Какие тесты написать

- Unit tests для `TaskService`.
- Unit tests для `GoalProgressService`.
- Repository tests для подсчета задач по статусу.
- Integration tests для CRUD tasks.
- Tests для принадлежность задач пользователю.

## 7. AI-service Foundation

### Цель

Создать базовое FastAPI приложение для AI-операций.

### Задачи

- Создать структуру `ai-service`.
- Настроить Python 3.12.
- Подключить FastAPI, Pydantic, httpx, python-dotenv.
- Создать `app/main.py`.
- Создать слои `api`, `services`, `schemas`, `clients`, `core`.
- Добавить health endpoint.
- Описать настройки через переменные окружения.
- Реализовать механизм graceful degradation для roadmap.

### Результат

AI-service запускается и может возвращать тестовый roadmap без внешнего AI API.

### Критерии готовности

- AI-service стартует локально.
- Health endpoint отвечает успешно.
- Pydantic schemas валидируют входные данные.
- Механизм graceful degradation возвращает валидный roadmap.
- Сервис не зависит от PostgreSQL.

### Что проверить вручную

- Запустить AI-service.
- Вызвать health endpoint.
- Вызвать генерацию roadmap с примерной целью.
- Проверить, что ответ содержит `roadmap_title`, `roadmap_description`, `steps`, а задачи находятся внутри каждого step в поле `tasks`.

### Какие тесты написать

- Tests для request schema validation.
- Tests для response schema validation.
- Unit tests для механизма graceful degradation.
- API test для health endpoint.
- API test для successful roadmap generation.

## 8. Roadmap Generation Integration

### Цель

Интегрировать Spring Boot backend с AI-service и сохранять roadmap в базе.

### Задачи

- Создать entity `Roadmap` и `RoadmapStep`.
- Создать repositories для roadmap и steps.
- Реализовать backend AI client.
- Реализовать DTO для запроса к AI-service.
- Реализовать DTO для ответа AI-service.
- Реализовать генерацию roadmap по цели.
- Сохранять roadmap и steps.
- Автоматически создавать tasks из AI-ответа.
- Логировать AI-запросы в `ai_request_logs`.
- Обрабатывать timeout, invalid response и unavailable AI-service.
- Связывать roadmap steps с topics, если они есть.

### Результат

Пользователь может нажать генерацию roadmap, после чего backend получает AI-ответ, сохраняет roadmap и создает задачи.

### Критерии готовности

- `POST /api/v1/goals/{id}/generate-roadmap` работает для своей цели.
- `GET /api/v1/goals/{id}/roadmap` возвращает roadmap.
- `GET /api/v1/roadmaps/{id}` возвращает roadmap текущего пользователя.
- Roadmap имеет steps.
- Tasks могут автоматически создаваться из AI-ответа на следующем этапе; текущая интеграция сохраняет roadmap, topics и steps.
- Ошибки AI-service возвращаются в контролируемом формате.

### Что проверить вручную

- Создать цель.
- Сгенерировать roadmap.
- Проверить записи roadmap и steps в API.
- Проверить созданные tasks.
- Остановить AI-service и проверить ошибку или поведение graceful degradation.
- Проверить, что чужую цель нельзя использовать для генерации.

### Какие тесты написать

- Unit tests для `RoadmapGenerationService`.
- Unit tests для mapper AI response -> domain.
- Integration tests для generate-roadmap endpoint.
- Tests для автоматического создания задач.
- Tests для invalid AI response.
- Tests для AI timeout или unavailable case.

## 8A. Materials, Topics, Flashcards and Knowledge Graph

### Цель

Расширить learning model материалами, topics, flashcards, mastery score и минимальным knowledge graph без создания новой подсистемы.

### Задачи

- Создать `StudyMaterial`, `Topic`, `Flashcard`, `FlashcardReview`, `KnowledgeNode`, `KnowledgeEdge`.
- Добавить загрузку PDF, DOCX и TXT.
- Реализовать упрощенное извлечение текста.
- Добавить backend client для `POST /api/ai/extract-topics`.
- Сохранять topics и связывать их с `LearningGoal`.
- Добавить backend client для `POST /api/ai/generate-flashcards`.
- Сохранять flashcards и reviews.
- Реализовать explainable update logic для `Topic.masteryScore`.
- Добавить связь tasks и roadmap steps с topics.
- Реализовать deterministic knowledge graph builder.

### Результат

Пользователь может загрузить материал, получить topics, сгенерировать flashcards, пройти review и увидеть mastery score по темам.

### Критерии готовности

- Материал загружается и связан с целью.
- Topics извлекаются из материала.
- Roadmap может использовать topics.
- Flashcards генерируются по topics.
- Review карточки обновляет mastery score.
- Knowledge graph возвращает nodes и edges.

### Что проверить вручную

- Загрузить TXT материал.
- Извлечь topics.
- Сгенерировать flashcards.
- Пройти несколько reviews.
- Проверить изменение mastery score.
- Открыть knowledge graph цели.

### Какие тесты написать

- Tests для file type validation.
- Tests для text extraction service.
- Tests для topic extraction integration с mocked AI-service.
- Tests для flashcard generation integration с mocked AI-service.
- Unit tests для mastery score calculation.
- Tests для knowledge graph builder.

## 9. Dashboard API

### Цель

Реализовать агрегированную сводку для dashboard.

### Задачи

- Реализовать `DashboardService`.
- Реализовать `DashboardController`.
- Подготовить response DTO.
- Посчитать active/completed/archived goals.
- Посчитать total/completed tasks.
- Посчитать overall progress.
- Вернуть цели с прогрессом.
- Вернуть ближайшие задачи.

### Результат

Frontend может получить все данные для dashboard одним API-запросом.

### Критерии готовности

- `GET /api/v1/dashboard/summary` возвращает данные текущего пользователя.
- Пустой dashboard корректно отображается как нулевые значения и пустые массивы.
- Данные другого пользователя не попадают в summary.
- Overall progress считается корректно.

### Что проверить вручную

- Открыть summary у нового пользователя.
- Создать цели и задачи.
- Выполнить часть задач.
- Проверить, что числа в summary изменились.
- Проверить upcoming tasks.

### Какие тесты написать

- Unit tests для `DashboardService`.
- Repository tests для агрегирующих запросов.
- Integration test для dashboard summary.
- Test для пустого dashboard.
- Test на изоляцию данных пользователей.

## 10. Frontend Foundation

### Цель

Создать React frontend и подготовить базовую инфраструктуру приложения.

### Задачи

- Создать Vite React TypeScript проект.
- Подключить Tailwind CSS.
- Настроить React Router.
- Создать структуру `app`, `pages`, `components`, `features`, `services`, `hooks`, `types`, `utils`.
- Создать Axios client.
- Настроить `VITE_API_BASE_URL`.
- Реализовать хранение JWT.
- Реализовать protected routes.
- Создать базовый layout с sidebar.

### Результат

Frontend запускается, имеет routing, layout, API client и основу auth-flow.

### Критерии готовности

- Frontend стартует локально.
- `/login` и `/register` доступны без JWT.
- Protected routes перенаправляют на `/login` без JWT.
- Axios client добавляет JWT в Authorization header.
- Sidebar отображается в protected area.

### Что проверить вручную

- Запустить frontend.
- Открыть public routes.
- Открыть protected route без token.
- Вручную добавить token и проверить доступ к protected layout.
- Проверить адаптивность sidebar на базовом уровне.

### Какие тесты написать

- Component tests для `ProtectedRoute`.
- Unit tests для token storage utils.
- Unit tests для API client interceptor, если он вынесен.
- Smoke test рендера App.

## 11. Frontend Pages

### Цель

Реализовать страницы базовой функциональности и связать их с backend API.

### Задачи

- Реализовать Login page.
- Реализовать Register page.
- Реализовать Dashboard page.
- Реализовать Notes page.
- Реализовать Learning Goals page.
- Реализовать Roadmap Details page.
- Реализовать Tasks page.
- Добавить состояния загрузки, пустого списка и ошибки.
- Добавить формы создания и редактирования сущностей.
- Добавить изменение статуса задач.
- Добавить генерацию roadmap с состоянием загрузки.

### Результат

Пользователь может пройти полный основной пользовательский сценарий через UI.

### Критерии готовности

- Пользователь может зарегистрироваться через UI.
- Пользователь может войти через UI.
- Пользователь может создать заметку.
- Пользователь может создать учебную цель.
- Пользователь может сгенерировать roadmap.
- Пользователь видит созданные tasks.
- Пользователь может отметить task как `DONE`.
- Dashboard отображает обновленный прогресс.

### Что проверить вручную

- Пройти основной пользовательский сценарий от регистрации до dashboard.
- Проверить empty states на новом аккаунте.
- Проверить ошибки форм.
- Проверить состояние загрузки во время генерации roadmap.
- Проверить logout.
- Проверить обновление данных после изменения статуса задачи.

### Какие тесты написать

- Component tests для основных форм.
- Component tests для status badges.
- Component tests для dashboard cards.
- Integration-style UI tests для login flow.
- Integration-style UI tests для create goal -> generate roadmap -> complete task, если тестовая инфраструктура готова.

## 12. Docker Compose

### Цель

Обеспечить запуск всей системы одной командой.

### Задачи

- Создать Dockerfile для backend.
- Создать Dockerfile для frontend.
- Создать Dockerfile для AI-service.
- Создать `docker-compose.yml`.
- Добавить PostgreSQL service.
- Настроить network между сервисами.
- Настроить переменные окружения.
- Добавить health checks, если требуется.
- Проверить порядок старта сервисов.

### Результат

Frontend, backend, AI-service и PostgreSQL запускаются через Docker Compose.

### Критерии готовности

- `docker compose up` поднимает все сервисы.
- Frontend доступен в браузере.
- Backend доступен frontend.
- Backend доступен AI-service.
- Backend подключен к PostgreSQL.
- Данные сохраняются между перезапусками, если настроен volume.

### Что проверить вручную

- Запустить все сервисы через Docker Compose.
- Открыть frontend.
- Зарегистрироваться.
- Создать цель.
- Сгенерировать roadmap.
- Проверить logs backend и AI-service.
- Остановить и снова запустить compose.

### Какие тесты написать

- Минимальный smoke test backend health в контейнере.
- Минимальный smoke test AI-service health в контейнере.
- Проверка frontend build.
- Опциональный smoke script для полного основного пользовательского сценария.

## 13. Тестирование

### Цель

Покрыть критические сценарии текущего релиза автоматическими и ручными тестами.

### Задачи

- Настроить test profiles для backend.
- Настроить тестовую базу или Testcontainers.
- Настроить pytest для AI-service.
- Настроить frontend test runner.
- Написать backend integration tests для Auth, Notes, Goals, Tasks, Roadmap, Dashboard.
- Написать AI-service tests для генерации и ошибок.
- Написать AI-service tests для topic extraction и flashcard generation.
- Написать frontend component tests для основных компонентов.
- Подготовить manual test checklist.

### Результат

Критические сценарии текущего релиза проверяются перед пользовательской проверкой.

### Критерии готовности

- Backend tests проходят.
- AI-service tests проходят.
- Frontend tests проходят.
- Manual checklist описывает полный основной пользовательский сценарий.
- Основные ошибки API проверены.

### Что проверить вручную

- Полный сценарий нового пользователя.
- Сценарий недоступного AI-service.
- Сценарий пустого dashboard.
- Сценарий попытки доступа к чужим данным.
- Сценарий logout и повторного login.

### Какие тесты написать

- Backend: auth integration tests.
- Backend: tests проверки принадлежности данных пользователю для notes/goals/tasks.
- Backend: roadmap generation tests с mocked AI-service.
- Backend: materials/topics/flashcards tests.
- Backend: mastery score tests.
- Backend: knowledge graph tests.
- Backend: dashboard aggregation tests.
- AI-service: schema validation tests.
- AI-service: graceful degradation tests.
- Frontend: protected routes tests.
- Frontend: form validation tests.
- Frontend: key components render tests.

## 14. Финальная подготовка релиза

### Цель

Довести текущий релиз до состояния, пригодного для ревью и эксплуатации.

### Задачи

- Проверить все документы в корне проекта.
- Обновить README с инструкцией запуска.
- Подготовить test account или сценарий пользовательской проверки.
- Улучшить тексты ошибок во frontend.
- Проверить состояния загрузки.
- Проверить empty states.
- Проверить визуальную целостность UI.
- Удалить лишние debug logs.
- Проверить, что секреты не попали в репозиторий.
- Подготовить краткое описание архитектуры для ревью.

### Результат

Текущий релиз стабильно поддерживает основной пользовательский сценарий.

### Критерии готовности

- Проект запускается по README.
- Основной пользовательский сценарий проходит без ручного вмешательства в базу.
- UI выглядит аккуратно.
- Ошибки показываются понятным языком.
- Документация соответствует реализованному поведению.
- Нет очевидных security leaks.

### Что проверить вручную

- Полностью пройти сценарий:
  1. регистрация;
  2. создание цели;
  3. генерация roadmap;
  4. просмотр задач;
  5. выполнение задач;
  6. просмотр dashboard;
  7. создание заметки.
- Проверить проект после перезапуска Docker Compose.
- Проверить README на новой машине или в чистой папке, если возможно.

### Какие тесты написать

- Smoke test полного сценария backend API.
- Smoke test запуска Docker Compose.
- E2E test основного пользовательского сценария, если есть время.
- Regression tests для ошибок, найденных перед пользовательской проверкой.

## Порядок работы агента

Рекомендуемый порядок реализации:

1. Брать этапы строго сверху вниз.
2. Не начинать страницы frontend до готовности базовых backend endpoints.
3. Не подключать внешний AI API до готовности механизма graceful degradation.
4. После каждого этапа запускать тесты, относящиеся к этому этапу.
5. После каждого backend-модуля проверять принадлежность данных текущему пользователю.
6. После каждого UI-экрана проверять состояния загрузки, пустого списка и ошибки.
7. Не добавлять функции из будущих расширений до завершения первой стабильной версии.

## Граф зависимостей между этапами

```text
Настройка проекта
  -> Базовая структура backend
    -> Модуль auth
      -> Модуль notes
      -> Модуль learning goals
        -> Модуль tasks
        -> Базовая структура AI-service
          -> Интеграция генерации roadmap
            -> Materials, Topics, Flashcards и Knowledge Graph
              -> Dashboard API
                -> Базовая структура frontend
                  -> Страницы frontend
                    -> Docker Compose
                      -> Тестирование
                        -> Финальная подготовка релиза
```

Правила зависимостей:

- Auth должен быть готов до пользовательских данных.
- Learning Goals должны быть готовы до roadmap, tasks, materials и topics.
- AI-service foundation должен быть готов до integrations.
- Dashboard строится после появления goals, tasks, topics и flashcards.
- Страницы frontend подключаются после стабилизации API contracts.

## Стратегия миграций

- Все изменения БД выполняются через Flyway migrations.
- Каждая migration должна быть атомарной и иметь понятное имя.
- Сначала добавляются nullable поля и новые таблицы.
- Затем backend начинает писать новые данные.
- После стабилизации можно ужесточать constraints.
- Данные enum-like статусов должны мигрироваться явно.

Порядок для learning extension:

1. Добавить новые поля `learning_goals.type`, `estimated_duration`.
2. Добавить `study_materials`.
3. Добавить `topics`.
4. Добавить связи `topic_id` в `tasks` и `roadmap_steps`.
5. Добавить `flashcards` и `flashcard_reviews`.
6. Добавить `knowledge_nodes` и `knowledge_edges`.
7. Добавить индексы.

## Соображения по rollback

- Rollback должен быть безопасным для данных пользователя.
- Для destructive rollback требуется отдельное ручное решение.
- Новые nullable поля можно игнорировать старым кодом.
- Новые таблицы можно оставить неиспользуемыми при откате backend.
- Удаление таблиц с пользовательскими материалами, flashcards или reviews не выполняется автоматически.
- Ошибки AI-service не должны ломать существующий roadmap/task сценарий.

## Стратегия постепенной поставки

Рекомендуемая поставка:

1. Auth + base backend.
2. Goals + notes.
3. Tasks + progress.
4. AI-service roadmap.
5. Materials + topic extraction.
6. Flashcards + reviews.
7. Mastery score.
8. Knowledge graph.
9. Dashboard aggregation.
10. Frontend integration.

Каждый шаг должен оставлять систему в рабочем состоянии.

## Проверочный список после каждого этапа

После каждого этапа проверить:

- приложение запускается;
- миграции применяются на чистой БД;
- health endpoints отвечают;
- защищенные endpoints требуют JWT;
- пользователь не видит чужие данные;
- API response соответствует `api-spec.md`;
- ошибки возвращаются в едином формате;
- основные тесты этапа проходят;
- документация не расходится с реализованным контрактом.

Дополнительно каждый этап сверяется с [критериями готовности](definition-of-done.md). Если модуль не соответствует функциональным, архитектурным, тестовым, security и API-критериям, этап не считается завершенным.

## Критерии готовности для первой стабильной версии

Первая стабильная версия считается готовой, если:

- пользователь может зарегистрироваться;
- пользователь может войти;
- пользователь может создать заметку;
- пользователь может создать учебную цель;
- пользователь может сгенерировать roadmap;
- система автоматически создает задачи;
- пользователь может выполнить задачи;
- прогресс цели пересчитывается;
- dashboard показывает актуальную сводку;
- все компоненты запускаются через Docker Compose;
- основной пользовательский сценарий проходит стабильно.







