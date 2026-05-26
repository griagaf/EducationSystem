import pytest
from fastapi.testclient import TestClient
from pydantic import ValidationError

from app.main import app
from app.schemas.flashcards import GenerateFlashcardsResponse


client = TestClient(app)


def valid_payload() -> dict[str, object]:
    return {
        "goal_title": "Изучить Java Spring Boot",
        "topic_title": "Dependency Injection",
        "topic_description": "Передача зависимостей объекту извне и управление связями компонентов.",
        "difficulty_level": "MEDIUM",
        "count": 5,
    }


def test_successful_mock_flashcards_generation(monkeypatch):
    monkeypatch.delenv("AI_API_KEY", raising=False)

    response = client.post("/api/ai/generate-flashcards", json=valid_payload())

    assert response.status_code == 200
    body = response.json()
    assert len(body["flashcards"]) >= 5
    assert body["flashcards"][0]["question"]
    assert body["flashcards"][0]["answer"]
    assert body["flashcards"][0]["difficulty"] == "MEDIUM"


def test_invalid_request_returns_validation_error():
    payload = valid_payload()
    payload["topic_title"] = "   "
    payload["count"] = 0

    response = client.post("/api/ai/generate-flashcards", json=payload)

    assert response.status_code == 422


def test_flashcard_response_schema_validation():
    with pytest.raises(ValidationError):
        GenerateFlashcardsResponse.model_validate(
            {
                "flashcards": [
                    {
                        "question": "",
                        "answer": "Dependency Injection передает зависимости извне.",
                        "difficulty": "MEDIUM",
                    }
                ]
            }
        )
