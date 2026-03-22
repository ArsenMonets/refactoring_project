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
package сom.github.arsenmonets.refactoringproject.refactored;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import сom.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import сom.github.arsenmonets.refactoringproject.refactored.core.GameSession;
import сom.github.arsenmonets.refactoringproject.refactored.input.GameInputManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.CameraManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.EnvironmentManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.ObstacleManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import сom.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

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
    private static final Geometry PROTOTYPE_OBSTACLE = new Geometry("Box", new Box(1, 1, 1));

	private GameRunner gameRunner;

    public static void main(String[] args) {
        new CubeField().start();
    }

    @Override
    public void simpleInitApp() {
        configureEngine();
        initializeComponents();
    }

    private void configureEngine() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);
        flyCam.setEnabled(false);
        setDisplayStatView(false);
    }

    private void initializeComponents() {
    	TpfTpsHandler tpfTpsHandler = new TpfTpsHandler(TICKS_PER_SECOND);
        GameSession session = new GameSession(timer, INITIAL_OBSTACLES, tpfTpsHandler);
        PlayerManager playerManager = new PlayerManager(assetManager, rootNode, session, tpfTpsHandler);
        EnvironmentManager environmentManager = new EnvironmentManager(assetManager, rootNode, playerManager);
        ThemeManager themeManager = new ThemeManager(renderer, playerManager, environmentManager, timer, THEME_CHANGE_INTERVAL);
        ObstacleManager obstacleManager = new ObstacleManager(rootNode, assetManager, playerManager, session, themeManager, PROTOTYPE_OBSTACLE);
        CameraManager cameraManager = new CameraManager(cam, playerManager, tpfTpsHandler);
        UIManager uiManager = new UIManager(assetManager, guiNode, session);
        gameRunner = new GameRunner(environmentManager, cameraManager, session, playerManager, 
        		obstacleManager, uiManager, tpfTpsHandler, themeManager);
        new GameInputManager(gameRunner, playerManager, cameraManager, uiManager, tpfTpsHandler, inputManager).init();
    }

    @Override
    public void simpleUpdate(float tpf) {
    	gameRunner.update(tpf);
    }
}