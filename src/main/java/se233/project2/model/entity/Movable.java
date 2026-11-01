package se233.project2.model.entity;

/**
 * Interface defining movement behavior for dynamic game objects
 * (Paddle, Ball, LaserProjectile, UpgradeCapsule).
 */
public interface Movable {

    void move(double deltaTime);

    double getVelocityX();

    void setVelocityX(double velocityX);

    double getVelocityY();

    void setVelocityY(double velocityY);
}
