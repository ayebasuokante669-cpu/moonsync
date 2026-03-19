"""
Community Service Layer

Handles business logic and database interaction.
"""

from .moderation import moderate_post


def create_post(user_id, content):
    """
    Creates a community post after moderation.
    """

    moderation_result = moderate_post(content)

    if not moderation_result["approved"]:
        raise ValueError(moderation_result["reason"])

    # DB logic goes here
    # Example placeholder:
    return {
        "user_id": user_id,
        "content": content,
        "created_at": "now()"
    }