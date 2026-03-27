# Звіт про рефакторинг та тестування: Проєкт "CubeField"

## Порівняльні метрики проєкту

| Метрика | Оригінальний код (Старий) | Рефакторений код (Новий) | Результат |
| :--- | :---: | :---: | :--- |
| **Кількість файлів** | 1 | 12 | Покращена модульність |
| **Загальна кількість функцій/методів** | 15 | ~54 | **+350%** (Зростання деталізації) |
| **Сер. кількість методів на клас** | 15 | ~6 | Краще дотримання SRP |
| **SLOC (Логічні рядки коду)** | ~240 | ~680 | Розширені можливості |
| **Макс. цикломатична складність** | 11 | 4 | **Зменшено на 64%** |
| **Когнітивна складність** | Висока | Дуже низька | Краща читабельність |
| **Єдина відповідальність (SRP)** | Порушено (God Object) | Дотримано (8+ менеджерів) | Легко тестувати |

---

## Список впроваджених змін

### 1. "Мертвий" код (Dead Code)
*   **Опис проблеми:** Метод `randomizeCube` містив закоментовані розрахунки координат, які більше не використовувалися, але створювали візуальний шум.
*   **До:**
    ```java
    // float x = FastMath.nextRandomInt(playerX + difficulty + 10, playerX + difficulty + 150);
    float x = FastMath.nextRandomInt(playerX + difficulty + 30, playerX + difficulty + 90);
    // playerX+difficulty+30,playerX+difficulty+90
    ```
*   **Після:**
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
*   **Рішення:** Повне видалення закоментованих фрагментів та перехід до використання динамічних параметрів з `GameSession`.
*   **Обґрунтування:** Чистий код має бути вільним від "сміття". Історія ідей зберігається в Git, а закоментовані рядки лише відволікають від активної логіки.

---

### 2. "Магічні числа" (Magic Numbers)
*   **Опис проблеми:** Використання жорстко закодованих чисел (наприклад, 400) для керування швидкістю та часом без пояснення їхнього значення.
*   **До:**
    ```java
    speed = lowCap / 400f;
    ```
*   **Після:**
    ```java
    public void reset() {
        spawnAreaScale = initialSpawnScale;
        moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;
    }
    ```
*   **Рішення:** Впровадження іменованих констант (`SPEED_DIVIDER_CONSTANT`).
*   **Обґрунтування:** Це покращує самодокументованість коду. Назва константи пояснює фізичний зміст значення.

---

### 3. Беззмістовні назви (Meaningless Names)
*   **Опис проблеми:** Змінна `difficulty` мала занадто загальну назву, яка не відображала її реальну роль (розмір зони генерації).
*   **До:**
    ```java
    private int difficulty; 
    private Geometry fcube;
    ```
*   **Після:**
    ```java
    // у класі GameSession
    private int spawnAreaScale;
    // у класі ObstacleManager
    private final Geometry prototype;
    ```
*   **Рішення:** Поле перейменовано на `spawnAreaScale`.
*   **Обґрунтування:** Назва має чітко відображати намір розробника. Proper naming зменшує час на розуміння коду.

---

### 4. Довгий метод (Long Method)
*   **Опис проблеми:** Метод `gameLogic` виконував занадто багато непов'язаних операцій одночасно (фізика, таймери, колізії, інтерфейс).
*   **До:**
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
*   **Після:**
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
*   **Рішення:** Застосовано техніку рефакторингу **Extract Method**.
*   **Обґрунтування:** Дотримання принципу єдиної відповідальності (SRP). Менші методи легше тестувати та читати.

---

### 5. Оператори Switch (Switch Statements)
*   **Опис проблеми:** Метод `colorLogic` використовував довгий `switch` для зміни візуальних тем, що ускладнювало масштабування.
*   **До:**
    ```java
    switch (colorInt) {
        case 1: // Логіка синьої теми
            break;
        case 2: // Логіка червоної теми
            break;
        // ... багато кейсів
    }
    ```
