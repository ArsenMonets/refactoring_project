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
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.ArrayList;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class ObstacleManager {
    private static final int SPAWN_MIN_DISTANCE_X = 30;
    private static final int SPAWN_MAX_DISTANCE_X = 90;
    private static final int SPAWN_Z_SPREAD = 50;
    private static final float CLEANUP_THRESHOLD_X = 10f;
    private static final String MATERIAL_PATH = "Common/MatDefs/Misc/Unshaded.j3md";

    private final ArrayList<Geometry> activeObstacles = new ArrayList<>();
    private final Node rootNode;
    private final AssetManager assetManager;
    private Geometry prototype;

    public ObstacleManager(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
    }

    public void setPrototype(Geometry prototype) { 
        this.prototype = prototype; 
    }

    public void spawnIfNeeded(Vector3f playerPos, int currentScale, ThemeManager themeManager) {
        if (activeObstacles.size() < currentScale) {
            spawn(playerPos.x, playerPos.z, currentScale, themeManager);
        }
    }

    private void spawn(float pX, float pZ, int scale, ThemeManager themes) {
        Geometry cube = prototype.clone();
        
        int minX = (int)pX + scale + SPAWN_MIN_DISTANCE_X;
        int maxX = (int)pX + scale + SPAWN_MAX_DISTANCE_X;
        int minZ = (int)pZ - scale - SPAWN_Z_SPREAD;
        int maxZ = (int)pZ + scale + SPAWN_Z_SPREAD;

        float x = FastMath.nextRandomInt(minX, maxX);
        float z = FastMath.nextRandomInt(minZ, maxZ);
        cube.setLocalTranslation(x, 0, z);

        Material mat = new Material(assetManager, MATERIAL_PATH);
        if (themes.isCurrentThemeWireframe()) {
            mat.getAdditionalRenderState().setWireframe(true);
        }
        mat.setColor("Color", themes.getRandomObstacleColor());
        cube.setMaterial(mat);

        rootNode.attachChild(cube);
        activeObstacles.add(cube);
    }

    public boolean checkCollisions(BoundingVolume playerVolume) {
        for (Geometry obs : activeObstacles) {
            if (playerVolume.intersects(obs.getWorldBound())) {
                return true;
            }
        }
        return false;
    }

    public void cleanup(float playerX) {
        for (int i = 0; i < activeObstacles.size(); i++) {
            if (activeObstacles.get(i).getLocalTranslation().getX() + CLEANUP_THRESHOLD_X < playerX) {
                activeObstacles.get(i).removeFromParent();
                activeObstacles.remove(i--);
            }
        }
    }

    public void clear() {
        for (Geometry g : activeObstacles) {
            g.removeFromParent();
        }
        activeObstacles.clear();
    }
}