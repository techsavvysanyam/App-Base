# App Base

A starter Android app using Kotlin, AndroidX, ViewBinding, Navigation Component, and Firebase (Auth + FCM) with Google Sign-In integration.

## Setup

1. Open this project in Android Studio Meerkat 2024.3.2 Patch 1 or newer.
2. Download your `google-services.json` from Firebase Console and place it in `app/`.
3. Enable Google Sign-In in Firebase Console:
   - Go to Firebase Console > Authentication > Sign-in method
   - Enable Google Sign-In provider
   - Add your app's SHA-1 fingerprint to the project settings
4. Sync Gradle and run the app.

### Face Recognition Setup Notes
- No additional API keys required (on-device ML Kit face detection)
- Permissions handled at runtime: Camera, and Storage (only on Android 12 and lower)
- Uses Android 13+ Photo Picker when available (no storage permission needed)

### OCR Setup Notes
- No additional API keys required (on-device ML Kit text recognition)
- Permissions handled at runtime: Camera, and Storage (only on Android 12 and lower)
- Uses Android 13+ Photo Picker when available (no storage permission needed)
- Supports multiple languages: English, Chinese, Japanese, Korean, and Devanagari
- Automatic language detection based on recognized text patterns

### Navigation Setup Notes
- **Google Maps API Key Required**: Add your API key to `local.properties` file
- **Required APIs**: Enable in Google Cloud Console:
  - Maps SDK for Android
  - Places API (Legacy) - doesn't require billing
  - Directions API
- **Permissions**: Location permissions handled at runtime
- **Internet Required**: For map tiles, place search, and directions
- **Setup Guide**: See `NAVIGATION_API_SETUP_GUIDE.md` for detailed configuration

## Features
- Single-Activity architecture with Navigation Component
- Main Menu with four core feature options (Face Recognition, Object Detection, OCR, Navigation)
- Sign-In screen with Google authentication
- User profile display with photo, name, and email
- Sign-out functionality
- Firebase Authentication and Cloud Messaging (FCM) integrated
- Anonymous authentication support
- Material Design UI components
- Responsive layout for different screen sizes
- **Face Recognition (ML Kit)**: capture or pick an image, on-device face detection, bounding boxes overlay, face count, and details
- **OCR (Optical Character Recognition)**: extract text from images using ML Kit Text Recognition API with multi-language support, camera capture, gallery selection, and text sharing capabilities
- **Navigation (Google Maps)**: full-featured navigation with Google Maps integration, location services, place search, and route planning

## Dependencies
- AndroidX
- Kotlin
- Navigation Component
- Firebase Auth
- Firebase Cloud Messaging
- Google Play Services Auth
- Glide (for image loading)
- ML Kit Face Detection (on-device)
- **ML Kit Text Recognition** (on-device OCR for multiple languages)
- **CameraX** (camera integration for OCR)
- **Kotlin Coroutines** (asynchronous OCR processing)
- **Google Maps SDK** (interactive maps and location services)
- **Google Places API** (place search and autocomplete)
- **Google Directions API** (route planning and navigation)

## 📸 Screenshots

