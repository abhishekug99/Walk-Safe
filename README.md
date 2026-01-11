# Walk Safe

**Walk Safe** is an Android mobile application designed to ensure personal safety, with a primary focus on women. It provides real-time tracking, SOS alerts, and additional safety features like spy camera detection, self-defense tools, and access to self-defense tutorial videos.

---

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [How It Works](#how-it-works)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Introduction

**Walk Safe** empowers individuals, especially women, with the tools to safeguard themselves in distress situations. With features like instant SOS alerts, live location tracking, and spy camera detection, it ensures that users can stay safe and informed. The app also includes a collection of self-defense tutorial videos powered by the YouTube API, providing users with useful information during emergencies.

---

## Features

- **SOS Alert**: Shake the phone to trigger an SOS alert, sending your real-time location to emergency contacts and authorities.
- **Live Location Sharing**: Share your real-time location with trusted contacts to keep them informed of your safety.
- **Self-Defense Tools**: Use self-defense features, including a siren and live location updates.
- **Self-Defense Tutorial Videos**: Access self-defense tutorial videos powered by the YouTube API, helping you learn important techniques.
- **Spy Camera Detection**: Detect hidden cameras by using the phone’s magnetometer to measure magnetic field strength.
- **Emergency Contact Setup**: Add multiple emergency contacts who will receive alerts in case of an emergency.
- **Activity History**: Keep a log of past alerts and safety-related activities for monitoring and review.
- **Safety News**: Stay updated with local safety news to stay aware of potential risks.

---

## Tech Stack

- **Android Core APIs**:
    - **Sensors**: Magnetometer, Accelerometer, Gyroscope
    - **Location Services**: FusedLocationProviderClient, Google Maps SDK
    - **Camera**: CameraX (for IR detection)
    - **Notifications**: Notification API
- **Backend & Cloud Services**:
    - **Database**: Firebase Firestore (for contact storage and syncing)
    - **Authentication**: Firebase Authentication
    - **Location-Based Services**: Google Maps Places API
    - **News API**: External news API for local safety news
    - **YouTube API**: For providing self-defense tutorial videos from YouTube

---

## How It Works

1. **Shake Detection**: The app detects when the user shakes their phone and automatically triggers an SOS alert.
2. **SOS Alert**: When triggered, the app sends the user's live location to emergency contacts and nearby authorities.
3. **Live Tracking**: Emergency contacts can track the user's real-time location for continuous support.
4. **Spy Camera Detection**: The app uses the magnetometer to detect hidden cameras, ensuring user privacy.
5. **Accidental Trigger Prevention**: Users have 5 seconds to cancel the alert if it was triggered accidentally.
6. **Self-Defense Tutorial Videos**: The app offers self-defense videos powered by the YouTube API, helping users learn critical techniques.

---

## Installation

To install the Walk Safe app on your Android device:

1. Clone the repository:
   ```bash
   git clone https://github.com/abhishekug99/Walk-Safe.git

## Conributors:

1. Abhishek Umesh Gavali
2. Uddesh Shyam Kshirsagar
3. Namita Sacchidanand Naikwadi
## Contributing:

We welcome contributions to Walk Safe! If you would like to contribute, please follow these steps:

## Fork the repository.

1. Create a new branch (git checkout -b feature-branch).

2. Make your changes.

3. Commit your changes (git commit -m 'Add new feature').

4. Push to the branch (git push origin feature-branch).

5. Create a pull request to merge your changes.
