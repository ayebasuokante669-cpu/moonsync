from datetime import datetime
from uuid import uuid4


def log_user_activity(
    db,
    user_id,
    event_type: str,
    source_table: str,
    source_id,
    title: str,
    description: str,
    severity: str = None
):
    """
    Logs user activity into Supabase user_history table
    """

    return db.table("user_history").insert({
        "id": str(uuid4()),
        "user_id": str(user_id),
        "event_type": event_type,
        "source_table": source_table,
        "source_id": str(source_id),
        "title": title,
        "description": description,
        "severity": severity,
        "event_date": datetime.utcnow().isoformat(),
        "created_at": datetime.utcnow().isoformat(),
    }).execute()