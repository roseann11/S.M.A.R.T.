
## :spiral_notepad: Project Overview

**Objective**  
<p align="justify">
This project aims to develop an IoT-enabled smoke detection system that integrates with a mobile Android app for real-time notifications. The smoke detector will monitor environments for smoke or fire, and when detected, send alerts to users through the Android app. By leveraging IoT technology, the system ensures immediate response and enhances safety in residential and commercial spaces. The app, built using Android Studio, allows users to receive instant notifications, providing them with remote monitoring capabilities for faster action and improved fire safety management.
<p>
  
---
## :old_key: Features
- **Real-Time Smoke Detection:** Uses IoT sensors to continuously monitor for smoke and trigger alerts.
- **Remote Monitoring:** Allows users remotely monitor smoke detection via the Android app for added safety.
- **Fire Prevention Tips:** Offers educational content and preventive measures to minimize fire risks and enhance safety practices.
- **SOS Messaging:** Allows users to enable an SOS mode to auto-send messages to emergency contacts during orange or red smoke levels.
- **Emergency Contact Management:** Lets users add, edit, or delete emergency contacts for automated text notifications during critical events.
- **Emergency Contact Alerts:** Sends critical messages to emergency contacts during orange or red smoke levels for timely help.

---

## :robot: Hardware
- **ESP32 Microcontroller:** Integrated Wi-Fi and Bluetooth connectivity allows connection of the hardware and the mobile device. 
- **MQ2 Gas Sensor:** Used for gas leak detection, can detect alcohol, carbon monoxide, liquefied petroleum gas(LPG), methane, propane, and smoke. 
- **Active Buzzer:** Gives off a sound alarm when high concentration of gas or smoke is detected to notify nearby users.
- **RGB LED Module:** Serves as an indicator regarding the current condition of the environment 

## :gear: Technologies Used
- **Frontend:** Android Studio (Java) – for designing and implementing the mobile app interface.
- **Backend:** Firebase Cloud Functions – for managing server-side logic and sending push notifications.
- **Database:** Firebase Realtime Database – for storing and synchronizing real-time data.
- **Notifications:** Firebase Cloud Messaging (FCM) – for delivering push notifications.
- **Communication:** SMS API – for sending emergency text alerts.
- **Version Control:** Git and GitHub – for collaborative development and version management.

