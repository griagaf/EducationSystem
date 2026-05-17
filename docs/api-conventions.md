# Соглашения API

## 1. Назначение

Документ фиксирует соглашения для REST API Spring Boot backend.

Публичный backend API использует версионирование:

```text
/api/v1
```

Внутренний AI-service не версионируется на текущем этапе и использует:

```text
/api/ai
```

Frontend обращается только к backend API. Frontend не вызывает AI-service напрямую.

## 2. Соглашения по именованию

- Resource paths пишутся во множественном числе: `/notes`, `/goals`, `/tasks`.
- Path variables называются `{id}` в публичной спецификации и могут маппиться на `goalId`, `taskId` внутри backend.
- JSON поля пишутся в `camelCase`.
- Database поля пишутся в `snake_case`.
- DTO называются по действию и направлению: `CreateNoteRequest`, `NoteResponse`, `DashboardSummaryResponse`.
- Entity не возвращаются наружу напрямую.

## 3. REST-соглашения

- `GET` используется для чтения.
- `POST` используется для создания и командных операций.
- `PUT` используется для полного обновления ресурса.
- `PATCH` используется для частичного изменения, например статуса.
- `DELETE` используется для удаления или архивирования, если это явно описано.

Командные операции допускаются, если они отражают доменное действие:

- `POST /api/v1/goals/{id}/generate-roadmap`;
- `POST /api/v1/materials/{id}/extract-topics`;
- `POST /api/v1/goals/{id}/flashcards/generate`.

## 4. HTTP-статусы

- `200 OK` - успешное чтение или обновление.
- `201 Created` - успешное создание ресурса.
- `204 No Content` - успешное удаление без response body.
- `400 Bad Request` - ошибка валидации или некорректный формат запроса.
- `401 Unauthorized` - отсутствует или недействителен JWT.
- `403 Forbidden` - пользователь авторизован, но доступ запрещен.
- `404 Not Found` - ресурс не найден или не принадлежит пользователю.
- `409 Conflict` - конфликт состояния, например повторная генерация запрещена.
- `500 Internal Server Error` - непредвиденная ошибка backend.
- `502 Bad Gateway` - AI-service или AI-провайдер вернул невалидный ответ.
- `503 Service Unavailable` - AI-service недоступен.
- `504 Gateway Timeout` - истекло время ожидания ответа AI-service.

## 5. Формат ошибки

Единый формат ошибки:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "title": "must not be blank"
  },
  "traceId": "uuid"
}
```

Правила:

- `code` стабилен и пригоден для обработки frontend.
- `message` понятен пользователю или разработчику.
- `details` используется для field-level ошибок.
- `traceId` помогает связать ошибку с backend logs.
- Stack trace не возвращается в API.

## 5.1. Соглашения по response body

Для одиночного ресурса API возвращает объект:

```json
{
  "id": "uuid",
  "title": "Spring Boot",
  "createdAt": "2026-05-15T10:00:00Z",
  "updatedAt": "2026-05-15T10:00:00Z"
}
```

Для списков API возвращает:

- массив, если список небольшой и не требует пагинации;
- paginated response, если список может расти.

Для командных операций API возвращает результат команды, если frontend должен сразу отобразить созданные данные. Если тело ответа не требуется, используется `204 No Content`.

## 5.2. Примеры ошибок

Ошибка валидации:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "durationWeeks": "must be greater than 0"
  },
  "traceId": "uuid"
}
```

Ошибка аутентификации:

```json
{
  "code": "UNAUTHORIZED",
  "message": "Authentication is required",
  "details": {},
  "traceId": "uuid"
}
```

Ошибка AI-service:

```json
{
  "code": "AI_SERVICE_UNAVAILABLE",
  "message": "AI-service is unavailable",
  "details": {},
  "traceId": "uuid"
}
```

## 6. Стратегия пагинации

Для списков, которые могут расти, используется постраничная пагинация:

```text
?page=0&size=20
```

Response format:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 100,
  "totalPages": 5
}
```

Для небольших списков текущего релиза допустим массив без пагинации, если это явно указано в спецификации API.

## 7. Стратегия сортировки и фильтрации

Сортировка:

```text
?sort=createdAt,desc
```

Фильтрация:

```text
?status=ACTIVE&type=TECHNOLOGY_LEARNING
```

Правила:

- фильтры должны быть явными;
- backend всегда применяет фильтр по текущему пользователю;
- frontend не должен передавать `userId` для пользовательских данных.
- неизвестные query parameters по умолчанию считаются ошибкой запроса.

## 8. Формат времени

- Все временные метки возвращаются в ISO 8601.
- Рекомендуемый формат: `2026-05-15T10:00:00Z`.
- Backend хранит даты с часовым поясом, где это применимо.
- Date-only поля, например `targetDate`, передаются как `YYYY-MM-DD`.

## 9. Стратегия UUID

- Все публичные id передаются как строки UUID.
- Backend валидирует формат UUID до обращения к repository.
- Невалидный UUID возвращает `400 Bad Request`.
- Не найденный UUID возвращает `404 Not Found`.

## 10. Ошибки валидации

Валидация выполняется на request DTO.

Типовые ошибки:

- обязательное поле отсутствует;
- строка пустая;
- enum имеет недопустимое значение;
- число вне допустимого диапазона;
- файл имеет неподдерживаемый тип.

Ошибки валидации возвращаются с `400 Bad Request`.

## 11. Ошибки аутентификации

- Нет JWT: `401 Unauthorized`.
- JWT истек: `401 Unauthorized`.
- JWT невалиден: `401 Unauthorized`.
- Доступ к чужому ресурсу: предпочтительно `404 Not Found`, чтобы не раскрывать существование ресурса.

## 12. Ошибки AI-service

Backend преобразует ошибки AI-service в единый формат backend API.

Frontend не должен знать детали AI-провайдера.


