"""
Pydantic schemas for authentication.
These schemas define the shape of authentication-related
data coming into and going out of the API.
"""

from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime


class TokenRequest(BaseModel):
    """
    Schema used when the client sends a Firebase ID token
    to the backend for verification.
    """
    id_token: str


class UserBase(BaseModel):
    """
    Shared fields for a user.
    """
    email: EmailStr
    name: Optional[str] = None


class UserCreate(UserBase):
    """
    Schema used when creating a new user in the database
    after successful Firebase authentication.
    """
    firebase_uid: str


class UserResponse(UserBase):
    id: str
    role: str
    created_at: Optional[datetime] = None

    class Config:
        from_attributes = True

class UserOut(BaseModel):
    """
    Schema representing the authenticated user, used in route dependencies.
    """
    uid: str
    email: EmailStr
    name: Optional[str] = None
    role: str