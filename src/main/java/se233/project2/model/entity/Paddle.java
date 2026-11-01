package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;
import se233.project2.util.GameConfig;

/**
 * Player-controlled paddle entity.
 * Supports dynamic width upgrades, movement speed scaling, and dual-laser special attack mode.
 */
public class Paddle extends GameObject implements Movable, Collidable {

    private double velocityX;
    private double moveSpeed;
    private boolean laserEquipped;
    private double minBoundX;
    private double maxBoundX;

    public Paddle(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.moveSpeed = GameConfig.PADDLE_DEFAULT_SPEED;
        this.minBoundX = GameConfig.PLAY_LEFT;
        this.maxBoundX = GameConfig.PLAY_RIGHT;
    }

    public void moveLeft() {
        this.velocityX = -moveSpeed;
    }

    public void moveRight() {
        this.velocityX = moveSpeed;
    }

    public void stop() {
        this.velocityX = 0.0;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
    }

    @Override
    public void move(double deltaTime) {
        x += velocityX * deltaTime;
        clampWithinBounds();
    }

    public void clampWithinBounds() {
        if (x < minBoundX) {
            x = minBoundX;
        }
        if (x + width > maxBoundX) {
            x = Math.max(minBoundX, maxBoundX - width);
        }
    }

    public void expandWidth(double multiplier) {
        double center = getCenterX();
        double newWidth = Math.min(GameConfig.PADDLE_MAX_WIDTH, Math.max(GameConfig.PADDLE_MIN_WIDTH, width * multiplier));
        this.width = newWidth;
        this.x = center - newWidth * 0.5;
        clampWithinBounds();
    }

    public void increaseSpeed(double multiplier) {
        this.moveSpeed = Math.min(GameConfig.PADDLE_MAX_SPEED, moveSpeed * multiplier);
    }

    public void resetPosition() {
        this.x = (minBoundX + maxBoundX - width) * 0.5;
        this.y = GameConfig.PADDLE_Y;
        this.velocityX = 0.0;
    }

    /**
     * Returns visual sprite size tier: 0 = compact, 1 = standard, 2 = expanded.
     */
    public int getSizeTier() {
        if (width < 92.0) {
            return 0;
        } else if (width < 130.0) {
            return 1;
        }
        return 2;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public boolean isLaserEquipped() {
        return laserEquipped;
    }

    public void setLaserEquipped(boolean laserEquipped) {
        this.laserEquipped = laserEquipped;
    }

    public void setPlayBounds(double minBoundX, double maxBoundX) {
        this.minBoundX = minBoundX;
        this.maxBoundX = maxBoundX;
        clampWithinBounds();
    }

    @Override
    public double getVelocityX() {
        return velocityX;
    }

    @Override
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    @Override
    public double getVelocityY() {
        return 0.0;
    }

    @Override
    public void setVelocityY(double velocityY) {
        // Horizontal-only paddle movement
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
