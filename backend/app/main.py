from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from dotenv import load_dotenv

load_dotenv()

from app.core.errors import (
    http_exception_handler,
    validation_exception_handler,
    generic_exception_handler,
)

from app.cycle.router import router as cycle_router
from app.calendar.router import router as calendar_router
from app.notifications.router import router as notifications_router
from app.ai.router import router as ai_router
from app.history.router import router as history_router
from app.community.router import router as community_router
from app.admin.router import router as admin_router
from app.auth.router import router as auth_router

from app.core.firebase import init_firebase

init_firebase()

app = FastAPI()

# ✅ CORRECT exception handlers
app.add_exception_handler(StarletteHTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, generic_exception_handler)

app.include_router(auth_router)
app.include_router(cycle_router)
app.include_router(calendar_router)
app.include_router(notifications_router)
app.include_router(ai_router)
app.include_router(history_router)
app.include_router(community_router)
app.include_router(admin_router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "https://moonsync.vercel.app"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def root():
    return {"status": "MoonSync backend running"}

@app.get("/health")
def health():
    return {"status": "healthy"}