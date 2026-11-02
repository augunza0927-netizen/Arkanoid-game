package se233.project2.model.ability;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.entity.Ball;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;


public class SlowMotionAbility extends ActiveAbility {

    private final double speedFactor;

    public SlowMotionAbility() {
        super("Slow Motion", GameConfig.SLOW_MOTION_DURATION_SECONDS, GameConfig.SLOW_MOTION_COOLDOWN_SECONDS);
        this.speedFactor = GameConfig.SLOW_MOTION_SPEED_FACTOR;
    }

    @Override
    public boolean activate(GameState state) {
        if (state == null) {
            throw new InvalidGameStateException("Cannot activate Slow Motion on a null GameState");
        }
        if (!canActivate()) {
            GameLogger.logWarning(String.format(
                    "Slow Motion unavailable (active=%s, cooldown=%.2fs)", active, remainingCooldown));
            return false;
        }

        this.active = true;
        this.remainingDuration = maxDurationSeconds;
        applySpeedFactorToAllBalls(state, speedFactor);
        GameLogger.logGameEvent(String.format(
                "Slow Motion ACTIVATED: Ball speed halved (%.1fx) for %.1f seconds",
                speedFactor, maxDurationSeconds));
        return true;
    }


    public void grantCapsuleCharge(GameState state) {
        if (state == null) {
            return;
        }
        if (active) {
            remainingDuration = Math.min(maxDurationSeconds + 3.0, remainingDuration + 3.0);
        } else {
            refreshCooldown();
        }
    }

    @Override
    public void update(double deltaTime, GameState state) {
        if (deltaTime <= 0.0 || state == null) {
            return;
        }

        if (active) {
            applySpeedFactorToAllBalls(state, speedFactor);
            remainingDuration -= deltaTime;
            if (remainingDuration <= EPSILON) {
                deactivate(state);
                remainingCooldown = cooldownSeconds;
            }
        } else if (remainingCooldown > 0.0) {
            remainingCooldown = Math.max(0.0, remainingCooldown - deltaTime);
        }
    }

    @Override
    public void deactivate(GameState state) {
        this.active = false;
        this.remainingDuration = 0.0;
        if (state != null) {
            applySpeedFactorToAllBalls(state, 1.0);
        }
        GameLogger.logGameEvent("Slow Motion EXPIRED: Normal ball speed (1.0x) restored");
    }

    private void applySpeedFactorToAllBalls(GameState state, double factor) {
        for (Ball ball : state.getBalls()) {
            ball.setSpeedMultiplier(factor);
        }
    }

    public double getSpeedFactor() {
        return speedFactor;
    }
}
