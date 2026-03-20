



  








// index.js
const functions = require("firebase-functions");
const { createClient } = require("@supabase/supabase-js");
require("dotenv").config({ path: __dirname + "/.env.local" });

// Load Supabase credentials
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

// Check if keys exist
if (!supabaseUrl || !supabaseKey) {
  throw new Error("Supabase URL or Service Role Key missing in .env.local");
}

// Create Supabase client (server-side, secret key)
const supabase = createClient(supabaseUrl, supabaseKey);

// Tables to fetch
const tables = [
   "symptoms_types",      // use exact spelling as in Supabase
  "symptoms",
  "user_settings",
  "mood_types",
  "onboarding_data",
  "chat_sessions",
  "users",
  "cycle_logs",
  "moods",
  "chat_messages",
  "birth_control",
  "voice_inputs",
  "cycle_phases",
  "notifications",
  "prescriptions",
  "image_uploads",
  "calendar_events",
  "community_posts",
  "community_comments",
  "app_activity_logs"
];


Snapshot = functions.https.onRequest(async (req, res) => {
  try {
    const snapshot = {};

    
    for (const table of tables) {
      const { data, error } = await supabase.from(table).select("*");

      // Never send Supabase errors that leak the key
      snapshot[table] = error ? { error: "Failed to fetch data" } : data;
    }

    res.status(200).json(snapshot);
  } catch (err) {
    console.error("Error fetching tables:", err);
    res.status(500).json({ error: "Internal server error" });
  }
});

// Test function
exports.hello = functions.https.onRequest((req, res) => {
  res.send("Hello from Firebase Functions!");
});









