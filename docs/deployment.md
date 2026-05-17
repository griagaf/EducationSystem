# Развертывание

## 1. Назначение

Документ описывает deployment architecture для локального и контейнерного запуска через Docker Compose.

## 2. Архитектура Docker Compose

Состав контейнеров:

```text
frontend
backend
ai-service
postgres
```

Поток запросов:

```text
Browser -> frontend -> backend -> postgres
                         |
                         v
                    ai-service
```

## 3. Ответственность контейнеров

### frontend

- обслуживает React/Vite build;
- обращается только к backend API `/api/v1`;
- не знает URL AI-service.

### backend

- обрабатывает REST API;
- выполняет JWT-аутентификацию;
- содержит бизнес-логику;
- работает с PostgreSQL;
- вызывает AI-service.

### ai-service

- выполняет AI-операции без хранения состояния;
- не имеет доступа к PostgreSQL;
- возвращает structured JSON;
- использует graceful degradation при недоступности внешнего AI API.

### postgres

- хранит данные платформы;
- использует volume для сохранения данных между перезапусками.

## 4. Сеть

- Все сервисы находятся в одной Docker Compose network.
- Backend обращается к PostgreSQL по service name `postgres`.
- Backend обращается к AI-service по service name `ai-service`.
- Frontend обращается к backend через публичный URL backend или reverse proxy.
- AI-service не публикуется наружу без необходимости.

## 5. Переменные окружения

### backend

- `SPRING_DATASOURCE_URL`;
- `SPRING_DATASOURCE_USERNAME`;
- `SPRING_DATASOURCE_PASSWORD`;
- `JWT_SECRET`;
- `JWT_ACCESS_TOKEN_TTL`;
- `AI_SERVICE_BASE_URL`;
- `CORS_ALLOWED_ORIGINS`.

### ai-service

- `AI_API_BASE_URL`;
- `AI_API_KEY`;
- `AI_MODEL`;
- `AI_TIMEOUT_SECONDS`;
- `AI_MAX_RETRIES`;
- `AI_GRACEFUL_DEGRADATION_ENABLED`.

### frontend

- `VITE_API_BASE_URL`.

### postgres

- `POSTGRES_DB`;
- `POSTGRES_USER`;
- `POSTGRES_PASSWORD`.

## 6. Порты

Рекомендуемые локальные порты:

- frontend: `3000`;
- backend: `8080`;
- ai-service: внутренний порт `8000`;
- postgres: `5432`.

AI-service может не публиковаться на host, если доступ нужен только backend.

## 7. Volumes

Рекомендуемые volumes:

- volume данных PostgreSQL;
- опциональный volume локального хранилища для загруженных материалов.

Загруженные файлы должны храниться отдельно от строк БД. В БД хранится metadata и `storage_key`.

## 8. Порядок запуска

Рекомендуемый порядок:

1. `postgres`;
2. `ai-service`;
3. `backend`;
4. `frontend`.

Backend должен быть устойчив к временной недоступности AI-service и возвращать контролируемую ошибку или использовать graceful degradation на стороне AI-service.

## 9. Зависимости сервисов

- backend зависит от postgres;
- backend зависит от ai-service для AI-операций;
- frontend зависит от backend;
- ai-service не зависит от backend и database.

## 10. Стратегия секретов

- Секреты передаются через переменные окружения.
- `.env` не должен попадать в git.
- Для локальной разработки допустим `.env.example` без реальных секретов.
- Production secrets должны храниться во внешнем secret storage.

## 11. Локальный процесс разработки

1. Подготовить `.env`.
2. Запустить `docker compose up --build`.
3. Проверить backend health endpoint.
4. Проверить AI-service health endpoint.
5. Открыть frontend.
6. Пройти основной сценарий обучения.

## 12. Границы

Не добавляется:

- Kubernetes;
- микросервисная декомпозиция backend;
- service mesh;
- распределенная платформа трассировки.

Архитектура остается практичной и реализуемой одним разработчиком.


