from fastapi import APIRouter
from app.ml.lora.router import router as ai_router

api_v1_router = APIRouter(prefix="/api/v1")
api_v1_router.include_router(ai_router)

@api_v1_router.get("/ping")
def ping():
    return {"msg": "api v1 alive"}
