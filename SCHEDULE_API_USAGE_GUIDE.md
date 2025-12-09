# Schedule API Usage Guide

## Quick Reference

### API Endpoints

#### 1. Create Schedule (POST)
```http
POST http://localhost:3000/api/v1/attendanceSchedule
Content-Type: application/json

{
  "facultyId": 2,
  "courseId": 1,
  "startTime": "09:00",
  "endTime": "10:30",
  "room": "Room 101",
  "day": {
    "monday": true,
    "tuesday": false,
    "wednesday": true,
    "thursday": false,
    "friday": true,
    "saturday": false,
    "sunday": false
  }
}
```

**Response:**
```json
{
  "id": 1,
  "facultyId": 2,
  "courseId": 1,
  "courseName": "Data Structures",
  "startTime": "09:00",
  "endTime": "10:30",
  "room": "Room 101",
  "day": {
    "monday": true,
    "tuesday": false,
    "wednesday": true,
    "thursday": false,
    "friday": true,
    "saturday": false,
    "sunday": false
  }
}
```

#### 2. Get All Schedules (GET)
```http
GET http://localhost:3000/api/v1/attendanceSchedule
```

**Response:**
```json
{
  "success": true,
  "message": "Schedules retrieved successfully",
  "data": [
    {
      "id": 1,
      "facultyId": 2,
      "courseId": 1,
      "courseName": "Data Structures",
      "startTime": "09:00",
      "endTime": "10:30",
      "room": "Room 101",
      "day": {
        "monday": true,
        "tuesday": false,
        "wednesday": true,
        "thursday": false,
        "friday": true,
        "saturday": false,
        "sunday": false
      }
    },
    {
      "id": 2,
      "facultyId": 2,
      "courseId": 3,
      "courseName": "Algorithms",
      "startTime": "11:00",
      "endTime": "12:30",
      "room": "Room 205",
      "day": {
        "monday": false,
        "tuesday": true,
        "wednesday": false,
        "thursday": true,
        "friday": false,
        "saturday": false,
        "sunday": false
      }
    }
  ]
}
```

#### 3. Filter by Faculty ID
```http
GET http://localhost:3000/api/v1/attendanceSchedule?facultyId=1
```

**Use Case:** Get all schedules for a specific faculty member

#### 4. Filter by Course ID
```http
GET http://localhost:3000/api/v1/attendanceSchedule?courseId=1
```

**Use Case:** Get all schedules for a specific course (all sections/faculty)

#### 5. Filter by Room
```http
GET http://localhost:3000/api/v1/attendanceSchedule?room=Room%20101
```

**Note:** URL encode spaces as `%20` or `+`

**Use Case:** Check room availability or see what courses use a specific room

#### 6. Combined Filters
```http
GET http://localhost:3000/api/v1/attendanceSchedule?facultyId=1&courseId=1
```

**Use Case:** Get schedules for a specific faculty teaching a specific course

```http
GET http://localhost:3000/api/v1/attendanceSchedule?courseId=1&room=Room%20101
```

**Use Case:** Check if a course is scheduled in a specific room

## Android App Usage

### 1. Basic Usage (No Filters)
```kotlin
// Load all schedules
viewModel.loadSchedules()
```

### 2. Filter by Course
```kotlin
// Load schedules for course ID 1
viewModel.loadSchedules(courseId = 1)
```

### 3. Filter by Room
```kotlin
// Load schedules for Room 101
viewModel.loadSchedules(room = "Room 101")
```

### 4. Multiple Filters
```kotlin
// Load schedules for faculty 2 teaching course 1
viewModel.loadSchedules(
    facultyId = 2,
    courseId = 1
)
```

### 5. Clear Filters
```kotlin
// Reset to show all schedules
viewModel.loadSchedules()
```

## Common Use Cases

### Use Case 1: Faculty Dashboard
**Scenario:** Show only the logged-in faculty's schedules

```kotlin
val facultyId = viewModel.facultyId.value
viewModel.loadSchedules(facultyId = facultyId)
```

### Use Case 2: Course Management
**Scenario:** Administrator wants to see all sections of a course

```kotlin
val selectedCourseId = 5L
viewModel.loadSchedules(courseId = selectedCourseId)
```

### Use Case 3: Room Booking
**Scenario:** Check what classes are scheduled in a room

