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
package com.github.arsenmonets.refactoringproject.refactored.input;

import com.github.arsenmonets.refactoringproject.refactored.core.GameRunner;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.CameraManager;
import com.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import com.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import com.github.arsenmonets.refactoringproject.refactored.ui.UIManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class GameInputManager implements AnalogListener {
    
    private static final String ACTION_START = "START";
    private static final String MOVE_LEFT = "Left";
    private static final String MOVE_RIGHT = "Right";

    private final InputManager inputManager;
    private final GameRunner gameRunner;
    private final PlayerManager player;
    private final CameraManager camera;
    private final UIManager ui;
	private TpfTpsHandler tpfTpshandler;

    public GameInputManager(GameRunner gameRunner, PlayerManager player, CameraManager camera, UIManager ui, TpfTpsHandler tpsTpfhandler, InputManager inputManager) {
        this.inputManager = inputManager;
        this.gameRunner = gameRunner;
        this.player = player;
        this.camera = camera;
        this.ui = ui;
        this.tpfTpshandler = tpsTpfhandler;
    }

    public void init() {
        inputManager.addMapping(ACTION_START, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, ACTION_START, MOVE_LEFT, MOVE_RIGHT);
    }

    @Override
    public void onAnalog(String name, float val, float tpf) {
        if (name.equals(ACTION_START) && !gameRunner.isGameStarted()) {
            gameRunner.startGame();
            ui.hideStatus();
        } else if (gameRunner.isGameStarted()) { 
        	tpfTpshandler.setSidewaysVaues(val, tpf);
            if (name.equals(MOVE_LEFT)) {
                player.moveLeft();
                camera.addLeftTilt();
            } else if (name.equals(MOVE_RIGHT)) {
            	player.moveRight();
                camera.addRightTilt();
            }
        }
    }
}