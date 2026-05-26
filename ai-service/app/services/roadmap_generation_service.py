import json
from typing import Any

from pydantic import ValidationError

from app.clients.openai_compatible_client import OpenAICompatibleClient
from app.core.config import get_settings
from app.core.exceptions import AiServiceError, EmptyAiResultError, InvalidAiResponseError
from app.schemas.roadmap import GenerateRoadmapRequest, GenerateRoadmapResponse, RoadmapStepSchema, RoadmapTaskSchema


class RoadmapGenerationService:
    async def generate(self, request: GenerateRoadmapRequest) -> GenerateRoadmapResponse:
        settings = get_settings()
        client = OpenAICompatibleClient(settings)

        if not settings.ai_api_key:
            return self._generate_fallback(request)

        try:
            raw_response = await client.generate_json(
                self._build_system_prompt(),
                self._build_user_prompt(request),
            )
            return self._validate_response(raw_response)
        except AiServiceError:
            if settings.graceful_degradation_enabled:
                return self._generate_fallback(request)
            raise

    def _build_system_prompt(self) -> str:
        return (
            "You generate structured learning roadmaps for a personal learning platform. "
            "Return only valid JSON with roadmap_title, roadmap_description and steps. "
            "Each step must contain title, description, order_index, estimated_days, topics and tasks. "
            "Each task must contain title, description and priority: LOW, MEDIUM or HIGH."
        )

    def _build_user_prompt(self, request: GenerateRoadmapRequest) -> str:
        payload = {
            "goal_title": request.goal_title,
            "goal_description": request.goal_description,
            "goal_type": request.goal_type,
            "target_date": request.target_date.isoformat() if request.target_date else None,
            "estimated_duration_weeks": request.estimated_duration_weeks,
            "user_level": request.user_level,
        }
        return json.dumps(payload, ensure_ascii=False)

    def _validate_response(self, raw_response: dict[str, Any]) -> GenerateRoadmapResponse:
        try:
            response = GenerateRoadmapResponse.model_validate(raw_response)
        except ValidationError as exc:
            raise InvalidAiResponseError("AI response does not match roadmap schema") from exc

        if not response.steps:
            raise EmptyAiResultError("AI response must contain at least one roadmap step")

        for step in response.steps:
            if not step.tasks:
                raise InvalidAiResponseError("Each roadmap step must contain at least one task")
            if not step.topics:
                raise InvalidAiResponseError("Each roadmap step must contain at least one topic")

        return response

    def _generate_fallback(self, request: GenerateRoadmapRequest) -> GenerateRoadmapResponse:
        weeks = request.estimated_duration_weeks or 8
        step_count = min(max(weeks // 2, 3), 6)
        estimated_days = max(3, (weeks * 7) // step_count)

        templates = self._fallback_templates(request.goal_type, request.user_level)
        steps: list[RoadmapStepSchema] = []

        for index in range(step_count):
            template = templates[index % len(templates)]
            priority = "HIGH" if index == step_count - 1 else "MEDIUM"
            steps.append(
                RoadmapStepSchema(
                    title=template["title"],
                    description=f"{template['description']} Цель: {request.goal_title}.",
                    order_index=index + 1,
                    estimated_days=estimated_days,
                    topics=template["topics"],
                    tasks=[
                        RoadmapTaskSchema(
                            title=template["task_title"],
                            description=template["task_description"],
                            priority=priority,
                        ),
                        RoadmapTaskSchema(
                            title=f"Закрепить этап {index + 1} на практике",
                            description="Выполнить небольшое практическое задание и зафиксировать результаты в заметках.",
                            priority="MEDIUM",
                        ),
                    ],
                )
            )

        return GenerateRoadmapResponse(
            roadmap_title=f"Roadmap: {request.goal_title}",
            roadmap_description=(
                f"Структурированный план на {weeks} недель для уровня {request.user_level}. "
                "Roadmap сформирован механизмом graceful degradation."
            ),
            steps=steps,
        )

    def _fallback_templates(self, goal_type: str, user_level: str) -> list[dict[str, Any]]:
        base_templates = [
            {
                "title": "Контекст и базовые понятия",
                "description": "Разобрать цель, ключевые термины и минимальную теоретическую основу.",
                "topics": ["Базовые понятия", "Термины", "Цели обучения"],
                "task_title": "Составить краткий конспект базовых понятий",
                "task_description": "Выделить основные термины, вопросы и ожидаемые результаты этапа.",
            },
            {
                "title": "Основные темы и практика",
                "description": "Перейти к центральным темам и регулярно закреплять материал упражнениями.",
                "topics": ["Основные темы", "Практические упражнения", "Разбор ошибок"],
                "task_title": "Выполнить практический набор заданий",
                "task_description": "Применить изученные темы на небольших самостоятельных задачах.",
            },
            {
                "title": "Интеграция знаний",
                "description": "Связать отдельные темы в единую картину и устранить пробелы.",
                "topics": ["Связи между темами", "Повторение", "Пробелы в знаниях"],
                "task_title": "Собрать карту изученных тем",
                "task_description": "Описать связи между темами и отметить слабые места для повторения.",
            },
            {
                "title": "Итоговая практика",
                "description": "Проверить знания через комплексную практическую работу.",
                "topics": ["Итоговая работа", "Самопроверка", "Качество результата"],
                "task_title": "Подготовить итоговый практический результат",
                "task_description": "Собрать результат, который показывает владение ключевыми темами цели.",
            },
        ]

        if goal_type == "EXAM_PREPARATION":
            base_templates.append(
                {
                    "title": "Пробное тестирование",
                    "description": "Отработать формат проверки знаний и типовые ошибки.",
                    "topics": ["Пробный экзамен", "Типовые вопросы", "Стратегия ответа"],
                    "task_title": "Пройти пробную проверку",
                    "task_description": "Решить тренировочный вариант и разобрать неправильные ответы.",
                }
            )

        if user_level == "advanced":
            base_templates.append(
                {
                    "title": "Углубление и оптимизация",
                    "description": "Разобрать сложные случаи и улучшить качество решений.",
                    "topics": ["Сложные сценарии", "Оптимизация", "Best practices"],
                    "task_title": "Разобрать сложный практический кейс",
                    "task_description": "Выбрать сложный пример и описать принятые инженерные решения.",
                }
            )

        return base_templates
