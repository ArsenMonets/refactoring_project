# Refactoring Project: CubeField Game

##  Project Description
This project is a refactored version of the "CubeField" game, built using the **jMonkeyEngine** (jME3) framework. The primary goal was to transform a monolithic, difficult-to-maintain codebase into a modern, modular, and testable application. It serves as a practical demonstration of identifying "code smells" and applying structural refactoring techniques to improve software architecture.

##  Key Refactoring Achievements
The project moved away from a "God Object" architecture toward a **Manager-based Component** system:
*   **Separation of Concerns:** Logic is split into specialized managers: `PlayerManager`, `ObstacleManager`, `ThemeManager`, `UIManager`, and `CameraManager`.
*   **Encapsulation:** Direct access to spatial coordinates and raw game states was replaced with high-level domain methods.
*   **Consistency:** Implementation of a `TpfTpsHandler` ensures that game physics and scoring remain consistent regardless of the user's frame rate (FPS).
*   **Scalability:** Visual themes are now handled through polymorphism rather than massive `switch` blocks, allowing for easy addition of new palettes.

---

##  Getting Started & Setup

### **Prerequisites**
*   **Java Development Kit (JDK) 8** or higher.
*   **Maven 3.6+** (for dependency management).
*   An IDE like **IntelliJ IDEA** (recommended) or **Eclipse**.

### **Option 1: IntelliJ IDEA**
1.  **Open Project:** Select `File > Open`, navigate to the project root, and select the `pom.xml` file. Choose **Open as Project**.
2.  **Sync Maven:** Wait for the IDE to download jMonkeyEngine and JUnit libraries.
3.  **Run the Game:** Navigate to `src/main/java`, find the main class (e.g., `CubeField.java`), right-click and select **Run**.
4.  **Run Tests:** Right-click the `src/test/java` folder and select **Run 'All Tests'**.

### **Option 2: Eclipse IDE**
1.  **Import:** `File > Import... > Maven > Existing Maven Projects`.
2.  **Configuration:** Ensure your Project Facets are set to Java 8+.
3.  **Run:** Right-click the project -> `Run As > Java Application`.
4.  **Test:** Right-click the project -> `Run As > JUnit Test`.

### **Option 3: Command Line**
*   **Build:** `mvn clean install`
*   **Run Tests:** `mvn test`
*   **Run Game:** `mvn exec:java -Dexec.mainClass="com.github.arsenmonets.refactoringproject.refactored(old).CubeField"`

---

##  How to Play
*   **ENTER:** Start or Restart the game.
*   **LEFT ARROW / A:** Move the player to the left.
*   **RIGHT ARROW / D:** Move the player to the right.
*   **Objective:** Avoid the cubes! The speed and difficulty (spawn area density) increase as you progress.

---

##  Main Techniques Applied
1.  **Removing Dead Code:** Cleaned up "zombie" comments and unused calculations.
2.  **Extracting Magic Numbers:** Used named constants for speed dividers and scales.
3.  **Renaming Variables:** Improved clarity (e.g., `difficulty` became `spawnAreaScale`).
4.  **Extract Method:** Decomposed 100+ line methods into readable sub-routines.
5.  **Long switch:** Replaced `switch(colorInt)` with a `GameTheme` collection.
6.  **Extract Class:** Broke down the `CubeField` into 6+ specialized classes.
7.  **Duplication:** Centralized delta-time logic in `TpfTpsHandler`.
8.  **Primitive Obsession:** Replaced raw coordinate manipulation with `moveLeft()`/`moveRight()`.
9.  **Message Chains:** Simplified message chains to prevent deep object coupling.
10. **Data clumps:** Used classes for related objects.

---

##  Test Suite Overview (29 Scenarios)
The project includes comprehensive unit tests covering:
*   **Input System:** Validation of key mappings and listener registrations.
*   **Mechanics:** Player movement, collision detection, and score calculation.
*   **Session Logic:** Acceleration curves, difficulty scaling, and speed capping.
*   **Themes & UI:** Theme rotation timing, material updates, and UI message accuracy.
*   **Lifecycle:** State transitions during Game Over and proper data resets.

---

##  Project Structure
- README.md
- refactoring_report_en.md
- refactoring_report_ua.md
- src
  - main
    - java
      - com
        - github
          - arsenmonets
            - refactoringproject
              - old
                - CubeField.java
              - refactored
                - CubeField.java
                - core
                  - GameRunner.java
                  - GameSession.java
                - input
                  - GameInputManager.java
                - objectmanagers
                  - CameraManager.java
                  - EnvironmentManager.java
                  - ObstacleManager.java
                  - PlayerManager.java
                - themes
                  - GameTheme.java
                  - ThemeManager.java
                - tpftps
                  - TpfTpsHandler.java
                - ui
                  - UIManager.java
- test
  - java
    - com
      - github
        - arsenmonets
          - refactoringproject
            - ThemeTest.java
            - GameRunnerTest.java
            - ObstacleManagerTest.java
            - UITest.java
            - GameInputTest.java
- pom.xml