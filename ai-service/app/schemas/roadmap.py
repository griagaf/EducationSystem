from datetime import date
from typing import Literal

from pydantic import BaseModel, Field, model_validator


LearningGoalType = Literal[
    "SELF_STUDY",
    "EXAM_PREPARATION",
    "INTERVIEW_PREPARATION",
    "TECHNOLOGY_LEARNING",
]

UserLevel = Literal["beginner", "intermediate", "advanced"]
TaskPriority = Literal["LOW", "MEDIUM", "HIGH"]


class GenerateRoadmapRequest(BaseModel):
    goal_title: str = Field(min_length=1, max_length=255)
    goal_description: str = Field(min_length=1, max_length=4000)
    goal_type: LearningGoalType
    target_date: date | None = None
    estimated_duration_weeks: int | None = Field(default=None, ge=1, le=52)
    user_level: UserLevel = "beginner"

    @model_validator(mode="after")
    def validate_planning_input(self) -> "GenerateRoadmapRequest":
        if self.target_date is None and self.estimated_duration_weeks is None:
            raise ValueError("target_date or estimated_duration_weeks must be provided")
        return self


class RoadmapTaskSchema(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    description: str = Field(min_length=1, max_length=2000)
    priority: TaskPriority = "MEDIUM"


class RoadmapStepSchema(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    description: str = Field(min_length=1, max_length=3000)
    order_index: int = Field(ge=1)
    estimated_days: int = Field(ge=1, le=365)
    topics: list[str] = Field(default_factory=list)
    tasks: list[RoadmapTaskSchema] = Field(default_factory=list)


class GenerateRoadmapResponse(BaseModel):
    roadmap_title: str = Field(min_length=1, max_length=255)
    roadmap_description: str = Field(min_length=1, max_length=3000)
    steps: list[RoadmapStepSchema] = Field(min_length=1)


class ErrorResponse(BaseModel):
    code: str
    message: str
