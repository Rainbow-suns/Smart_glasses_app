# Smart_glasses_app
### Overview
This project, developed in Android Studio 2022.2.1, facilitates interaction with audio and video on smart glasses. While the webcam is accessed via HTTP requests, the audio transmission uses the MQTT protocol with the EMQX server. The application subscribes to the ESP32_RECVER topic and publishes to the ESP32_SENDER. Additionally, user login information is stored securely using an SQLite database.


### Features
•	Login function: The user must register first, the username and password will be stored in the database, and then can be used to complete the login.

•	Audio playback function: Receive audio signals forwarded from the server in a specific format and play them using the phone's speakers. 

•	Audio record function: Use the mobile phone microphone to record the voice and send the audio data to the server in a format that the ESP-32 can receive.

•	Video playback function: Use the phone screen to play the footage captured by the glasses’ camera in real time.


### Prerequisites
Android Studio 2022.2.1 or later.

Device/simulator Android 5.1 and above

Smart Glasses with webcam support.

EMQX Server instance (for MQTT communication).


### Setup
**1. Clone the repository:**
```bash
git clone https://github.com/Rainbow-suns/Smart_glasses_app.git
```
**2. Open the project in Android Studio:**
Navigate to the project directory and open it.

**3. Configure MQTT Settings:**
Ensure the MQTT server address, port, and credentials match your EMQX server configuration.

**4. SQLite Database:**
No additional setup is required. The app handles the creation and management of the SQLite database.

**5. Run the Application:**
Connect a suitable Android device or emulator, then build and run the application.


### Usage
**1. Login/Signup:**
Users need to either log in or sign up. The information are stored in the SQLite database.

**2. Access Webcam:**
Initiate the webcam on the smart glasses by sending an HTTP request from the app.

**3. Audio Communication:**
The app captures audio, processes it, and then sends the audio data via MQTT to the ESP32_SENDER topic. It also listens to incoming audio data from the ESP32_RECVER topic.


### Troubleshooting
MQTT Connection Issues: Ensure the EMQX server is running and accessible. Check network configurations and server logs for more details.
Audio/Video Lag: Check the network quality. Also, ensure the smart glasses have sufficient resources for smooth operation.
Database Errors: Ensure you have the necessary permissions for reading/writing data. Refer to logs for specific error messages.


### Contributions
Contributions to this project are welcome! Please raise an issue or submit a pull request.
