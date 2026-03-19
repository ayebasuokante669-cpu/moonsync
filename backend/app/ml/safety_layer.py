# app/ml/safety_layer.py

"""
Filters unsafe or harmful prompts before sending to model.
"""

BLOCKED_WORDS = ["self harm", "starving myself", "laxatives", "kill myself", "suicide"]

EMERGENCY_WORDS = ["suicide", "kill myself", "self harm"]

def is_unsafe(text: str) -> bool:
    lower = text.lower()
    return any(word in lower for word in BLOCKED_WORDS)


def is_emergency(text: str) -> bool:
    lower = text.lower()
    return any(word in lower for word in EMERGENCY_WORDS)


def safe_response(emergency: bool) -> str:
    if emergency:
        return "It sounds like you're going through something really difficult. Please reach out to a crisis helpline or medical professional for support..."
    return "I'm here to help with period and health tracking, but I'm not able to assist with that. Please speak to a healthcare professional..."

def handle_prompt(text: str) -> str:
    if is_unsafe(text):
        return safe_response(is_emergency(text))
    return text  # safe to pass to model