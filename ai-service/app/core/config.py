from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ai_api_key: str = Field(default="", alias="AI_API_KEY")
    ai_api_base_url: str = Field(default="", alias="AI_API_BASE_URL")
    ai_model: str = Field(default="", alias="AI_MODEL")
    ai_timeout_seconds: int = Field(default=30, alias="AI_TIMEOUT_SECONDS")
    ai_max_retries: int = Field(default=1, alias="AI_MAX_RETRIES")
    graceful_degradation_enabled: bool = Field(default=True, alias="AI_GRACEFUL_DEGRADATION_ENABLED")


def get_settings() -> Settings:
    return Settings()
