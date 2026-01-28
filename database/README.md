# MoonSync Database

This folder documents the database design and data storage strategy for **MoonSync**.

## Overview
The database is responsible for storing:
- User profiles and authentication mappings
- Menstrual cycle data and predictions
- Onboarding and personalization data
- Chat history with the AI assistant
- Community posts and comments
- Notifications and reminders
- Voice and image uploads metadata
- Mood and symptom tracking

## Technology
- **Supabase (PostgreSQL)**
- **Row Level Security (RLS)**
- **UUID-based primary keys**
- **Timestamp-based auditing**

## Core Tables
- `users`
- `onboarding_data`
- `cycle_logs`
- `cycle_phases`
- `calendar_events`
- `symptoms`
- `moods`
- `chat_sessions`
- `chat_messages`
- `community_posts`
- `community_comments`
- `notifications`
- `voice_inputs`
- `image_uploads`
- `user_settings`

## Authentication
- Authentication is handled via **Firebase (Google Sign-In)**.
- Each authenticated user is mapped to a record in the `users` table.
- The `firebase_uid` is used to securely link frontend auth with backend data.

## Security
- Row Level Security ensures users can only access their own data.
- Community content follows moderation rules (soft deletes, reporting).
- Sensitive health data is protected by strict access policies.

## Optional Improvements (Future Enhancements)
- Add database indexes for performance:
```sql
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX
