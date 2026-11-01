package se233.project2.controller;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import se233.project2.exception.GlobalExceptionHandler;
import se233.project2.model.brick.Brick;
import se233.project2.model.effect.FloatingScoreText;
import se233.project2.model.effect.VisualEffect;
import se233.project2.model.entity.Ball;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.entity.UpgradeCapsule;
import se233.project2.model.state.GameState;
import se233.project2.model.state.GameStatus;
import se233.project2.model.state.StageConfig;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;
import se233.project2.view.AssetManager;
import se233.project2.view.GameCanvas;
import se233.project2.view.GameOverView;
import se233.project2.view.HudPane;
import se233.project2.view.StartMenuView;
import se233.project2.view.UpgradeSelectionView;

import java.util.Iterator;
import java.util.List;

/**
 * Primary controller orchestrating the JavaFX {@link AnimationTimer} game loop,
 * stage transitions, lose-life/game-over checks, Slow Motion & Dual Laser abilities,
 * multithreaded background tasks, and communication between Model and View.
 */
public class GameController {

    private final GameState gameState;
    private final CollisionManager collisionManager;
    private final StageLoader stageLoader;
    private final UpgradeManager upgradeManager;
    private final InputHandler inputHandler;
    private final AsyncTaskManager asyncTaskManager;

    private AssetManager assetManager;
    private GameCanvas gameCanvas;
    private HudPane hudPane;
    private StartMenuView startMenuView;
    private UpgradeSelectionView upgradeSelectionView;
    private GameOverView gameOverView;

    private AnimationTimer gameLoop;
    private long lastFrameNanos = 0L;
    private double currentStageBaseBallSpeed = GameConfig.BALL_BASE_SPEED;

    public GameController(GameState gameState) {
        this.gameState = gameState;
        this.collisionManager = new CollisionManager();
        this.stageLoader = new StageLoader();
        this.upgradeManager = new UpgradeManager();
        this.inputHandler = new InputHandler();
        this.asyncTaskManager = new AsyncTaskManager();

        this.inputHandler.setOnKeyTriggered(this::handleSingleKeyPress);
        this.collisionManager.setSoundEventCallback(this::playSoundSafe);
    }

    public void bindViews(AssetManager assetManager,
                          GameCanvas gameCanvas,
                          HudPane hudPane,
                          StartMenuView startMenuView,
                          UpgradeSelectionView upgradeSelectionView,
                          GameOverView gameOverView) {
        this.assetManager = assetManager;
        this.gameCanvas = gameCanvas;
        this.hudPane = hudPane;
        this.startMenuView = startMenuView;
        this.upgradeSelectionView = upgradeSelectionView;
        this.gameOverView = gameOverView;

        if (this.gameCanvas != null) {
            this.gameCanvas.setAssetManager(assetManager);
        }
        if (this.hudPane != null) {
            this.hudPane.setAssetManager(assetManager);
            this.hudPane.setOnTriggerSlowMotion(this::triggerSlowMotion);
            this.hudPane.setOnTriggerDualLaser(this::triggerDualLaser);
            this.hudPane.setOnToggleMute(this::toggleSoundMute);
            this.hudPane.setOnTogglePause(this::togglePause);
            this.hudPane.setOnReturnToMenu(this::returnToMainMenu);
        }
        if (this.upgradeSelectionView != null) {
            this.upgradeSelectionView.setAssetManager(assetManager);
        }
        if (this.startMenuView != null) {
            this.startMenuView.setOnStartGame(this::startNewGame);
            this.startMenuView.setOnToggleSound(this::toggleSoundMute);
        }
        if (this.gameOverView != null) {
            this.gameOverView.setOnPlayAgain(this::startNewGame);
            this.gameOverView.setOnMainMenu(this::returnToMainMenu);
        }

        runStartupDiagnosticsTask();
    }

    private void runStartupDiagnosticsTask() {
        Task<String> warmupTask = new Task<>() {
            @Override
            protected String call() {
                int totalStages = GameConfig.TOTAL_STAGES;
                int totalBricksAcrossStages = 0;
                for (int s = 1; s <= totalStages; s++) {
                    StageConfig cfg = stageLoader.loadStageConfig(s);
                    totalBricksAcrossStages += stageLoader.instantiateBricks(cfg).size();
                }
                return "Verified " + totalStages + " stages (" + totalBricksAcrossStages + " total bricks) on background worker";
            }
        };
        warmupTask.setOnSucceeded(e -> GameLogger.logGameEvent(warmupTask.getValue()));
        asyncTaskManager.submitTask(warmupTask);
    }

