# MoonSync Documentation

This folder contains high-level documentation for the MoonSync project.

## Project Overview
MoonSync is a menstrual and reproductive health tracking application designed to provide:
- Cycle tracking and prediction
- Health education and guidance
- AI-powered support
- Community discussion features
- Secure data handling

## Project Structure
/ |----backend/ #FastAPI backend services|----frontend/ #Mobile app frontend |----database/ #Supbase schema Firebase auth and data logic |----docs/ #Project documentation

## Team Responsibilities
- **Backend:** APIs, AI logic, cycle calculations, notifications - Meindinyo Ezra
- **Frontend:** UI/UX, animations, user interaction, accessibility - Soronnadi Nice and Prince Nyobah Joseph
- **Database:** Data modeling, Supabase setup, RLS, migrations - Henry Chiara and Meindinyo Ezra
- **Deisgn:** Figma design, blueprint for the frontend - Prince Nyobah Joseph, David Diffa and Meindinyo Ezra

## Technology Stack
- Backend: Python (FastAPI)
- AI: LLaMA-based models, Whisper (voice), TTS
- Frontend: Mobile framework (TBD)
- Authentication: Firebase (Google Sign-In)
- Database: Supabase (PostgreSQL)

## Development Flow
1. Frontend communicates with backend APIs only
2. Backend handles all logic and validation
3. Database accessed only by backend
4. AI services integrated server-side

## Notes
- This structure supports scalability
- Responsibilities are clearly separated
- Changes are tracked via GitHub
