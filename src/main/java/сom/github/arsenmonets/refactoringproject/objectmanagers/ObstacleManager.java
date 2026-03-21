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
package сom.github.arsenmonets.refactoringproject.objectmanagers;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import сom.github.arsenmonets.refactoringproject.core.GameSession;
import сom.github.arsenmonets.refactoringproject.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

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

    private final List<Geometry> activeObstacles = new ArrayList<>();
    private final Node rootNode;
    private final AssetManager assetManager;
    private final GameSession session;
    private final PlayerManager playerManager;
    private final ThemeManager themeManager;
    
    private final Geometry prototype;

    public ObstacleManager(Node rootNode, AssetManager assetManager, PlayerManager playerManager, 
                           GameSession session, ThemeManager themeManager, Geometry prototype) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.playerManager = playerManager;
        this.session = session;
        this.themeManager = themeManager;
		this.prototype = prototype;
    }

    public void update() {
        spawnIfNeeded();
        cleanup();
    }

    private void spawnIfNeeded() {
        if (activeObstacles.size() < session.getSpawnAreaScale()) {
            spawn();
        }
    }

    private void spawn() {
        if (prototype == null) return;

        Geometry cube = prototype.clone();
        int scale = session.getSpawnAreaScale();
        Vector3f playerPos = playerManager.getLocation();

        float minX = playerPos.x + scale + SPAWN_MIN_DISTANCE_X;
        float maxX = playerPos.x + scale + SPAWN_MAX_DISTANCE_X;
        
        float minZ = playerPos.z - scale - SPAWN_Z_SPREAD;
        float maxZ = playerPos.z + scale + SPAWN_Z_SPREAD;

        float x = FastMath.nextRandomFloat() * (maxX - minX) + minX;
        float z = FastMath.nextRandomFloat() * (maxZ - minZ) + minZ;
        
        cube.setLocalTranslation(x, 0, z);

        Material mat = new Material(assetManager, MATERIAL_PATH);
        if (themeManager.isCurrentThemeWireframe()) {
            mat.getAdditionalRenderState().setWireframe(true);
        }
        mat.setColor("Color", themeManager.getRandomObstacleColor());
        cube.setMaterial(mat);

        rootNode.attachChild(cube);
        activeObstacles.add(cube);
    }

    public boolean checkCollisions() {
        BoundingVolume playerVolume = playerManager.getCollisionBounds();
        for (Geometry obs : activeObstacles) {
            if (playerVolume.intersects(obs.getWorldBound())) {
                return true;
            }
        }
        return false;
    }

    private void cleanup() {
        float playerX = playerManager.getLocation().x;
        for (int i = 0; i < activeObstacles.size(); i++) {
            Geometry obstacle = activeObstacles.get(i);
            if (obstacle.getLocalTranslation().x + CLEANUP_THRESHOLD_X < playerX) {
                obstacle.removeFromParent();
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