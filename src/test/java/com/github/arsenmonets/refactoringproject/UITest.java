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