    public void startGameLoop() {
        if (gameLoop != null) {
            return;
        }
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    if (lastFrameNanos == 0L) {
                        lastFrameNanos = now;
                        return;
                    }
                    double deltaTime = (now - lastFrameNanos) / 1_000_000_000.0;
                    lastFrameNanos = now;
                    // Clamp maximum frame step so window drags never cause physics tunneling
                    deltaTime = Math.min(0.045, Math.max(0.0, deltaTime));

                    updateFrame(deltaTime);
                    renderViews();
                } catch (Throwable t) {
                    GlobalExceptionHandler.handle(t);
                }
            }
        };
        lastFrameNanos = 0L;
        gameLoop.start();
    }

    public void startNewGame() {
        GameLogger.logGameEvent("=== Starting New Arkanoid Roguelike Run ===");
        inputHandler.clear();
        gameState.resetForNewRun();

        if (startMenuView != null) {
            startMenuView.setVisible(false);
        }
        if (upgradeSelectionView != null) {
            upgradeSelectionView.setVisible(false);
        }
        if (gameOverView != null) {
            gameOverView.setVisible(false);
        }

        loadStage(1);
        renderViews();
    }

    public void loadStage(int stageNumber) {
        StageConfig config = stageLoader.loadStageConfig(stageNumber);
        List<Brick> stageBricks = stageLoader.instantiateBricks(config);

        gameState.setCurrentStageNumber(config.getStageNumber());
        gameState.setCurrentStageName(config.getStageName());
        gameState.getBricks().clear();
        gameState.getBricks().addAll(stageBricks);
        gameState.getBalls().clear();
        gameState.getLasers().clear();
        gameState.getCapsules().clear();
        gameState.getVisualEffects().clear();

        if (gameState.getPaddle() != null) {
            gameState.getPaddle().resetPosition();
        }

        this.currentStageBaseBallSpeed = GameConfig.BALL_BASE_SPEED * config.getBaseBallSpeedMultiplier();
        gameState.spawnDockedPrimaryBall(currentStageBaseBallSpeed);
        gameState.setStatus(GameStatus.PLAYING);

        GameLogger.logGameEvent(String.format(
                "Loaded Stage %d [%s] with %d bricks (Base Ball Speed: %.0f)",
                stageNumber, config.getStageName(), stageBricks.size(), currentStageBaseBallSpeed));
    }

    public void returnToMainMenu() {
        inputHandler.clear();
        gameState.setStatus(GameStatus.MENU);
        if (upgradeSelectionView != null) {
            upgradeSelectionView.setVisible(false);
        }
        if (gameOverView != null) {
            gameOverView.setVisible(false);
        }
        if (startMenuView != null) {
            startMenuView.setVisible(true);
        }
        GameLogger.logPlayerAction("Returned to Start Menu");
    }

    public void togglePause() {
        if (gameState.getStatus() == GameStatus.PLAYING) {
            gameState.setStatus(GameStatus.PAUSED);
            GameLogger.logPlayerAction("Game Paused");
        } else if (gameState.getStatus() == GameStatus.PAUSED) {
            gameState.setStatus(GameStatus.PLAYING);
            GameLogger.logPlayerAction("Game Resumed");
        }
    }

    public void toggleSoundMute() {
        if (assetManager == null) {
            return;
        }
        boolean muted = assetManager.toggleMute();
        if (hudPane != null) {
            hudPane.updateMuteLabel(muted);
        }
        if (startMenuView != null) {
            startMenuView.updateSoundButton(muted);
        }
        GameLogger.logPlayerAction("Audio mute toggled: " + muted);
    }

    /**
     * Activates the custom Slow Motion feature (halves ball speed to 0.5x for 5.0 seconds).
     */
    public boolean triggerSlowMotion() {
        if (gameState.getStatus() != GameStatus.PLAYING) {
            return false;
        }
        GameLogger.logPlayerAction("Player pressed Slow Motion button");
        boolean activated = gameState.getSlowMotionAbility().activate(gameState);
        if (activated) {
            playSoundSafe("slowmo");
            if (gameState.getPaddle() != null) {
                gameState.getVisualEffects().add(new FloatingScoreText(
                        gameState.getPaddle().getCenterX(),
                        gameState.getPaddle().getY() - 16,
                        "SLOW MOTION 0.5x (5s)!",
                        "#48f0ff"
                ));
            }
        }
        return activated;
    }

    /**
     * Activates the Dual Laser Special Attack when off cooldown.
     */
    public boolean triggerDualLaser() {
        if (gameState.getStatus() != GameStatus.PLAYING) {
            return false;
        }
        GameLogger.logPlayerAction("Player pressed Dual Laser button");
        boolean activated = gameState.getDualLaserAbility().activate(gameState);
        if (activated) {
            playSoundSafe("upgrade");
            if (gameState.getPaddle() != null) {
                gameState.getVisualEffects().add(new FloatingScoreText(
                        gameState.getPaddle().getCenterX(),
                        gameState.getPaddle().getY() - 16,
                        "DUAL LASER ARMED!",
                        "#ff758f"
                ));
            }
        }
        return activated;
    }

    public void launchDockedBalls() {
        if (gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }
        boolean launchedAny = false;
        for (Ball ball : gameState.getBalls()) {
            if (ball.isDockedToPaddle()) {
                ball.launch(16.0);
                launchedAny = true;
            }
        }
        if (launchedAny) {
            GameLogger.logPlayerAction("Launched ball from paddle");
            playSoundSafe("bounce");
            // Spawn extra balls earned from MultiBallUpgrade
            int extras = gameState.getExtraStartingBalls();
            for (int i = 0; i < extras; i++) {
                double angle = ((i % 2 == 0) ? -1 : 1) * (22.0 + (i / 2) * 10.0);
                gameState.spawnExtraActiveBall(angle);
            }
        }
    }

    private void handleSingleKeyPress(KeyCode code) {
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            togglePause();
            return;
        }

        if (gameState.getStatus() == GameStatus.UPGRADE_SELECTION && upgradeSelectionView != null) {
            if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                upgradeSelectionView.selectByIndex(0);
            } else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                upgradeSelectionView.selectByIndex(1);
            } else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                upgradeSelectionView.selectByIndex(2);
            }
            return;
        }

        if (gameState.getStatus() != GameStatus.PLAYING) {
            return;
        }

        if (code == KeyCode.SPACE) {
            launchDockedBalls();
            if (gameState.getDualLaserAbility().fireLasersIfReady(gameState)) {
                playSoundSafe("laser");
            }
        } else if (code == KeyCode.SHIFT || code == KeyCode.S) {
            triggerSlowMotion();
        } else if (code == KeyCode.F) {
            triggerDualLaser();
        } else if (code == KeyCode.W || code == KeyCode.UP) {
            if (gameState.getDualLaserAbility().fireLasersIfReady(gameState)) {
                playSoundSafe("laser");
            }
        }
    }

    public void updateFrame(double deltaTime) {
        if (gameState.getStatus() != GameStatus.PLAYING || deltaTime <= 0.0) {
            return;
        }

        // Update Paddle Velocity from InputHandler
        Paddle paddle = gameState.getPaddle();
        if (paddle != null) {
            boolean left = inputHandler.isMovingLeft();
            boolean right = inputHandler.isMovingRight();
            if (left && !right) {
                paddle.moveLeft();
            } else if (right && !left) {
                paddle.moveRight();
            } else {
                paddle.stop();
            }
        }

        // Update Active Abilities (5-second Slow Motion & Dual Laser timers)
        gameState.getSlowMotionAbility().update(deltaTime, gameState);
        gameState.getDualLaserAbility().update(deltaTime, gameState);

        // Auto-fire Dual Lasers while active (or while firing key held)
        if (gameState.getDualLaserAbility().isActive()) {
            if (gameState.getDualLaserAbility().fireLasersIfReady(gameState)) {
                playSoundSafe("laser");
            }
        }

        // Sub-step movement and collision detection to prevent high-speed tunneling
        int subSteps = deltaTime > 0.014 ? 2 : 1;
        double subDt = deltaTime / subSteps;

        for (int step = 0; step < subSteps; step++) {
            if (paddle != null) {
                paddle.update(subDt);
            }
            for (Ball ball : gameState.getBalls()) {
                if (ball.isDockedToPaddle()) {
                    ball.syncWithPaddle(paddle);
                }
                ball.update(subDt);
            }
            for (LaserProjectile laser : gameState.getLasers()) {
                laser.update(subDt);
            }
            for (UpgradeCapsule capsule : gameState.getCapsules()) {
                capsule.update(subDt);
            }
            collisionManager.processCollisions(gameState);
        }

        // Update Brick Flash Timers & Visual Effects
        for (Brick brick : gameState.getBricks()) {
            if (brick.isActive()) {
                brick.update(deltaTime);
            }
        }
        for (VisualEffect fx : gameState.getVisualEffects()) {
            fx.update(deltaTime);
        }

        cleanupInactiveEntities();
        checkBallLossAndLives();
        checkStageCompletion();
    }

    private void cleanupInactiveEntities() {
        gameState.getLasers().removeIf(laser -> !laser.isActive());
        gameState.getCapsules().removeIf(capsule -> !capsule.isActive());
        gameState.getVisualEffects().removeIf(fx -> !fx.isActive() || fx.isFinished());
    }

    private void checkBallLossAndLives() {
        Iterator<Ball> it = gameState.getBalls().iterator();
        boolean lostAtLeastOneBall = false;
        while (it.hasNext()) {
            Ball ball = it.next();
            if (!ball.isDockedToPaddle() && ball.getY() > GameConfig.PLAY_BOTTOM) {
                it.remove();
                lostAtLeastOneBall = true;
            }
        }

        if (lostAtLeastOneBall && gameState.getBalls().isEmpty()) {
            gameState.loseLife();
            GameLogger.logWarning("Ball missed! Remaining lives: " + gameState.getLives());

            if (gameState.getStatus() == GameStatus.GAME_OVER) {
                playSoundSafe("game_over");
                GameLogger.logGameEvent("GAME OVER! Final Score: "
                        + gameState.getScoreManager().getCurrentScore()
                        + " at Stage " + gameState.getCurrentStageNumber());
                if (gameOverView != null) {
                    gameOverView.showResult(false,
                            gameState.getScoreManager().getCurrentScore(),
                            gameState.getCurrentStageNumber());
                }
            } else {
                playSoundSafe("lose_life");
                if (gameState.getPaddle() != null) {
                    gameState.getPaddle().resetPosition();
                }
                gameState.spawnDockedPrimaryBall(currentStageBaseBallSpeed);
            }
        }
    }

    private void checkStageCompletion() {
        if (gameState.getStatus() != GameStatus.PLAYING || !gameState.areAllBricksCleared()) {
            return;
        }

        int clearedStage = gameState.getCurrentStageNumber();
        GameLogger.logGameEvent("Stage " + clearedStage + " CLEARED!");

        if (clearedStage >= GameConfig.TOTAL_STAGES) {
            gameState.setStatus(GameStatus.VICTORY);
            playSoundSafe("stage_clear");
            GameLogger.logGameEvent("VICTORY! All stages conquered! Final Score: "
                    + gameState.getScoreManager().getCurrentScore());
            if (gameOverView != null) {
                gameOverView.showResult(true,
                        gameState.getScoreManager().getCurrentScore(),
                        clearedStage);
            }
        } else {
            gameState.setStatus(GameStatus.UPGRADE_SELECTION);
            inputHandler.clear();
            playSoundSafe("stage_clear");
            List<Upgrade> choices = upgradeManager.getRandomChoices(3);
            if (upgradeSelectionView != null) {
                upgradeSelectionView.displayChoices(choices, this::selectUpgradeAndAdvance);
            }
        }
    }

    public void selectUpgradeAndAdvance(Upgrade selectedUpgrade) {
        upgradeManager.applyUpgrade(selectedUpgrade, gameState);
        playSoundSafe("upgrade");
        int nextStage = gameState.getCurrentStageNumber() + 1;
        loadStage(nextStage);
    }

    private void renderViews() {
        if (gameCanvas != null) {
            gameCanvas.render(gameState);
        }
        if (hudPane != null) {
            hudPane.refresh(gameState);
        }
    }

    private void playSoundSafe(String soundKey) {
        if (assetManager != null) {
            assetManager.playSound(soundKey);
        }
    }

    public void shutdown() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        asyncTaskManager.shutdownAll();
    }

    public GameState getGameState() {
        return gameState;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public StageLoader getStageLoader() {
        return stageLoader;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
}
