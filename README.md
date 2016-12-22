# Resident Tracking - Android App
The Resident Android App is a client for the nurse/staff to get notified about alerts by residents. The App displays alerts, send push notifications about new alerts (Android Wear compatible), provide informations about the residents and locations. Also the App shows the name of the nearest resident to the smartphone owner.

### Software requirements
* Android Studio https://developer.android.com/studio/

### Getting Started
1. Clone this repository

2. Use Android Studio to and open the project (= repository root directory)

3. Configure the API URL
   * Open Gradle Scripts -> build.gradle (Module: app)
   * Change buildConfigField API_ROOT for productFlavors "develop" and "staging"
``` groovy
buildConfigField 'String', 'API_ROOT', '"http://my-resident-tracking-server"'
```

### Environments
* Develop - Developing environment.
   * App point to the develop-Server

* Staging - Staging- / Demonstration environment
  * App point to the staging-Server

### Permissions
* android.permission.ACCESS_NETWORK_STATE
  * Check if there is an internet connecetion available

* android.permission.INTERNET
  * Connect to the API

### Firebase Cloud Messaging
The Android App is using Firebase Cloud Messaging for Push Notifications between the Resident Tracking Server and the App.
The Google service file that describe the connection to Firebase can be found in /app/google-services.json

### Found a bug?
* Please open an issue in GitHub

### See also
* Resident Tracking Server Repository - https://github.com/qinjie/PatientTracking-Web