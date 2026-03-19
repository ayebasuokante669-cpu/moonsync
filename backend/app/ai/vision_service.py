from PIL import Image
from io import BytesIO

def extract_image_metadata(image_bytes: bytes):
    img = Image.open(BytesIO(image_bytes))
    safe_info = {}
    for k, v in img.info.items():
        try:
            str(v) #test serialiability
            safe_info[k] = str (v)
        except Exception:
            safe_info[k] = "unserializable"

    return{
        "format": img.format,
        "mode": img.mode,
        "size": img.size,
        "info": safe_info,
        "height": img.height,
        "width": img.width
    }

def resize_image(image_bytes: bytes, target_size: tuple) -> bytes:
    img = Image.open(BytesIO(image_bytes))
    img.load()
    img = img.resize(target_size)
    if img.mode != "RGB":
        img = img.convert("RGB")

    output_buffer = BytesIO()
    img.save(output_buffer, format=img.format)
    return output_buffer.getvalue()

def convert_image_format(image_bytes: bytes, target_format: str) -> bytes:
    img = Image.open(BytesIO(image_bytes))
    img.load()

    output_buffer = BytesIO()
    img.save(output_buffer, format=target_format)
    return output_buffer.getvalue()

def generate_thumbnail(image_bytes: bytes, thumbnail_size: tuple = (128, 128)) -> bytes:
    img = Image.open(BytesIO(image_bytes))
    img.load()
    img.thumbnail(thumbnail_size)
    if img.mode != "RGB":
        img = img.convert("RGB")


    output_buffer = BytesIO()
    img.save(output_buffer, format=img.format)
    return output_buffer.getvalue()

def vision_placeholder():
    return {
        "status": "not_implemented",
        "message": "This is a placeholder for vision service."}