# Firebase Setup Instructions

To use this app, you need to set up a Firebase project and configure it correctly. Follow these steps:

## 1. Create a Firebase Project

If you don't have one already, create a new project in the [Firebase Console](https://console.firebase.google.com/).

## 2. Get `google-services.json`

1.  In your Firebase project, go to **Project settings**.
2.  In the **Your apps** card, select the Android icon to add an Android app.
3.  Follow the setup steps. When prompted, download the `google-services.json` file.
4.  Place this file in the `app` directory of this project, replacing the `google-services.json.template` file.

## 3. Enable Firebase Authentication

1.  In the Firebase console, go to the **Authentication** section.
2.  Click on the **Sign-in method** tab.
3.  Enable the **Email/Password** and **Google** sign-in providers.

## 4. Configure Firebase Firestore

1.  Go to the **Firestore Database** section and create a database.
2.  Choose **Start in production mode**.
3.  Go to the **Rules** tab and paste the following rules:

    ```
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        match /users/{userId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```

4.  Publish the rules.

## 5. Configure Firebase Storage

1.  Go to the **Storage** section and click **Get started**.
2.  Follow the setup steps to create a storage bucket.
3.  Go to the **Rules** tab and paste the following rules:

    ```
    rules_version = '2';
    service firebase.storage {
      match /b/{bucket}/o {
        match /profile_images/{userId}_{imageId} {
          allow read;
          allow write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```

4.  Publish the rules.
