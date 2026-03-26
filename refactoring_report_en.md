
---

## List of Applied Changes

### 1. Dead Code
*   **Problem:** The `randomizeCube` method contained legacy coordinate calculations that were commented out, creating visual noise and confusion.
*   **Before:**
    ```java
    // float x = FastMath.nextRandomInt(playerX + difficulty + 10, playerX + difficulty + 150);
    float x = FastMath.nextRandomInt(playerX + difficulty + 30, playerX + difficulty + 90);
    // playerX+difficulty+30,playerX+difficulty+90
    ```
*   **After:**
    ```java
    int scale = session.getSpawnAreaScale();
    Vector3f playerPos = playerManager.getLocation();

    float minX = playerPos.x + scale + minDistanceX;
    float maxX = playerPos.x + scale + maxDistanceX;
    // ... logic continues with dynamic GameSession parameters ...
    ```
*   **Solution:** Complete removal of "zombie code" and transition to dynamic parameters within the `GameSession`.
*   **Rationale:** Clean code must be free of debris. Version history is managed by Git; commented-out lines only distract from current logic.

### 2. Magic Numbers
*   **Problem:** Use of hardcoded numeric literals (e.g., `400f`) for speed and timing without context.
*   **Before:** `speed = lowCap / 400f;`
*   **After:** `moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;`
*   **Solution:** Extraction of constants into named variables like (`SPEED_DIVIDER_CONSTANT`).
*   **Rationale:** Improves **self-documentation**. The constant name explains the physical or logical intent of the value.

### 3. Meaningless Names
*   **Problem:** The variable `difficulty` was too generic and did not reflect its actual purpose (controlling the spawn area size).
*   **Before:** `private int difficulty;`
*   **After:** `private int spawnAreaScale;`
*   **Solution:** Renamed the field to `spawnAreaScale`.
*   **Rationale:** Proper naming reduces cognitive load and makes the code's intent clear at a glance.

### 4. Long Method
*   **Problem:** The `gameLogic` method was a "God Method," handling physics, timers, collisions, and UI updates simultaneously.
*   **Before:** A single method exceeding 100 lines of mixed logic.
*   **After:**
    ```java
    private void runGameLogic() {
        session.update();    
        playerManager.moveForward();
        obstacleManager.update();
        if (obstacleManager.checkCollisions()) {
            handleGameOver();
        }
        uiManager.update();
        themeManager.checkThemeUpdate();
    }
    ```
*   **Solution:** Applied the **Extract Method** technique.
*   **Rationale:** Adheres to the **Single Responsibility Principle (SRP)**. Smaller methods are easier to test and maintain.

### 5. Switch Statements (God Switch)
*   **Problem:** A massive `switch` block controlled visual themes, making it difficult to add new styles without modifying core logic.
*   **Solution:** Replaced conditional logic with a collection of `GameTheme` objects (Polymorphism).
*   **Rationale:** Enhances scalability. Adding a new theme now only requires creating a new object, not editing core switching logic.

### 6. God Object
*   **Problem:** The main class `CubeField` (SimpleApplication) managed every system, from input to rendering.
*   **Solution:** Applied **Extract Class**. Decomposed into `PlayerManager`, `ObstacleManager`, `ThemeManager`, `UIManager`, and `CameraManager`.
*   **Rationale:** Decomposing a large class into single-responsibility objects simplifies maintenance and unit testing.

