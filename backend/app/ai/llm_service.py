import os
import requests
from app.ai.prompt import build_prompt
from app.ai.mock import mock_response

HF_TOKEN = os.getenv("HF_TOKEN")

API_URL = "https://qt3vdzu4nu7ochkh.us-east-1.aws.endpoints.huggingface.cloud"

headers = {
    "Authorization": f"Bearer {HF_TOKEN}"
}


def generate_reply(user_message: str):
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
        # print("STATUS:", response.status_code)
        # print("RAW:", response.text)

        if response.status_code != 200:
            return mock_response()

        data = response.json()

        if isinstance(data, list):
            text = data[0].get("generated_text", "")
        elif isinstance(data, str):
            text = data
        else:
            return mock_response()

        answer = text.split("Assistant:")[-1].strip()
        answer = answer.split("\nassistant:")[0].strip()
        answer = answer.split("\nUser:")[0].strip()
        answer = answer.replace("\\n", "\n")
        answer = answer.strip('"').strip()

        return {
            "response": answer,
            "confidence": "medium"
        }

    except Exception:
        return mock_response()
    

print("HF TOKEN:", HF_TOKEN)