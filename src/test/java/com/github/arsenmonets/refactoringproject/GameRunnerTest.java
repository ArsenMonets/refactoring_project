/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.arsenmonets.refactoringproject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import com.github.arsenmonets.refactoringproject.refactored.core.GameSession;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.*;
import com.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import com.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import com.github.arsenmonets.refactoringproject.refactored.ui.UIManager;
import com.jme3.system.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
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