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

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.EnvironmentManager;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import com.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
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
        themeManager.resetThemeTimer(); 

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
        themeManager.resetThemeTimer(); 
        
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
        themeManager.resetThemeTimer(); 
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
        themeManager.resetThemeTimer(); 
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