package se233.project2.controller;

import javafx.geometry.Rectangle2D;
import se233.project2.model.brick.Brick;
import se233.project2.model.effect.BounceEffect;
import se233.project2.model.effect.BrickDestroyEffect;
import se233.project2.model.effect.FloatingScoreText;
import se233.project2.model.entity.Ball;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.entity.UpgradeCapsule;
import se233.project2.model.entity.UpgradeCapsule.CapsuleKind;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Resolves 2D collisions between balls, play-area boundaries, paddle, bricks,
 * dual-laser projectiles, and falling upgrade capsules.
 */
public class CollisionManager {

    private static final double SPECIAL_BLAST_RADIUS = 78.0;
    private final Random random = new Random();
    private Consumer<String> soundEventCallback;

    public void setSoundEventCallback(Consumer<String> soundEventCallback) {
        this.soundEventCallback = soundEventCallback;
    }

    public void processCollisions(GameState state) {
        if (state == null) {
            return;
        }
        processBallCollisions(state);
        processLaserCollisions(state);
        processCapsuleCollisions(state);
    }

    private void processBallCollisions(GameState state) {
        Paddle paddle = state.getPaddle();

        for (Ball ball : state.getBalls()) {
            if (!ball.isActive() || ball.isDockedToPaddle()) {
                continue;
            }

            // 1. Left, Right, Top Play Area Boundaries
            if (ball.getX() <= GameConfig.PLAY_LEFT) {
                ball.setX(GameConfig.PLAY_LEFT);
                ball.setVelocityX(Math.abs(ball.getVelocityX()));
                ball.triggerBounce();
                spawnSpark(state, ball.getX(), ball.getCenterY());
                emitSound("bounce");
            } else if (ball.getX() + ball.getWidth() >= GameConfig.PLAY_RIGHT) {
                ball.setX(GameConfig.PLAY_RIGHT - ball.getWidth());
                ball.setVelocityX(-Math.abs(ball.getVelocityX()));
                ball.triggerBounce();
                spawnSpark(state, ball.getX() + ball.getWidth(), ball.getCenterY());
                emitSound("bounce");
            }

            if (ball.getY() <= GameConfig.PLAY_TOP) {
                ball.setY(GameConfig.PLAY_TOP);
                ball.setVelocityY(Math.abs(ball.getVelocityY()));
                ball.triggerBounce();
                spawnSpark(state, ball.getCenterX(), ball.getY());
                emitSound("bounce");
            }

            // 2. Paddle Deflection
            if (paddle != null && paddle.isActive() && ball.getVelocityY() > 0 && ball.intersects(paddle)) {
                ball.deflectFromPaddle(paddle);
                spawnSpark(state, ball.getCenterX(), paddle.getY());
                emitSound("bounce");
            }

            // 3. Brick Collisions
            for (Brick brick : state.getBricks()) {
                if (!brick.isActive() || brick.isDestroyed()) {
                    continue;
                }
                if (ball.intersects(brick)) {
                    Rectangle2D ballBox = ball.getCollisionBounds();
                    Rectangle2D brickBox = brick.getCollisionBounds();

                    brick.onHit(1);
                    boolean destroyed = brick.isDestroyed();

                    // Resolve bounce reflection unless ball is in Piercing mode and destroyed the brick
                    if (!(ball.isPiercing() && destroyed)) {
                        reflectBallFromBrick(ball, ballBox, brickBox);
                    } else {
                        ball.triggerBounce();
                    }

                    handleBrickImpactOutcome(state, brick, destroyed, ball.getCenterX(), ball.getCenterY());

                    // Only pierce-through balls can hit multiple bricks on the exact same sub-frame
                    if (!ball.isPiercing() || !destroyed) {
                        break;
                    }
                }
            }
        }
    }