### 7. Code Duplication
*   **Problem:** `tpf` (Time Per Frame) calculations were repeated across multiple classes for movement and scoring.
*   **Solution:** Created a centralized `TpfTpsHandler` class to handle time-step synchronization.
*   **Rationale:** Adheres to the **DRY (Don't Repeat Yourself)** principle. Logic changes only need to occur in one place.

### 8. Primitive Obsession
*   **Problem:** Direct manipulation of raw float coordinates for player movement.
*   **Before:** `player.move(0, 0, (speed / 2f) * value * fpsRate);`
*   **After:** 
    ```java
    playerManager.moveLeft();
    playerManager.moveRight();
    ```
*   **Solution:** Created high-level domain methods within specialized managers.
*   **Rationale:** Transition from manipulating raw numbers to high-level game concepts (Move Forward, Left, Right).

### 9. Message Chains
*   **Problem:** Accessing deeply nested object properties (e.g., `object.getA().getB().getX()`).
*   **Solution:** Implemented wrapper methods to hide delegation.
*   **Rationale:** Adheres to the **Law of Demeter**. Objects should only talk to their immediate neighbors.

### 10. Inappropriate Intimacy
*   **Problem:** The obstacle generator was directly calculating coordinates based on the player object's internal state.
*   **Solution:** Used the `getLocation()` method in `PlayerManager` to encapsulate player spatial data.
*   **Rationale:** Reduces coupling between systems, making the code more robust against changes in the player's internal implementation.

---

## Test Suite Overview (29 Scenarios)

### 1. Initialization & Mapping (3 Tests)
*   **1. `testKeyboardMappings()`**: Verifies registration of "START", "Left", and "Right" in the jME system.
*   **2. `testInputManagerMappingsExist()`**: Ensures all required keys are present in the mapping dictionary.
*   **3. `testListenerRegistration()`**: Confirms the `InputManager` correctly attached the event listener.

### 2. Game State Management (2 Tests)
*   **4. `testStartGameActionWhenGameIsNotStarted()`**: Checks if the game transitions to an active state on "Enter".
*   **5. `testStartGameActionWhenGameIsAlreadyStarted()`**: Prevents duplicate logic execution if the game is already running.

### 3. Movement Mechanics (4 Tests)
*   **6. `testMoveLeftActionWhenGameIsStarted()`**: Verifies leftward translation and camera tilt.
*   **7. `testMoveRightActionWhenGameIsStarted()`**: Verifies rightward translation.
*   **8. `testMovementActionsWhenGameIsNotStarted()`**: Ensures input is ignored if the game hasn't started.
*   **9. `testSidewaysValueCalculation()`**: Validates movement step accuracy relative to current speed.

### 4. Game Session (`GameSession`) (5 Tests)
*   **10. `testScoreIncrementsBasedOnTimeStep()`**: Validates score growth relative to elapsed time.
*   **11. `testSpeedIncrementsCorrectly()`**: Confirms speed acceleration logic per tick.
*   **12. `testSpeedDoesNotExceedMaxLimit()`**: Enforces the maximum speed cap.
*   **13. `testDifficultyScalingReducesSpawnArea()`**: Verifies spawn area shrinks every 10 seconds.
*   **14. `testDifficultyDoesNotScaleBelowObstacleCount()`**: Validates the lower bounds of the spawn area.

### 5. Game Lifecycle (`GameRunner`) (3 Tests)
*   **15. `testGameRunnerStateTransitionOnCollision()`**: Stops logic execution upon collision.
*   **16. `testResetRestoresInitialDataState()`**: Confirms score and speed reset on game restart.
*   **17. `testStateFreezesAfterCollision()`**: Ensures no data mutation occurs after a "Game Over" event.

### 6. Obstacle Management (`ObstacleManager`) (4 Tests)
*   **18. `testCheckCollisionsDetectsCollision()`**: Validates intersection detection between player and cubes.
*   **19. `testSpawnIfNeededSpawnsObstacles()`**: Checks automatic generation of cubes when count is low.
*   **20. `testCleanupRemovesOffscreenObstacles()`**: Verifies removal of cubes that are behind the player.
*   **21. `testClearRemovesAllObstacles()`**: Ensures full cleanup during transitions.

### 7. Visual Themes (`ThemeManager`) (5 Tests)
*   **22. `testInitialThemeStateOnReset()`**: Verifies default theme application.
*   **23. `testThemeChangeTiming()`**: Checks theme rotation every 20 seconds.
*   **24. `testCyclingThroughAllThemes()`**: Validates the theme loop (Last -> First).
*   **25. `testPlayerMaterialUpdatesWithTheme()`**: Ensures player color syncs with the active theme.
*   **26. `testRandomObstacleColorFromCurrentTheme()`**: Confirms obstacle colors belong to the active palette.

### 8. User Interface (`UIManager`) (3 Tests)
*   **27. `testInitialStatusOnStartup()`**: Displays "PRESS ENTER" at launch.
*   **28. `testGameOverShowsExactMessage()`**: Validates loss notification text.
*   **29. `testUIManagerFormatsScoreCorrectly()`**: Validates string formatting for "Current Score: [Value]".
