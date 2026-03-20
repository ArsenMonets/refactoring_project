/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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
package сom.github.arsenmonets.refactoringproject.themes;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.Renderer;
import com.jme3.material.Material;
import java.util.ArrayList;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class ThemeManager {
    private final ArrayList<GameTheme> themes = new ArrayList<>();
    private int currentThemeIndex = 0;

    public void initDefaultThemes() {
        themes.add(new GameTheme(ColorRGBA.White, ColorRGBA.Red, ColorRGBA.Gray, false, ColorRGBA.Orange, ColorRGBA.Red, ColorRGBA.Yellow));
        themes.add(new GameTheme(ColorRGBA.Black, ColorRGBA.White, ColorRGBA.Black, true, ColorRGBA.Green));
        themes.add(new GameTheme(ColorRGBA.White, ColorRGBA.Gray, ColorRGBA.LightGray, false, ColorRGBA.Black));
        themes.add(new GameTheme(ColorRGBA.White, ColorRGBA.Gray, ColorRGBA.LightGray, false, ColorRGBA.Pink));
        themes.add(new GameTheme(ColorRGBA.Gray, ColorRGBA.White, ColorRGBA.Gray, false, ColorRGBA.Cyan, ColorRGBA.Magenta));
        themes.add(new GameTheme(ColorRGBA.Pink, ColorRGBA.White, ColorRGBA.Gray, true, ColorRGBA.Cyan, ColorRGBA.Magenta));
        themes.add(new GameTheme(ColorRGBA.Black, ColorRGBA.Gray, ColorRGBA.LightGray, false, ColorRGBA.White));
        themes.add(new GameTheme(ColorRGBA.Gray, ColorRGBA.Black, ColorRGBA.Orange, false, ColorRGBA.Green));
        themes.add(new GameTheme(ColorRGBA.White, ColorRGBA.Red, ColorRGBA.Pink, false, ColorRGBA.Red));
    }

    public void applyTheme(int index, Renderer renderer, Material playerMat, Material floorMat) {
        GameTheme t = themes.get(index);
        renderer.setBackgroundColor(t.getBackground());
        playerMat.setColor("Color", t.getPlayer());
        floorMat.setColor("Color", t.getFloor());
    }

    public void nextTheme(Renderer renderer, Material playerMat, Material floorMat) {
        currentThemeIndex = (currentThemeIndex + 1) % themes.size();
        applyTheme(currentThemeIndex, renderer, playerMat, floorMat);
    }

    public ColorRGBA getRandomObstacleColor() {
        ColorRGBA[] obs = themes.get(currentThemeIndex).getObstacles();
        return obs[FastMath.nextRandomInt(0, obs.length - 1)];
    }

    public boolean isCurrentThemeWireframe() {
        return themes.get(currentThemeIndex).isWireframe();
    }

    public void reset() { currentThemeIndex = 0; }
}
