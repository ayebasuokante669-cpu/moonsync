import * as functions from "firebase-functions";
import express from "express";

const app = express();

app.use(express.json());

// ✅ HEALTH CHECK (THIS FIXES "Cannot GET")
app.get("/", (req, res) => {
  res.status(200).json({
    status: "OK",
    message: "MoonSync API is running 🚀",
  });
});

// example api route
app.get("/test", (req, res) => {
  res.json({ success: true });
});

export const api = functions.https.onRequest(app);
