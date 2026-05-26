# API Specification

## 1. Общие правила

REST API используется frontend-приложением для работы с Spring Boot backend.

Базовый префикс:

```text
/api/v1
```

Версионирование применяется только к backend API. Внутренний AI-service остается без версии и использует `/api/ai/...`, потому что он не является публичным контрактом frontend.

Формат данных:

```text
JSON
```

Для защищенных endpoints используется JWT:

```text
Authorization: Bearer <accessToken>
```

## 2. Общий формат ошибки

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "field": "error description"
  },
  "traceId": "uuid"
}
```

Типовые ошибки:

- `400 Bad Request` - некорректный request body или параметры;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `403 Forbidden` - нет доступа к ресурсу;
- `404 Not Found` - ресурс не найден;
- `409 Conflict` - конфликт данных, например email уже занят;
- `500 Internal Server Error` - непредвиденная ошибка backend;
- `502 Bad Gateway` - некорректный ответ AI-service;
- `503 Service Unavailable` - AI-service недоступен;
- `504 Gateway Timeout` - timeout при обращении к AI-service.

## 3. System API

### GET /api/v1/health

Назначение: проверка доступности Spring Boot backend.

Метод: `GET`

JWT: не требуется.

Request body: отсутствует.

Response body:

```json
{
  "status": "UP",
  "service": "backend",
  "timestamp": "2026-05-21T12:46:51Z"
}
```

Возможные ошибки:

- `500 Internal Server Error` - backend запущен, но не может обработать запрос.

## 4. Auth API

### POST /api/v1/auth/register

Назначение: регистрация нового пользователя.

Метод: `POST`

JWT: не требуется.

Request body:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Student"
}
```

Response body:

```json
{
  "accessToken": "jwt-token",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Student"
  }
}
```

Возможные ошибки:

- `400 Bad Request` - email, password или displayName не прошли валидацию;
- `409 Conflict` - пользователь с таким email уже существует;
- `500 Internal Server Error` - ошибка регистрации.

### POST /api/v1/auth/login

Назначение: вход пользователя в систему.

Метод: `POST`

JWT: не требуется.

Request body:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response body:

```json
{
  "accessToken": "jwt-token",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Student"
  }
}
```

Возможные ошибки:

- `400 Bad Request` - некорректный формат данных;
- `401 Unauthorized` - неверный email или пароль;
- `500 Internal Server Error` - ошибка авторизации.

### GET /api/v1/auth/me

Назначение: получение данных текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "displayName": "Student"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - пользователь не найден;
- `500 Internal Server Error` - ошибка получения пользователя.

## 5. Notes API

### GET /api/v1/notes