    private void reflectBallFromBrick(Ball ball, Rectangle2D ballBox, Rectangle2D brickBox) {
        double overlapLeft = (ballBox.getMaxX()) - brickBox.getMinX();
        double overlapRight = brickBox.getMaxX() - ballBox.getMinX();
        double overlapTop = (ballBox.getMaxY()) - brickBox.getMinY();
        double overlapBottom = brickBox.getMaxY() - ballBox.getMinY();

        double minOverlapX = Math.min(overlapLeft, overlapRight);
        double minOverlapY = Math.min(overlapTop, overlapBottom);

        if (minOverlapX < minOverlapY) {
            if (overlapLeft < overlapRight) {
                ball.setX(brickBox.getMinX() - ball.getWidth() - 0.5);
                ball.setVelocityX(-Math.abs(ball.getVelocityX()));
            } else {
                ball.setX(brickBox.getMaxX() + 0.5);
                ball.setVelocityX(Math.abs(ball.getVelocityX()));
            }
            ball.triggerBounce();
        } else {
            if (overlapTop < overlapBottom) {
                ball.setY(brickBox.getMinY() - ball.getHeight() - 0.5);
                ball.setVelocityY(-Math.abs(ball.getVelocityY()));
            } else {
                ball.setY(brickBox.getMaxY() + 0.5);
                ball.setVelocityY(Math.abs(ball.getVelocityY()));
            }
            ball.triggerBounce();
        }
    }

    private void processLaserCollisions(GameState state) {
        for (LaserProjectile laser : state.getLasers()) {
            if (!laser.isActive()) {
                continue;
            }
            for (Brick brick : state.getBricks()) {
                if (!brick.isActive() || brick.isDestroyed()) {
                    continue;
                }
                if (laser.intersects(brick)) {
                    laser.setActive(false);
                    brick.onHit(laser.getDamage());
                    handleBrickImpactOutcome(state, brick, brick.isDestroyed(), laser.getCenterX(), laser.getY());
                    break;
                }
            }
        }
    }

