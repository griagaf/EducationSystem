import json
from typing import Any

from pydantic import ValidationError

from app.clients.openai_compatible_client import OpenAICompatibleClient
from app.core.config import get_settings
from app.core.exceptions import AiServiceError, EmptyAiResultError, InvalidAiResponseError
from app.schemas.flashcards import FlashcardSchema, GenerateFlashcardsRequest, GenerateFlashcardsResponse


class FlashcardGenerationService:
    async def generate(self, request: GenerateFlashcardsRequest) -> GenerateFlashcardsResponse:
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
            "You generate flashcards for a personal learning platform. "
            "Return only valid JSON with a flashcards array. "
            "Each flashcard must contain question, answer and difficulty. "
            "Difficulty must be one of EASY, MEDIUM or HARD. "
            "Questions must check understanding, not only memorization."
        )

    def _build_user_prompt(self, request: GenerateFlashcardsRequest) -> str:
        payload = {
            "goal_title": request.goal_title,
            "topic_title": request.topic_title,
            "topic_description": request.topic_description,
            "difficulty_level": request.difficulty_level,
            "count": request.count,
            "response_schema": {
                "flashcards": [
                    {
                        "question": "string",
                        "answer": "string",
                        "difficulty": "EASY | MEDIUM | HARD",
                    }
                ]
            },
        }
        return json.dumps(payload, ensure_ascii=False)

    def _validate_response(self, raw_response: dict[str, Any]) -> GenerateFlashcardsResponse:
        try:
            response = GenerateFlashcardsResponse.model_validate(raw_response)
        except ValidationError as exc:
            raise InvalidAiResponseError("AI response does not match flashcards schema") from exc

        if not response.flashcards:
            raise EmptyAiResultError("AI response must contain at least one flashcard")

        return response

    def _generate_fallback(self, request: GenerateFlashcardsRequest) -> GenerateFlashcardsResponse:
        card_count = max(request.count, 5)
        templates = self._fallback_templates()
        flashcards: list[FlashcardSchema] = []

        for index in range(card_count):
            template = templates[index % len(templates)]
            flashcards.append(
                FlashcardSchema(
                    question=template["question"].format(topic=request.topic_title, goal=request.goal_title),
                    answer=template["answer"].format(
                        topic=request.topic_title,
                        description=request.topic_description,
                        goal=request.goal_title,
                    ),
                    difficulty=request.difficulty_level,
                )
            )

        return GenerateFlashcardsResponse(flashcards=flashcards)

    def _fallback_templates(self) -> list[dict[str, str]]:
        return [
            {
                "question": "Что означает тема «{topic}» в контексте цели «{goal}»?",
                "answer": "Тема «{topic}» относится к следующему учебному контексту: {description}",
            },
            {
                "question": "Почему важно разобраться в теме «{topic}»?",
                "answer": "Эта тема помогает связать теорию с практикой и продвинуться к цели «{goal}».",
            },
            {
                "question": "Как можно применить тему «{topic}» на практике?",
                "answer": "Нужно выбрать небольшой практический пример, применить ключевые идеи темы и зафиксировать результат.",
            },
            {
                "question": "Какая типичная ошибка возможна при изучении темы «{topic}»?",
                "answer": "Типичная ошибка - запомнить определения без проверки понимания на самостоятельных задачах.",
            },
            {
                "question": "Как проверить, что тема «{topic}» действительно освоена?",
                "answer": "Нужно объяснить тему своими словами и решить практическую задачу без подсказок.",
            },
        ]
