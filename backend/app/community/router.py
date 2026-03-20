"""
Community Router

Defines API endpoints for community features.
"""

from fastapi import APIRouter, Depends, HTTPException
from .schemas import PostCreate, PostResponse
from .service import create_post
from .guidelines import get_guidelines

router = APIRouter(prefix="/community", tags=["Community"])


@router.get("/guidelines")
def fetch_guidelines():
    """
    Returns community guidelines.
    """
    return {"guidelines": get_guidelines()}


@router.post("/posts")
def create_community_post(payload: PostCreate):
    """
    Creates a community post.
    """

    try:
        # Replace with real user extraction later
        user_id = "mock-user-id"

        post = create_post(user_id, payload.content)

        return post

    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))