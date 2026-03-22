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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Dome;

import сom.github.arsenmonets.refactoringproject.core.GameSession;
import сom.github.arsenmonets.refactoringproject.tpftps.TpfTpsHandler;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class PlayerManager {
    private final Geometry playerMesh;
    private final Material material;
    private final GameSession session;
    private final TpfTpsHandler tpfTpsHandler;

    public PlayerManager(AssetManager assetManager, Node rootNode, GameSession session, TpfTpsHandler tpfTpsHandler) {
        Dome dome = new Dome(Vector3f.ZERO, 10, 100, 1);
        playerMesh = new Geometry("Player", dome);
        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		this.session = session;
        material.setColor("Color", ColorRGBA.Red);
        playerMesh.setMaterial(material);
        rootNode.attachChild(playerMesh);
        this.tpfTpsHandler = tpfTpsHandler;
    }

    public Material getMaterial() { return material; }
    public Vector3f getLocation() { return playerMesh.getLocalTranslation(); }
    
    public BoundingVolume getCollisionBounds() {
        return playerMesh.getWorldBound();
    }

    public void reset() {
        playerMesh.setLocalTranslation(Vector3f.ZERO);
    }

    public void moveForward() {
    	playerMesh.move(tpfTpsHandler.getTimeStep() * session.getMoveSpeed(), 0, 0);
    }

    public void moveLeft() { 
        playerMesh.move(0, 0, -tpfTpsHandler.getSidewaysMoveVal() * session.getMoveSpeed()); 
    }
    
    public void moveRight() {
        playerMesh.move(0, 0, tpfTpsHandler.getSidewaysMoveVal() * session.getMoveSpeed());
    }
}