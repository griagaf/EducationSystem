# Knowledge Graph

## 1. Назначение

Knowledge graph дает visualization-ready представление знаний пользователя внутри учебной цели.

Первая стабильная версия graph не является AI-heavy подсистемой. Graph строится из уже существующих сущностей: goal, topics, roadmap steps и tasks.

## 2. Источники graph

Graph строится из:

- `LearningGoal`;
- `Topic`;
- `RoadmapStep`;
- `Task`;
- связей roadmap steps;
- связей tasks с topics.

Пользовательские материалы влияют на graph через extracted topics.

## 3. Сущности

### KnowledgeNode

Типы узлов:

- `GOAL`;
- `TOPIC`;
- `ROADMAP_STEP`;
- `TASK`.

Каждый узел содержит:

- `nodeType`;
- `sourceType`;
- `sourceId`;
- `title`;
- `description`;
- `masteryScore`, если применимо.

### KnowledgeEdge

Типы связей:

- `CONTAINS`;
- `PREREQUISITE_OF`;
- `RELATED_TO`;
- `GENERATED_TASK`;
- `COVERS_TOPIC`.

## 4. Построение graph

Базовые правила:

- goal содержит topics;
- topic покрывается roadmap step;
- roadmap step создает tasks;
- task покрывает topic;
- соседние roadmap steps могут быть связаны как prerequisite chain.

Graph builder должен быть deterministic и explainable. AI-service не строит graph напрямую и не хранит graph.

## 5. API

Backend endpoint:

- `GET /api/v1/goals/{id}/knowledge-graph`.

Response должен быть visualization-ready:

```json
{
  "nodes": [],
  "edges": []
}
```

## 6. Ограничения

Не реализуется:

- графовая БД;
- embeddings;
- семантический поиск;
- графовые нейронные сети;
- автоматическая сложная онтология;
- совместное редактирование графа в реальном времени.

## 7. Расширение

Следующие версии могут добавить:

- pgvector embeddings для nodes;
- дополнительные типы edge;
- graph analytics;
- поиск related topics;
- рекомендации на основе слабых тем.


