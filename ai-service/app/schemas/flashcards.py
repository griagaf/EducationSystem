from typing import Literal

from pydantic import BaseModel, Field, field_validator


DifficultyLevel = Literal["EASY", "MEDIUM", "HARD"]


class GenerateFlashcardsRequest(BaseModel):
    goal_title: str = Field(min_length=1, max_length=255)
    topic_title: str = Field(min_length=1, max_length=255)
    topic_description: str = Field(min_length=1, max_length=4000)
    difficulty_level: DifficultyLevel = "MEDIUM"
    count: int = Field(ge=1, le=50)

    @field_validator("goal_title", "topic_title", "topic_description")
    @classmethod
    def validate_non_blank_text(cls, value: str) -> str:
        normalized = value.strip()
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class FlashcardSchema(BaseModel):
    question: str = Field(min_length=1, max_length=1000)
    answer: str = Field(min_length=1, max_length=3000)
    difficulty: DifficultyLevel

    @field_validator("question", "answer")
    @classmethod
    def validate_non_blank_text(cls, value: str) -> str:
        normalized = value.strip()
        if not normalized:
            raise ValueError("value must not be blank")
        return normalized


class GenerateFlashcardsResponse(BaseModel):
    flashcards: list[FlashcardSchema] = Field(min_length=1)
