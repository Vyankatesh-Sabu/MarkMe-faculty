# Analytics Feature Implementation

## Overview
I've successfully implemented a comprehensive analytics feature for your MarkMe home screen that displays graphs and insights from your attendance API.

## Files Created/Modified

### 1. **New Data Models** (`AnalyticsResponse.kt`)
Created data classes to handle the API response:
- `AnalyticsResponse`: Main response wrapper
- `AttendanceRecord`: Individual attendance record with nested objects
- `AttendanceAnalytics`: Processed analytics data for display
- `TrendPoint`: Data points for attendance trend chart
- `CourseStats`: Statistics per course
- `SessionSummary`: Recent session information

### 2. **Updated API Service** (`ApiService.kt`)
Added endpoint:
```kotlin
@GET("api/v1/attendances/plain")
suspend fun getAnalyticsData(): Response<AnalyticsResponse>
```

### 3. **Updated Repository** (`AuthRepository.kt`)
Added method to fetch analytics data:
```kotlin
suspend fun getAnalyticsData(): Result<AnalyticsResponse>
```

### 4. **Updated ViewModel** (`HomeViewModel.kt`)
Added:
- State flows for analytics data and loading state
- `loadAnalytics()` method to fetch data
- `processAnalyticsData()` method to transform raw API data into displayable analytics:
  - Calculates total sessions and students
  - Computes average attendance percentage
  - Creates trend data for last 7 days
  - Groups statistics by course
  - Identifies recent sessions

### 5. **Updated Home Screen** (`HomeScreen.kt`)
Added UI components:

#### **AnalyticsSection**
Main container displaying all analytics with:
- Three stat cards showing total sessions, students, and average attendance
- Attendance trend chart (line graph)
- Course-wise statistics
- Recent sessions list

#### **StatCard**
Compact card displaying a single metric with:
- Icon with colored background
- Large value text
- Small title text

#### **AttendanceTrendChart**
Line chart showing attendance percentage over time using Vico chart library

#### **CourseStatsSection**
List of courses with:
- Color indicator
- Course name
- Number of sessions
- Average attendance percentage

#### **RecentSessionsSection**
List of recent sessions with:
- Room location icon
- Course name and room
- Present/total count
- Attendance percentage

## Features

### 📊 Analytics Cards
- **Total Sessions**: Count of unique attendance sessions
- **Total Students**: Count of unique students
- **Average Attendance**: Overall attendance percentage

### 📈 Attendance Trend Chart
- Line graph showing attendance rate over the last 7 days
- Uses Vico charting library for smooth animations
- Automatically updates when data changes

### 📚 Course Statistics
- Per-course breakdown with color coding
- Shows total sessions and average attendance per course
- Up to 5 different color schemes for visual distinction

### 🕐 Recent Sessions
- Last 5 attendance sessions
- Shows course name, room, and attendance stats
- Present/total count with percentage

## Data Processing

The `processAnalyticsData()` method transforms raw API data:

1. **Aggregation**: Groups records by date, course, and session
2. **Calculation**: Computes attendance rates and percentages
3. **Sorting**: Orders trend data chronologically and sessions by recency
4. **Limiting**: Shows last 7 days for trends and 5 most recent sessions

## Loading States

- Shows `CircularProgressIndicator` while fetching data
- Gracefully handles empty or null data
- Only displays analytics section when data is available

## API Integration

The feature expects this endpoint to return data in the format you provided:
```
GET /api/v1/attendances/plain
```

Response structure:
```json
{
  "statusCode": 200,
  "success": true,
  "message": "...",
  "data": [
    {
      "attendanceId": ...,
      "isPresent": ...,
      "session": { ... },
      "student": { ... },
      "course": { ... },
      ...
    }
  ]
}
```

## Usage

The analytics automatically loads when the home screen is displayed:
```kotlin
LaunchedEffect(Unit) { 
    homeViewModel.load() 
}
```

This calls both `getFacultySubjects()` and `loadAnalytics()` to populate the screen.

## Error Handling

- Network failures are caught and handled gracefully
- Empty data states are properly managed
- The UI won't crash if analytics data is unavailable

## Visual Design

- Follows Material Design 3 principles
- Uses consistent color scheme with your existing app
- Cards have subtle shadows and rounded corners
- Responsive layout adapts to different screen sizes
- Icons and emojis for visual appeal

## Build Status

✅ Project builds successfully with no compilation errors
✅ All dependencies are properly configured
✅ Vico chart library (v2.3.6) is compatible and working

The analytics feature is now fully integrated into your home screen and ready to use!

