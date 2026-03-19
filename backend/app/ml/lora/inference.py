# app/ml/lora/inference.py

from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel
import torch

BASE_MODEL = "meta-llama/Llama-3.2-3B-Instruct"
LORA_PATH = "model/moonsync_lora"

tokenizer = AutoTokenizer.from_pretrained(BASE_MODEL)

base_model = AutoModelForCausalLM.from_pretrained(
    BASE_MODEL,
    dtype=torch.float32,
    device_map="cpu"
)

model = PeftModel.from_pretrained(base_model, LORA_PATH)


def generate_response(prompt: str):
    inputs = tokenizer(prompt, return_tensors="pt")

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=120,
            temperature=0.7,
            top_p=0.9
        )

    return tokenizer.decode(output[0], skip_special_tokens=True)