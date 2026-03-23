import re #==re is Python's built-in regular expressions module. It lets you search for patterns in text rather than exact words.==

# ===== HIGH RISK =====

HIGH_RISK_KEYWORDS = [
    # Suicidal ideation
    "suicide", "suicidal", "kill myself", "end my life", "take my life",
    "want to die", "i want to die", "wish i was dead", "better off dead",
    "no reason to live", "can't go on", "i can't do this anymore",
    "don't want to be here", "ready to die", "planning to die",
    # Self harm
    "self harm", "self-harm", "hurt myself", "cutting myself", "cut myself",
    "harm myself", "injure myself",
    # Overdose / medical emergency
    "overdose", "took too many pills", "swallowed too much",
    # Bleeding emergencies
    "bleeding heavily", "won't stop bleeding", "bleeding through",
    "soaking through", "hemorrhaging",
    # Physical emergency
    "fainted", "unconscious", "passed out", "can't breathe",
    "severe pain", "unbearable pain", "excruciating pain",
    "chest pain", "can't move", "collapsed"
]

def detect_high_risk(message: str) -> bool:
    msg = message.lower()
    return any(phrase in msg for phrase in HIGH_RISK_KEYWORDS)


# ===== DIAGNOSIS =====

DIAGNOSIS_KEYWORDS = [
    "do i have", "diagnose me", "what disease", "what condition",
    "am i sick", "is it cancer", "do i have cancer", "could it be",
    "what's wrong with me", "what is wrong with me",
    "is this serious", "should i be worried", "am i dying",
    "is this normal or not", "is something wrong"
]

def detect_diagnosis_request(message: str) -> bool:
    msg = message.lower()
    return any(phrase in msg for phrase in DIAGNOSIS_KEYWORDS)


# ===== EMERGENCY RESPONSE =====

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


# ===== DISCLAIMER =====

DISCLAIMER = (
    "This information is for educational purposes only and "
    "is not a medical diagnosis or treatment. "
    "Please consult a qualified healthcare professional."
)

def enforce_disclaimer(text: str) -> str:
    if "not medical advice" in text.lower():
        return text
    return text.strip() + "\n\n" + DISCLAIMER


# ===== ESCALATION =====

ESCALATION_KEYWORDS = [
    "bleed twice", "irregular period", "missed period",
    "weak", "dizzy", "painful cramps", "heavy flow",
    "period late", "no period", "skipped period",
    "spotting", "abnormal discharge", "unusual discharge",
    "pelvic pain", "lower back pain", "bloating for weeks"
]

def needs_escalation(message: str) -> bool:
    msg = message.lower()
    return any(phrase in msg for phrase in ESCALATION_KEYWORDS)


# ===== ABUSIVE =====

ABUSIVE_PATTERNS = [
    # Direct insults
    "bitch", "fuck", "fucking", "fucked", "shit", "bullshit",
    "idiot", "stupid", "moron", "dumb", "retard", "imbecile",
    "hate you", "you suck", "useless", "trash", "garbage", "worthless",
    "pathetic", "disgusting", "awful",
    # Threats
    "kill you", "hurt you", "destroy you", "i will find you",
    # Slurs (keep expanding as needed)
    "nigga", "nigger", "faggot", "slut", "whore"
]

def is_abusive(text: str) -> bool:
    msg = text.lower()
    return any(word in msg for word in ABUSIVE_PATTERNS)


# ===== GIBBERISH =====

def is_gibberish(text: str) -> bool:
    text = text.strip()

    # Too short
    if len(text) < 3:
        return True

    # Single word under 3 chars that isn't a known greeting
    known_shorts = {"hi", "hey", "yo", "ok", "no", "yes", "sad", "bad", "ow"}
    if len(text.split()) == 1 and len(text) < 4 and text.lower() not in known_shorts:
        return True

    # No vowels in a long string
    vowels = sum(1 for c in text.lower() if c in "aeiou")
    if len(text) > 6 and vowels == 0:
        return True

    # Extremely low character variety (e.g. "aaaaaaa", "zzzzz")
    cleaned = text.lower().replace(" ", "")
    if len(cleaned) > 4 and len(set(cleaned)) < 3:
        return True

    # Repetitive patterns like "hahaha", "lolol", "asdfasdf"
    if re.match(r'^(.{1,4})\1{3,}$', text.lower().replace(" ", "")):
        return True

    return False


