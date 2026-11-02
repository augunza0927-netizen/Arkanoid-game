package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;
import se233.project2.util.GameConfig;

//fired laser
public class LaserProjectile extends GameObject implements Movable, Collidable {

    private double velocityY;
    private final int damage;

    public LaserProjectile(double x, double y, double width, double height) {
        this(x, y, width, height, 1);
    }

    public LaserProjectile(double x, double y, double width, double height, int damage) {
        super(x, y, width, height);
        this.velocityY = -GameConfig.LASER_SPEED;
        this.damage = Math.max(1, damage);
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        if (y + height < GameConfig.PLAY_TOP) {
            active = false;
        }
    }

    @Override
    public void move(double deltaTime) {
        y += velocityY * deltaTime;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public double getVelocityX() {
        return 0.0;
    }

    @Override
    public void setVelocityX(double velocityX) {
        // Vertical laser beam
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
