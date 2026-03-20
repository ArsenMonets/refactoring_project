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
package сom.github.arsenmonets.refactoringproject;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class CubeField extends SimpleApplication implements AnalogListener {

    private static final float THEME_INTERVAL = 20.0f;
    private static final float CAM_SMOOTHING = .99f;
    private static final Vector3f CAM_OFFSET = new Vector3f(-8f, 2f, 0);
    
    private static final int INITIAL_MIN_OBSTACLES = 10;
    private static final int PLAYER_MESH_INDEX = 0;
    private static final float SIDE_SPEED_FACTOR = 2.0f;
    
    private static final float UI_SCORE_X_POS = 0f;
    private static final float UI_SCORE_Y_POS = 2f;
    private static final float UI_START_X_POS = 0f;
    private static final float UI_START_Y_POS = 5f;

    private ThemeManager themeManager;
    private ObstacleManager obstacleManager;
    private PlayerManager playerManager;
    private GameSession session;
    
    private boolean isGameStarted;
    private float nextThemeChange;
    private float currentCamAngle = 0;
    
    private BitmapText scoreText, startText;
    private BitmapFont defaultFont;
    private final float ticksPerSecond = 1000f;

    public static void main(String[] args) {
        new CubeField().start();
    }

    @Override
    public void simpleInitApp() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);
        flyCam.setEnabled(false);
        setDisplayStatView(false);

        themeManager = new ThemeManager();
        themeManager.initDefaultThemes();
        obstacleManager = new ObstacleManager(rootNode, assetManager);
        playerManager = new PlayerManager(assetManager);
        session = new GameSession(INITIAL_MIN_OBSTACLES);
        
        rootNode.attachChild(playerManager.getPlayerNode());
        initUI();
        Keys();
        gameReset();
    }

    private void initUI() {
        defaultFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        scoreText = new BitmapText(defaultFont);
        startText = new BitmapText(defaultFont);
        loadText(scoreText, "Score: 0", UI_SCORE_X_POS, UI_SCORE_Y_POS);
        loadText(startText, "PRESS ENTER", UI_START_X_POS, UI_START_Y_POS);
    }

    private void gameReset() {
        session.reset(timer.getTimeInSeconds());
        obstacleManager.clear();
        obstacleManager.setPrototype(new Geometry("Box", new Box(1, 1, 1))); 
        
        themeManager.reset();
        themeManager.applyTheme(0, renderer, playerManager.getPlayerMaterial(), playerManager.getFloorMaterial());
        
        playerManager.reset();
        nextThemeChange = timer.getTimeInSeconds() + THEME_INTERVAL;
    }

    @Override
    public void simpleUpdate(float tpf) {
        updateCamera(tpf);
        if (isGameStarted) {
            runGameLoop(tpf);
        }
        updateVisuals();
    }

    private void updateCamera(float tpf) {
        cam.setLocation(playerManager.getLocation().add(CAM_OFFSET));
        cam.lookAt(playerManager.getLocation(), Vector3f.UNIT_Y);
        
        Quaternion rot = new Quaternion().fromAngleNormalAxis(currentCamAngle, Vector3f.UNIT_Z);
        cam.setRotation(cam.getRotation().mult(rot));
        currentCamAngle *= FastMath.pow(CAM_SMOOTHING, ticksPerSecond * tpf);
    }

    private void runGameLoop(float tpf) {
        session.update(tpf, timer.getTimeInSeconds(), ticksPerSecond);
        
        playerManager.move(session.getMoveSpeed() * tpf * ticksPerSecond, 0, 0);
        obstacleManager.spawnIfNeeded(playerManager.getLocation(), session.getSpawnAreaScale(), themeManager);
        obstacleManager.cleanup(playerManager.getLocation().x);
        
        Spatial playerMesh = playerManager.getPlayerNode().getChild(PLAYER_MESH_INDEX);
        if (obstacleManager.checkCollisions(playerMesh.getWorldBound())) {
            gameOver();
        }

        scoreText.setText("Current Score: " + session.getCurrentScore());
    }

    private void updateVisuals() {
        if (timer.getTimeInSeconds() >= nextThemeChange) {
            nextThemeChange += THEME_INTERVAL;
            themeManager.nextTheme(renderer, playerManager.getPlayerMaterial(), playerManager.getFloorMaterial());
        }
    }

    private void gameOver() {
        isGameStarted = false;
        startText.setText("You lost! Press enter to try again.");
        guiNode.attachChild(startText);
        gameReset();
    }

    private void Keys() {
        inputManager.addMapping("START", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "START", "Left", "Right");
    }

    @Override
    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("START") && !isGameStarted) {
            isGameStarted = true;
            guiNode.detachChild(startText);
        } else if (isGameStarted) {
            float sideSpeed = (session.getMoveSpeed() / SIDE_SPEED_FACTOR) * value * ticksPerSecond;
            if (binding.equals("Left")) {
                playerManager.move(0, 0, -sideSpeed);
                currentCamAngle -= value * tpf;
            } else if (binding.equals("Right")) {
                playerManager.move(0, 0, sideSpeed);
                currentCamAngle += value * tpf;
            }
        }
    }

    private void loadText(BitmapText txt, String text, float x, float y) {
        txt.setSize(defaultFont.getCharSet().getRenderedSize());
        txt.setLocalTranslation(txt.getLineWidth() * x, txt.getLineHeight() * y, 0);
        txt.setText(text);
        guiNode.attachChild(txt);
    }
}