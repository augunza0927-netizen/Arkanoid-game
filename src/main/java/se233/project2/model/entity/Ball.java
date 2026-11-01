package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;
import se233.project2.util.GameConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Ball entity supporting continuous 2D bouncing, separated move vs. bounce animation states,
 * piercing/pass-through mode, and speed scaling (including the 0.5x Slow Motion modifier).
 */
public class Ball extends GameObject implements Movable, Collidable {

    public enum AnimationAction {
        MOVING,
        BOUNCING
    }

    private static final double BOUNCE_ACTION_DURATION = 0.18;
    private static final int MAX_TRAIL_POINTS = 7;

    private double velocityX;
    private double velocityY;
    private double baseSpeed = GameConfig.BALL_BASE_SPEED;
    private double speedMultiplier = 1.0;
    private boolean piercing = false;
    private boolean dockedToPaddle = true;

    private AnimationAction currentAction = AnimationAction.MOVING;
    private double bounceActionTimer = 0.0;
    private double animationTime = 0.0;
    private double trailSampleTimer = 0.0;
    private final Deque<double[]> trailPositions = new ArrayDeque<>();

    public Ball(double x, double y, double size) {
        super(x, y, size, size);
    }

    public void dockOnPaddle(Paddle paddle) {
        this.dockedToPaddle = true;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.currentAction = AnimationAction.MOVING;
        this.trailPositions.clear();
        syncWithPaddle(paddle);
    }

    public void syncWithPaddle(Paddle paddle) {
        if (dockedToPaddle && paddle != null) {
            this.x = paddle.getCenterX() - width * 0.5;
            this.y = paddle.getY() - height - 1.5;
        }
    }

    public void launch(double angleDegreesFromVertical) {
        this.dockedToPaddle = false;
        double rad = Math.toRadians(angleDegreesFromVertical);
        this.velocityX = baseSpeed * Math.sin(rad);
        this.velocityY = -baseSpeed * Math.cos(rad);
        this.currentAction = AnimationAction.MOVING;
    }

    public void launch() {
        launch(18.0);
    }

    public void triggerBounce() {
        this.currentAction = AnimationAction.BOUNCING;
        this.bounceActionTimer = BOUNCE_ACTION_DURATION;
    }

    public void bounceHorizontal() {
        this.velocityX = -this.velocityX;
        triggerBounce();
    }

    public void bounceVertical() {
        this.velocityY = -this.velocityY;
        triggerBounce();
    }

    public void deflectFromPaddle(Paddle paddle) {
        double hitOffset = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() * 0.5);
        hitOffset = Math.max(-0.95, Math.min(0.95, hitOffset));
        double maxAngleRad = Math.toRadians(62.0);
        double bounceAngle = hitOffset * maxAngleRad;

        double speed = getVectorSpeed();
        if (speed < 1.0) {
            speed = baseSpeed;
        }
        this.velocityX = speed * Math.sin(bounceAngle);
        this.velocityY = -Math.abs(speed * Math.cos(bounceAngle));
        this.y = paddle.getY() - this.height - 0.5;
        triggerBounce();
    }

    @Override
    public void update(double deltaTime) {
        animationTime += deltaTime;

        if (bounceActionTimer > 0.0) {
            bounceActionTimer -= deltaTime;
            if (bounceActionTimer <= 0.0) {
                bounceActionTimer = 0.0;
                currentAction = AnimationAction.MOVING;
            }
        }

        if (!dockedToPaddle) {
            trailSampleTimer += deltaTime;
            if (trailSampleTimer >= 0.025) {
                trailSampleTimer = 0.0;
                if (trailPositions.size() >= MAX_TRAIL_POINTS) {
                    trailPositions.pollFirst();
                }
                trailPositions.addLast(new double[]{x, y});
            }
            move(deltaTime);
        }
    }

    @Override
    public void move(double deltaTime) {
        if (dockedToPaddle) {
            return;
        }
        x += velocityX * speedMultiplier * deltaTime;
        y += velocityY * speedMultiplier * deltaTime;
    }

    public double getVectorSpeed() {
        return Math.hypot(velocityX, velocityY);
    }

    public double getEffectiveSpeed() {
        return getVectorSpeed() * speedMultiplier;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = Math.min(GameConfig.BALL_MAX_SPEED, Math.max(120.0, baseSpeed));
        double currentVec = getVectorSpeed();
        if (!dockedToPaddle && currentVec > 0.001) {
            double scale = this.baseSpeed / currentVec;
            this.velocityX *= scale;
            this.velocityY *= scale;
        }
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public boolean isDockedToPaddle() {
        return dockedToPaddle;
    }

    public void setDockedToPaddle(boolean dockedToPaddle) {
        this.dockedToPaddle = dockedToPaddle;
    }

    public AnimationAction getCurrentAction() {
        return currentAction;
    }

    public double getAnimationTime() {
        return animationTime;
    }

    public List<double[]> getTrailPositions() {
        return new ArrayList<>(trailPositions);
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
