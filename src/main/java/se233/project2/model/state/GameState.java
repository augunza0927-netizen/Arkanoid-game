package se233.project2.model.state;

import se233.project2.model.ability.DualLaserAbility;
import se233.project2.model.ability.SlowMotionAbility;
import se233.project2.model.brick.Brick;
import se233.project2.model.effect.VisualEffect;
import se233.project2.model.entity.Ball;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.entity.UpgradeCapsule;
import se233.project2.util.GameConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Central aggregate model holding the entire state of the current roguelike run.
 * Demonstrates OOP composition and aggregation across entities, abilities, and score state.
 */
public class GameState {

    private GameStatus status = GameStatus.MENU;
    private int lives = GameConfig.INITIAL_LIVES;
    private int currentStageNumber = 1;
    private String currentStageName = "Stage 1";

    private Paddle paddle;
    private final List<Ball> balls = new ArrayList<>();
    private final List<Brick> bricks = new ArrayList<>();
    private final List<LaserProjectile> lasers = new ArrayList<>();
    private final List<UpgradeCapsule> capsules = new ArrayList<>();
    private final List<VisualEffect> visualEffects = new ArrayList<>();

    private final ScoreManager scoreManager = new ScoreManager();
    private final SlowMotionAbility slowMotionAbility = new SlowMotionAbility();
    private final DualLaserAbility dualLaserAbility = new DualLaserAbility();

    // Persistent roguelike run modifiers
    private boolean persistentPiercing = false;
    private int extraStartingBalls = 0;
    private final List<String> appliedUpgrades = new ArrayList<>();

    public GameState() {
        this.paddle = new Paddle(
                (GameConfig.PLAY_LEFT + GameConfig.PLAY_RIGHT - GameConfig.PADDLE_DEFAULT_WIDTH) * 0.5,
                GameConfig.PADDLE_Y,
                GameConfig.PADDLE_DEFAULT_WIDTH,
                GameConfig.PADDLE_HEIGHT
        );
    }

    public void resetForNewRun() {
        this.status = GameStatus.PLAYING;
        this.lives = GameConfig.INITIAL_LIVES;
        this.currentStageNumber = 1;
        this.currentStageName = "Stage 1";
        this.persistentPiercing = false;
        this.extraStartingBalls = 0;
        this.appliedUpgrades.clear();

        this.balls.clear();
        this.bricks.clear();
        this.lasers.clear();
        this.capsules.clear();
        this.visualEffects.clear();

        this.scoreManager.reset();
        this.slowMotionAbility.reset();
        this.dualLaserAbility.reset();

        this.paddle = new Paddle(
                (GameConfig.PLAY_LEFT + GameConfig.PLAY_RIGHT - GameConfig.PADDLE_DEFAULT_WIDTH) * 0.5,
                GameConfig.PADDLE_Y,
                GameConfig.PADDLE_DEFAULT_WIDTH,
                GameConfig.PADDLE_HEIGHT
        );
    }

    public void addLife(int count) {
        this.lives = Math.min(GameConfig.MAX_LIVES, this.lives + Math.max(0, count));
    }

    public void loseLife() {
        this.lives = Math.max(0, this.lives - 1);
        if (this.lives == 0) {
            this.status = GameStatus.GAME_OVER;
        }
    }

    public boolean areAllBricksCleared() {
        for (Brick brick : bricks) {
            if (brick.isActive() && !brick.isDestroyed()) {
                return false;
            }
        }
        return !bricks.isEmpty();
    }

    public int getRemainingBrickCount() {
        int count = 0;
        for (Brick brick : bricks) {
            if (brick.isActive() && !brick.isDestroyed()) {
                count++;
            }
        }
        return count;
    }

    public Ball spawnDockedPrimaryBall(double stageBaseSpeed) {
        Ball primary = new Ball(0, 0, GameConfig.BALL_SIZE);
        primary.setBaseSpeed(stageBaseSpeed);
        primary.setPiercing(persistentPiercing);
        primary.setSpeedMultiplier(slowMotionAbility.isActive() ? slowMotionAbility.getSpeedFactor() : 1.0);
        primary.dockOnPaddle(paddle);
        balls.add(primary);
        return primary;
    }

    public Ball spawnExtraActiveBall(double angleDegrees) {
        double spawnX = paddle != null ? paddle.getCenterX() - GameConfig.BALL_SIZE * 0.5 : 260.0;
        double spawnY = paddle != null ? paddle.getY() - GameConfig.BALL_SIZE - 2.0 : 650.0;
        double refSpeed = GameConfig.BALL_BASE_SPEED;
        if (!balls.isEmpty()) {
            Ball ref = balls.get(0);
            spawnX = ref.getX();
            spawnY = ref.getY();
            refSpeed = ref.getBaseSpeed();
        }
        Ball extra = new Ball(spawnX, spawnY, GameConfig.BALL_SIZE);
        extra.setBaseSpeed(refSpeed);
        extra.setPiercing(persistentPiercing);
        extra.setSpeedMultiplier(slowMotionAbility.isActive() ? slowMotionAbility.getSpeedFactor() : 1.0);
        extra.launch(angleDegrees);
        balls.add(extra);
        return extra;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    public int getCurrentStageNumber() {
        return currentStageNumber;
    }

    public void setCurrentStageNumber(int currentStageNumber) {
        this.currentStageNumber = currentStageNumber;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(String currentStageName) {
        this.currentStageName = currentStageName;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public void setPaddle(Paddle paddle) {
        this.paddle = paddle;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public List<LaserProjectile> getLasers() {
        return lasers;
    }

    public List<UpgradeCapsule> getCapsules() {
        return capsules;
    }

    public List<VisualEffect> getVisualEffects() {
        return visualEffects;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public SlowMotionAbility getSlowMotionAbility() {
        return slowMotionAbility;
    }

    public DualLaserAbility getDualLaserAbility() {
        return dualLaserAbility;
    }

    public boolean isPersistentPiercing() {
        return persistentPiercing;
    }

    public void setPersistentPiercing(boolean persistentPiercing) {
        this.persistentPiercing = persistentPiercing;
    }

    public int getExtraStartingBalls() {
        return extraStartingBalls;
    }

    public void setExtraStartingBalls(int extraStartingBalls) {
        this.extraStartingBalls = Math.max(0, extraStartingBalls);
    }

    public void incrementExtraStartingBalls() {
        this.extraStartingBalls++;
    }

    public List<String> getAppliedUpgrades() {
        return appliedUpgrades;
    }

    public void recordUpgrade(String upgradeTitle) {
        this.appliedUpgrades.add(upgradeTitle);
    }
}