<div style="text-align: center;">
  <img src="screenshots/screen1.jpg" width="200" alt="Home Screen"/>
  <img src="screenshots/screen2.jpg" width="200" alt="Dummy Anonymous Sign-in"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen3.jpg" width="200" alt="Google Sign-in Successful with User Info Shown"/>
  <img src="screenshots/screen4.jpg" width="200" alt="FCM Notification Message Test"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen5.jpg" width="200" alt="Main Menu (Includes 4 - Features"/>
  <img src="screenshots/screen6.jpg" width="200" alt="Sample Fragment A/c to Respected Feature"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen7.jpg" width="200" alt="Face Recognition (Capture or Pick)"/>
  <img src="screenshots/screen8.jpg" width="200" alt="Sample of face detection results and bounding boxes"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen9.jpg" width="200" alt="Object detection (Live or Pick)"/>
  <img src="screenshots/screen10.jpg" width="200" alt="Live Object Detection S-1"/>
</div>
<div style="text-align: center;">
  <img src="screenshots/screen11.jpg" width="200" alt="Live Object Detection S-2"/>
  <img src="screenshots/screen12.jpg" width="200" alt="Selected Image Object Detection"/>
</div>
<div style="text-align: center;">
  <img src="screenshots/screen13.jpg" width="200" alt="OCR (Capture or Pick)"/>
  <img src="screenshots/screen14.jpg" width="200" alt="Captured Image OCR (Extract Text From Image)"/>
</div>
<div style="text-align: center;">
  <img src="screenshots/screen15.jpg" width="200" alt="Navigation (Default Location)"/>
  <img src="screenshots/screen16.jpg" width="200" alt="Current user location/>
</div>

## Usage..

### OCR (Optical Character Recognition)
1. **Navigate to OCR**: Open the app and tap "OCR" from the Main Menu.
2. **Select Language**: Choose your preferred language from the chip selection (English, Chinese, Japanese, Korean, or Devanagari).
3. **Capture or Select Image**:
   - **Camera Capture**: Tap the camera button to open the camera preview, then tap the capture button to take a photo
   - **Gallery Selection**: Tap the gallery button to select an image from your device's gallery
4. **View Results**: The app will display the recognized text with confidence score and detected language
5. **Copy or Share**: Use the copy button to copy text to clipboard or share button to share the text via other apps
6. **Try Again**: Tap camera or gallery buttons to process a new image

**Features:**
- **Multi-language Support**: Automatic language detection and manual language selection
- **Real-time Camera Preview**: Live camera feed with capture functionality
- **Image Preprocessing**: Automatic image optimization for better OCR accuracy
- **Text Post-processing**: Cleaned and formatted text output
- **Confidence Scoring**: Shows recognition confidence percentage
- **Copy & Share**: Easy text copying and sharing capabilities

**Troubleshooting:**
- If camera permission is denied, grant it from system settings and try again
- If storage permission is denied (Android 12 and lower), grant it from system settings
- Very large images may take a moment to process; a progress bar shows while processing
- For best results, ensure text is clearly visible and well-lit in the image
- Try different language settings if recognition accuracy is low

### Navigation (Google Maps)
1. **Open Navigation**: Tap "Navigation" from the Main Menu to open the full-featured Google Maps navigation
2. **Grant Location Permissions**: Allow location access when prompted for current location functionality
3. **Search for Places**: Use the search bar to find destinations with autocomplete suggestions
4. **Get Current Location**: Tap "Current Location" button to center the map on your current position
5. **Select Destination**: Choose a place from search results to set as your destination
6. **Get Directions**: Tap "Navigate" to get driving directions with route visualization
7. **View Route Info**: See distance and estimated travel time for your selected route
8. **Clear Route**: Tap "Clear Route" to remove the current route and start over

**Features:**
- **Interactive Google Maps**: Full-screen map with zoom, pan, and map type controls
- **Location Services**: Real-time current location tracking with GPS
- **Place Search**: Google Places API integration with autocomplete suggestions
- **Route Planning**: Google Directions API for driving directions and route visualization
- **Visual Route Display**: Polylines showing the complete route on the map
- **Distance & Duration**: Real-time route information display
- **Modern UI**: Material Design interface with floating action buttons

**Setup Requirements:**
- **Google Maps API Key**: Configure in `local.properties` file
- **Required APIs**: Enable Maps SDK for Android, Places API (Legacy), and Directions API in Google Cloud Console
- **Location Permissions**: Fine and coarse location permissions for GPS functionality
- **Internet Connection**: Required for map tiles, place search, and directions

**Troubleshooting:**
- If location permission is denied, grant it from system settings
- If search doesn't work, check your Google Maps API key configuration
- If routes don't display, verify Directions API is enabled in Google Cloud Console
- For best results, use the app in areas with good GPS signal

### Face Recognition
1. Open the app and navigate to Face Recognition from the Main Menu.
2. Tap "Capture Image" to take a photo or "Pick Image" to select from gallery.
3. The app draws green bounding boxes around detected faces and shows the face count and details.
4. Tap "Clear" to reset the view.

Troubleshooting:
- If camera permission is denied, grant it from system settings and try again.
- Very large images may take a moment to process; a spinner shows while detecting.


### Google Sign-In Setup
1. **Firebase Console Setup:**
   - Enable Google Sign-In in Firebase Console
   - Add your app's SHA-1 fingerprint to project settings
   - Download updated `google-services.json`

2. **SHA-1 Fingerprint:**
   - For debug: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - For release: Use your release keystore

3. **Testing:**
   - Run the app
   - Tap "Sign in with Google" button
   - Complete Google Sign-In flow
   - View user profile information
   - Use "Sign Out" to log out

### Navigation
- **Main Menu:** Default start destination with four core feature options
- **Home Screen:** Shows current user info and navigation options (which includes Anonymous Sign-in and Go to Sign-in Buttons)
- **Feature Screens:** Face Recognition, OCR, and Navigation are fully implemented. Object Detection is a placeholder.

### Main Menu Navigation Testing

1. **Launch the App:**
   - The app will start at the Main Menu screen
   - You'll see four cards: Face Recognition, Object Detection, OCR, and Navigation

2. **Test Navigation to Features:**
   - Tap on Face Recognition to try the live face detection feature
   - Tap on OCR to try the live text recognition feature
   - Tap on Navigation to open the full-featured Google Maps navigation
   - Object Detection card navigates to a placeholder screen with a "coming soon" message
   - Use the back button or action bar back arrow to return to the main menu

3. **Test Responsive Layout:**
   - Rotate the device to test landscape orientation
   - The cards will adjust to maintain proper spacing and readability
   - Test on different screen sizes (phone, tablet) if available

4. **Test Back Navigation:**
   - From any feature screen, press the back button
   - You should return to the main menu
   - The action bar should show the correct title for each screen

5. **Access Main Menu from Home:**
   - Navigate to the Home screen using the "Go to Main Menu" button
   - This provides an alternative way to access the main menu

### Firebase Cloud Messaging (FCM) Setup & Testing

### How to Test Push Notifications

1. **Get Device FCM Token:**
   - Run the app. The FCM token will be logged in Logcat with tag `FCM_TOKEN` and shown as a Toast on app start.
   - Copy this token for use in Firebase Console.
2. **Send Test Notification:**
   - Go to Firebase Console > Cloud Messaging > Send your first message.
   - Enter a title and message.
   - Under 'Target', select 'Single device' and paste your device's FCM token.
   - Send the message.
3. **Observe Notification:**
   - If the app is in foreground, a Toast will display the message.
   - If the app is in background or killed, a notification will appear in the system tray.

### Notes
- The app logs and displays the FCM token on every launch.
- The app handles both notification and data payloads.
- The FCM service is declared in the manifest and handles token refresh automatically.

## OCR Technical Implementation

### Architecture
- **Fragment-based UI**: `OcrFragment.kt` handles user interactions and UI updates
- **Utility Class**: `OcrUtils.kt` provides OCR processing and image preprocessing
- **CameraX Integration**: Real-time camera preview with `PreviewView` and `ImageCapture`
- **ML Kit Text Recognition**: On-device text recognition with language-specific recognizers

### Key Components

#### 1. Language Support
```kotlin
enum class Language {
    ENGLISH, CHINESE, JAPANESE, KOREAN, DEVANAGARI
}
```
- **English**: Default ML Kit recognizer
- **Chinese**: Chinese-specific recognizer for better accuracy
- **Japanese**: Japanese-specific recognizer
- **Korean**: Korean-specific recognizer  
- **Devanagari**: Hindi/Sanskrit script support

#### 2. Image Processing Pipeline
1. **Image Loading**: Optimized bitmap loading with memory management
2. **Preprocessing**: Automatic resizing for large images (max 2048px)
3. **OCR Processing**: Language-specific text recognition
4. **Post-processing**: Text cleaning and artifact removal
5. **Result Display**: Formatted output with confidence scoring

#### 3. Camera Integration
- **CameraX**: Modern camera API with lifecycle management
- **Preview**: Real-time camera feed display
- **ImageCapture**: High-quality photo capture
- **Permissions**: Runtime permission handling for camera and storage

#### 4. UI Features
- **Language Selection**: Chip-based language picker
- **Camera Preview**: Live camera feed with capture button
- **Image Preview**: Display of captured/selected images
- **Text Results**: Formatted text display with confidence score
- **Action Buttons**: Copy to clipboard and share functionality
- **Theme Support**: Automatic light/dark theme adaptation

### Performance Optimizations
- **Memory Management**: Efficient bitmap loading and disposal
- **Async Processing**: Coroutine-based OCR processing
- **Image Resizing**: Automatic optimization for large images
- **Background Processing**: Non-blocking UI during OCR operations

### Error Handling
- **Permission Denied**: Graceful handling with user guidance
- **Image Loading Failures**: Error messages and fallback options
- **OCR Failures**: User-friendly error messages
- **No Text Detected**: Clear feedback to user

## Navigation Technical Implementation

### Architecture
- **Activity-based UI**: `NavigationActivity.kt` handles full-screen navigation interface
- **Utility Classes**: `LocationManager.kt` and `DirectionsManager.kt` provide specialized functionality
- **Google Maps Integration**: Native Android Maps SDK with custom controls
- **Places API Integration**: Legacy Places API for place search and autocomplete

### Key Components

#### 1. Location Services
```kotlin
class LocationManager(private val context: Context) {
    suspend fun getCurrentLocation(): Location?
    fun getLocationUpdates(): Flow<Location>
    fun hasLocationPermission(): Boolean
}
```
- **FusedLocationProviderClient**: Google's recommended location provider
- **Real-time Updates**: Continuous location tracking with configurable intervals
- **Permission Management**: Runtime permission handling and validation
- **Distance Calculations**: Utility functions for location-based calculations

#### 2. Directions API Integration
```kotlin
class DirectionsManager(private val context: Context, private val apiKey: String) {
    suspend fun getDirections(origin: LatLng, destination: LatLng): Result<RouteInfo>
    suspend fun getTravelTime(origin: LatLng, destination: LatLng): Result<String>
}
```
- **HTTP-based Implementation**: Direct API calls to Google Directions API
- **Polyline Decoding**: Custom algorithm for route visualization
- **Route Information**: Distance and duration parsing from API responses
- **Error Handling**: Comprehensive error management and user feedback

#### 3. Places API Integration
- **AutocompleteSupportFragment**: Built-in place search with suggestions
- **Place Selection**: Automatic destination setting from search results
- **Legacy API**: Uses Places API (Legacy) that doesn't require billing
- **Real-time Search**: Live autocomplete as user types

#### 4. Map Features
- **Interactive Maps**: Full Google Maps functionality with custom controls
- **Marker Management**: Current location and destination markers
- **Route Visualization**: Polylines with custom styling and colors
- **Camera Controls**: Automatic centering and zoom management
- **Floating Action Button**: Quick access to current location

### Performance Optimizations
- **Memory Management**: Efficient marker and polyline management
- **Async Operations**: Coroutine-based location and API operations
- **Request Caching**: Optimized API calls and response handling
- **Background Processing**: Non-blocking UI during location updates

### Error Handling
- **Location Errors**: Graceful handling of GPS and permission issues
- **API Failures**: User-friendly error messages for network issues
- **Permission Denied**: Clear guidance for enabling location access
- **Network Issues**: Offline map support and retry mechanisms

### Security Considerations
- **API Key Protection**: Secure storage in local.properties
- **Location Privacy**: User consent and permission management
- **Request Validation**: Input sanitization and validation
- **Error Logging**: Comprehensive logging without sensitive data exposure

---

**Note:** This project is set up for easy extension and task experimentation. 

