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
- Sign-In screen with Google authentication
- User profile display with photo, name, and email
- Sign-out functionality
- Firebase Authentication and Cloud Messaging (FCM) integrated
- Anonymous authentication support

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
  <img src="screenshots/screen1.png" width="1080" alt="Home Screen"/>
  <img src="screenshots/screen2.png" width="1080" alt="Dummy Anonymous Sign-in"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen3.png" width="1080" alt="Google Sign-in Screen"/>
  <img src="screenshots/screen4.png" width="1080" alt="User Info after Successful Google Sign-in"/>
</div>

<div style="text-align: center;">
  <img src="screenshots/screen5.png" width="1080" alt="User profile with Sign-out button"/>
  <img src="screenshots/screen6.png" width="1080" alt="Google account removed after Sign-out"/>
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
- **Sign-In Screen:** Default start destination with Google Sign-In
- **Home Screen:** Shows current user info and navigation options (which includes Anonymous Sign-in and Go to Sign-in Buttons)

---

**Note:** This project is set up for easy extension and task experimentation. 