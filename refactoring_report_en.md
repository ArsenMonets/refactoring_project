# Refactoring and Testing Report: "CubeField" Project

## Comparative Project Metrics

| Metric | Original Code (Old) | Refactored Code (New) | Result |
| :--- | :---: | :---: | :--- |
| **Number of Files** | 1 | 12 | Improved Modularity |
| **Total Functions/Methods** | 15 | ~54 | **+350%** (Granularity increase) |
| **Avg Methods per Class** | 15 | ~6 | Better adherence to SRP |
| **SLOC (Logic Lines)** | ~240 | ~680 | Expanded Capabilities |
| **Max Cyclomatic Complexity** | 11 | 4 | **Decreased by 64%** |
| **Cognitive Complexity** | High | Very Low | Better Readability |
| **Single Responsibility** | Violated (God Object) | Maintained (8+ Mgrs) | Easy to Test |

---

## List of Implemented Changes

### 1. Dead Code
*   **Problem Description:** The `randomizeCube` method contained commented-out coordinate calculations that were no longer used but created visual noise.
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
    float minZ = playerPos.z - scale - zSpread;
    float maxZ = playerPos.z + scale + zSpread;

    float x = FastMath.nextRandomFloat() * (maxX - minX) + minX;
    float z = FastMath.nextRandomFloat() * (maxZ - minZ) + minZ;

    return new Vector3f(x, 0, z);
    ```
*   **Solution:** Complete removal of commented fragments and transition to using dynamic parameters from `GameSession`.
*   **Rationale:** Clean code should be free of "trash." Idea history is preserved in Git, while commented lines only distract from active logic.

---

### 2. Magic Numbers
*   **Problem Description:** Use of hard-coded numbers (e.g., 400) to control speed and timing without explaining their meaning.
*   **Before:**
    ```java
    speed = lowCap / 400f;
    ```
*   **After:**
    ```java
    public void reset() {
        spawnAreaScale = initialSpawnScale;
        moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;
    }
    ```
*   **Solution:** Implementation of named constants (`SPEED_DIVIDER_CONSTANT`).
*   **Rationale:** This improves the self-documenting nature of the code. The constant name explains the physical meaning of the value.

---

### 3. Meaningless Names
*   **Problem Description:** The variable `difficulty` had an overly generic name that did not reflect its actual role (the size of the generation zone).
*   **Before:**
    ```java
    private int difficulty; 
    ```
*   **After:**
    ```java
    private int spawnAreaScale;
    ```
*   **Solution:** Renamed the field to `spawnAreaScale`.
*   **Rationale:** The name should clearly reflect the developer's intent. Proper naming reduces the time required to understand the code.

---

### 4. Long Method
*   **Problem Description:** The `gameLogic` method performed too many unrelated operations simultaneously (physics, timers, collisions, UI).
*   **Before:**
    ```java
    private void gameLogic(float tpf) {
        if(timer.getTimeInSeconds()>=coreTime2){
            coreTime2=timer.getTimeInSeconds()+10;
            if(difficulty<=lowCap){ difficulty=lowCap; }
            else if(difficulty>lowCap){ difficulty-=5; }
        }
        if(speed<.1f){ speed+=.000001f*tpf*fpsRate; }
        player.move(speed * tpf * fpsRate, 0, 0);
        if (cubeField.size() > difficulty){ cubeField.remove(0); }
        // ... (collision logic and cube removal)
        Score += fpsRate * tpf;
        fpsScoreText.setText("Current Score: "+Score);
    }
    ```
*   **After:**
    ```java
    public void update(float tpf) {
        tpfTpsHandler.updateTimeStep(tpf); 
        if (isGameStarted) {
            runGameLogic();
        } 
        environmentManager.update();
        cameraManager.update();
    }
    
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
*   **Solution:** Applied the **Extract Method** refactoring technique.
*   **Rationale:** Adherence to the Single Responsibility Principle (SRP). Smaller methods are easier to test and read.

---

### 5. Switch Statements
*   **Problem Description:** The `colorLogic` method used a long `switch` to change visual themes, making scaling difficult.
*   **Before:**
    ```java
    switch (colorInt) {
        case 1: // Blue theme logic
            break;
        case 2: // Red theme logic
            break;
        // ... many cases
    }
    ```
