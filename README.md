# RelateAI 💬❤️

An Android app that analyzes WhatsApp chat exports using AI to provide relationship health insights, red flags, and actionable advice.

## Features

- 📁 **Import chats** — Share directly from WhatsApp or pick a `.txt` file from storage
- 🧠 **AI-powered analysis** — Powered by Llama 3.3 70B via Groq API
- 💯 **Health score** — 0–100 relationship health rating
- 🚩 **Red flags** — Identifies unhealthy communication patterns
- 📋 **Action plan** — Concrete, personalized improvement suggestions
- 📊 **Message balance** — Shows who sends how many messages
- 😊 **Dominant emotions** — Detects the main emotional tones in the conversation
- 🌍 **Turkish UI** — Fully localized interface and analysis output

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Navigation | Jetpack Navigation Compose |
| AI | Groq API (`llama-3.3-70b-versatile`) |
| Serialization | kotlinx.serialization |
| Async | Kotlin Coroutines |

## Setup

1. Clone the repository
2. Create a free API key at [console.groq.com](https://console.groq.com)
3. Add your key to `local.properties`:
   ```
   GROQ_API_KEY=gsk_your_key_here
   sdk.dir=/path/to/your/android/sdk
   ```
4. Build and run in Android Studio

> **Note:** `local.properties` is excluded from version control and must be created manually.

## How It Works

1. User imports a WhatsApp chat export (`.txt` file)
2. `ChatParser` parses all message formats (including Turkish locale)
3. For large chats (>300 messages), smart sampling takes messages from the beginning, middle, and end to capture the full relationship arc
4. The formatted chat is sent to the Groq API with a structured prompt
5. The AI response is parsed into a typed `AnalysisResult` and displayed on the dashboard

## Architecture

```
app/
├── data/
│   ├── model/         # AnalysisResult, ChatMessage
│   ├── parser/        # ChatParser (multi-format WhatsApp support)
│   └── repository/    # GeminiRepository + GeminiRepositoryImpl (Groq API)
├── di/                # Hilt modules
└── ui/
    ├── screens/       # Home, Loading, Dashboard, Onboarding
    ├── components/    # ScoreCard, RedFlagItem, ActionPlanList
    ├── navigation/    # NavGraph, Routes
    └── theme/         # Color, Typography, Theme
```

## Privacy

All analysis is performed via API call — no chat data is stored locally or on any server beyond the inference request. The app does not collect any personal information.