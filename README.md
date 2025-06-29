#This is a modular Java application developed as part of the Udacity Security Engineer Nanodegree. The project simulates a home security system, integrating image processing, sensor logic, and GUI components.

---

## 📌 Project Overview

The CatPoint system monitors a home environment for intrusions using motion sensors and image recognition. The goal is to simulate and implement the detection of unauthorized entries while differentiating common pets like cats from potential threats.

The system consists of three primary modules:

1. **Security Service**  
   Handles all sensor logic, system arming/disarming, alarm state, and repository integration.

2. **Image Service**  
   Interfaces with either a fake image classifier or a mock AWS Rekognition service to identify the presence of a cat.

3. **Security GUI**  
   A Java Swing-based graphical user interface to control and monitor the system.

---

## ✅ Project Requirements

- Build a modular, component-based application using Java.
- Implement core sensor and alarm logic.
- Integrate an image classifier service (mock or simulated).
- Build a responsive GUI to interact with system states.
- Ensure full Maven integration and reporting (Surefire, SpotBugs, etc.).
- Include JUnit-based unit tests for core components.

---

## 🧠 What the System Does

- Allows users to arm/disarm the security system.
- Processes input from motion/contact sensors.
- Uses image classification to determine if a cat is present.
- Automatically sets off alarms based on combined sensor and image data.
- Provides a user interface for real-time monitoring and control.
- Generates reports via Maven tools like SpotBugs and Surefire.

---

## 🧱 Tech Stack

| Component     | Technology                     |
|---------------|-------------------------------|
| Language       | Java 11+                      |
| Build Tool     | Maven                         |
| UI Framework   | Java Swing                    |
| Testing        | JUnit 5, Maven Surefire       |
| Static Analysis| SpotBugs                      |
| Modularization | Java Modules (module-info.java) |

---

## 🧪 Unit Test Coverage

### `SecurityServiceTest.java`
- Verifies alarm state transitions.
- Tests sensor activation/deactivation handling.
- Validates logic for presence of cats under armed conditions.
- Ensures image detection correctly influences alarm state.

### `AppTest.java` (image-service)
- Placeholder/unit structure for future testing of image service components.

---

## 📊 Reports and Analysis

- **SpotBugs**: Found under `target/site/spotbugs.html`.
- **Surefire**: JUnit test output available in `target/surefire-reports/`.

---

