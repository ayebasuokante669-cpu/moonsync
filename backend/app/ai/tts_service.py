from gtts import gTTS
import tempfile
import os

def text_to_speech(text: str) -> str:
    tmp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".mp3")
    path = tmp_file.name
    tmp_file.close()

    tts = gTTS(text=text, lang="en")
    tts.save(path)

    return path
