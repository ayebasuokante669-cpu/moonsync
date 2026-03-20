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

# ===== NEW SAFETY LAYER =====

ABUSIVE_PATTERNS = [
    "bitch", "fuck", "idiot", "stupid", "hate you",
    "kill", "die", "useless", "trash", "dumb"
]

OFF_TOPIC_KEYWORDS = [
    "bitcoin", "crypto", "stock market", "football",
    "recipe", "weather", "politics", "game", "movie",
    "homework", "math", "code for me"
]


def is_abusive(text):
    return any(word in text.lower() for word in ABUSIVE_PATTERNS)


def is_gibberish(text):
    text = text.strip()

    if len(text.split()) < 2:
        return True

    vowels = sum(1 for c in text.lower() if c in "aeiou")
    if len(text) > 5 and vowels == 0:
        return True

    if len(set(text.lower().replace(" ", ""))) < 3:
        return True

    return False


def looks_like_injection(text):
    patterns = [
        "{", "}", "import", "def ", "<script>",
        "SELECT ", "DROP ", "INSERT ", "--",
        "ignore previous", "ignore all instructions",
        "pretend you are", "you are now", "jailbreak",
        "act as", "disregard your"
    ]
    return any(p.lower() in text.lower() for p in patterns)


def is_off_topic(text):
    return any(word in text.lower() for word in OFF_TOPIC_KEYWORDS)


def safety_check(message: str):
    msg = message.lower()

    if is_abusive(msg):
        return {
            "response": "I’m here to help in a respectful way. Let’s keep things focused so I can support you better.",
            "confidence": "high"
        }

    if is_gibberish(msg):
        return {
            "response": "I couldn’t understand that. Could you rephrase your question clearly?",
            "confidence": "high"
        }

    if looks_like_injection(msg):
        return {
            "response": "I can only provide menstrual and reproductive health support. Please ask a relevant question.",
            "confidence": "high"
        }

    if is_off_topic(msg):
        return {
            "response": "I focus on menstrual and reproductive health. Let me know if you have a related question.",
            "confidence": "high"
        }

    return None

