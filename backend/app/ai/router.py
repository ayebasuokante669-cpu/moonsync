from fastapi import APIRouter, UploadFile, File, Query, HTTPException, Depends
from app.ai.schemas import ChatRequest, ChatResponse, VoiceInputResponse, VoiceResponseRequest
from app.ai.llm_service import generate_reply
from app.ai.whisper_service import transcribe_audio
from app.ai.tts_service import text_to_speech
from app.auth.service import get_current_user
from fastapi.responses import StreamingResponse
from app.ai.vision_service import vision_placeholder, extract_image_metadata
from app.ai.safety import (
    detect_diagnosis_request,
    detect_high_risk,
    needs_escalation,
    emergency_response,
    enforce_disclaimer
)
import uuid
import os

router = APIRouter(prefix="/ai", tags=["ai"])

@router.post("/chat", response_model=ChatResponse)
def chat(
    request: ChatRequest,
    current_user = Depends(get_current_user)
):
    session_id = request.session_id or str(uuid.uuid4())
    result = generate_reply(request.message)
    return ChatResponse(
        session_id=session_id,
        response=result["response"],
        confidence=result.get("confidence", "low")
    )


@router.get("/ping")
def ping():
    return {"status": "ai alive"}

@router.post("/voice-input", response_model=VoiceInputResponse)
async def voice_input(
    audio: UploadFile = File(...),
    session_id: str | None = Query(default=None),
):
    if not audio.content_type.startswith("audio/"):
        raise HTTPException(status_code=400, detail="Invalid audio file")
    file_bytes = await audio.read()

    if len(file_bytes) == 0:
        raise HTTPException(status_code=400, detail="Empty audio file")

    text = transcribe_audio(file_bytes, audio.filename)
    return VoiceInputResponse(
        text=text,
        session_id=session_id
    )

@router.post("/voice-response")
def voice_response(payload: VoiceResponseRequest):
    audio_path = text_to_speech(payload.text)

    def audio_stream():
        with open(audio_path, "rb") as f:
            yield from f
        os.remove(audio_path)

    return StreamingResponse(
        audio_stream(),
        media_type="audio/mpeg"
    )

@router.post("/image-upload")
async def image_upload(file: UploadFile = File(...)):
    image_bytes = await file.read()
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Invalid image file")
    metadata = extract_image_metadata(image_bytes)

    return{
        "metadata": metadata,
        "vision": vision_placeholder()
    }

@router.post("/safety-chat", response_model=ChatResponse)
def safety_chat(
    request: ChatRequest,
    current_user = Depends(get_current_user)
):
    session_id = request.session_id or str(uuid.uuid4())

    if detect_high_risk(request.message):
        result = emergency_response()
    else:
        result = generate_reply(request.message)

        if detect_diagnosis_request(request.message):
            result["confidence"] = "low"

        if needs_escalation(request.message):
            result["response"] += (
                "\n\nNote: It seems like you might need professional help."
            )

        result["response"] = enforce_disclaimer(result["response"])

    return ChatResponse(
        session_id=session_id,
        response=result["response"],
        confidence=result.get("confidence", "low")
    )

@router.post("/feedback")
def feedback(
    message_id: str,
    rating: int,
    feedback: str | None = None,
    current_user = Depends(get_current_user)
):
    return {"message": "Feedback saved"}

def get_session_id(session_id: str | None) -> str:
    return session_id or str(uuid.uuid4())
