from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def valid_payload() -> dict[str, object]:
    return {
        "goal_title": "Изучить Java Spring Boot",
        "goal_description": "Хочу изучить Spring Boot и собрать небольшой backend project.",
        "goal_type": "TECHNOLOGY_LEARNING",
        "target_date": "2026-08-15",
        "estimated_duration_weeks": 12,
        "user_level": "beginner",
    }


def test_successful_mock_generation(monkeypatch):
    monkeypatch.delenv("AI_API_KEY", raising=False)

    response = client.post("/api/ai/generate-roadmap", json=valid_payload())

    assert response.status_code == 200
    body = response.json()
    assert body["roadmap_title"] == "Roadmap: Изучить Java Spring Boot"
    assert body["roadmap_description"]
    assert len(body["steps"]) >= 1
    assert body["steps"][0]["order_index"] == 1
    assert body["steps"][0]["estimated_days"] > 0
    assert body["steps"][0]["topics"]
    assert body["steps"][0]["tasks"]
    assert body["steps"][0]["tasks"][0]["priority"] in {"LOW", "MEDIUM", "HIGH"}


def test_invalid_request_returns_validation_error():
    response = client.post("/api/ai/generate-roadmap", json={})

    assert response.status_code == 422


def test_ai_unavailable_uses_graceful_degradation(monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "test-key")
    monkeypatch.setenv("AI_API_BASE_URL", "http://127.0.0.1:1/v1")
    monkeypatch.setenv("AI_GRACEFUL_DEGRADATION_ENABLED", "true")

    response = client.post("/api/ai/generate-roadmap", json=valid_payload())

    assert response.status_code == 200
    body = response.json()
    assert body["steps"]
    assert "graceful degradation" in body["roadmap_description"]