*   **After:**
    ```java
    private void applyTheme() {
        GameTheme t = themes.get(currentThemeIndex);
        render.setBackgroundColor(t.getBackground());
        playerManager.getMaterial().setColor("Color", t.getPlayer());
        enviromentManager.getFloorMaterial().setColor("Color", t.getFloor());
    }
    
    public void checkThemeUpdate() {
        if (timer.getTimeInSeconds() >= nextThemeChange) {
            nextThemeChange += themeChangeInterval;
            this.nextTheme();
        }
    }

    private void nextTheme() {
        currentThemeIndex = (currentThemeIndex + 1) % themes.size();
        applyTheme();
    }
    ```
*   **Solution:** Replaced the conditional operator with a collection of `GameTheme` objects.
*   **Rationale:** This makes the code scalable. Adding a new theme no longer requires editing the switching logic.

---

### 6. God Object
*   **Problem Description:** The `CubeField` class (SimpleApplication) managed all aspects of the game simultaneously.
*   **Before:**
    ```java
    public class CubeField extends SimpleApplication {
        // Fields for player, obstacles, cameras, UI, timers...
        // All initialization and update methods in one file.
    }
    ```
*   **After:**
    ```java
    private void initializeComponents() {
        TpfTpsHandler tpfTpsHandler = new TpfTpsHandler(TICKS_PER_SECOND);
        GameSession session = new GameSession(...);
        PlayerManager playerManager = new PlayerManager(assetManager, rootNode, session, tpfTpsHandler);
        EnvironmentManager environmentManager = new EnvironmentManager(assetManager, rootNode, playerManager);
        ThemeManager themeManager = new ThemeManager(renderer, playerManager, environmentManager, timer, THEME_CHANGE_INTERVAL);
        ObstacleManager obstacleManager = new ObstacleManager(...);
        CameraManager cameraManager = new CameraManager(cam, playerManager, tpfTpsHandler);
        UIManager uiManager = new UIManager(assetManager, guiNode, session);
        gameRunner = new GameRunner(...);
        new GameInputManager(...).init();
    }
    ```
*   **Solution:** **Extract Class** (Separating classes by areas of responsibility).
*   **Rationale:** Each class now handles its own narrow task, which simplifies maintenance.

---

### 7. Code Duplication
*   **Problem Description:** Calculation of object displacement based on `tpf` was repeated in several places in the program.
*   **Before:**
    ```java
    player.move(speed * tpf * fpsRate, 0, 0);
    Score += fpsRate * tpf;
    ```
*   **After:**
    ```java
    public class TpfTpsHandler {
        private final float ticksPerSecond;
        private float timeStep = 0f;
       	public void updateTimeStep(float tpf) {
            this.timeStep = ticksPerSecond * tpf;
        }
        
        public float getTimeStep() {
            return this.timeStep;
        }
        // ... methods for tilt and movement calculation
    }
    ```
