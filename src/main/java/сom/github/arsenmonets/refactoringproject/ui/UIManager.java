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
package сom.github.arsenmonets.refactoringproject.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;

/**
 * @author Original: Kyle "bonechilla" Williams
 * @author Refactoring: Arsen Monets
 */
public class UIManager {
    private static final String FONT_PATH = "Interface/Fonts/Default.fnt";
    private final BitmapText scoreText;
    private final BitmapText statusText;
    private final Node guiNode;

    public UIManager(AssetManager assetManager, Node guiNode) {
        this.guiNode = guiNode;
        BitmapFont font = assetManager.loadFont(FONT_PATH);
        
        scoreText = createText(font, 0, 2);
        statusText = createText(font, 0, 5);
        
        guiNode.attachChild(scoreText);
    }

    private BitmapText createText(BitmapFont font, float x, float y) {
        BitmapText txt = new BitmapText(font);
        txt.setSize(font.getCharSet().getRenderedSize());
        txt.setLocalTranslation(txt.getLineWidth() * x, txt.getLineHeight() * y, 0);
        return txt;
    }

    public void updateScore(int score) {
        scoreText.setText("Current Score: " + score);
    }

    public void showStatus(String text) {
        statusText.setText(text);
        if (!guiNode.hasChild(statusText)) {
            guiNode.attachChild(statusText);
        }
    }

    public void hideStatus() {
        statusText.removeFromParent();
    }
}