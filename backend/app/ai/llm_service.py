import os
import requests
from app.ai.prompt import build_prompt
from app.ai.mock import mock_response
from app.ai.safety import safety_check

HF_TOKEN = os.getenv("HF_TOKEN")

API_URL = "https://qt3vdzu4nu7ochkh.us-east-1.aws.endpoints.huggingface.cloud"

headers = {
    "Authorization": f"Bearer {HF_TOKEN}"
}


# ✅ Emoji normalization
def normalize_emojis(text: str) -> str:
    emoji_map = {
        # Sadness / distress
        "😢": "feeling sad",
        "😭": "feeling very sad and overwhelmed",
        "😞": "feeling upset and disappointed",
        "😔": "feeling down",
        "🥺": "feeling vulnerable",

        # Anger / frustration
        "😡": "feeling angry",
        "🤬": "feeling very angry",
        "😤": "feeling frustrated",

        # Anxiety / worry
        "😰": "feeling anxious and worried",
        "😨": "feeling scared",
        "😟": "feeling concerned and uneasy",

        # Happiness (could be genuine or sarcastic — add qualifier)
        "😃": "feeling happy or upbeat",
        "😊": "feeling warm and positive",
        "🙂": "feeling okay or possibly sarcastic",
        "😁": "feeling excited or possibly sarcastic",
        "😂": "finding something funny or being sarcastic",
        "🤣": "laughing hard or being sarcastic",
        "😆": "amused or possibly sarcastic",

        # Pain / discomfort
        "🤢": "feeling nauseous",
        "🤮": "feeling very sick",
        "😣": "feeling pain or discomfort",
        "😖": "feeling distressed and uncomfortable",
        "🥴": "feeling dizzy or out of sorts",

        # Confusion
        "😕": "feeling confused",
        "🤔": "thinking and unsure",
        "😶": "feeling speechless or unsure how to express",

        # Neutral / sarcastic risk
        "😐": "feeling neutral or possibly unamused",
        "🙄": "feeling annoyed or being sarcastic",
        "😒": "feeling unimpressed or sarcastic",

        # Positive health related
        "💪": "feeling determined and strong",
        "🙏": "feeling hopeful and grateful",
        "❤️": "expressing care and love",
        "💔": "feeling heartbroken or emotionally hurt",
    }

    for emoji, meaning in emoji_map.items():
        text = text.replace(emoji, f" ({meaning}) ")
    return text


def generate_reply(user_message: str, history=None):
    # ✅ Normalize emojis
    user_message = normalize_emojis(user_message)

    # ✅ SAFETY FIRST (before LLM)
    safety = safety_check(user_message)
    if safety:
        return safety

    prompt = build_prompt(user_message)

    payload = {
        "inputs": prompt,
        "parameters": {
            "max_new_tokens": 450,
            "temperature": 0.5,
            "top_p": 0.9,
            "repetition_penalty": 1.15,
            "do_sample": True
        }
    }

    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=180)

        if response.status_code != 200:
            return mock_response()

        data = response.json()

        if isinstance(data, list):
            text = data[0].get("generated_text", "")
        elif isinstance(data, str):
            text = data
        else:
            return mock_response()

        # ✅ CLEAN OUTPUT
        answer = text.split("Assistant:")[-1].strip()
        answer = answer.split("\nassistant:")[0].strip()
        answer = answer.split("\nUser:")[0].strip()
        answer = answer.replace("\\n", "\n")
        answer = answer.strip('"').strip()

        # ✅ LOW CONFIDENCE GUARD
        if len(answer) < 20:
            return {
                "response": "I’m not completely sure about that. It would be best to check with a healthcare professional.",
                "confidence": "low"
            }

        return {
            "response": answer,
            "confidence": "medium"
        }

    except Exception:
        return mock_response()