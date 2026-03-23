import os
import requests
from app.ai.prompt import build_prompt
from app.ai.mock import mock_response
from app.ai.safety import safety_check, emergency_response


HF_TOKEN = os.getenv("HF_TOKEN")

API_URL = "https://qt3vdzu4nu7ochkh.us-east-1.aws.endpoints.huggingface.cloud"

headers = {
    "Authorization": f"Bearer {HF_TOKEN}"
}


# ---------------------------
# RESPONSE CLEANER
# ---------------------------

def clean_response(text: str) -> str:
    unwanted_phrases = [
        "Key Points:",
        "Note:",
        "Explanation:",
        "I provided",
        "This response",
        "The tone is"
    ]
    for phrase in unwanted_phrases:
        if phrase in text:
            text = text.split(phrase)[0].strip()
    return text


# ---------------------------
# BASIC DETECTION HELPERS
# ---------------------------

def is_greeting(message: str):
    greetings = ["hi", "hello", "hey", "sup", "yo", "good morning", "good evening", "good day"]
    msg = message.lower().strip()
    return msg in greetings


def is_casual_chat(message: str) -> bool:
    casual = ["how are you", "how r u", "what's up", "whats up", "how's it going"]
    msg = message.lower().strip()
    return any(c in msg for c in casual)


# ---------------------------
# EMOTION SYSTEM
# ---------------------------

def detect_emotion_level(message: str) -> int:
    msg = message.lower()

    # 🔴 LEVEL 4 — CRITICAL
    critical_patterns = [
        "kill myself", "end my life", "i can't do this anymore",
        "i want to die", "suicide"
    ]
    if any(p in msg for p in critical_patterns):
        return 4

    # 🔵 LEVEL 3 — HIGH EMOTION
    high_words = [
        "sad", "depressed", "terrible", "crying",
        "hurt", "hopeless", "empty"
    ]
    high_emojis = ["💔", "😭", "😢", "😞", "😔"]

    if any(w in msg for w in high_words) or any(e in message for e in high_emojis):
        return 3

    # 🟡 LEVEL 2 — MODERATE
    moderate_words = [
        "stressed", "worried", "tired", "overwhelmed", "anxious"
    ]

    sarcasm_patterns = ["😂", "😅", "🙂"]
    negative_context = ["fine", "great", "okay"]

    if any(w in msg for w in moderate_words):
        return 2

    if any(e in message for e in sarcasm_patterns) and any(n in msg for n in negative_context):
        return 2

    # ⚪ LEVEL 0 — DEFAULT
    return 0


# ---------------------------
# MAIN GENERATION FUNCTION
# ---------------------------

def generate_reply(user_message: str):

    # 1️⃣ SAFETY FIRST
    safety = safety_check(user_message)
    if safety:
        return safety

    # 2️⃣ GREETING (EARLY EXIT)
    if is_greeting(user_message):
        return {
            "response": "Hey 😊 I'm Cyra. How can I help you today?",
            "confidence": "high"
        }

    # 3️⃣ EMOTION DETECTION
    level = detect_emotion_level(user_message)

    # 🔴 LEVEL 4 — EMERGENCY
    if level == 4:
        return emergency_response()

    # 🔵 LEVEL 3 — HIGH EMOTION
    if level == 3:
        return {
            "response": "I'm really sorry you're feeling this way 💔 You don't have to go through it alone. Do you want to talk about what's going on?",
            "confidence": "high"
        }

    # 🟡 LEVEL 2 — MODERATE
    if level == 2:
        prefix = "That can be really stressful sometimes "
        prompt = build_prompt(prefix + user_message)

    else:
        # ⚪ LEVEL 0 — NORMAL
        user_message = user_message + " Explain clearly in 2-4 simple sentences."
        prompt = build_prompt(user_message)

    # ---------------------------
    # LLM CALL
    # ---------------------------

    payload = {
        "inputs": prompt,
        "parameters": {
            "max_new_tokens": 350,
            "temperature": 0.5,
            "top_p": 0.9,
            "repetition_penalty": 1.15,
            "do_sample": True,
            "stop": ["\nUser:", "\nuser:", "User:", "user:", "\nAssistant:"]
        }
    }

    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=180)

        print("STATUS:", response.status_code)
        print("RAW RESPONSE:", response.text)

        if response.status_code != 200:
            return mock_response()

        data = response.json()

        if isinstance(data, list):
            text = data[0].get("generated_text", "")
        elif isinstance(data, str):
            text = data
        else:
            return mock_response()

        # Normalize
        text = text.replace("\\n", "\n")

        # Extract response
        if "Assistant:" in text:
            answer = text.rsplit("Assistant:", 1)[-1]
        elif "assistant:" in text:
            answer = text.rsplit("assistant:", 1)[-1]
        else:
            answer = text

        # Cut extra convo
        if "User:" in answer:
            answer = answer.split("User:")[0].strip()

        if "\nUser:" in answer:
            answer = answer.split("\nUser:")[0]

        # Clean
        answer = clean_response(answer.strip()).strip()

        print("FINAL ANSWER:", answer)

        return {
            "response": answer,
            "confidence": "medium"
        }

    except Exception as e:
        print("LLM ERROR:", e)
        return mock_response()


print("HF TOKEN:", HF_TOKEN)