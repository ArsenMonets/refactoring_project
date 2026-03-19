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
import com.jme3.bounding.BoundingVolume;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class CubeField extends SimpleApplication implements AnalogListener {

    public static void main(String[] args) {
        CubeField app = new CubeField();
        app.start();
    }

    private static final float THEME_CHANGE_INTERVAL = 20.0f;
    private static final float DIFFICULTY_UPDATE_INTERVAL = 10.0f;
    private static final float INITIAL_SPEED_DIVIDER = 400f;
    private static final float ACCELERATION_INCREMENT = .000001f;
    private static final float MAX_SPEED_LIMIT = .1f;
    
    private static final int BASE_SPAWN_DISTANCE_X = 30;
    private static final int MAX_SPAWN_DISTANCE_X = 90;
    private static final int SPAWN_LATERAL_RANGE_Z = 50;
    private static final int OBSTACLE_CLEANUP_THRESHOLD = 10;
    
    private static final float CAM_OFFSET_X = -8f;
    private static final float CAM_OFFSET_Y = 2f;
    private static final float CAM_SMOOTHING_FACTOR = .99f;
    
    private static final int UI_SCORE_Y_POS = 2;
    private static final int UI_START_MSG_Y_POS = 5;

    private BitmapFont defaultFont;

    private boolean isGameStarted;
    private int spawnAreaScale, currentScore, currentThemeIndex, minObstaclesLimit;
    private Node playerNode;
    private Geometry obstaclePrototype;
    private ArrayList<Geometry> activeObstacles;
    private ArrayList<ColorRGBA> themeColors;
    private float moveSpeed, nextThemeChangeTime, nextDifficultyUpdateTime;
    private float currentCamAngle = 0;
    private BitmapText fpsScoreText, pressStart;

    private boolean isWireframeMode = false;
    private Material playerMaterial;
    private Material floorMaterial;

    final private float ticksPerSecond = 1000f / 1f;

    /**
     * Initializes game 
     */
    @Override
    public void simpleInitApp() {
        Logger.getLogger("com.jme3").setLevel(Level.WARNING);

        flyCam.setEnabled(false);
        setDisplayStatView(false);

        Keys();

        defaultFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        pressStart = new BitmapText(defaultFont);
        fpsScoreText = new BitmapText(defaultFont);

        loadText(fpsScoreText, "Current Score: 0", defaultFont, 0, UI_SCORE_Y_POS, 0);
        loadText(pressStart, "PRESS ENTER", defaultFont, 0, UI_START_MSG_Y_POS, 0);
        
        playerNode = createPlayer();
        rootNode.attachChild(playerNode);
        activeObstacles = new ArrayList<Geometry>();
        themeColors = new ArrayList<ColorRGBA>();

        gameReset();
    }

    /**
     * Used to reset cubeField 
     */
    private void gameReset(){
        currentScore = 0;
        minObstaclesLimit = 10;
        currentThemeIndex = 0;
        spawnAreaScale = 40;

        for (Geometry cube : activeObstacles){
            cube.removeFromParent();
        }
        activeObstacles.clear();

        if (obstaclePrototype != null){
            obstaclePrototype.removeFromParent();
        }
        obstaclePrototype = createFirstCube();

        themeColors.clear();
        themeColors.add(ColorRGBA.Orange);
        themeColors.add(ColorRGBA.Red);
        themeColors.add(ColorRGBA.Yellow);
        renderer.setBackgroundColor(ColorRGBA.White);
        moveSpeed = minObstaclesLimit / INITIAL_SPEED_DIVIDER;
        nextThemeChangeTime = THEME_CHANGE_INTERVAL;
        nextDifficultyUpdateTime = DIFFICULTY_UPDATE_INTERVAL;
        playerNode.setLocalTranslation(0,0,0);
    }

    @Override
    public void simpleUpdate(float ticksPerFrame) {
        camTakeOver(ticksPerFrame);
        if (isGameStarted){
            gameLogic(ticksPerFrame);
        }
        colorLogic();
    }

    /**
     * Forcefully takes over Camera adding functionality and placing it behind the character
     * @param ticksPerFrame Ticks Per Frame
     */
    private void camTakeOver(float ticksPerFrame) {
        cam.setLocation(playerNode.getLocalTranslation().add(CAM_OFFSET_X, CAM_OFFSET_Y, 0));
        cam.lookAt(playerNode.getLocalTranslation(), Vector3f.UNIT_Y);
        
        Quaternion rot = new Quaternion();
        rot.fromAngleNormalAxis(currentCamAngle, Vector3f.UNIT_Z);
        cam.setRotation(cam.getRotation().mult(rot));
        currentCamAngle *= FastMath.pow(CAM_SMOOTHING_FACTOR, ticksPerSecond * ticksPerFrame);
    }

    @Override
    public void requestClose(boolean esc) {
        if (!esc){
            System.out.println("The game was quit.");
        }else{
            System.out.println("Player has Collided. Final Score is " + currentScore);
        }
        context.destroy(false);
    }

    /**
     * Randomly Places a cube on the map between 30 and 90 paces away from player
     */
    private void randomizeCube() {
        Geometry cube = obstaclePrototype.clone();
        int playerX = (int) playerNode.getLocalTranslation().getX();
        int playerZ = (int) playerNode.getLocalTranslation().getZ();
        float x = FastMath.nextRandomInt(playerX + spawnAreaScale + BASE_SPAWN_DISTANCE_X, playerX + spawnAreaScale + MAX_SPAWN_DISTANCE_X);
        float z = FastMath.nextRandomInt(playerZ - spawnAreaScale - SPAWN_LATERAL_RANGE_Z, playerZ + spawnAreaScale + SPAWN_LATERAL_RANGE_Z);
        cube.getLocalTranslation().set(x, 0, z);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        if (isWireframeMode){
            mat.getAdditionalRenderState().setWireframe(true);
        }
        mat.setColor("Color", themeColors.get(FastMath.nextRandomInt(0, themeColors.size() - 1)));
        cube.setMaterial(mat);

        rootNode.attachChild(cube);
        activeObstacles.add(cube);
    }

    private Geometry createFirstCube() {
        Vector3f loc = playerNode.getLocalTranslation();
        loc.addLocal(4, 0, 0);
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(loc);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        return geom;
    }

    private Node createPlayer() {
        Dome b = new Dome(Vector3f.ZERO, 10, 100, 1);
        Geometry playerMesh = new Geometry("Box", b);

        playerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playerMaterial.setColor("Color", ColorRGBA.Red);
        playerMesh.setMaterial(playerMaterial);
        playerMesh.setName("player");

        Box floor = new Box(100, 0, 100);
        
        Geometry floorMesh = new Geometry("Box", floor);

        Vector3f translation = Vector3f.ZERO.add(playerMesh.getLocalTranslation().getX(),
                playerMesh.getLocalTranslation().getY() - 1, 0);

        floorMesh.setLocalTranslation(translation);

        floorMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMaterial.setColor("Color", ColorRGBA.LightGray);
        floorMesh.setMaterial(floorMaterial);
        floorMesh.setName("floor");

        Node playerNode = new Node();
        playerNode.attachChild(playerMesh);
        playerNode.attachChild(floorMesh);

        return playerNode;
    }

    /**
     * If Game is Lost display Score and Reset the Game
     */
    private void gameLost(){
        isGameStarted = false;
        loadText(pressStart, "You lost! Press enter to try again.", defaultFont, 0, UI_START_MSG_Y_POS, 0);
        gameReset();
    }
    
    /**
     * Core Game Logic
     */
    private void gameLogic(float ticksPerFrame){
        updateDifficulty(ticksPerFrame);
        movePlayer(ticksPerFrame);
        updateObstacles();
        handleCollisions();
        updateScore(ticksPerFrame);
    }

    private void updateDifficulty(float ticksPerFrame) {
        if(timer.getTimeInSeconds() >= nextDifficultyUpdateTime){
            nextDifficultyUpdateTime = timer.getTimeInSeconds() + DIFFICULTY_UPDATE_INTERVAL;
            if(spawnAreaScale > minObstaclesLimit){
                spawnAreaScale -= 5;
            } else {
                spawnAreaScale = minObstaclesLimit;
            }
        }
        
        if(moveSpeed < MAX_SPEED_LIMIT){
            moveSpeed += ACCELERATION_INCREMENT * ticksPerFrame * ticksPerSecond;
        }
    }

    private void movePlayer(float ticksPerFrame) {
        playerNode.move(moveSpeed * ticksPerFrame * ticksPerSecond, 0, 0);
    }

    private void updateObstacles() {
        if (activeObstacles.size() > spawnAreaScale){
            activeObstacles.remove(0);
        } else if (activeObstacles.size() != spawnAreaScale){
            randomizeCube();
        }

        if (activeObstacles.isEmpty()){
            requestClose(false);
        }
    }

    private void handleCollisions() {
        Geometry playerModel = (Geometry) playerNode.getChild(0);
        BoundingVolume pVol = playerModel.getWorldBound();

        for (int i = 0; i < activeObstacles.size(); i++) {
            Geometry cubeModel = activeObstacles.get(i);
            BoundingVolume vVol = cubeModel.getWorldBound();

            if (pVol.intersects(vVol)) {
                gameLost();
                return;
            }

            if (cubeModel.getLocalTranslation().getX() + OBSTACLE_CLEANUP_THRESHOLD < playerNode.getLocalTranslation().getX()) {
                cubeModel.removeFromParent();
                activeObstacles.remove(i);
                i--; 
            }
        }
    }

    private void updateScore(float ticksPerFrame) {
        currentScore += ticksPerSecond * ticksPerFrame;
        fpsScoreText.setText("Current Score: " + currentScore);
    }

    /**
     * Sets up the keyboard bindings
     */
    private void Keys() {
        inputManager.addMapping("START", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "START", "Left", "Right");
    }

    @Override
    public void onAnalog(String binding, float value, float ticksPerFrame) {
        if (binding.equals("START") && !isGameStarted){
            isGameStarted = true;
            guiNode.detachChild(pressStart);
            System.out.println("START");
        }else if (isGameStarted == true && binding.equals("Left")){
            playerNode.move(0, 0, -(moveSpeed / 2f) * value * ticksPerSecond);
            currentCamAngle -= value * ticksPerFrame;
        }else if (isGameStarted == true && binding.equals("Right")){
            playerNode.move(0, 0, (moveSpeed / 2f) * value * ticksPerSecond);
            currentCamAngle += value * ticksPerFrame;
        }
    }

    /**
     * Determines the colors of the player, floor, obstacle and background
     */
    private void colorLogic() {
        if (timer.getTimeInSeconds() >= nextThemeChangeTime){
            
            currentThemeIndex++;
            nextThemeChangeTime = timer.getTimeInSeconds() + THEME_CHANGE_INTERVAL;
        

            switch (currentThemeIndex){
                case 1:
                    themeColors.clear();
                    isWireframeMode = true;
                    themeColors.add(ColorRGBA.Green);
                    renderer.setBackgroundColor(ColorRGBA.Black);
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    floorMaterial.setColor("Color", ColorRGBA.Black);
                    break;
                case 2:
                    themeColors.set(0, ColorRGBA.Black);
                    isWireframeMode = false;
                    renderer.setBackgroundColor(ColorRGBA.White);
                    playerMaterial.setColor("Color", ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.LightGray);
                    break;
                case 3:
                    themeColors.set(0, ColorRGBA.Pink);
                    break;
                case 4:
                    themeColors.set(0, ColorRGBA.Cyan);
                    themeColors.add(ColorRGBA.Magenta);
                    renderer.setBackgroundColor(ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.Gray);
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    break;
                case 5:
                    themeColors.remove(0);
                    renderer.setBackgroundColor(ColorRGBA.Pink);
                    isWireframeMode = true;
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    break;
                case 6:
                    themeColors.set(0, ColorRGBA.White);
                    isWireframeMode = false;
                    renderer.setBackgroundColor(ColorRGBA.Black);
                    playerMaterial.setColor("Color", ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.LightGray);
                    break;
                case 7:
                    themeColors.set(0, ColorRGBA.Green);
                    renderer.setBackgroundColor(ColorRGBA.Gray);
                    playerMaterial.setColor("Color", ColorRGBA.Black);
                    floorMaterial.setColor("Color", ColorRGBA.Orange);
                    break;
                case 8:
                    themeColors.set(0, ColorRGBA.Red);
                    floorMaterial.setColor("Color", ColorRGBA.Pink);
                    break;
                case 9:
                    themeColors.set(0, ColorRGBA.Orange);
                    themeColors.add(ColorRGBA.Red);
                    themeColors.add(ColorRGBA.Yellow);
                    renderer.setBackgroundColor(ColorRGBA.White);
                    playerMaterial.setColor("Color", ColorRGBA.Red);
                    floorMaterial.setColor("Color", ColorRGBA.Gray);
                    currentThemeIndex=0;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Sets up a BitmapText to be displayed
     * @param txt the Bitmap Text
     * @param text the 
     * @param font the font of the text
     * @param x    
     * @param y
     * @param z
     */
    private void loadText(BitmapText txt, String text, BitmapFont font, float x, float y, float z) {
        txt.setSize(font.getCharSet().getRenderedSize());
        txt.setLocalTranslation(txt.getLineWidth() * x, txt.getLineHeight() * y, z);
        txt.setText(text);
        guiNode.attachChild(txt);
    }
}