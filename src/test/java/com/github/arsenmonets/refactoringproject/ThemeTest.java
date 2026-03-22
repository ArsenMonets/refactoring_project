package com.github.arsenmonets.refactoringproject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Renderer;
import com.jme3.system.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.EnvironmentManager;

@ExtendWith(MockitoExtension.class)
class ThemeTest {

    @Mock private Renderer renderer;
    @Mock private PlayerManager playerManager;
    @Mock private EnvironmentManager environmentManager;
    @Mock private Timer timer;
    @Mock private Material floorMaterial;
    @Mock private Material playerMaterial;

    private ThemeManager themeManager;
    private final float INTERVAL = 20.0f;

    @BeforeEach
    void setUp() {
        when(environmentManager.getFloorMaterial()).thenReturn(floorMaterial);
        when(playerManager.getMaterial()).thenReturn(playerMaterial);
        
        themeManager = new ThemeManager(renderer, playerManager, environmentManager, timer, INTERVAL);
    }

    @Test
    void testInitialThemeStateOnReset() {
        themeManager.reset();
        
        ArgumentCaptor<ColorRGBA> colorCaptor = ArgumentCaptor.forClass(ColorRGBA.class);
        verify(renderer, atLeastOnce()).setBackgroundColor(colorCaptor.capture());
        assertEquals(ColorRGBA.White, colorCaptor.getValue());

        verify(floorMaterial, atLeastOnce()).setColor(eq("Color"), eq(ColorRGBA.Gray));
    }

    @Test
    void testThemeChangeTimingAfterStartLoop() {
    	themeManager.reset();
        when(timer.getTimeInSeconds()).thenReturn(10.0f);
        themeManager.startThemeChangingLoop(); 

        when(timer.getTimeInSeconds()).thenReturn(25.0f);
        themeManager.checkThemeUpdate();
        
        verify(renderer, times(1)).setBackgroundColor(any(ColorRGBA.class));

        when(timer.getTimeInSeconds()).thenReturn(31.0f);
        themeManager.checkThemeUpdate();

        ArgumentCaptor<ColorRGBA> colorCaptor = ArgumentCaptor.forClass(ColorRGBA.class);
        verify(renderer, times(2)).setBackgroundColor(colorCaptor.capture());
        
        assertEquals(ColorRGBA.Black, colorCaptor.getValue());
    }

    @Test
    void testCyclingThroughAllThemes() {
        themeManager.reset();
        when(timer.getTimeInSeconds()).thenReturn(0.0f);
        themeManager.startThemeChangingLoop();
        
        int totalThemes = 9; 

        for (int i = 1; i <= totalThemes; i++) {
            when(timer.getTimeInSeconds()).thenReturn(i * (INTERVAL + 1));
            themeManager.checkThemeUpdate();
        }
        ArgumentCaptor<ColorRGBA> colorCaptor = ArgumentCaptor.forClass(ColorRGBA.class);
        verify(renderer, atLeastOnce()).setBackgroundColor(colorCaptor.capture());
        assertEquals(ColorRGBA.White, colorCaptor.getValue());
    }

    @Test
    void testResetAlwaysReturnsToIndexZero() {
    	themeManager.reset();
        when(timer.getTimeInSeconds()).thenReturn(0.0f);
        themeManager.startThemeChangingLoop();
        when(timer.getTimeInSeconds()).thenReturn(21.0f);
        themeManager.checkThemeUpdate();

        themeManager.reset();

        ArgumentCaptor<ColorRGBA> colorCaptor = ArgumentCaptor.forClass(ColorRGBA.class);
        verify(renderer, atLeastOnce()).setBackgroundColor(colorCaptor.capture());
        assertEquals(ColorRGBA.White, colorCaptor.getValue());
        assertFalse(themeManager.isCurrentThemeWireframe());
    }

    @Test
    void testPlayerMaterialUpdatesWithTheme() {
        themeManager.reset(); 
        verify(playerMaterial).setColor(eq("Color"), eq(ColorRGBA.Red));

        when(timer.getTimeInSeconds()).thenReturn(0.0f);
        themeManager.startThemeChangingLoop();
        when(timer.getTimeInSeconds()).thenReturn(21.0f);
        themeManager.checkThemeUpdate(); 

        verify(playerMaterial).setColor(eq("Color"), eq(ColorRGBA.White));
    }

    @Test
    void testRandomObstacleColorFromCurrentTheme() {
        themeManager.reset(); 
        ColorRGBA color = themeManager.getRandomObstacleColor();
        
        assertTrue(color.equals(ColorRGBA.Orange) || 
                   color.equals(ColorRGBA.Red) || 
                   color.equals(ColorRGBA.Yellow));
    }
}