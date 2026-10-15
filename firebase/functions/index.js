"use strict";
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {createService} = require("./service");
initializeApp();
// Same published word list and rotation as the Android app.
async function puzzleFor(day, mode) {
  const response = await fetch("https://raw.githubusercontent.com/oussamaessid/WordsData/refs/heads/main/words.json", {signal:AbortSignal.timeout(15000)});
  if (!response.ok) throw new HttpsError("unavailable", "Mots indisponibles.");
  const data = await response.json();
  const words = data[mode === "FRENCH" ? "french" : "english"];
  if (!Array.isArray(words) || !words.length) throw new HttpsError("unavailable", "Langue indisponible.");
  const days = Math.floor((Date.parse(day + "T00:00:00Z") - Date.parse("2026-01-01T00:00:00Z"))/86400000);
  const solution = words[((days % words.length) + words.length) % words.length].toUpperCase();
  if (!/^[A-ZÀ-ÖØ-Þ]{5}$/u.test(solution)) throw new HttpsError("unavailable", "Mot invalide.");
  return {number:1, solution};
}
const service = createService(getFirestore(), puzzleFor, Date.now, (code, message) => {throw new HttpsError(code, message);});
for (const name of ["saveProfile", "beginAttempt", "finishAttempt", "leaderboard"]) {
  exports["words" + name[0].toUpperCase() + name.slice(1)] = onCall({region:"europe-west1", maxInstances:3, timeoutSeconds:30}, service[name]);
}
