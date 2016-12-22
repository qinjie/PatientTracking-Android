# Resident Tracking - Android App

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

### Package Overview

### Permissions

### Firebase Cloud Messaging

### Found a bug?
* Please open an issue in GitHub

### See also
* Resident Tracking Server Repository - https://github.com/qinjie/PatientTracking-Web