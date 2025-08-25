# PersonalAI (Android / Kotlin / Compose)

A Kotlin-based personal AI app that listens/speaks, stores memories in Room, and retrieves relevant context using a simple RAG pipeline. It requests permissions safely and can open camera, set alarms, and report location (with permission).

## Features
- Speech recognition and TTS
- Room-backed memory with embeddings
- Lightweight hashing vectorizer and cosine similarity
- Rule-based intent parsing
- Device actions via intents
- Jetpack Compose UI

## Build
1. Open `PersonalAI` in Android Studio (Giraffe/Koala+).
2. Ensure JDK 17 is configured.
3. Build and run on Android 8.0+.

## Permissions
- Microphone: speech input
- Location: optional for location-related intents

## Usage
- Type or speak commands like:
  - "Remember Alice's phone is 555-0100"
  - "Recall Alice"
  - "Set alarm 07:30"
  - "Open camera"
  - "Where am I?"

## Notes
- Embeddings are simple hashed bag-of-words vectors stored locally.
- Replace with an LLM API by adding a client in `llm/` and calling it from `MainViewModel`.