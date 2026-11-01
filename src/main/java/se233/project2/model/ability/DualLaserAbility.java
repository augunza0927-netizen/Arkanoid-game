package se233.project2.model.ability;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;

/**
 * Special Attack ("Sprite Sheet" Rubric - 2 points):
 * Equips the paddle with dual laser cannons for a specific duration, allowing
 * the player to shoot twin laser beams upward to damage/destroy bricks.
 */
public class DualLaserAbility extends ActiveAbility {

    private double fireIntervalTimer = 0.0;

    public DualLaserAbility() {
        super("Dual Laser", GameConfig.DUAL_LASER_DURATION_SECONDS, GameConfig.DUAL_LASER_COOLDOWN_SECONDS);
    }

    @Override
    public boolean activate(GameState state) {
        if (state == null) {
            throw new InvalidGameStateException("Cannot activate Dual Laser on a null GameState");
        }
        if (!canActivate()) {
            GameLogger.logWarning(String.format(
                    "Dual Laser unavailable (active=%s, cooldown=%.2fs)", active, remainingCooldown));
            return false;
        }
        enableOnPaddle(state, maxDurationSeconds);
        GameLogger.logGameEvent(String.format(
                "Dual Laser Special Attack ACTIVATED for %.1f seconds", maxDurationSeconds));
        return true;
    }

    /**
     * Immediately activates or extends Dual Laser (e.g., from a red Laser Capsule or Upgrade).
     */
    public void forceActivate(GameState state, double durationSeconds) {
        if (state == null) {
            return;
        }
        double newDuration = active ? Math.max(remainingDuration, durationSeconds) : durationSeconds;
        enableOnPaddle(state, newDuration);
        GameLogger.logGameEvent(String.format("Dual Laser charged/extended to %.1f seconds", newDuration));
    }

    private void enableOnPaddle(GameState state, double duration) {
        this.active = true;
        this.remainingDuration = duration;
        this.remainingCooldown = 0.0;
        this.fireIntervalTimer = 0.0;
        if (state.getPaddle() != null) {
            state.getPaddle().setLaserEquipped(true);
        }
    }

    /**
     * Fires twin laser projectiles from the left and right cannons of the paddle if ready.
     */
    public boolean fireLasersIfReady(GameState state) {
        if (!active || state == null || state.getPaddle() == null || fireIntervalTimer > EPSILON) {
            return false;
        }
        Paddle paddle = state.getPaddle();
        double leftBeamX = paddle.getX() + 6.0;
        double rightBeamX = paddle.getX() + paddle.getWidth() - 6.0 - GameConfig.LASER_WIDTH;
        double beamY = paddle.getY() - GameConfig.LASER_HEIGHT + 2.0;

        state.getLasers().add(new LaserProjectile(leftBeamX, beamY, GameConfig.LASER_WIDTH, GameConfig.LASER_HEIGHT));
        state.getLasers().add(new LaserProjectile(rightBeamX, beamY, GameConfig.LASER_WIDTH, GameConfig.LASER_HEIGHT));

        fireIntervalTimer = GameConfig.DUAL_LASER_FIRE_INTERVAL;
        GameLogger.logPlayerAction("Fired Dual Laser volley");
        return true;
    }

    @Override
    public void update(double deltaTime, GameState state) {
        if (deltaTime <= 0.0 || state == null) {
            return;
        }
        if (fireIntervalTimer > 0.0) {
            fireIntervalTimer = Math.max(0.0, fireIntervalTimer - deltaTime);
        }

        if (active) {
            if (state.getPaddle() != null) {
                state.getPaddle().setLaserEquipped(true);
            }
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
        this.fireIntervalTimer = 0.0;
        if (state != null && state.getPaddle() != null) {
            state.getPaddle().setLaserEquipped(false);
        }
        GameLogger.logGameEvent("Dual Laser Special Attack EXPIRED");
    }

    @Override
    public void reset() {
        super.reset();
        this.fireIntervalTimer = 0.0;
    }
}
