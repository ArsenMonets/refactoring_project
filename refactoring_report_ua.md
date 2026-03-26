---

## Перелік внесених змін

### 1. Мертвий код (Dead Code)
*   **Опис проблеми:** У методі `randomizeCube` містяться закоментовані розрахунки координат, які не використовуються, але створюють візуальний шум.
*   **Вигляд до:**
    ```java
    // float x = FastMath.nextRandomInt(playerX + difficulty + 10, playerX + difficulty + 150);
    float x = FastMath.nextRandomInt(playerX + difficulty + 30, playerX + difficulty + 90);
    // playerX+difficulty+30,playerX+difficulty+90
    ```
*   **Вигляд після:**
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
*   **Рішення:** Повне видалення закоментованих фрагментів та перехід до використання динамічних параметрів `GameSession`.
*   **Обґрунтування:** Чистий код має бути позбавлений "сміття". Історія ідей зберігається в Git, а закоментовані рядки лише відволікають від актуальної логіки.

---

### 2. Магічні числа (Magic Numbers)
*   **Опис проблеми:** Використання жорстко закодованих чисел (400) для керування швидкістю та часом без пояснення їхнього змісту.
*   **Вигляд до:**
    ```java
    speed = lowCap / 400f;
    ```
*   **Вигляд після:**
    ```java
    public void reset() {
        spawnAreaScale = initialSpawnScale;
        moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;
    }
    ```
*   **Рішення:** Впровадження іменованих констант (`SPEED_DIVIDER_CONSTANT`).
*   **Обґрунтування:** Це покращує самодокументованість коду. Назва константи пояснює фізичний зміст числа.

---

### 3. Неправильні назви (Meaningless Names)
*   **Опис проблеми:** Змінна `difficulty` має занадто загальну назву, яка не передає її реальну роль (розмір зони генерації).
*   **Вигляд до:**
    ```java
    private int difficulty; 
    ```
*   **Вигляд після:**
    ```java
    private int spawnAreaScale;
    ```
*   **Рішення:** Перейменування поля на `spawnAreaScale`.
*   **Обґрунтування:** Назва має чітко відображати намір розробника. Правильне іменування скорочує час на розуміння коду.

---

### 4. Довгий метод (Long Method)
*   **Опис проблеми:** Метод `gameLogic` виконує занадто багато непов'язаних операцій одночасно (фізика, таймери, колізії, UI).
*   **Вигляд до:**
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
        // ... (логіка колізій та видалення кубів)
        Score += fpsRate * tpf;
        fpsScoreText.setText("Current Score: "+Score);
    }
    ```
*   **Вигляд після:**
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
*   **Рішення:** Застосування рефакторингу **Extract Method** (Вилучення методів).
*   **Обґрунтування:** Дотримання принципу єдиної відповідальності (SRP). Менші методи легше тестувати та читати.

---

### 5. Величезний Switch (Switch Statements)
*   **Опис проблеми:** Метод `colorLogic` використовує довгий `switch` для зміни візуальних тем, що ускладнює масштабування.
*   **Вигляд до:**
    ```java
    switch (colorInt) {
        case 1: // Blue theme logic
            break;
        case 2: // Red theme logic
            break;
        // ... багато кейсів
    }
    ```
*   **Вигляд після:**
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
*   **Рішення:** Заміна умовного оператора колекцією об'єктів `GameTheme`.
*   **Обґрунтування:** Це робить код масштабованим. Додавання нової теми тепер не потребує редагування логіки перемикання.

---

### 6. Великий клас (God Object)
*   **Опис проблеми:** Клас `CubeField` (SimpleApplication) керує всіма аспектами гри одночасно.
*   **Вигляд до:**
    ```java
    public class CubeField extends SimpleApplication {
        // Поля для гравця, перешкод, камер, UI, таймерів...
        // Усі методи ініціалізації та оновлення в одному файлі.
    }
    ```
*   **Вигляд після:**
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
*   **Рішення:** **Extract Class** (Вилучення класів за сферами відповідальності).
*   **Обґрунтування:** Кожен клас тепер займається своєю вузькою задачею, що полегшує підтримку.

---

### 7. Дублювання коду (Code Duplication)
*   **Опис проблеми:** Розрахунок зміщення об'єктів на основі `tpf` повторюється в декількох місцях програми.
*   **Вигляд до:**
    ```java
    player.move(speed * tpf * fpsRate, 0, 0);
    Score += fpsRate * tpf;
    ```
*   **Вигляд після:**
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
        // ... методи для розрахунку нахилу та руху
    }
    ```
*   **Рішення:** Створення централізованого класу `TpfTpsHandler` для розрахунку дельти часу.
*   **Обґрунтування:** Принцип **DRY**. Зміна логіки розрахунку часу тепер відбувається в одному місці.

---

### 8. Одержимість примітивами (Primitive Obsession)
*   **Опис проблеми:** Керування рухом гравця відбувається через прямі маніпуляції координатами.
*   **Вигляд до:**
    ```java
    player.move(0, 0, (speed / 2f) * value * fpsRate);
    ```
*   **Вигляд після:**
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
*   **Рішення:** Створення високорівневих методів в `PlayerManager`.
*   **Обґрунтування:** Перехід від маніпуляції цифрами до ігрових понять (рух вперед, вліво, вправо).

---

