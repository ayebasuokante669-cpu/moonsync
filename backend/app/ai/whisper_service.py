import whisper
import tempfile
import os 

# Load model once at startup
# Use "base", or "small" for now (fast, good quality)

model = whisper.load_model("base")

def transcribe_audio(file_bytes: bytes, filename: str) -> str:
    # Create temporary file
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(filename)[1]) as tmp:
        tmp.write(file_bytes)
        tmp_path = tmp.name

    try:
        # Transcribe the audio file
        result = model.transcribe(tmp_path)
        text = result.get("text", "").strip()
        return text

    finally:
        # Clean up the temporary file
        if os.path.exists(tmp_path):
            os.remove(tmp_path)