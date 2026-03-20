"""
Community Guidelines Module

This file stores static community guidelines.
These are returned via API and displayed in the app.
"""

COMMUNITY_GUIDELINES = [
    "Be respectful and supportive.",
    "No harassment, hate speech, or discrimination.",
    "Do not share medical prescriptions or diagnoses.",
    "Do not post explicit or inappropriate content.",
    "Avoid spreading misinformation.",
    "Respect privacy — do not share personal data.",
]

def get_guidelines():
    """
    Returns the official community guidelines.
    """
    return COMMUNITY_GUIDELINES