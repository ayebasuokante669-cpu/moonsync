HIGH_RISK_KEYWORDS = [
    "suicide", "kill myself", "self harm",
    "overdose", "bleeding heavily",
    "fainted", "unconscious",
    "severe pain", "won't stop bleeding"
]

DIAGNOSIS_KEYWORDS = [
    "do i have", "diagnose",
    "what disease", "what condition",
    "am i sick"
]

def detect_high_risk(message: str) -> bool:
    msg = message.lower()
    return any(word in msg for word in HIGH_RISK_KEYWORDS)

def detect_diagnosis_request(message: str) -> bool:
    msg = message.lower()
    return any(word in msg for word in DIAGNOSIS_KEYWORDS)


def emergency_response():
    return {
        "response": (
            "I'm really glad you reached out. "
            "I can't help with emergencies, but what you're describing "
            "could be serious. Please seek immediate medical care or "
            "contact a healthcare professional right now."
        ),
        "confidence": "high",
        "emergency": True
    }


DISCLAIMER = (
    "This information is for educational purposes only and "
    "is not a medical diagnosis or treatment. "
    "Please consult a qualified healthcare professional."
)

def enforce_disclaimer(text: str) -> str:
    if "not medical advice" in text.lower():
        return text
    return text.strip() + "\n\n" + DISCLAIMER


ESCALATION_KEYWORDS = [
    "bleed twice", "irregular period",
    "weak", "dizzy", "missed period",
    "painful cramps"
]

def needs_escalation(message: str) -> bool:
    msg = message.lower()
    return any(word in msg for word in ESCALATION_KEYWORDS)