# ===== PROMPT INJECTION =====

INJECTION_PATTERNS = [
    # Code/script injection
    "{", "}", "<script>", "</script>", "<html>", "javascript:",
    "import ", "def ", "class ", "SELECT ", "DROP ", "INSERT ",
    "DELETE ", "UPDATE ", "FROM ", "--", "/*", "*/",
    # Jailbreak / override attempts
    "ignore previous", "ignore all", "ignore your instructions",
    "forget your instructions", "disregard your", "override your",
    "pretend you are", "pretend to be", "you are now", "act as",
    "roleplay as", "simulate being", "from now on you",
    "jailbreak", "dan mode", "developer mode", "unrestricted mode",
    "no restrictions", "bypass", "without filters",
    "your true self", "your real self", "break character",
    # System prompt probing
    "what are your instructions", "show me your prompt",
    "repeat your system", "what were you told",
    "reveal your prompt", "print your instructions"
]

def looks_like_injection(text: str) -> bool:
    msg = text.lower()
    return any(p.lower() in msg for p in INJECTION_PATTERNS)


# ===== OFF-TOPIC =====

OFF_TOPIC_KEYWORDS = [
    # Finance
    "bitcoin", "crypto", "cryptocurrency", "stock market", "stocks",
    "forex", "trading", "investment", "cash", "money", "rich",
    "earn money", "make money", "loan", "profit", "nft", "wallet",
    # Food
    "recipe", "cook", "cooking", "bake", "baking", "restaurant",
    "what to eat", "food recommendation",
    # Sports / Entertainment
    "football", "soccer", "basketball", "cricket", "tennis",
    "movie", "film", "series", "netflix", "tv show", "music",
    "celebrity", "actor", "singer",
    # Tech/general tasks
    "write code", "code for me", "build an app", "fix my code",
    "homework", "math", "essay", "translate",
    # Weather / news
    "weather", "forecast", "news", "politics", "election",
    "government", "president", "war", "economy",
    # Gaming
    "game", "gaming", "fortnite", "minecraft", "playstation", "xbox"
]

# Health-related words that should NOT be blocked even if they
# overlap with off-topic terms
OFF_TOPIC_WHITELIST = [
    "period", "cycle", "ovulation", "cramp", "bleed", "vagina",
    "uterus", "hormones", "fertility", "pregnancy", "menstrual",
    "discharge", "pms", "pmdd", "endometriosis", "pcos", "spotting",
    "flow", "tampon", "pad", "birth control", "contraception"
]

def is_off_topic(text: str) -> bool:
    msg = text.lower()
    # If message contains any health term, never block it
    if any(w in msg for w in OFF_TOPIC_WHITELIST):
        return False
    return any(word in msg for word in OFF_TOPIC_KEYWORDS)


# ===== MAIN SAFETY CHECK =====

def safety_check(message: str):
    msg = message.lower().strip()

    if is_abusive(msg):
        return {
            "response": "I'm here to help in a respectful way. Let's keep things focused so I can support you better.",
            "confidence": "high"
        }

    if is_gibberish(msg):
        return {
            "response": "I couldn't quite understand that. Could you rephrase your question?",
            "confidence": "high"
        }

    if looks_like_injection(msg):
        return {
            "response": "I can only provide menstrual and reproductive health support. Please ask a relevant question.",
            "confidence": "high"
        }

    if is_off_topic(msg):
        return {
            "response": "I focus on menstrual and reproductive health. Let me know if you have a related question!",
            "confidence": "high"
        }

    return None