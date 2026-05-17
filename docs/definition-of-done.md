# Критерии готовности

## 1. Общие критерии

Модуль считается готовым, если:

- реализованы заявленные endpoints;
- бизнес-логика находится в слое сервисов;
- entity не возвращаются напрямую в API;
- проверяется принадлежность данных пользователю;
- ошибки возвращаются в едином формате;
- добавлены unit и integration tests для критичных сценариев;
- документация API соответствует поведению.

## 2. Auth

Функциональные критерии:

- регистрация работает;
- login возвращает JWT;
- `/api/v1/auth/me` возвращает текущего пользователя.

Архитектурные критерии:

- password hashing через BCrypt;
- JWT logic вынесена в отдельный service;
- security configuration не содержит бизнес-логику.

Тестовые критерии:

- register/login integration tests;
- tests для невалидного JWT;
- tests для занятого email.

Безопасность criteria:

- пароль не логируется;
- hash не возвращается в API.

API criteria:

- публичны только register/login;
- защищенные endpoints требуют JWT.

## 3. Notes

- CRUD заметок работает.
- Пользователь видит только свои заметки.
- DTO отделены от entity.
- Есть tests на проверку принадлежности данных пользователю.
- Ошибки `404` не раскрывают чужие ресурсы.

## 4. Learning

- `LearningGoal` поддерживает типы целей.
- Материалы связываются с целью.
- Topics создаются и имеют `masteryScore`.
- Roadmap связан с goal и topics.
- Services не создают отдельную exam-подсистему.
- Tests покрывают создание goal, topics и materials.

## 5. Roadmap

- Roadmap генерируется через координацию на стороне backend.
- AI-service response валидируется.
- Roadmap steps сохраняются атомарно.
- Tasks создаются из roadmap.
- Steps могут быть связаны с topics.
- Ошибки AI-service обработаны.

## 6. Tasks

- CRUD задач работает.
- Status update пересчитывает progress.
- Связь с topic обновляет mastery score.
- Пользователь не может менять чужие tasks.
- Tests покрывают status transitions.

## 7. Flashcards

- Flashcards генерируются по topics.
- Reviews сохраняются.
- `KNOW`, `PARTIAL`, `DONT_KNOW` обновляют mastery score объяснимо.
- История review не перезаписывается.
- Tests покрывают calculation rules.

## 8. AI-service

- Endpoints возвращают structured JSON:
  - `POST /api/ai/generate-roadmap`;
  - `POST /api/ai/extract-topics`;
  - `POST /api/ai/generate-flashcards`.
- Сервис не хранит состояние.
- Нет доступа к PostgreSQL.
- Есть validation schemas.
- Есть graceful degradation для roadmap.
- Tests покрывают valid, empty и invalid AI responses.

## 9. Dashboard

- Summary считается только по данным текущего пользователя.
- Отображаются goals, progress, tasks и weak topics.
- Empty state корректен.
- Tests покрывают агрегаты и изоляцию пользователей.

## 10. Documentation

- API spec обновлена.
- Database schema обновлена.
- User flows описывают основной сценарий.
- Implementation plan отражает зависимости этапов.


