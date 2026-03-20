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

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class GameSession {
    private static final float DIFFICULTY_UPDATE_INTERVAL = 10.0f;
    private static final float ACCELERATION_PER_TICK = .000001f;
    private static final float MAX_SPEED_LIMIT = .1f;
    private static final float SPEED_DIVIDER_CONSTANT = 400f;
    private static final int SPAWN_SCALE_REDUCTION = 5;
    private static final int INITIAL_SPAWN_SCALE = 40;

    private float currentScore;
    private int spawnAreaScale;
    private float moveSpeed;
    private float nextDifficultyUpdate;
    private final int minObstacles;

    public GameSession(int minObstacles) {
        this.minObstacles = minObstacles;
    }

    public void reset(float currentTime) {
        currentScore = 0;
        spawnAreaScale = INITIAL_SPAWN_SCALE;
        moveSpeed = minObstacles / SPEED_DIVIDER_CONSTANT;
        nextDifficultyUpdate = currentTime + DIFFICULTY_UPDATE_INTERVAL;
    }

    public void update(float tpf, float currentTime, float ticksPerSecond) {
        if (currentTime >= nextDifficultyUpdate) {
            nextDifficultyUpdate += DIFFICULTY_UPDATE_INTERVAL;
            spawnAreaScale = Math.max(minObstacles, spawnAreaScale - SPAWN_SCALE_REDUCTION);
        }

        if (moveSpeed < MAX_SPEED_LIMIT) {
            moveSpeed += ACCELERATION_PER_TICK * tpf * ticksPerSecond;
        }

        currentScore += ticksPerSecond * tpf;
    }

    public float getMoveSpeed() { return moveSpeed; }
    public int getSpawnAreaScale() { return spawnAreaScale; }
    public int getCurrentScore() { return (int) currentScore; }
}