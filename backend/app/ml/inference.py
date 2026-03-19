# app/ml/inference.py

"""
Handles AI inference logic.
This is separated so model loading is centralized.
"""

from transformers import GPT2Tokenizer, GPT2LMHeadModel
import torch

MODEL_PATH = "./model/moonsync-final"

tokenizer = GPT2Tokenizer.from_pretrained(MODEL_PATH)
model = GPT2LMHeadModel.from_pretrained(MODEL_PATH)


def generate_response(prompt: str) -> str:
    inputs = tokenizer(prompt, return_tensors="pt")

    outputs = model.generate(
        inputs["input_ids"],
        max_length=80,
        temperature=0.7,
        top_p=0.9,
        do_sample=True,
        pad_token_id=tokenizer.eos_token_id
    )

    response = tokenizer.decode(outputs[0], skip_special_tokens=True)
    return response