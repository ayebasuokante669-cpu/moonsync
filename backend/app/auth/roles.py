"""
This file defines all user roles used in the application.
Roles are used for authorization (what a user is allowed to do),
not authentication (who the user is).
"""

# Default role for all users
ROLE_USER = "user"

# Admin role (for moderation, reports, dashboard, etc.)
ROLE_ADMIN = "admin"

# Optional future roles
ROLE_MODERATOR = "moderator"


def is_admin(role: str) -> bool:
    """
    Checks if the given role is an admin role.
    """
    return role == ROLE_ADMIN


def is_moderator(role: str) -> bool:
    """
    Checks if the given role is a moderator role.
    """
    return role in {ROLE_ADMIN, ROLE_MODERATOR}

def get_default_role() -> str:
    """""
    Returns the default role assigned to new users.
    """""
    return ROLE_USER