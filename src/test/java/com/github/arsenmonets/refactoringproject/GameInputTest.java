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

import static org.mockito.Mockito.*;

import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import сom.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import сom.github.arsenmonets.refactoringproject.refactored.input.GameInputManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.CameraManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import сom.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
@ExtendWith(MockitoExtension.class)
class GameInputTest {

    private static final String ACTION_START = "START";
    private static final String MOVE_LEFT = "Left";
    private static final String MOVE_RIGHT = "Right";

    @Mock private InputManager inputManager;
    @Mock private GameRunner gameRunner;
    @Mock private PlayerManager playerManager;
    @Mock private CameraManager cameraManager;
    @Mock private UIManager uiManager;
    @Mock private TpfTpsHandler tpfTpsHandler;

    private GameInputManager gameInputManager;

    @BeforeEach
    void setUp() {
        gameInputManager = new GameInputManager(gameRunner, playerManager, cameraManager, uiManager, tpfTpsHandler, inputManager);
    }
    
    @Test
    void testKeyboardMappings() {
    	gameInputManager.init();
        verify(inputManager).addMapping(eq(ACTION_START), any(KeyTrigger.class));
        verify(inputManager).addMapping(eq(MOVE_LEFT), any(KeyTrigger.class));
        verify(inputManager).addMapping(eq(MOVE_RIGHT), any(KeyTrigger.class));
        verify(inputManager).addListener(any(AnalogListener.class), eq(ACTION_START), eq(MOVE_LEFT), eq(MOVE_RIGHT));
    }

    @Test
    void testStartGameActionWhenGameIsNotStarted() {
        when(gameRunner.isGameStarted()).thenReturn(false);
        float val = 1.0f;
        float tpf = 0.016f;

        gameInputManager.onAnalog(ACTION_START, val, tpf);

        verify(gameRunner).startGame();
        verify(uiManager).hideStatus();
        verify(tpfTpsHandler, never()).setSidewaysVaues(anyFloat(), anyFloat());
        verify(playerManager, never()).moveLeft();
        verify(playerManager, never()).moveRight();
        verify(cameraManager, never()).addLeftTilt();
        verify(cameraManager, never()).addRightTilt();
    }

    @Test
    void testStartGameActionWhenGameIsAlreadyStarted() {
        when(gameRunner.isGameStarted()).thenReturn(true);
        float val = 1.0f;
        float tpf = 0.016f;

        gameInputManager.onAnalog(ACTION_START, val, tpf);

        verify(gameRunner, never()).startGame(); 
        verify(uiManager, never()).hideStatus(); 
    }

    @Test
    void testMoveLeftActionWhenGameIsStarted() {
        when(gameRunner.isGameStarted()).thenReturn(true);
        float val = 1.0f;
        float tpf = 0.016f;

        gameInputManager.onAnalog(MOVE_LEFT, val, tpf);

        verify(tpfTpsHandler).setSidewaysVaues(val, tpf);
        verify(playerManager).moveLeft();
        verify(cameraManager).addLeftTilt();
        verify(playerManager, never()).moveRight();
        verify(cameraManager, never()).addRightTilt();
        verify(gameRunner, never()).startGame();
        verify(uiManager, never()).hideStatus();
        
    }

    @Test
    void testMoveRightActionWhenGameIsStarted() {
        when(gameRunner.isGameStarted()).thenReturn(true);
        float val = 1.0f;
        float tpf = 0.016f;

        gameInputManager.onAnalog(MOVE_RIGHT, val, tpf);

        verify(tpfTpsHandler).setSidewaysVaues(val, tpf);
        verify(playerManager).moveRight();
        verify(cameraManager).addRightTilt();
        verify(playerManager, never()).moveLeft();
        verify(cameraManager, never()).addLeftTilt();
        verify(gameRunner, never()).startGame();
        verify(uiManager, never()).hideStatus();
    }

    @Test
    void testMovementActionsWhenGameIsNotStarted() {
        when(gameRunner.isGameStarted()).thenReturn(false);
        float val = 1.0f;
        float tpf = 0.016f;

        gameInputManager.onAnalog(MOVE_LEFT, val, tpf);
        gameInputManager.onAnalog(MOVE_RIGHT, val, tpf);

        verify(tpfTpsHandler, never()).setSidewaysVaues(anyFloat(), anyFloat());
        verify(playerManager, never()).moveLeft();
        verify(playerManager, never()).moveRight();
        verify(cameraManager, never()).addLeftTilt();
        verify(cameraManager, never()).addRightTilt();
        verify(gameRunner, never()).startGame();
        verify(uiManager, never()).hideStatus();
    }
}

