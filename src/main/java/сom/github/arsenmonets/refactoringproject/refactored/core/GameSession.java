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
package сom.github.arsenmonets.refactoringproject.refactored.core;

import com.jme3.system.Timer;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class GameSession {
    private static final float SPEED_DIVIDER_CONSTANT = 400f;
    
    private final Timer timer;
    private final TpfTpsHandler tpfTpsHandler;

    private final float difficultyInterval;
    private final float acceleration;
    private final float maxSpeed;
    private final int spawnReduction;
    private final int initialSpawnScale;
    private final int minObstacles;

    private float currentScore = 0;
    private int spawnAreaScale;
    private float moveSpeed;
    private float nextDifficultyUpdate;

    public GameSession(Timer timer, TpfTpsHandler tpfTpsHandler, 
                       float difficultyInterval, float accelerationPerTick, float maxSpeed,
                       int spawnReduction, int initialSpawnScale, int minObstacles) {
        this.timer = timer;
        this.tpfTpsHandler = tpfTpsHandler;
        this.difficultyInterval = difficultyInterval;
        this.acceleration = accelerationPerTick;
        this.maxSpeed = maxSpeed;
        this.spawnReduction = spawnReduction;
        this.initialSpawnScale = initialSpawnScale;
        this.minObstacles = minObstacles;
        reset(); 
    }

    public void reset() {
        spawnAreaScale = initialSpawnScale;
        moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;
    }
    
    public void startSession() {
    	currentScore = 0;
        nextDifficultyUpdate = timer.getTimeInSeconds() + difficultyInterval;
    }

    public void update() {
        if (timer.getTimeInSeconds() >= nextDifficultyUpdate) {
            nextDifficultyUpdate += difficultyInterval;
            spawnAreaScale = Math.max(minObstacles, spawnAreaScale - spawnReduction);
        }
        
        if (moveSpeed < maxSpeed) {
            moveSpeed = Math.min(maxSpeed, moveSpeed + acceleration * tpfTpsHandler.getTimeStep());
        }
        
        currentScore += tpfTpsHandler.getTimeStep();
    }

    public float getMoveSpeed() { return moveSpeed; }
    public int getSpawnAreaScale() { return spawnAreaScale; }
    public int getCurrentScore() { return (int) currentScore; }
}