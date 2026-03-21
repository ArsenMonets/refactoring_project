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

import com.jme3.renderer.Camera;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class CameraManager {
    private static final Vector3f CAM_OFFSET = new Vector3f(-8f, 2f, 0);
    private static final float SMOOTHING_FACTOR = .99f;
    private final Camera cam;
    private float currentAngle = 0;
    private final PlayerManager playerManager;

    public CameraManager(Camera cam, PlayerManager playerManager) {
        this.cam = cam;
        this.playerManager = playerManager;
    }

    public void update(float timeStep) {
        Vector3f targetLocation = playerManager.getLocation();
		cam.setLocation(targetLocation.add(CAM_OFFSET));
        cam.lookAt(targetLocation, Vector3f.UNIT_Y);
        Quaternion rot = new Quaternion().fromAngleNormalAxis(currentAngle, Vector3f.UNIT_Z);
        cam.setRotation(cam.getRotation().mult(rot));
        currentAngle *= FastMath.pow(SMOOTHING_FACTOR, timeStep);
    }

    public void addTilt(float value) {
        currentAngle += value;
    }
}
