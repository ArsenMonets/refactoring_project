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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import сom.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import сom.github.arsenmonets.refactoringproject.refactored.core.GameSession;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.*;
import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import сom.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

import java.lang.reflect.Field;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
@ExtendWith(MockitoExtension.class)
class UITest {

    @Mock private EnvironmentManager environmentManager;
    @Mock private CameraManager cameraManager;
    @Mock private GameSession session;
    @Mock private PlayerManager playerManager;
    @Mock private ObstacleManager obstacleManager;
    @Mock private UIManager uiManager;
    @Mock private ThemeManager themeManager;
    @Mock private TpfTpsHandler tpfTpsHandler;
    
    @Mock private AssetManager assetManager;
    @Mock private Node guiNode;
    @Mock private BitmapFont mockFont;

    private GameRunner gameRunner;
    private UIManager realUiManager;

    @BeforeEach
    void setUp() {
        gameRunner = new GameRunner(
                environmentManager, cameraManager, session, playerManager,
                obstacleManager, uiManager, tpfTpsHandler, themeManager
        );
        lenient().when(assetManager.loadFont(anyString())).thenReturn(mockFont);
        lenient().when(mockFont.getCharSet()).thenReturn(mock(BitmapCharacterSet.class));
        realUiManager = new UIManager(assetManager, guiNode, session);
    }

    @Test
    void testInitialStatusOnStartup() {
    	ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
    	verify(uiManager, atLeastOnce()).showStatus(statusCaptor.capture());
    	assertEquals("PRESS ENTER", statusCaptor.getValue());
    }

    @Test
    void testGameOverShowsExactMessage() {
        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        
        gameRunner.startGame();
        when(obstacleManager.checkCollisions()).thenReturn(true);
        
        gameRunner.update(0.016f);

        verify(uiManager, atLeastOnce()).showStatus(statusCaptor.capture());
        assertEquals("You lost! Press enter to try again.", statusCaptor.getValue());
    }

    @Test
    void testUIManagerFormatsScoreCorrectly() {
        int expectedScore = 1250;
        when(session.getCurrentScore()).thenReturn(expectedScore);

        realUiManager.update();

        try {
            Field scoreField = UIManager.class.getDeclaredField("scoreText");
            scoreField.setAccessible(true);
            BitmapText scoreText = (BitmapText) scoreField.get(realUiManager);
            
            assertEquals("Current Score: 1250", scoreText.getText());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
    }
}
