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
package сom.github.arsenmonets.refactoringproject.core;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import сom.github.arsenmonets.refactoringproject.input.GameInputManager;
import сom.github.arsenmonets.refactoringproject.objectmanagers.CameraManager;
import сom.github.arsenmonets.refactoringproject.objectmanagers.EnvironmentManager;
import сom.github.arsenmonets.refactoringproject.objectmanagers.ObstacleManager;
import сom.github.arsenmonets.refactoringproject.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.ui.UIManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class CubeField extends SimpleApplication {

    private static final float THEME_CHANGE_INTERVAL = 20.0f;
    private static final int INITIAL_OBSTACLES = 10;
    private static final float TICKS_PER_SECOND = 1000f;

    private ThemeManager themeManager;
    private ObstacleManager obstacleManager;
    private PlayerManager playerManager;
    private EnvironmentManager environmentManager;
    private CameraManager cameraManager;
    private UIManager uiManager;
    private GameSession session;
    
    private boolean isGameStarted;
    private float nextThemeChange;

    public static void main(String[] args) {
        new CubeField().start();
    }

    @Override
    public void simpleInitApp() {
        configureEngine();
        initializeComponents();
        gameReset();
    }

    private void configureEngine() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);
        flyCam.setEnabled(false);
        setDisplayStatView(false);
    }

    private void initializeComponents() {
        session = new GameSession(timer, INITIAL_OBSTACLES);
        playerManager = new PlayerManager(assetManager, session);
        environmentManager = new EnvironmentManager(assetManager, rootNode, playerManager);
        themeManager = new ThemeManager(renderer, playerManager, environmentManager);
        obstacleManager = new ObstacleManager(rootNode, assetManager, playerManager, session, themeManager,
        		new Geometry("Box", new Box(1, 1, 1)));
        cameraManager = new CameraManager(cam, playerManager);
        uiManager = new UIManager(assetManager, guiNode, session);
        
        new GameInputManager(this, playerManager, cameraManager, uiManager, TICKS_PER_SECOND).init();
        
        rootNode.attachChild(playerManager.getSpatial());
    }

    @Override
    public void simpleUpdate(float tpf) {
        float timeStep = tpf * TICKS_PER_SECOND;    
        if (isGameStarted) {
            runGameLogic(timeStep);
        } 
        environmentManager.update();
        cameraManager.update(timeStep);
        checkThemeUpdate();
    }

    private void runGameLogic(float timeStep) {
        session.update(timeStep);    
        playerManager.moveForward(timeStep);
        obstacleManager.update();
        if (obstacleManager.checkCollisions()) {
            handleGameOver();
        }
        uiManager.update();
    }

    private void checkThemeUpdate() {
        if (timer.getTimeInSeconds() >= nextThemeChange) {
            nextThemeChange += THEME_CHANGE_INTERVAL;
            themeManager.nextTheme();
        }
    }

    private void handleGameOver() {
        isGameStarted = false;
        uiManager.showStatus("You lost! Press enter to try again.");
        gameReset();
    }

    private void gameReset() {
        session.reset(timer.getTimeInSeconds());
        obstacleManager.clear();
        themeManager.reset();
        playerManager.reset();
        uiManager.showStatus("PRESS ENTER");
        nextThemeChange = timer.getTimeInSeconds() + THEME_CHANGE_INTERVAL;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void startGame() {
        isGameStarted = true;
    }
}