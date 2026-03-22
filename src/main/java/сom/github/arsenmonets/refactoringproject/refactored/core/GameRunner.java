package сom.github.arsenmonets.refactoringproject.refactored.core;

import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.CameraManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.EnvironmentManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.ObstacleManager;
import сom.github.arsenmonets.refactoringproject.refactored.objectmanagers.PlayerManager;
import сom.github.arsenmonets.refactoringproject.refactored.themes.ThemeManager;
import сom.github.arsenmonets.refactoringproject.refactored.tpftps.TpfTpsHandler;
import сom.github.arsenmonets.refactoringproject.refactored.ui.UIManager;

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
        uiManager.showStatus("PRESS ENTER");
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void startGame() {
        isGameStarted = true;
        themeManager.startThemeChangingLoop();
        session.startDifficultyChangeLoop();
    }
}
