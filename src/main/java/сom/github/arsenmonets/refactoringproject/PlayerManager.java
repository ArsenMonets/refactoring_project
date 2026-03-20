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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class PlayerManager {
    
    private static final int DOME_PLANES = 10;
    private static final int DOME_RADIAL_SAMPLES = 100;
    private static final float DOME_RADIUS = 1f;

    private static final float FLOOR_X_EXTENT = 100f;
    private static final float FLOOR_Y_EXTENT = 0f;
    private static final float FLOOR_Z_EXTENT = 100f;
    private static final float FLOOR_Y_OFFSET = -1f;
    
    private static final String UN_SHADED_MAT = "Common/MatDefs/Misc/Unshaded.j3md";
    private static final String PLAYER_NAME = "player";
    private static final String FLOOR_NAME = "floor";

    private final Node playerNode;
    private final Material playerMaterial;
    private final Material floorMaterial;

    public PlayerManager(AssetManager assetManager) {
        Dome domeShape = new Dome(Vector3f.ZERO, DOME_PLANES, DOME_RADIAL_SAMPLES, DOME_RADIUS);
        Geometry playerMesh = new Geometry("PlayerMesh", domeShape);
        
        playerMaterial = new Material(assetManager, UN_SHADED_MAT);
        playerMaterial.setColor("Color", ColorRGBA.Red);
        playerMesh.setMaterial(playerMaterial);
        playerMesh.setName(PLAYER_NAME);

        Box floorShape = new Box(FLOOR_X_EXTENT, FLOOR_Y_EXTENT, FLOOR_Z_EXTENT);
        Geometry floorMesh = new Geometry("FloorMesh", floorShape);

        Vector3f translation = new Vector3f(0, FLOOR_Y_OFFSET, 0);
        floorMesh.setLocalTranslation(translation);

        floorMaterial = new Material(assetManager, UN_SHADED_MAT);
        floorMaterial.setColor("Color", ColorRGBA.LightGray);
        floorMesh.setMaterial(floorMaterial);
        floorMesh.setName(FLOOR_NAME);

        playerNode = new Node("PlayerNode");
        playerNode.attachChild(playerMesh);
        playerNode.attachChild(floorMesh);
    }

    public Node getPlayerNode() { return playerNode; }
    public Material getPlayerMaterial() { return playerMaterial; }
    public Material getFloorMaterial() { return floorMaterial; }
    public Vector3f getLocation() { return playerNode.getLocalTranslation(); }
    
    public void reset() {
        playerNode.setLocalTranslation(Vector3f.ZERO);
    }

    public void move(float x, float y, float z) {
        playerNode.move(x, y, z);
    }
}