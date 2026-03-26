package com.github.arsenmonets.refactoringproject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jme3.system.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import сom.github.arsenmonets.refactoringproject.refactored.core.GameSession;
import сom.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.*;
import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

@ExtendWith(MockitoExtension.class)
class GameRunnerTest {

    @Mock private Timer timer;
    @Mock private TpfTpsHandler tpfTpsHandler;
    @Mock private ObstacleManager obstacleManager;
    @Mock private PlayerManager playerManager;
    @Mock private ThemeManager themeManager;
    @Mock private CameraManager cameraManager;
    @Mock private EnvironmentManager environmentManager;
    @Mock private UIManager uiManager;

    private GameSession session;
    private GameRunner gameRunner;

    private final float ACCEL = 0.01f;
    private final float MAX_SPEED = 0.5f;
    private final int SCALE_REDUCTION = 5;
    private final int INITIAL_SCALE = 40;

    @BeforeEach
    void setUp() {
        session = new GameSession(timer, tpfTpsHandler, 10.0f, ACCEL, MAX_SPEED, SCALE_REDUCTION, INITIAL_SCALE, 10);
        gameRunner = new GameRunner(environmentManager, cameraManager, session, playerManager, 
                                   obstacleManager, uiManager, tpfTpsHandler, themeManager);
    }

    @Test
    void testScoreIncrementsBasedOnTimeStep() {
        session.reset();
        when(tpfTpsHandler.getTimeStep()).thenReturn(5.0f);
        
        session.update();
        
        assertEquals(5, session.getCurrentScore());
    }

    @Test
    void testSpeedIncrementsCorrectly() {
        float initialSpeed = session.getMoveSpeed();
        when(tpfTpsHandler.getTimeStep()).thenReturn(1.0f);
        
        session.update();
        
        assertEquals(initialSpeed + ACCEL, session.getMoveSpeed(), 0.0001);
    }

    @Test
    void testSpeedDoesNotExceedMaxLimit() {
        when(tpfTpsHandler.getTimeStep()).thenReturn(100.0f);
        
        for(int i = 0; i < 10; i++) {
            session.update();
        }
        
        assertEquals(MAX_SPEED, session.getMoveSpeed(), 0.0001);
    }

    @Test
    void testDifficultyScalingReducesSpawnArea() {
        when(timer.getTimeInSeconds()).thenReturn(0.0f);
        session.startSession();
        
        int scaleBefore = session.getSpawnAreaScale();
        
        when(timer.getTimeInSeconds()).thenReturn(11.0f);
        session.update();
        
        int scaleAfter = session.getSpawnAreaScale();
        assertEquals(scaleBefore - SCALE_REDUCTION, scaleAfter);
    }

    @Test
    void testGameRunnerStateTransitionOnCollision() {
        gameRunner.startGame();
        assertTrue(gameRunner.isGameStarted());

        when(obstacleManager.checkCollisions()).thenReturn(true);
        gameRunner.update(0.016f);

        assertFalse(gameRunner.isGameStarted());
    }

    @Test
    void testResetRestoresInitialDataState() {
        when(tpfTpsHandler.getTimeStep()).thenReturn(10.0f);
        session.update();
        session.reset();
        assertEquals(10.0f, session.getCurrentScore());
        assertEquals(INITIAL_SCALE, session.getSpawnAreaScale());
        assertEquals(0.025f, session.getMoveSpeed(), 0.0001);
        gameRunner.startGame();
        assertEquals(0, session.getCurrentScore());
    }

    @Test
    void testStateFreezesAfterCollision() {
        gameRunner.startGame();
        
        when(obstacleManager.checkCollisions()).thenReturn(true);
        gameRunner.update(0.016f); 

        float speedAtDeath = session.getMoveSpeed();
        int scoreAtDeath = session.getCurrentScore();

        gameRunner.update(0.016f);

        assertFalse(gameRunner.isGameStarted());
        assertEquals(speedAtDeath, session.getMoveSpeed());
        assertEquals(scoreAtDeath, session.getCurrentScore());
    }

    @Test
    void testDifficultyDoesNotScaleBelowObstacleCount() {
        when(timer.getTimeInSeconds()).thenReturn(0.0f);
        session.startSession();

        for(int i = 1; i <= 20; i++) {
            when(timer.getTimeInSeconds()).thenReturn(i * 11.0f);
            session.update();
        }

        assertTrue(session.getSpawnAreaScale() >= 10);
    }
 
}