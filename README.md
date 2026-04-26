# MarkMe — Faculty Attendance App

> **MarkMe** is an Android application built for faculty members to manage and track student attendance efficiently. It provides real-time session management, analytics dashboards, schedule management, and push notification support — all in a modern Material Design 3 interface.

---

## 📱 Screenshots

_Login · Home Dashboard · Attendance · Schedule · Profile_

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Secure Login** | JWT-based faculty authentication with password visibility toggle |
| 🏠 **Home Dashboard** | Course overview cards with live subject list |
| 📊 **Analytics** | Attendance trends (line chart), course stats, recent session summaries |
| 📋 **Attendance Management** | View sessions by date, mark/review present & absent students |
| 📅 **Schedule Management** | Create and filter recurring attendance schedules by course, room, or faculty |
| 👤 **Profile** | Faculty profile view with logout |
| 🔔 **Push Notifications** | Firebase Cloud Messaging (FCM) integration for real-time alerts |

---

## 🏗️ Architecture

The app follows a clean **MVVM (Model-View-ViewModel)** pattern:

```
app/
├── data/
│   ├── remote/
│   │   ├── api/          # Retrofit ApiService interface
│   │   └── models/       # Data classes (responses & requests)
│   └── repository/       # AuthRepository — single source of truth
├── ui/
│   ├── screens/
│   │   ├── login/        # LoginScreen + AuthViewModel
│   │   ├── home/         # HomeScreen + HomeViewModel
│   │   ├── attendanceScreen/  # Attendance list, student list, student stats
│   │   ├── attendanceSelection/  # Course/session selection
│   │   ├── schedule/     # ScheduleScreen + ScheduleViewModel
│   │   └── profile/      # ProfileScreen + ProfileViewModel
│   ├── components/       # Reusable composables (charts, shared pref helper)
│   ├── navigation/       # Navigation graph & Screen routes
│   └── theme/            # Material 3 colour, typography & shape tokens
├── navigation/           # Sealed Screen route definitions
└── pushNotification/     # FCM service & token store
```

---

## 🖥️ Screens

### Login
- Email/password form with show/hide password toggle
- Inline error messages on failed login
- Navigates to Home on success, clearing back stack

### Home
- Greeting header with profile shortcut
- Scrollable subject cards — tap any to open attendance details
- **Analytics section** (loads automatically):
  - Three metric cards: Total Sessions · Total Students · Avg Attendance %
  - Line chart of attendance trends over the last 7 days
  - Per-course breakdown with colour coding
  - List of the 5 most recent sessions

### Attendance
- Sessions grouped by date with present/total counts
- Tap a session to see the full student list (present/absent)
- Student statistics screen with per-student attendance percentage and a trend chart

### Schedule
- Collapsible **Filter panel** — filter by course (dropdown) and/or room (text field)
- Create new schedules: select course, room, time range, and repeat days (Mon–Sun)
- Clear filters to restore all schedules

### Profile
- Displays faculty name, department, contact number, dates of birth & joining
- Logout button with confirmation dialog

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Navigation | Navigation Compose |
| Networking | Retrofit 2 + Gson converter + OkHttp logging interceptor |
| Charts | [Vico](https://github.com/patrykandpatrick/vico) v2.3.6 |
| Auth storage | EncryptedSharedPreferences |
| Push notifications | Firebase Cloud Messaging (FCM) |
| Build system | Gradle with Kotlin DSL |

---

## 📡 API Reference

Base URL: `http://<your-server>/api/v1/`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `auth/login` | Faculty login |
| `GET` | `faculty/me` | Fetch logged-in faculty profile |
| `GET` | `faculty/me/course` | List courses taught by faculty |
| `GET` | `faculty/attendance?courseId=` | Attendance overview for a course |
| `POST` | `attendancesessions` | Start a new attendance session |
| `GET` | `faculty/me/attendance/plain` | Raw attendance records for analytics |
| `POST` | `attendanceSchedule` | Create a recurring schedule |
| `GET` | `attendanceSchedule` | List schedules (optional filters: `facultyId`, `courseId`, `room`) |

### Schedule filter examples
```
GET /api/v1/attendanceSchedule                             # all schedules
GET /api/v1/attendanceSchedule?courseId=1                  # by course
GET /api/v1/attendanceSchedule?room=Room%20101             # by room
GET /api/v1/attendanceSchedule?facultyId=1&courseId=1      # combined
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (or later)
- Android SDK 30+ (minSdk 30, targetSdk 36)
- A running instance of the MarkMe backend API
- A Firebase project with a `google-services.json` file

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Vyankatesh-Sabu/MarkMe-faculty.git
   cd MarkMe-faculty
   ```

2. **Add Firebase configuration**
   Place your `google-services.json` in the `app/` directory.

3. **Configure the base URL**
   Open `app/src/main/java/com/vrsabu/markme/data/remote/RetrofitInstance.kt` and set `BASE_URL` to point to your backend server.

4. **Build & run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open the project in Android Studio and press **Run ▶**.

---

## 📂 Key Files

| File | Purpose |
|---|---|
| `ApiService.kt` | All Retrofit endpoint declarations |
| `AuthRepository.kt` | Networking + token storage logic |
| `HomeViewModel.kt` | Subjects & analytics state management |
| `HomeScreen.kt` | Dashboard UI + analytics composables |
| `ScheduleScreen.kt` | Schedule list, create form, and filter UI |
| `AttendanceViewModel.kt` | Session & student data state management |
| `EncryptedSharedPref.kt` | JWT token persistence helper |
| `MyFirebaseMessagingService.kt` | FCM push notification handler |

---

## 🔒 Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | API communication |
| `POST_NOTIFICATIONS` | Display FCM push notifications (Android 13+) |

---

## 📦 Dependencies

```kotlin
// Networking
"com.squareup.retrofit2:retrofit:2.9.0"
"com.squareup.retrofit2:converter-gson:2.9.0"
"com.squareup.okhttp3:logging-interceptor:4.11.0"

// Charts
"com.patrykandpatrick.vico:core:2.3.6"
"com.patrykandpatrick.vico:compose:2.3.6"
"com.patrykandpatrick.vico:compose-m3:2.3.6"

// UI extras
"androidx.compose.material:material-icons-extended:1.7.0"
"androidx.navigation:navigation-compose:2.8.3"

// Firebase
"com.google.firebase:firebase-messaging-ktx:24.0.3"
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is open-source. Feel free to use, modify, and distribute it.
