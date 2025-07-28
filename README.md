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

## Dependencies
- AndroidX
- Kotlin
- Navigation Component
- Firebase Auth
- Firebase Cloud Messaging
- Google Play Services Auth
- Glide (for image loading)

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

## Usage

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
- **Feature Screens:** Placeholder screens for Face Recognition, Object Detection, OCR, and Navigation features

### Main Menu Navigation Testing

1. **Launch the App:**
   - The app will start at the Main Menu screen
   - You'll see four cards: Face Recognition, Object Detection, OCR, and Navigation

2. **Test Navigation to Features:**
   - Tap on any of the four feature cards
   - Each will navigate to a placeholder screen with the feature name and "coming soon" message
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

---

**Note:** This project is set up for easy extension and task experimentation. 