*   **Після:**
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
*   **Обґрунтування:** Це робить код масштабованим. Додавання нової теми більше не потребує редагування логіки перемикання.

---

### 6. "Божественний об'єкт" (God Object)
*   **Опис проблеми:** Клас `CubeField` (SimpleApplication) керував усіма аспектами гри одночасно.
*   **До:**
    ```java
    public class CubeField extends SimpleApplication {
        // Поля для гравця, перешкод, камер, інтерфейсу, таймерів...
        // Усі методи ініціалізації та оновлення в одному файлі.
    }
    ```
*   **Після:**
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
*   **Рішення:** **Extract Class** (Розподіл класів за сферами відповідальності).
*   **Обґрунтування:** Кожен клас тепер відповідає за власне вузьке завдання, що спрощує обслуговування.

---

### 7. Дублювання коду (Code Duplication)
*   **Опис проблеми:** Розрахунок зміщення об'єктів на основі `tpf` повторювався у кількох місцях програми.
*   **До:**
    ```java
    player.move(speed * tpf * fpsRate, 0, 0);
    Score += fpsRate * tpf;
    ```
*   **Після:**
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
*   **Рішення:** Створено централізований клас `TpfTpsHandler` для розрахунку дельти часу.
*   **Обґрунтування:** Принцип **DRY** (Don't Repeat Yourself). Зміна логіки розрахунку часу тепер відбувається в одному місці.

---

### 8. Одержимість примітивами (Primitive Obsession)
*   **Опис проблеми:** Рух гравця керувався через пряму маніпуляцію координатами.
*   **До:**
    ```java
    player.move(0, 0, (speed / 2f) * value * fpsRate);
    ```
*   **Після:**
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
*   **Рішення:** Створено високорівневі методи в `PlayerManager`.
*   **Обґрунтування:** Перехід від маніпуляції цифрами до ігрових концепцій (Рух вперед, ліворуч, праворуч).

---

### 9. Ланцюжок викликів (Message Chain)
*   **Опис проблеми:** Для отримання координат перешкод код звертався до глибоко вкладених методів об'єктів.
*   **До:**
    ```java
    for (int i = 0; i < cubeField.size(); i++){
        // інший код для перевірки перетину
        if (cubeField.get(i).getLocalTranslation().getX() + 10 < player.getLocalTranslation().getX()){
            cubeField.get(i).removeFromParent();
            cubeField.remove(cubeField.get(i));
        }
    }
    ```
*   **Після:**
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
*   **Рішення:** Приховано делегування за допомогою лямбда-виразів та високорівневої функції (`removeIf`).
*   **Обґрунтування:** Код став більш читабельним. Усунуто дублювання викликів (`get(i)`).

---

### 10. Купчастість даних (Data Clumps)
*   **Опис проблеми:** Деякі поля тісно пов'язані між собою, але не були згруповані за класами.
*   **До:**
    ```java
    // UI текст
    private BitmapFont defaultFont;
    private BitmapText fpsScoreText, pressStart;
    // пов'язане з гравцем
    private Node player;
    private Material playerMaterial;
    ```
*   **Після:**
    ```java
    // у класі UIManager
    private static final String FONT_PATH = "Interface/Fonts/Default.fnt";
    private final BitmapText scoreText;
    private final BitmapText statusText;
    // у класі PlayerManager
    private final Geometry playerMesh;
    private final Material material;
    ```
*   **Рішення:** Групування пов'язаних полів за класами.
*   **Обґрунтування:** Код став більш впорядкованим. Пов'язані речі знаходяться в одному місці, що полегшує внесення змін.

---

## Список тестів (29 сценаріїв)

### 1. Тести ініціалізації та мапінгу (3 тести)
*   **1. `testKeyboardMappings()`**: Перевірка реєстрації команд "START", "Left" та "Right" у системі jME.
*   **2. `testInputManagerMappingsExist()`**: Валідація наявності всіх необхідних клавіш у словнику мапінгу.
*   **3. `testListenerRegistration()`**: Підтвердження того, що `InputManager` правильно додав слухача подій.

### 2. Тести керування станом гри (2 тести)
*   **4. `testStartGameActionWhenGameIsNotStarted()`**: Перевірка переходу `isGameStarted` у стан `true` при натисканні Enter.
*   **5. `testStartGameActionWhenGameIsAlreadyStarted()`**: Запобігання повторним викликам логіки старту під час активної гри.

### 3. Тести механіки руху (4 тести)
*   **6. `testMoveLeftActionWhenGameIsStarted()`**: Перевірка зміщення моделі ліворуч та активації нахилу камери.
*   **7. `testMoveRightActionWhenGameIsStarted()`**: Перевірка зміщення моделі праворуч.
*   **8. `testMovementActionsWhenGameIsNotStarted()`**: Ігнорування команд руху, якщо гру ще не розпочато.
*   **9. `testSidewaysValueCalculation()`**: Валідація розрахунку кроку руху відносно швидкості.

### 4. Тести ігрової сесії (5 тестів)
*   **10. `testScoreIncrementsBasedOnTimeStep()`**: Перевірка збільшення рахунку залежно від витраченого часу.
*   **11. `testSpeedIncrementsCorrectly()`**: Валідація зростання швидкості (прискорення) з кожним тіком.
*   **12. `testSpeedDoesNotExceedMaxLimit()`**: Перевірка обмеження максимальної швидкості.
*   **13. `testDifficultyScalingReducesSpawnArea()`**: Зменшення `spawnAreaScale` кожні 10 секунд.
*   **14. `testDifficultyDoesNotScaleBelowObstacleCount()`**: Перевірка нижньої межі масштабування складності.

### 5. Тести життєвого циклу гри (3 тести)
*   **15. `testGameRunnerStateTransitionOnCollision()`**: Зупинка логіки при виявленні зіткнення.
*   **16. `testResetRestoresInitialDataState()`**: Повернення рахунку та швидкості до початкових значень при перезапуску.
*   **17. `testStateFreezesAfterCollision()`**: Гарантія того, що `update` не змінює дані після Game Over.

### 6. Тести менеджера перешкод (4 тести)
*   **18. `testCheckCollisionsDetectsCollision()`**: Виявлення перетину фізичних меж гравця та кубів.
*   **19. `testSpawnIfNeededSpawnsObstacles()`**: Автоматична генерація нових об'єктів при їхній малій кількості.
*   **20. `testCleanupRemovesOffscreenObstacles()`**: Видалення кубів, що залишилися позаду гравця.
*   **21. `testClearRemovesAllObstacles()`**: Очищення сцени при поверненні в меню.

### 7. Тести системи тем (5 тестів)
*   **22. `testInitialThemeStateOnReset()`**: Встановлення теми за замовчуванням під час ініціалізації.
*   **23. `testThemeChangeTiming()`**: Перемикання на нову тему рівно через 20 секунд.
*   **24. `testCyclingThroughAllThemes()`**: Циклічний перехід від останньої теми назад до першої.
*   **25. `testPlayerMaterialUpdatesWithTheme()`**: Зміна кольору гравця при зміні теми.
*   **26. `testRandomObstacleColorFromCurrentTheme()`**: Перевірка того, що нові перешкоди використовують кольори з палітри поточної теми.

### 8. Тести інтерфейсу користувача (3 тести)
*   **27. `testInitialStatusOnStartup()`**: Відображення тексту "PRESS ENTER" при запуску.
*   **28. `testGameOverShowsExactMessage()`**: Перевірка точного змісту повідомлення про програш.
*   **29. `testUIManagerFormatsScoreCorrectly()`**: Валідація форматування рядка "Current Score: [Значення]".