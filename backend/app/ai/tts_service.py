import pyttsx3
import tempfile
import os 

engine = pyttsx3.init()

def text_to_speech(text: str) -> str:
    tmp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".mp3")
    path = tmp_file.name
    tmp_file.close()

    engine.save_to_file(text, path)
    engine.runAndWait()

    return path