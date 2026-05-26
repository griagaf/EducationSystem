import json
from typing import Any

import httpx

from app.core.config import Settings
from app.core.exceptions import AiTimeoutError, AiUnavailableError, EmptyAiResultError, InvalidAiResponseError


class OpenAICompatibleClient:
    def __init__(self, settings: Settings):
        self.settings = settings

    async def generate_json(self, system_prompt: str, user_prompt: str) -> dict[str, Any]:
        if not self.settings.ai_api_key:
            raise AiUnavailableError("AI_API_KEY is not configured")
        if not self.settings.ai_api_base_url:
            raise AiUnavailableError("AI_API_BASE_URL is not configured")

        url = self._chat_completions_url()
        payload = {
            "model": self.settings.ai_model or "gpt-4o-mini",
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.2,
            "response_format": {"type": "json_object"},
        }
        headers = {
            "Authorization": f"Bearer {self.settings.ai_api_key}",
            "Content-Type": "application/json",
        }

        try:
            async with httpx.AsyncClient(timeout=self.settings.ai_timeout_seconds) as client:
                response = await client.post(url, json=payload, headers=headers)
                response.raise_for_status()
        except httpx.TimeoutException as exc:
            raise AiTimeoutError("AI request timed out") from exc
        except httpx.HTTPStatusError as exc:
            raise AiUnavailableError(f"AI provider returned HTTP {exc.response.status_code}") from exc
        except httpx.RequestError as exc:
            raise AiUnavailableError("AI provider is unavailable") from exc

        return self._parse_response(response)

    def _chat_completions_url(self) -> str:
        base_url = self.settings.ai_api_base_url.rstrip("/")
        if base_url.endswith("/chat/completions"):
            return base_url
        return f"{base_url}/chat/completions"

    def _parse_response(self, response: httpx.Response) -> dict[str, Any]:
        try:
            provider_response = response.json()
            content = provider_response["choices"][0]["message"]["content"]
        except (KeyError, IndexError, TypeError, ValueError) as exc:
            raise InvalidAiResponseError("AI provider response has unexpected structure") from exc

        if isinstance(content, list):
            content = "".join(part.get("text", "") for part in content if isinstance(part, dict))

        if not isinstance(content, str) or not content.strip():
            raise EmptyAiResultError("AI provider returned empty content")

        try:
            parsed = json.loads(content)
        except json.JSONDecodeError as exc:
            raise InvalidAiResponseError("AI provider returned non-JSON content") from exc

        if not isinstance(parsed, dict):
            raise InvalidAiResponseError("AI provider JSON response must be an object")
        return parsed
