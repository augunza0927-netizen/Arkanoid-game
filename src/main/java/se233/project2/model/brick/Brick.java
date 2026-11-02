package se233.project2.model.brick;

import javafx.geometry.Rectangle2D;
import se233.project2.model.entity.Collidable;
import se233.project2.model.entity.Damageable;
import se233.project2.model.entity.GameObject;


public abstract class Brick extends GameObject implements Collidable, Damageable {

    protected final BrickType brickType;
    protected int hitPoints;
    protected final int maxHitPoints;
    protected final int scoreValue;
    protected final int colorColumn;
    protected double hitFlashTimer = 0.0;

    protected Brick(double x, double y, double width, double height,
                    BrickType brickType, int maxHitPoints, int scoreValue, int colorColumn) {
        super(x, y, width, height);
        this.brickType = brickType;
        this.maxHitPoints = Math.max(1, maxHitPoints);
        this.hitPoints = this.maxHitPoints;
        this.scoreValue = scoreValue;
        this.colorColumn = Math.max(0, Math.min(5, colorColumn));
    }

    protected Brick(double x, double y, double width, double height,
                    BrickType brickType, int maxHitPoints, int scoreValue) {
        this(x, y, width, height, brickType, maxHitPoints, scoreValue, 0);
    }

    public abstract void onHit(int damage);

    public double getCapsuleDropChance() {
        return 0.14;
    }

    public boolean triggersAreaBlastOnDestroy() {
        return false;
    }

    public int getDamageSpriteRow() {
        if (maxHitPoints <= 1 || hitPoints >= maxHitPoints) {
            return 0;
        }
        double ratio = (double) hitPoints / maxHitPoints;
        return ratio > 0.5 ? 1 : 2;
    }

    public BrickType getBrickType() {
        return brickType;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public int getColorColumn() {
        return colorColumn;
    }

    public boolean isFlashing() {
        return hitFlashTimer > 0.0;
    }

    //Hit Flash Overlay
    @Override
    public void takeDamage(int amount) {
        if (amount <= 0 || isDestroyed()) {
            return;
        }
        hitPoints = Math.max(0, hitPoints - amount);
        hitFlashTimer = 0.12;
        if (hitPoints == 0) {
            active = false;
        }
    }

    @Override
    public int getHitPoints() {
        return hitPoints;
    }

    @Override
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    @Override
    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    @Override
    public Rectangle2D getCollisionBounds() {
        return getBounds();
    }

    @Override
    public boolean intersects(Collidable other) {
        return other != null && getCollisionBounds().intersects(other.getCollisionBounds());
    }

    @Override
    public void update(double deltaTime) {
        if (hitFlashTimer > 0.0) {
            hitFlashTimer = Math.max(0.0, hitFlashTimer - deltaTime);
        }
    }
}