```kotlin
val roomNumber = "Room 101"
viewModel.loadSchedules(room = roomNumber)
```

### Use Case 4: Conflict Detection
**Scenario:** Check if faculty has overlapping schedules for a course

```kotlin
viewModel.loadSchedules(
    facultyId = currentFacultyId,
    courseId = newCourseId
)
// Then check time conflicts in the results
```

## UI Implementation Examples

### Example 1: Simple Filter Button
```kotlin
Button(onClick = {
    viewModel.loadSchedules(
        courseId = selectedCourse?.id,
        room = roomInput.text
    )
}) {
    Text("Apply Filters")
}
```

### Example 2: Auto-filter on Selection
```kotlin
// Auto-filter when course is selected
LaunchedEffect(selectedCourseId) {
    selectedCourseId?.let {
        viewModel.loadSchedules(courseId = it)
    }
}
```

### Example 3: Search by Room
```kotlin
OutlinedTextField(
    value = roomSearch,
    onValueChange = { 
        roomSearch = it
        // Debounce and search
        viewModel.loadSchedules(room = it)
    },
    label = { Text("Search by room") }
)
```

## Testing Scenarios

### Test 1: No Filters
```bash
curl -X GET http://localhost:3000/api/v1/attendanceSchedule
```
**Expected:** All schedules returned

### Test 2: Faculty Filter
```bash
curl -X GET "http://localhost:3000/api/v1/attendanceSchedule?facultyId=1"
```
**Expected:** Only schedules where facultyId = 1

### Test 3: Course Filter
```bash
curl -X GET "http://localhost:3000/api/v1/attendanceSchedule?courseId=1"
```
**Expected:** Only schedules where courseId = 1

### Test 4: Room Filter
```bash
curl -X GET "http://localhost:3000/api/v1/attendanceSchedule?room=Room%20101"
```
**Expected:** Only schedules in Room 101

### Test 5: Combined Filters
```bash
curl -X GET "http://localhost:3000/api/v1/attendanceSchedule?facultyId=1&courseId=1"
```
**Expected:** Schedules where facultyId = 1 AND courseId = 1

### Test 6: No Results
```bash
curl -X GET "http://localhost:3000/api/v1/attendanceSchedule?room=NonExistent"
```
**Expected:** Empty array in data field
```json
{
  "success": true,
  "message": "Schedules retrieved successfully",
  "data": []
}
```

## Error Handling

### Network Error
```kotlin
try {
    viewModel.loadSchedules(courseId = 1)
} catch (e: Exception) {
    // Show error message
    println("Error loading schedules: ${e.message}")
}
```

### Empty Results
```kotlin
if (schedules.isEmpty()) {
    // Show empty state UI
    EmptyScheduleState()
}
```

### Invalid Parameters
The API should handle invalid parameters gracefully:
- Invalid facultyId → Returns empty array
- Invalid courseId → Returns empty array  
- Invalid room → Returns empty array

## Performance Tips

1. **Debounce Room Search**: Don't filter on every keystroke
```kotlin
val searchFlow = snapshotFlow { roomSearch }
    .debounce(300)
    .collectAsState("")

LaunchedEffect(searchFlow) {
    viewModel.loadSchedules(room = searchFlow)
}
```

2. **Cache Results**: Store filtered results to avoid unnecessary API calls

3. **Pagination**: For large datasets, consider adding pagination parameters:
```
?page=1&limit=20&courseId=1
```

## Integration Checklist

- [x] API Service updated with query parameters
- [x] ViewModel supports filter parameters
- [x] UI has filter controls
- [x] Filter state management implemented
- [x] Apply/Clear buttons working
- [x] Empty state handling
- [x] Loading states
- [x] Error handling
- [x] Documentation complete
- [x] Build successful

## Next Steps

1. **Test with Backend**: Verify API responses match expected format
2. **Add More Filters**: Consider adding date/time filters
3. **Save Filter Preferences**: Remember user's last filter settings
4. **Export Functionality**: Allow exporting filtered schedules
5. **Calendar View**: Show schedules in a calendar format
6. **Conflict Detection**: Highlight overlapping schedules

## Support

For issues or questions:
1. Check error logs in Android Studio
2. Verify API endpoint is accessible
3. Confirm data models match API response
4. Test with simple curl commands first
5. Check network permissions in AndroidManifest.xml

