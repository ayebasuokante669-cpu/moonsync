# MoonSync Backend API

## Health
GET /health

## Cycle
GET /cycle/preview
Params:
- last_period_date (YYYY-MM-DD)
- cycle_length (int)

## Calendar
GET /calendar/preview

## Notifications
GET /notifications/preview

## Chat (AI)
POST /chat
Body:
{
  "message": "string"
}
