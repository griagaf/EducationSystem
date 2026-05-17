# Наблюдаемость

## 1. Назначение

Документ описывает готовность системы к наблюдаемости: логи, health checks и диагностические данные.

Prometheus, Grafana и полноценная трассировка не внедряются в текущем релизе, но архитектура не должна мешать их добавлению.

## 2. Структурированные логи

Backend и AI-service должны писать структурированные логи.

Рекомендуемые поля:

- `timestamp`;
- `level`;
- `service`;
- `traceId`;
- `userId`, если безопасно;
- `operation`;
- `durationMs`;
- `errorCode`;
- `message`.

Не логировать:

- пароли;
- JWT;
- полные API keys;
- полный текст пользовательских материалов;
- полный prompt, если он содержит пользовательские данные.

## 3. Логирование ошибок backend

Backend логирует:

- unhandled exceptions;
- validation failures на debug/info уровне;
- security failures без раскрытия секретов;
- ошибки обращения к AI-service;
- ошибки обработки файлов.

Каждая ошибка API должна иметь `traceId`.

## 4. Логирование AI-запросов

Таблица `ai_request_logs` хранит техническую информацию:

- `user_id`;
- `learning_goal_id`;
- `request_type`;
- `provider`;
- `model`;
- `status`;
- `prompt_preview`;
- `response_preview`;
- `error_message`;
- `duration_ms`;
- `created_at`.

Назначение:

- диагностика AI-ошибок;
- анализ стабильности AI-интеграции;
- проверка graceful degradation.

## 5. Health checks

Рекомендуемые endpoints:

Backend:

```text
GET /health
```

AI-service:

```text
GET /health
```

Health response должен быть простым:

```json
{
  "status": "UP"
}
```

## 6. Readiness checks

Backend readiness должен учитывать:

- доступность PostgreSQL;
- успешное применение миграций;
- базовую готовность приложения принимать API-запросы.

AI-service readiness должен учитывать:

- загрузку настроек;
- готовность FastAPI приложения;
- наличие graceful degradation, если внешний AI API не настроен.

## 7. Готовность к мониторингу

Система должна быть готова к будущему добавлению:

- метрик задержки HTTP;
- счетчиков ошибок API;
- счетчиков AI-запросов;
- времени ответа AI-service;
- количества операций graceful degradation.

Реализация метрик не входит в текущий объем.

## 8. Операционные сигналы

Минимальные сигналы для сопровождения:

- рост `5xx` ошибок backend;
- рост `AI_UNAVAILABLE`;
- частое использование graceful degradation;
- ошибки миграций;
- ошибки извлечения текста из материалов;
- рост времени генерации roadmap.