    private void handleBrickImpactOutcome(GameState state, Brick brick, boolean destroyed,
                                          double hitX, double hitY) {
        spawnSpark(state, hitX, hitY);

        if (destroyed) {
            int awarded = state.getScoreManager().awardPoints(brick.getScoreValue());
            state.getVisualEffects().add(new BrickDestroyEffect(
                    brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), brick.getColorColumn()));
            state.getVisualEffects().add(new FloatingScoreText(
                    brick.getCenterX(), brick.getCenterY(), "+" + awarded, "#ffe66d"));
            emitSound("brick_destroy");

            maybeSpawnCapsule(state, brick);

            if (brick.triggersAreaBlastOnDestroy()) {
                triggerSpecialAreaBlast(state, brick);
            }
        } else {
            // Brick was damaged (cracked) — show visual score feedback immediately
            int chipAward = state.getScoreManager().awardPoints(25);
            state.getVisualEffects().add(new FloatingScoreText(
                    brick.getCenterX(), brick.getY(), "+" + chipAward, "#90e0ef"));
            emitSound("brick_hit");
        }
    }

    private void triggerSpecialAreaBlast(GameState state, Brick origin) {
        GameLogger.logGameEvent("SpecialBrick area shockwave triggered!");
        List<Brick> chainTargets = new ArrayList<>();
        for (Brick other : state.getBricks()) {
            if (other != origin && other.isActive() && !other.isDestroyed()) {
                double dist = Math.hypot(other.getCenterX() - origin.getCenterX(),
                        other.getCenterY() - origin.getCenterY());
                if (dist <= SPECIAL_BLAST_RADIUS) {
                    chainTargets.add(other);
                }
            }
        }

        for (Brick target : chainTargets) {
            target.onHit(1);
            if (target.isDestroyed()) {
                int awarded = state.getScoreManager().awardPoints(target.getScoreValue());
                state.getVisualEffects().add(new BrickDestroyEffect(
                        target.getX(), target.getY(), target.getWidth(), target.getHeight(), target.getColorColumn()));
                state.getVisualEffects().add(new FloatingScoreText(
                        target.getCenterX(), target.getCenterY(), "+" + awarded, "#ff9f1c"));
            } else {
                int chip = state.getScoreManager().awardPoints(25);
                state.getVisualEffects().add(new FloatingScoreText(
                        target.getCenterX(), target.getY(), "+" + chip, "#90e0ef"));
            }
        }
    }

    private void maybeSpawnCapsule(GameState state, Brick brick) {
        if (random.nextDouble() > brick.getCapsuleDropChance()) {
            return;
        }
        CapsuleKind[] kinds = CapsuleKind.values();
        // Weight towards fun active capsules (Dual Laser, Slow-Mo, Multi-Ball, Expand)
        int roll = random.nextInt(100);
        CapsuleKind chosen;
        if (roll < 24) {
            chosen = CapsuleKind.DUAL_LASER;
        } else if (roll < 46) {
            chosen = CapsuleKind.SLOW_MO_CHARGE;
        } else if (roll < 64) {
            chosen = CapsuleKind.MULTI_BALL;
        } else if (roll < 80) {
            chosen = CapsuleKind.EXPAND_PADDLE;
        } else if (roll < 90) {
            chosen = CapsuleKind.EXTRA_LIFE;
        } else {
            chosen = kinds[CapsuleKind.BONUS_SCORE.ordinal()];
        }

        double cx = brick.getCenterX() - GameConfig.CAPSULE_WIDTH * 0.5;
        double cy = brick.getCenterY();
        state.getCapsules().add(new UpgradeCapsule(
                cx, cy, GameConfig.CAPSULE_WIDTH, GameConfig.CAPSULE_HEIGHT, chosen));
    }

    private void processCapsuleCollisions(GameState state) {
        Paddle paddle = state.getPaddle();
        if (paddle == null || !paddle.isActive()) {
            return;
        }

        for (UpgradeCapsule capsule : state.getCapsules()) {
            if (!capsule.isActive()) {
                continue;
            }
            if (paddle.intersects(capsule)) {
                capsule.setActive(false);
                applyCapsuleEffect(state, capsule);
            }
        }
    }

    private void applyCapsuleEffect(GameState state, UpgradeCapsule capsule) {
        CapsuleKind kind = capsule.getKind();
        GameLogger.logPlayerAction("Collected falling capsule: " + kind.name());
        emitSound("upgrade");

        switch (kind) {
            case EXTRA_LIFE -> {
                if (state.getLives() < GameConfig.MAX_LIVES) {
                    state.addLife(1);
                } else {
                    state.getScoreManager().awardPoints(500);
                }
            }
            case EXPAND_PADDLE -> state.getPaddle().expandWidth(1.16);
            case MULTI_BALL -> {
                for (Ball b : state.getBalls()) {
                    if (b.isDockedToPaddle()) {
                        b.launch(15.0);
                    }
                }
                state.spawnExtraActiveBall(-25.0);
                state.spawnExtraActiveBall(25.0);
            }
            case DUAL_LASER -> state.getDualLaserAbility().forceActivate(state, GameConfig.DUAL_LASER_DURATION_SECONDS);
            case SLOW_MO_CHARGE -> state.getSlowMotionAbility().grantCapsuleCharge(state);
            case BONUS_SCORE -> state.getScoreManager().awardPoints(500);
        }

        state.getVisualEffects().add(new FloatingScoreText(
                state.getPaddle().getCenterX(), state.getPaddle().getY() - 10, kind.getLabel(), "#7ef9ff"));
    }

    private void spawnSpark(GameState state, double x, double y) {
        state.getVisualEffects().add(new BounceEffect(x, y, 26.0));
    }

    private void emitSound(String key) {
        if (soundEventCallback != null) {
            soundEventCallback.accept(key);
        }
    }
}
