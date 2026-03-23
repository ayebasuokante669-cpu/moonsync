from dataclasses import dataclass

@dataclass
class User:
    id: str
    firebase_uid: str
    email: str
    role: str = "user"