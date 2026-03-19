# app/ml/prompts.py

"""
Central prompt templates for consistency.
"""

BASE_SYSTEM_PROMPT = """
You are Momo, a menstrual and reproductive health assistant.
Provide educational guidance only.
Do not diagnose medical conditions.
"""

def build_prompt(user_message: str):
    return f"{BASE_SYSTEM_PROMPT}\nUser: {user_message}\nAssistant:"