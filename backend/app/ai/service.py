from datetime import datetime
import uuid


def create_session(db, user_id: str):
    session_id = str(uuid.uuid4())

    db.table("chat_sessions").insert({
        "id": session_id,
        "user_id": user_id,
        "created_at": datetime.utcnow().isoformat()
    }).execute()

    return session_id


def save_message(db, session_id: str, user_id: str, role: str, content: str):
    return db.table("chat_messages").insert({
        "id": str(uuid.uuid4()),
        "session_id": session_id,
        "user_id": user_id,
        "role": role,  # "user" or "assistant"
        "content": content,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


def get_session_messages(db, session_id: str):
    result = db.table("chat_messages") \
        .select("*") \
        .eq("session_id", session_id) \
        .order("created_at") \
        .execute()

    return result.data


def get_session_history(db, session_id: str):
    messages = get_session_messages(db, session_id)

    # Convert to simple format for LLM
    history = []
    for msg in messages:
        history.append({
            "role": msg["role"],
            "content": msg["content"]
        })

    return history


def save_feedback(db, user_id: str, message_id: str, rating: int, feedback: str | None):
    return db.table("ai_feedback").insert({
        "id": str(uuid.uuid4()),
        "user_id": user_id,
        "message_id": message_id,
        "rating": rating,
        "feedback": feedback,
        "created_at": datetime.utcnow().isoformat()
    }).execute()