### 9. Ланцюжок викликів (Message Chain)
*   **Опис проблеми:** Для отримання координат перешкоди код звертається до глибоко вкладених методів об'єкта.
*   **Вигляд до:**
    ```java
    if (cubeField.get(i).getLocalTranslation().getX() + 10 < player.getLocalTranslation().getX()) { ... }
    ```
*   **Вигляд після:**
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
*   **Рішення:** Приховування делегування за допомогою методів-оберток.
*   **Обґрунтування:** Дотримання **Закону Деметри**. Клас не повинен знати про внутрішню структуру `Spatial` для простої перевірки.

---

### 10. Надмірна близькість (Inappropriate Intimacy)
*   **Опис проблеми:** Метод генерації перешкод занадто глибоко втручається в деталі об'єкта `Player`.
*   **Вигляд до:**
    ```java
    int playerX = (int) player.getLocalTranslation().getX();
    int playerZ = (int) player.getLocalTranslation().getZ();
    randomizeCube(playerX, playerZ); 
    ```
*   **Вигляд після:**
    ```java
    Vector3f playerPos = playerManager.getLocation();
    // Використання інкапсульованого методу отримання позиції
    ```
*   **Рішення:** Використання методу `getLocation()` в `PlayerManager` замість прямого доступу до `Spatial`.
*   **Обґрунтування:** Рефакторинг дозволяє краще інкапсулювати дані гравця.

---

## Перелік тестів (29 сценаріїв)

### 1. Тести ініціалізації та мапінгу (3 тести)
*   **1. `testKeyboardMappings()`**: Перевірка реєстрації команд "START", "Left", "Right" в системі jME.
*   **2. `testInputManagerMappingsExist()`**: Валідація наявності всіх необхідних клавіш у словнику мапінгу.
*   **3. `testListenerRegistration()`**: Підтвердження, що `InputManager` коректно додав слухача подій.

### 2. Тести управління станом гри (2 тести)
*   **4. `testStartGameActionWhenGameIsNotStarted()`**: Перевірка переходу стану `isGameStarted` у `true` при натисканні Enter.
*   **5. `testStartGameActionWhenGameIsAlreadyStarted()`**: Запобігання повторному виклику логіки старту під час ігрового процесу.

### 3. Тести ігрової механіки руху (4 тести)
*   **6. `testMoveLeftActionWhenGameIsStarted()`**: Перевірка зміщення моделі вліво та активації нахилу камери.
*   **7. `testMoveRightActionWhenGameIsStarted()`**: Перевірка зміщення моделі вправо.
*   **8. `testMovementActionsWhenGameIsNotStarted()`**: Ігнорування команд руху, якщо гра ще не почалася.
*   **9. `testSidewaysValueCalculation()`**: Розрахунок коректності кроку зміщення відносно швидкості.

### 4. Тести ігрової сесії (GameSession) (5 тестів)
*   **10. `testScoreIncrementsBasedOnTimeStep()`**: Перевірка нарахування очок залежно від часу.
*   **11. `testSpeedIncrementsCorrectly()`**: Валідація зростання швидкості (акселерації) з кожним тіком.
*   **12. `testSpeedDoesNotExceedMaxLimit()`**: Перевірка обмеження максимальної швидкості.
*   **13. `testDifficultyScalingReducesSpawnArea()`**: Зменшення `spawnAreaScale` кожні 10 секунд.
*   **14. `testDifficultyDoesNotScaleBelowObstacleCount()`**: Перевірка нижньої межі складності.

### 5. Тести життєвого циклу гри (GameRunner) (3 тести)
*   **15. `testGameRunnerStateTransitionOnCollision()`**: Зупинка логіки при виявленні зіткнення.
*   **16. `testResetRestoresInitialDataState()`**: Повернення очок та швидкості до початкових значень при рестарті.
*   **17. `testStateFreezesAfterCollision()`**: Гарантія того, що `update` не змінює дані після Game Over.

### 6. Тести менеджера перешкод (ObstacleManager) (4 тести)
*   **18. `testCheckCollisionsDetectsCollision()`**: Виявлення перетину фізичних меж гравця та куба.
*   **19. `testSpawnIfNeededSpawnsObstacles()`**: Автоматична генерація нових об'єктів при нестачі.
*   **20. `testCleanupRemovesOffscreenObstacles()`**: Видалення кубів, що залишилися за спиною гравця.
*   **21. `testClearRemovesAllObstacles()`**: Очищення сцени при переході в меню.

### 7. Тести системи візуальних тем (ThemeManager) (5 тестів)
*   **22. `testInitialThemeStateOnReset()`**: Встановлення стандартної теми при ініціалізації.
*   **23. `testThemeChangeTiming()`**: Перемикання на нову тему рівно через 20 секунд.
*   **24. `testCyclingThroughAllThemes()`**: Циклічний перехід від останньої теми до першої.
*   **25. `testPlayerMaterialUpdatesWithTheme()`**: Зміна кольору гравця при зміні теми.
*   **26. `testRandomObstacleColorFromCurrentTheme()`**: Перевірка, що колір нових перешкод належить до палітри поточної теми.

### 8. Тести користувацького інтерфейсу (UIManager) (3 тести)
*   **27. `testInitialStatusOnStartup()`**: Відображення тексту "PRESS ENTER" при запуску.
*   **28. `testGameOverShowsExactMessage()`**: Перевірка тексту повідомлення про програш.
*   **29. `testUIManagerFormatsScoreCorrectly()`**: Валідація форматування рядка "Current Score: [Value]".