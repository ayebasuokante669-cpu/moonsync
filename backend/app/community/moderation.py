"""
Moderation Module

Provides basic text filtering and flag detection.
"""

BANNED_KEYWORDS = [
    "hate",
    "kill",
    "suicide",
    "racist",
]

def contains_banned_content(text: str) -> bool:
    """
    Checks if text contains banned keywords.
    """
    text_lower = text.lower()
    return any(word in text_lower for word in BANNED_KEYWORDS)

def moderate_post(text: str) -> dict:
    """
    Moderates content and returns result.
    """
    if contains_banned_content(text):
        return {
            "approved": False,
            "reason": "Post contains inappropriate language."
        }

    return {
        "approved": True,
        "reason": None
    }