*   **Solution:** Created a centralized `TpfTpsHandler` class to calculate the time delta.
*   **Rationale:** **DRY** (Don't Repeat Yourself) principle. Changing the time calculation logic now happens in one place.

---

### 8. Primitive Obsession
*   **Problem Description:** Player movement was managed through direct manipulation of coordinates.
*   **Before:**
    ```java
    player.move(0, 0, (speed / 2f) * value * fpsRate);
    ```
*   **After:**
    ```java
    public void moveForward() {
        playerMesh.move(tpfTpsHandler.getTimeStep() * session.getMoveSpeed(), 0, 0);
    }

    public void moveLeft() { 
        playerMesh.move(0, 0, -tpfTpsHandler.getSidewaysMoveVal() * session.getMoveSpeed()); 
    }
    
    public void moveRight() {
        playerMesh.move(0, 0, tpfTpsHandler.getSidewaysMoveVal() * session.getMoveSpeed());
    }
    ```
*   **Solution:** Created high-level methods in `PlayerManager`.
*   **Rationale:** Moving from digit manipulation to game concepts (Move Forward, Left, Right).

---

### 9. Message Chain
*   **Problem Description:** To get obstacle coordinates, the code accessed deeply nested object methods.
*   **Before:**
    ```java
    if (cubeField.get(i).getLocalTranslation().getX() + 10 < player.getLocalTranslation().getX()) { ... }
    ```
*   **After:**
    ```java
    private void cleanup() {
        float playerX = playerManager.getLocation().x;
        activeObstacles.removeIf(obstacle -> {
            if (obstacle.getLocalTranslation().x + cleanupThreshold < playerX) {
                obstacle.removeFromParent();
                return true;
            }
            return false;
        });
    }
    ```
*   **Solution:** Hid delegation using wrapper methods.
*   **Rationale:** Adherence to the **Law of Demeter**. A class shouldn't know about the internal structure of a `Spatial` for a simple check.

---

### 10. Inappropriate Intimacy
*   **Problem Description:** The obstacle generation method interfered too deeply with `Player` object details.
*   **Before:**
    ```java
    int playerX = (int) player.getLocalTranslation().getX();
    int playerZ = (int) player.getLocalTranslation().getZ();
    randomizeCube(playerX, playerZ); 
    ```
*   **After:**
    ```java
    Vector3f playerPos = playerManager.getLocation();
    // Using an encapsulated method to retrieve position
    ```
*   **Solution:** Used the `getLocation()` method in `PlayerManager` instead of direct access to the `Spatial`.
*   **Rationale:** Refactoring allows for better encapsulation of player data.

---

## List of Tests (29 Scenarios)

### 1. Initialization and Mapping Tests (3 tests)
*   **1. `testKeyboardMappings()`**: Verify registration of "START", "Left", and "Right" commands in the jME system.
*   **2. `testInputManagerMappingsExist()`**: Validate the presence of all required keys in the mapping dictionary.
*   **3. `testListenerRegistration()`**: Confirm that the `InputManager` correctly added the event listener.

### 2. Game State Management Tests (2 tests)
*   **4. `testStartGameActionWhenGameIsNotStarted()`**: Verify `isGameStarted` transitions to `true` when Enter is pressed.
*   **5. `testStartGameActionWhenGameIsAlreadyStarted()`**: Prevent redundant calls to start logic during active gameplay.

### 3. Movement Mechanics Tests (4 tests)
*   **6. `testMoveLeftActionWhenGameIsStarted()`**: Verify model displacement to the left and camera tilt activation.
*   **7. `testMoveRightActionWhenGameIsStarted()`**: Verify model displacement to the right.
*   **8. `testMovementActionsWhenGameIsNotStarted()`**: Ignore movement commands if the game has not started.
*   **9. `testSidewaysValueCalculation()`**: Validate calculation of movement step relative to speed.

### 4. Game Session Tests (5 tests)
*   **10. `testScoreIncrementsBasedOnTimeStep()`**: Verify score increments based on time elapsed.
*   **11. `testSpeedIncrementsCorrectly()`**: Validate speed increase (acceleration) with each tick.
*   **12. `testSpeedDoesNotExceedMaxLimit()`**: Check the maximum speed limit constraint.
*   **13. `testDifficultyScalingReducesSpawnArea()`**: Decrease `spawnAreaScale` every 10 seconds.
*   **14. `testDifficultyDoesNotScaleBelowObstacleCount()`**: Check the lower boundary for difficulty scaling.

### 5. Game Lifecycle Tests (3 tests)
*   **15. `testGameRunnerStateTransitionOnCollision()`**: Stop logic upon detecting a collision.
*   **16. `testResetRestoresInitialDataState()`**: Return score and speed to initial values upon restart.
*   **17. `testStateFreezesAfterCollision()`**: Guarantee that `update` does not change data after Game Over.

### 6. Obstacle Manager Tests (4 tests)
*   **18. `testCheckCollisionsDetectsCollision()`**: Detect intersection between player physical boundaries and cubes.
*   **19. `testSpawnIfNeededSpawnsObstacles()`**: Automatically generate new objects when count is low.
*   **20. `testCleanupRemovesOffscreenObstacles()`**: Remove cubes that are left behind the player.
*   **21. `testClearRemovesAllObstacles()`**: Clear the scene when returning to the menu.

### 7. Theme System Tests (5 tests)
*   **22. `testInitialThemeStateOnReset()`**: Set default theme during initialization.
*   **23. `testThemeChangeTiming()`**: Switch to a new theme exactly after 20 seconds.
*   **24. `testCyclingThroughAllThemes()`**: Cyclical transition from the last theme back to the first.
*   **25. `testPlayerMaterialUpdatesWithTheme()`**: Change player color when the theme changes.
*   **26. `testRandomObstacleColorFromCurrentTheme()`**: Verify that new obstacles use colors from the current theme's palette.

### 8. User Interface Tests (3 tests)
*   **27. `testInitialStatusOnStartup()`**: Display "PRESS ENTER" text on startup.
*   **28. `testGameOverShowsExactMessage()`**: Verify the exact content of the game-over message.
*   **29. `testUIManagerFormatsScoreCorrectly()`**: Validate the formatting of the "Current Score: [Value]" string.