Назначение: получение списка заметок текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
[
  {
    "id": "uuid",
    "learningGoalId": "uuid",
    "title": "Spring Boot notes",
    "contentPreview": "IoC, DI, Beans...",
    "createdAt": "2026-05-15T10:00:00Z",
    "updatedAt": "2026-05-15T10:00:00Z"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `500 Internal Server Error` - ошибка получения заметок.

### POST /api/v1/notes

Назначение: создание заметки.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "learningGoalId": "uuid",
  "title": "Spring Boot notes",
  "content": "Important concepts about Spring Boot..."
}
```

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "title": "Spring Boot notes",
  "content": "Important concepts about Spring Boot...",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - title или content не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - указанная учебная цель не найдена;
- `500 Internal Server Error` - ошибка создания заметки.

### GET /api/v1/notes/{id}

Назначение: получение одной заметки текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "title": "Spring Boot notes",
  "content": "Important concepts about Spring Boot...",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - заметка не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения заметки.

### PUT /api/v1/notes/{id}

Назначение: обновление заметки.

Метод: `PUT`

JWT: требуется.

Request body:

```json
{
  "learningGoalId": "uuid",
  "title": "Updated Spring Boot notes",
  "content": "Updated note content..."
}
```

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "title": "Updated Spring Boot notes",
  "content": "Updated note content...",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T11:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - данные не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - заметка или учебная цель не найдена;
- `500 Internal Server Error` - ошибка обновления заметки.

### DELETE /api/v1/notes/{id}

Назначение: удаление заметки.

Метод: `DELETE`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "deleted": true
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - заметка не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка удаления заметки.

## 6. Learning Goals API

### GET /api/v1/goals

Назначение: получение списка учебных целей текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
[
  {
    "id": "uuid",
    "title": "Изучить Java Spring Boot",
    "description": "Хочу изучить Java Spring Boot за 3 месяца",
    "type": "TECHNOLOGY_LEARNING",
    "status": "ACTIVE",
    "targetDate": "2026-08-15",
    "durationWeeks": 12,
    "estimatedDuration": "12 weeks",
    "progressPercent": 0,
    "createdAt": "2026-05-15T10:00:00Z",
    "updatedAt": "2026-05-15T10:00:00Z"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `500 Internal Server Error` - ошибка получения целей.

### POST /api/v1/goals

Назначение: создание учебной цели.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "title": "Изучить Java Spring Boot",
  "description": "Хочу изучить Java Spring Boot за 3 месяца",
  "type": "TECHNOLOGY_LEARNING",
  "targetDate": "2026-08-15",
  "durationWeeks": 12,
  "estimatedDuration": "12 weeks"
}
```

Response body:

```json
{
  "id": "uuid",
  "title": "Изучить Java Spring Boot",
  "description": "Хочу изучить Java Spring Boot за 3 месяца",
  "type": "TECHNOLOGY_LEARNING",
  "status": "ACTIVE",
  "targetDate": "2026-08-15",
  "durationWeeks": 12,
  "estimatedDuration": "12 weeks",
  "progressPercent": 0,
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - title, description или durationWeeks не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `500 Internal Server Error` - ошибка создания цели.

### GET /api/v1/goals/{id}

Назначение: получение деталей учебной цели.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "title": "Изучить Java Spring Boot",
  "description": "Хочу изучить Java Spring Boot за 3 месяца",
  "type": "TECHNOLOGY_LEARNING",
  "status": "ACTIVE",
  "targetDate": "2026-08-15",
  "durationWeeks": 12,
  "estimatedDuration": "12 weeks",
  "progressPercent": 35,
  "roadmapId": "uuid",
  "topics": [
    {
      "id": "uuid",
      "title": "Spring IoC",
      "masteryScore": 40,
      "difficultyLevel": "MEDIUM"
    }
  ],
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T12:00:00Z"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - цель не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения цели.

### PUT /api/v1/goals/{id}

Назначение: обновление учебной цели.

Метод: `PUT`

JWT: требуется.

Request body:

```json
{
  "title": "Изучить Spring Boot и Spring Безопасность",
  "description": "Обновленное описание цели",
  "type": "TECHNOLOGY_LEARNING",
  "status": "ACTIVE",
  "targetDate": "2026-08-30",
  "durationWeeks": 14,
  "estimatedDuration": "14 weeks"
}
```

Response body:

```json
{
  "id": "uuid",
  "title": "Изучить Spring Boot и Spring Безопасность",
  "description": "Обновленное описание цели",
  "type": "TECHNOLOGY_LEARNING",
  "status": "ACTIVE",
  "targetDate": "2026-08-30",
  "durationWeeks": 14,
  "estimatedDuration": "14 weeks",
  "progressPercent": 35,
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T12:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - данные не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - цель не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка обновления цели.

### DELETE /api/v1/goals/{id}

Назначение: удаление или архивирование учебной цели.

Метод: `DELETE`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "deleted": true
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - цель не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка удаления цели.

## 7. Study Materials API

### POST /api/v1/goals/{id}/materials

Назначение: загрузка TXT-материала для учебной цели.

Метод: `POST`

JWT: требуется.

Request body: `multipart/form-data`

Поля:

- `file` - загружаемый файл.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "fileName": "spring-notes.txt",
  "contentType": "text/plain",
  "fileSize": 4096,
  "processingStatus": "TEXT_EXTRACTED",
  "extractedText": "Extracted text from TXT material",
  "createdAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - неподдерживаемый формат файла;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - учебная цель не найдена;
- `500 Internal Server Error` - ошибка сохранения материала.

### GET /api/v1/goals/{id}/materials

Назначение: получение материалов учебной цели.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
[
  {
    "id": "uuid",
    "learningGoalId": "uuid",
    "fileName": "spring-notes.txt",
    "contentType": "text/plain",
    "fileSize": 4096,
    "processingStatus": "TEXT_EXTRACTED",
    "extractedText": "Extracted text from TXT material",
    "createdAt": "2026-05-15T10:00:00Z"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - учебная цель не найдена;
- `500 Internal Server Error` - ошибка получения материалов.

### GET /api/v1/materials/{id}

Назначение: получение одного материала текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "fileName": "spring-notes.txt",
  "contentType": "text/plain",
  "fileSize": 4096,
  "processingStatus": "TEXT_EXTRACTED",
  "extractedText": "Extracted text from TXT material",
  "createdAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - материал не найден или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения материала.

### POST /api/v1/materials/{id}/extract-topics

Назначение: извлечение тем из материала через AI-service.

Статус: запланировано для следующих версий. В текущем backend-этапе реализованы загрузка TXT, сохранение metadata и чтение материалов.

Метод: `POST`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "materialId": "uuid",
  "topics": [
    {
      "id": "uuid",
      "title": "Spring IoC",
      "description": "Inversion of Control and dependency injection basics",
      "masteryScore": 0,
      "difficultyLevel": "MEDIUM"
    }
  ]
}
```

Возможные ошибки:

- `400 Bad Request` - текст материала пустой или не извлечен;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - материал не найден;
- `502 Bad Gateway` - AI-service вернул некорректный ответ;
- `503 Service Unavailable` - AI-service недоступен.

## 8. Topics API

### GET /api/v1/goals/{id}/topics

Назначение: получение тем учебной цели с mastery score.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
[
  {
    "id": "uuid",
    "learningGoalId": "uuid",
    "title": "Spring IoC",
    "description": "Inversion of Control and dependency injection basics",
    "masteryScore": 40,
    "difficultyLevel": "MEDIUM"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - учебная цель не найдена;
- `500 Internal Server Error` - ошибка получения topics.

## 9. Roadmap API

### POST /api/v1/goals/{id}/generate-roadmap

Назначение: генерация roadmap по учебной цели через AI-service.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "userLevel": "beginner",
  "includeMaterials": true
}
```

Response body:

```json
{
  "roadmap": {
    "id": "uuid",
    "learningGoalId": "uuid",
    "title": "Java Spring Boot за 12 недель",
    "description": "План изучения Spring Boot от основ до pet project",
    "steps": [
      {
        "id": "uuid",
        "topicId": "uuid",
        "title": "Основы Java backend",
        "description": "HTTP, REST, SQL и базовые принципы backend",
        "orderIndex": 1,
        "estimatedDays": 14,
        "status": "NOT_STARTED"
      }
    ]
  },
  "topics": [
    {
      "id": "uuid",
      "title": "HTTP и REST",
      "masteryScore": 0,
      "difficultyLevel": "EASY"
    }
  ],
  "createdTasks": [
    {
      "id": "uuid",
      "title": "Повторить HTTP методы",
      "status": "TODO",
      "priority": "MEDIUM"
    }
  ]
}
```

Возможные ошибки:

- `400 Bad Request` - некорректный userLevel;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - учебная цель не найдена;
- `409 Conflict` - roadmap для цели уже существует, если повторная генерация запрещена;
- `502 Bad Gateway` - AI-service вернул некорректный ответ;
- `503 Service Unavailable` - AI-service недоступен;
- `504 Gateway Timeout` - истекло время ожидания ответа AI-service;
- `500 Internal Server Error` - ошибка генерации roadmap.

### GET /api/v1/goals/{id}/roadmap

Назначение: получение roadmap по учебной цели.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "title": "Java Spring Boot за 12 недель",
  "description": "План изучения Spring Boot от основ до pet project",
  "steps": [
    {
      "id": "uuid",
      "topicId": "uuid",
      "title": "Основы Java backend",
      "description": "HTTP, REST, SQL и базовые принципы backend",
      "orderIndex": 1,
      "estimatedDays": 14,
      "status": "NOT_STARTED",
    }
  ]
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - цель или roadmap не найден;
- `500 Internal Server Error` - ошибка получения roadmap.

### GET /api/v1/roadmaps/{id}

Назначение: получение roadmap по id.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "title": "Java Spring Boot за 12 недель",
  "description": "План изучения Spring Boot от основ до pet project",
  "steps": [
    {
      "id": "uuid",
      "topicId": "uuid",
      "title": "Основы Java backend",
      "description": "HTTP, REST, SQL и базовые принципы backend",
      "orderIndex": 1,
      "estimatedDays": 14,
      "status": "NOT_STARTED"
    }
  ]
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - roadmap не найден или принадлежит цели другого пользователя;
- `500 Internal Server Error` - ошибка получения roadmap.

## 10. Tasks API

### GET /api/v1/tasks

Назначение: получение списка задач текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Query parameters:

- `status` - опционально: `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`;
- `priority` - опционально: `LOW`, `MEDIUM`, `HIGH`;
- `learningGoalId` - опционально, UUID учебной цели.

Response body:

```json
[
  {
    "id": "uuid",
    "learningGoalId": "uuid",
    "learningGoalTitle": "Изучить Java Spring Boot",
    "roadmapStepId": "uuid",
    "topicId": "uuid",
    "title": "Повторить HTTP методы",
    "description": "Разобрать основные HTTP методы и status codes",
    "status": "TODO",
    "priority": "MEDIUM",
    "dueDate": "2026-05-30",
    "createdAt": "2026-05-15T10:00:00Z",
    "updatedAt": "2026-05-15T10:00:00Z"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `500 Internal Server Error` - ошибка получения задач.

### GET /api/v1/tasks/{taskId}

Назначение: получение одной задачи текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "learningGoalTitle": "Изучить Java Spring Boot",
  "roadmapStepId": "uuid",
  "topicId": "uuid",
  "title": "Повторить HTTP методы",
  "description": "Разобрать основные HTTP методы и status codes",
  "status": "TODO",
  "priority": "MEDIUM",
  "dueDate": "2026-05-30",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - задача не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения задачи.

### POST /api/v1/tasks

Назначение: создание задачи.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "learningGoalId": "uuid",
  "roadmapStepId": "uuid",
  "topicId": "uuid",
  "title": "Сделать REST API practice task",
  "description": "Создать простой CRUD endpoint",
  "priority": "HIGH",
  "dueDate": "2026-06-10"
}
```

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "learningGoalTitle": "Изучить Java Spring Boot",
  "roadmapStepId": "uuid",
  "topicId": "uuid",
  "title": "Сделать REST API practice task",
  "description": "Создать простой CRUD endpoint",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-06-10",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - данные задачи не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - цель или этап roadmap не найден;
- `500 Internal Server Error` - ошибка создания задачи.

### PUT /api/v1/tasks/{taskId}

Назначение: обновление задачи.

Метод: `PUT`

JWT: требуется.

Request body:

```json
{
  "title": "Сделать REST API practice project",
  "description": "Создать CRUD API для заметок",
  "priority": "HIGH",
  "dueDate": "2026-06-12"
}
```

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "learningGoalTitle": "Изучить Java Spring Boot",
  "roadmapStepId": "uuid",
  "topicId": "uuid",
  "title": "Сделать REST API practice project",
  "description": "Создать CRUD API для заметок",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-06-12",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T12:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - данные не прошли валидацию;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - задача не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка обновления задачи.

### DELETE /api/v1/tasks/{taskId}

Назначение: удаление задачи.

Метод: `DELETE`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "deleted": true
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - задача не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка удаления задачи.

### PATCH /api/v1/tasks/{taskId}/status

Назначение: изменение статуса задачи.

Метод: `PATCH`

JWT: требуется.

Request body:

```json
{
  "status": "DONE"
}
```

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "learningGoalTitle": "Изучить Java Spring Boot",
  "roadmapStepId": "uuid",
  "topicId": "uuid",
  "title": "Повторить HTTP методы",
  "description": "Разобрать основные HTTP методы и status codes",
  "status": "DONE",
  "priority": "MEDIUM",
  "dueDate": "2026-05-30",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T12:00:00Z"
}
```

Возможные ошибки:

- `400 Bad Request` - недопустимый статус;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - задача не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка изменения статуса.

## 11. Flashcards API

### POST /api/v1/topics/{id}/generate-flashcards

Назначение: генерация flashcards по выбранной теме.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "count": 5
}
```

Response body:

```json
{
  "createdFlashcards": [
    {
      "id": "uuid",
      "topicId": "uuid",
      "question": "What is dependency injection?",
      "answer": "A pattern where dependencies are provided from outside the object.",
      "difficulty": "MEDIUM"
    }
  ]
}
```

Возможные ошибки:

- `400 Bad Request` - некорректное количество карточек;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - topic не найден или принадлежит другому пользователю;
- `502 Bad Gateway` - AI-service вернул некорректный ответ;
- `503 Service Unavailable` - AI-service недоступен.

### GET /api/v1/topics/{id}/flashcards

Назначение: получение flashcards выбранной темы.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
[
  {
    "id": "uuid",
    "topicId": "uuid",
    "question": "What is dependency injection?",
    "answer": "A pattern where dependencies are provided from outside the object.",
    "difficulty": "MEDIUM"
  }
]
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - topic не найден или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения flashcards.

### GET /api/v1/flashcards/{id}

Назначение: получение одной flashcard текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "id": "uuid",
  "learningGoalId": "uuid",
  "topicId": "uuid",
  "question": "What is dependency injection?",
  "answer": "A pattern where dependencies are provided from outside the object.",
  "difficulty": "MEDIUM"
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - flashcard не найдена или принадлежит другому пользователю;
- `500 Internal Server Error` - ошибка получения flashcard.

### POST /api/v1/flashcards/{id}/review

Назначение: сохранение результата прохождения карточки и обновление mastery score темы.

Метод: `POST`

JWT: требуется.

Request body:

```json
{
  "result": "PARTIAL"
}
```

Response body:

```json
{
  "review": {
    "id": "uuid",
    "flashcardId": "uuid",
    "result": "PARTIAL",
    "reviewedAt": "2026-05-15T12:00:00Z"
  },
  "topicMasteryScore": 45
}
```

Возможные ошибки:

- `400 Bad Request` - недопустимый result;
- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - flashcard не найдена;
- `500 Internal Server Error` - ошибка сохранения review.

## 12. Knowledge Graph API

### GET /api/v1/goals/{id}/knowledge-graph

Назначение: получение visualization-ready knowledge graph для учебной цели.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "nodes": [
    {
      "id": "uuid",
      "nodeType": "TOPIC",
      "sourceType": "TOPIC",
      "sourceId": "uuid",
      "title": "Spring IoC",
      "masteryScore": 40
    }
  ],
  "edges": [
    {
      "id": "uuid",
      "sourceNodeId": "uuid",
      "targetNodeId": "uuid",
      "edgeType": "COVERS_TOPIC",
      "weight": 1.0
    }
  ]
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `404 Not Found` - учебная цель не найдена;
- `500 Internal Server Error` - ошибка получения graph.

## 13. Dashboard API

### GET /api/v1/dashboard/summary

Назначение: получение сводки dashboard для текущего пользователя.

Метод: `GET`

JWT: требуется.

Request body: отсутствует.

Response body:

```json
{
  "totalGoals": 3,
  "activeGoals": 2,
  "totalTasks": 24,
  "completedTasks": 10,
  "taskCompletionPercent": 41,
  "averageMasteryScore": 38,
  "weakTopics": [
    {
      "id": "uuid",
      "learningGoalId": "uuid",
      "title": "Spring Безопасность",
      "masteryScore": 20,
      "difficultyLevel": "HARD"
    }
  ],
  "recentGoals": [
    {
      "id": "uuid",
      "title": "Изучить Java Spring Boot",
      "type": "TECHNOLOGY_LEARNING",
      "status": "ACTIVE",
      "progressPercent": 42,
      "createdAt": "2026-05-15T10:00:00Z"
    }
  ],
  "upcomingTasks": [
    {
      "id": "uuid",
      "learningGoalId": "uuid",
      "learningGoalTitle": "Изучить Java Spring Boot",
      "title": "Сделать REST API practice task",
      "status": "TODO",
      "priority": "HIGH",
      "dueDate": "2026-06-10"
    }
  ]
}
```

Возможные ошибки:

- `401 Unauthorized` - JWT отсутствует или недействителен;
- `500 Internal Server Error` - ошибка получения dashboard summary.

## 14. AI-service Internal API

Эти endpoints вызываются Spring Boot backend. Frontend не обращается к AI-service напрямую.

### POST /api/ai/generate-roadmap

Назначение: генерация structured roadmap по цели, темам и материалам.

JWT: не используется на AI-service. Доступ ограничивается сетевой конфигурацией.

Request body:

```json
{
  "goal_title": "Изучить Java Spring Boot",
  "goal_description": "Хочу изучить Spring Boot за 12 недель",
  "goal_type": "TECHNOLOGY_LEARNING",
  "target_date": "2026-08-15",
  "estimated_duration_weeks": 12,
  "user_level": "beginner"
}
```

Response body:

```json
{
  "roadmap_title": "Java Spring Boot за 12 недель",
  "roadmap_description": "План изучения Spring Boot",
  "steps": [
    {
      "title": "Основы Java backend",
      "description": "HTTP, REST, SQL и базовые принципы backend-разработки",
      "order_index": 1,
      "estimated_days": 14,
      "topics": [
        "HTTP и REST",
        "SQL basics"
      ],
      "tasks": [
        {
          "title": "Повторить HTTP методы",
          "description": "Разобрать GET, POST, PUT, PATCH, DELETE и основные status codes",
          "priority": "MEDIUM"
        }
      ]
    }
  ]
}
```

### POST /api/ai/extract-topics

Назначение: извлечение topics из текста материала.

Request body:

```json
{
  "goal_title": "Изучить Java Spring Boot",
  "material_title": "spring-notes.pdf",
  "text": "Extracted material text"
}
```

Response body:

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

### POST /api/ai/generate-flashcards

Назначение: генерация flashcards по одной теме. Backend вызывает endpoint отдельно для выбранной темы и сохраняет полученные карточки в своей базе данных.

Request body:

```json
{
  "goal_title": "Изучить Java Spring Boot",
  "topic_title": "Spring IoC",
  "topic_description": "Dependency injection basics",
  "difficulty_level": "MEDIUM",
  "count": 5
}
```

Response body:

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

Возможные ошибки AI-service:

- `400 Bad Request` - некорректный request body;
- `502 Bad Gateway` - AI-провайдер вернул невалидный ответ;
- `503 Service Unavailable` - AI-провайдер недоступен;
- `504 Gateway Timeout` - истекло время ожидания ответа AI-провайдера.

## 15. Правила принадлежности данных

Все защищенные endpoints возвращают и изменяют только данные текущего пользователя.

Правила:

- пользователь не может получить чужую заметку;
- пользователь не может получить чужую цель;
- пользователь не может получить roadmap чужой цели;
- пользователь не может изменить чужую задачу;
- dashboard строится только по данным текущего пользователя.

При попытке доступа к чужому ресурсу backend может вернуть:

- `403 Forbidden`, если нужно явно показать запрет;
- `404 Not Found`, если нужно скрыть существование ресурса.

Для текущего релиза предпочтительно использовать `404 Not Found`.







