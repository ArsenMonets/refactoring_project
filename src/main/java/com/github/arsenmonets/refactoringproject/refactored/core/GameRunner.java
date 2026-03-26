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
package com.github.arsenmonets.refactoringproject.refactored.core;

import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.CameraManager;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.EnvironmentManager;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.ObstacleManager;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import com.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import com.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import com.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class GameRunner {
	private final EnvironmentManager environmentManager;
	private final CameraManager cameraManager;
	private final GameSession session;
	private final PlayerManager playerManager;
	private final ObstacleManager obstacleManager;
	private final UIManager uiManager;
	private final ThemeManager themeManager;
	private final TpfTpsHandler tpfTpsHandler;
	private boolean isGameStarted = false;
	
	public GameRunner(EnvironmentManager environmentManager, CameraManager cameraManager, GameSession session,
			PlayerManager playerManager, ObstacleManager obstacleManager, UIManager uiManager,
			TpfTpsHandler tpfTpsHandler, ThemeManager themeManager) {
		super();
		this.environmentManager = environmentManager;
		this.cameraManager = cameraManager;
		this.session = session;
		this.playerManager = playerManager;
		this.obstacleManager = obstacleManager;
		this.uiManager = uiManager;
		this.themeManager = themeManager;
		this.tpfTpsHandler = tpfTpsHandler;
		uiManager.showStatus("PRESS ENTER");
		gameReset();
	}

	public void update(float tpf) {
		tpfTpsHandler.updateTimeStep(tpf); 
        if (isGameStarted) {
            runGameLogic();
        } 
        environmentManager.update();
        cameraManager.update();
	}
	
	private void runGameLogic() {
        session.update();    
        playerManager.moveForward();
        obstacleManager.update();
        if (obstacleManager.checkCollisions()) {
            handleGameOver();
        }
        uiManager.update();
        themeManager.checkThemeUpdate();
    }

    private void handleGameOver() {
        isGameStarted = false;
        uiManager.showStatus("You lost! Press enter to try again.");
        gameReset();
    }

    private void gameReset() {
        session.reset();
        obstacleManager.clear();
        themeManager.reset();
        playerManager.reset();
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void startGame() {
        isGameStarted = true;
        themeManager.resetThemeTimer();
        session.startSession();
    }
}
