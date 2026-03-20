from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # App
    APP_NAME: str = "MoonSync Backend"
    APP_VERSION: str = "0.1.0"
    ENVIRONMENT: str = "development"

    # AI / Model
    MODEL_PATH: str = "./model/moonsync-final"

    # API
    API_V1_PREFIX: str = "/api/v1"

    class Config:
        env_file = ".env"
        extra = "ignore"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
