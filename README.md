# HabitMind 🧠

A modern, production-ready Android habit tracker app built with Jetpack Compose, MVVM architecture, and OpenAI API integration. HabitMind helps users track their daily habits, visualize progress, and receive AI-generated weekly insights and motivational suggestions.

## 📱 Features

- **Habit Management**: Add, edit, and delete habits with customizable frequency goals
- **Daily Tracking**: Mark habits as completed with a simple checkbox interface
- **Progress Visualization**: View weekly completion rates with animated progress bars
- **Streak Tracking**: Track consecutive days of habit completion
- **AI-Powered Insights**: Get personalized weekly summaries and improvement tips using OpenAI GPT-4
- **Beautiful UI**: Material 3 design with dark/light theme support
- **Data Persistence**: All habits and logs stored locally using Room database

## 🏗️ Architecture

The app follows clean architecture principles with MVVM + Repository pattern:

```
com.habitmind/
├── data/
│   ├── local/          # Room entities and DAOs
│   ├── remote/         # OpenAI API service
│   ├── repository/     # HabitRepository, AIRepository
│   └── model/          # Data models
├── ui/
│   ├── screens/        # Compose screens
│   ├── components/     # Reusable UI components
│   ├── viewmodels/    # ViewModels with sealed classes for state
│   └── navigation/     # Navigation setup
├── di/                 # Hilt dependency injection modules
└── utils/              # Utility classes
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Networking**: Retrofit + OkHttp
- **AI Integration**: OpenAI API (GPT-4-turbo)
- **Charts**: Vico Charts for Compose
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Compose Navigation

## 📦 Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK (minSdk: 24, targetSdk: 35)
- OpenAI API key

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd HabitMind
   ```

2. **Set up OpenAI API Key**
   - Copy `api_key.properties.example` to `api_key.properties`
   - Edit `api_key.properties` and add your OpenAI API key:
     ```properties
     OPENAI_API_KEY="your-actual-openai-api-key-here"
     ```
   - Get your API key from: https://platform.openai.com/api-keys
   - ⚠️ **Important**: The `api_key.properties` file is in `.gitignore` and will not be committed to version control

3. **Sync Gradle**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete
   - If prompted, accept the Gradle wrapper update

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift+F10`

## 🚀 Usage

1. **Add Habits**: Tap the "+" button on the home screen to add a new habit. Set a name and target frequency (times per week).

2. **Track Daily**: Use the checkbox on each habit card to mark it as completed for today.

3. **View Progress**: Navigate to the "Progress" tab to see weekly charts, streak counts, and success rates.

4. **Get AI Insights**: Go to the "AI Insights" tab and tap the refresh button to generate personalized weekly summaries and improvement tips.

## 📸 Screenshots

*Add screenshots here once you've taken them*

## 🔐 API Configuration

The app uses OpenAI's GPT-4-turbo model for generating insights. The API key is loaded from `api_key.properties` at build time and injected via BuildConfig. Never commit your API key to version control.

## 🧪 Testing

The project includes basic test structure. To run tests:

```bash
./gradlew test           # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
```

## 📝 Code Structure

### Key Components

- **HabitViewModel**: Manages habit CRUD operations and progress tracking
- **InsightViewModel**: Handles AI insight generation
- **ProgressViewModel**: Manages progress data aggregation
- **HabitRepository**: Handles all local database operations
- **AIRepository**: Manages OpenAI API calls and prompt generation

### UI State Management

The app uses sealed classes for type-safe UI state management:

```kotlin
sealed class HabitUiState {
    object Idle : HabitUiState()
    object Loading : HabitUiState()
    data class Success(val message: String) : HabitUiState()
    data class Error(val message: String) : HabitUiState()
}
```

## 🎨 UI Design

- Material 3 design system with dynamic colors
- Support for both light and dark themes
- Smooth animations for progress indicators
- Card-based layout with rounded corners
- Responsive navigation with bottom bar

## 🔮 Future Enhancements

Potential features for future versions:

- Voice check-in via Android SpeechRecognizer
- Daily AI motivational quotes
- Firebase sync for backup
- On-device ML model to predict habit drop probability
- Habit reminders using WorkManager

## 📄 License

This project is open source and available for portfolio use.

## 👨‍💻 Development

Built with ❤️ using modern Android development practices.

---

**Note**: Make sure you have a valid OpenAI API key and sufficient credits before using the AI Insights feature. API calls are made directly from the device to OpenAI's servers.

