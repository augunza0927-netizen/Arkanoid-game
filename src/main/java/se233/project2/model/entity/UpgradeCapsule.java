package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;
import se233.project2.util.GameConfig;

/**
 * Falling collectible capsule dropped from destroyed bricks during gameplay
 * (e.g., temporary Dual Laser activation, Slow Motion refresh, multi-ball, extra life).
 */
public class UpgradeCapsule extends GameObject implements Movable, Collidable {

    public enum CapsuleKind {
        EXTRA_LIFE(0, "+1 LIFE"),
        EXPAND_PADDLE(1, "WIDE PADDLE"),
        MULTI_BALL(2, "MULTI-BALL"),
        DUAL_LASER(3, "DUAL LASER!"),
        SLOW_MO_CHARGE(4, "SLOW-MO READY!"),
        BONUS_SCORE(5, "+500 BONUS");

        private final int spriteIndex;
        private final String label;

        CapsuleKind(int spriteIndex, String label) {
            this.spriteIndex = spriteIndex;
            this.label = label;
        }

        public int getSpriteIndex() {
            return spriteIndex;
        }

        public String getLabel() {
            return label;
        }
    }

    private double velocityY;
    private final CapsuleKind kind;
    private final String capsuleType;

    public UpgradeCapsule(double x, double y, double width, double height, CapsuleKind kind) {
        super(x, y, width, height);
        this.kind = kind != null ? kind : CapsuleKind.BONUS_SCORE;
        this.capsuleType = this.kind.name();
        this.velocityY = GameConfig.CAPSULE_FALL_SPEED;
    }

    public UpgradeCapsule(double x, double y, double width, double height, String capsuleType) {
        super(x, y, width, height);
        CapsuleKind parsed = CapsuleKind.BONUS_SCORE;
        if (capsuleType != null) {
            try {
                parsed = CapsuleKind.valueOf(capsuleType);
            } catch (IllegalArgumentException ignored) {
                parsed = CapsuleKind.BONUS_SCORE;
            }
        }
        this.kind = parsed;
        this.capsuleType = parsed.name();
        this.velocityY = GameConfig.CAPSULE_FALL_SPEED;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        if (y > GameConfig.PLAY_BOTTOM + 20.0) {
            active = false;
        }
    }

    @Override
    public void move(double deltaTime) {
        y += velocityY * deltaTime;
    }

    public CapsuleKind getKind() {
        return kind;
    }

    public String getCapsuleType() {
        return capsuleType;
    }

    @Override
    public double getVelocityX() {
        return 0.0;
    }

    @Override
    public void setVelocityX(double velocityX) {
        // Vertical falling capsule
    }

    @Override
    public double getVelocityY() {
        return velocityY;
    }

    @Override
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    @Override
    public Rectangle2D getCollisionBounds() {
        return getBounds();
    }

    @Override
    public boolean intersects(Collidable other) {
        return other != null && getCollisionBounds().intersects(other.getCollisionBounds());
    }
}
