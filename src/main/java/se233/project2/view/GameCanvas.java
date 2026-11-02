package se233.project2.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se233.project2.model.brick.Brick;
import se233.project2.model.effect.BounceEffect;
import se233.project2.model.effect.BrickDestroyEffect;
import se233.project2.model.effect.FloatingScoreText;
import se233.project2.model.effect.VisualEffect;
import se233.project2.model.entity.Ball;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.entity.UpgradeCapsule;
import se233.project2.model.state.GameState;
import se233.project2.model.state.GameStatus;
import se233.project2.util.GameConfig;

import java.util.List;


public class GameCanvas extends Canvas {

    private static final Font PROMPT_FONT = Font.font("Consolas", FontWeight.BOLD, 16);
    private static final Font SCORE_POPUP_FONT = Font.font("Consolas", FontWeight.EXTRA_BOLD, 15);
    private static final Font BANNER_FONT = Font.font("Consolas", FontWeight.EXTRA_BOLD, 26);

    private AssetManager assetManager;
    private double uiPulseClock = 0.0;

    public GameCanvas(double width, double height) {
        super(width, height);
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void render(GameState state) {
        uiPulseClock += 0.05;
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        renderBackground(gc, state);
        if (state == null) {
            return;
        }

        renderBricks(gc, state);
        renderCapsules(gc, state);
        renderLasers(gc, state);
        renderPaddle(gc, state);
        renderBalls(gc, state);
        renderVisualEffects(gc, state);
        renderSlowMotionOverlay(gc, state);
        renderPromptsAndStatus(gc, state);
    }

    private void renderBackground(GraphicsContext gc, GameState state) {
        Image bg = assetManager != null ? assetManager.getPlayAreaImage() : null;
        if (bg != null) {
            gc.drawImage(bg, 0, 0, getWidth(), getHeight());
        } else {
            gc.setFill(Color.web("#0d0b1e"));
            gc.fillRect(0, 0, getWidth(), getHeight());
            gc.setStroke(Color.web("#3be8ff"));
            gc.setLineWidth(3);
            gc.strokeRect(GameConfig.PLAY_LEFT, GameConfig.PLAY_TOP,
                    GameConfig.PLAY_RIGHT - GameConfig.PLAY_LEFT,
                    GameConfig.PLAY_BOTTOM - GameConfig.PLAY_TOP);
        }

        if (state != null && state.getSlowMotionAbility().isActive()) {
            gc.setFill(Color.color(0.05, 0.72, 0.95, 0.11));
            gc.fillRect(GameConfig.PLAY_LEFT, GameConfig.PLAY_TOP,
                    GameConfig.PLAY_RIGHT - GameConfig.PLAY_LEFT,
                    GameConfig.PLAY_BOTTOM - GameConfig.PLAY_TOP);
        }
    }

    //render flash
    private void renderBricks(GraphicsContext gc, GameState state) {
        for (Brick brick : state.getBricks()) {
            if (!brick.isActive() || brick.isDestroyed()) {
                continue;
            }
            Image sprite = assetManager != null
                    ? assetManager.getBrickSprite(brick.getDamageSpriteRow(), brick.getColorColumn())
                    : null;

            if (sprite != null) {
                gc.drawImage(sprite, brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
            } else {
                gc.setFill(Color.CORNFLOWERBLUE);
                gc.fillRoundRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), 4, 4);
            }

            if (brick.isFlashing()) {
                gc.setFill(Color.color(1.0, 1.0, 1.0, 0.55));
                gc.fillRoundRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), 4, 4);
            }
        }
    }

    private void renderCapsules(GraphicsContext gc, GameState state) {
        for (UpgradeCapsule capsule : state.getCapsules()) {
            if (!capsule.isActive()) {
                continue;
            }
            Image sprite = assetManager != null
                    ? assetManager.getCapsuleSprite(capsule.getKind().getSpriteIndex())
                    : null;

            double pulse = 1.5 * Math.sin(uiPulseClock * 2.2);
            if (sprite != null) {
                gc.drawImage(sprite,
                        capsule.getX() - pulse * 0.5,
                        capsule.getY(),
                        capsule.getWidth() + pulse,
                        capsule.getHeight());
            } else {
                gc.setFill(Color.GOLD);
                gc.fillOval(capsule.getX(), capsule.getY(), capsule.getWidth(), capsule.getHeight());
            }
        }
    }

    private void renderLasers(GraphicsContext gc, GameState state) {
        Image beam = assetManager != null ? assetManager.getLaserBeamSprite() : null;
        for (LaserProjectile laser : state.getLasers()) {
            if (!laser.isActive()) {
                continue;
            }
            if (beam != null) {
                gc.drawImage(beam, laser.getX() - 3, laser.getY(), laser.getWidth() + 6, laser.getHeight());
            } else {
                gc.setFill(Color.ORANGERED);
                gc.fillRect(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
            }
        }
    }

    private void renderPaddle(GraphicsContext gc, GameState state) {
        Paddle paddle = state.getPaddle();
        if (paddle == null || !paddle.isActive()) {
            return;
        }
        Image sprite = assetManager != null //Laser Fire
                ? assetManager.getPaddleSprite(paddle.getSizeTier(), paddle.isLaserEquipped())
                : null;

        if (sprite != null) {
            double drawHeight = paddle.isLaserEquipped() ? paddle.getHeight() * 1.38 : paddle.getHeight();
            double drawY = paddle.isLaserEquipped() ? paddle.getY() - 3.0 : paddle.getY();
            gc.drawImage(sprite, paddle.getX(), drawY, paddle.getWidth(), drawHeight);
        } else {
            gc.setFill(Color.CYAN);
            gc.fillRoundRect(paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight(), 8, 8);
        }
    }

    //Switch to slowMo
    private void renderBalls(GraphicsContext gc, GameState state) {
        boolean slowMo = state.getSlowMotionAbility().isActive();

        for (Ball ball : state.getBalls()) {
            if (!ball.isActive()) {
                continue;
            }

            // RenderSlow Motion chrono
            List<double[]> trail = ball.getTrailPositions();
            if (!ball.isDockedToPaddle() && !trail.isEmpty()) {
                int n = trail.size();
                for (int i = 0; i < n; i++) {
                    double[] pt = trail.get(i);
                    double ratio = (double) (i + 1) / (n + 1);
                    double alpha = slowMo ? 0.36 * ratio : 0.14 * ratio;
                    double sz = ball.getWidth() * (0.55 + 0.38 * ratio);
                    double offset = (ball.getWidth() - sz) * 0.5;
                    gc.setFill(slowMo
                            ? Color.color(0.25, 0.94, 1.0, alpha)
                            : (ball.isPiercing()
                                ? Color.color(0.76, 0.42, 1.0, alpha)
                                : Color.color(1.0, 0.92, 0.72, alpha)));
                    gc.fillOval(pt[0] + offset, pt[1] + offset, sz, sz);
                }
            }

            //Check ball action/draw ballBounceAim
            Image frame = null;
            double drawScale = 1.0;
            if (assetManager != null) {
                if (ball.getCurrentAction() == Ball.AnimationAction.BOUNCING) {
                    frame = assetManager.getBallBounceAnim().getFrameAtTime(ball.getAnimationTime());
                    drawScale = 1.30;
                } else if (slowMo) {
                    frame = assetManager.getSlowMoBallMoveAnim().getFrameAtTime(ball.getAnimationTime());
                    drawScale = 1.12;
                } else if (ball.isPiercing()) {
                    frame = assetManager.getPiercingBallMoveAnim().getFrameAtTime(ball.getAnimationTime());
                    drawScale = 1.15;
                } else {
                    frame = assetManager.getNormalBallMoveAnim().getFrameAtTime(ball.getAnimationTime());
                }
            }

            if (frame != null) {
                double dw = ball.getWidth() * drawScale;
                double dh = ball.getHeight() * drawScale;
                double dx = ball.getCenterX() - dw * 0.5;
                double dy = ball.getCenterY() - dh * 0.5;
                gc.drawImage(frame, dx, dy, dw, dh);
            } else {
                gc.setFill(Color.WHITE);
                gc.fillOval(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
            }
        }
    }
    //Score
    private void renderVisualEffects(GraphicsContext gc, GameState state) {
        for (VisualEffect effect : state.getVisualEffects()) {
            if (!effect.isActive() || effect.isFinished()) {
                continue;
            }
            double progress = effect.getProgress();

            if (effect instanceof BrickDestroyEffect destroyFx) {
                Image shatter = assetManager != null
                        ? assetManager.getBrickShatterSprite(destroyFx.getFrameIndex(2), destroyFx.getColorColumn())
                        : null;
                if (shatter != null) {
                    gc.save();
                    gc.setGlobalAlpha(Math.max(0.0, 1.0 - progress * 0.85));
                    double expand = 1.0 + progress * 0.35;
                    double dw = destroyFx.getWidth() * expand;
                    double dh = destroyFx.getHeight() * (expand + 0.25);
                    double dx = destroyFx.getCenterX() - dw * 0.5;
                    double dy = destroyFx.getCenterY() - dh * 0.5;
                    gc.drawImage(shatter, dx, dy, dw, dh);
                    gc.restore();
                }
            } else if (effect instanceof BounceEffect bounceFx) {
                List<Image> sparks = assetManager != null ? assetManager.getSparkFrames() : List.of();
                if (!sparks.isEmpty()) {
                    Image spark = sparks.get(bounceFx.getFrameIndex(sparks.size()));
                    gc.save();
                    gc.setGlobalAlpha(Math.max(0.0, 1.0 - progress * 0.7));
                    gc.drawImage(spark, bounceFx.getX(), bounceFx.getY(), bounceFx.getWidth(), bounceFx.getHeight());
                    gc.restore();
                }
            } else if (effect instanceof FloatingScoreText scoreFx) {
                gc.save();
                gc.setGlobalAlpha(Math.max(0.0, 1.0 - progress));
                gc.setFont(SCORE_POPUP_FONT);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2.5);
                gc.strokeText(scoreFx.getText(), scoreFx.getX(), scoreFx.getY());
                gc.setFill(Color.web(scoreFx.getColorHex()));
                gc.fillText(scoreFx.getText(), scoreFx.getX(), scoreFx.getY());
                gc.restore();
            }
        }
    }

    private void renderSlowMotionOverlay(GraphicsContext gc, GameState state) {
        if (!state.getSlowMotionAbility().isActive()) {
            return;
        }
        double rem = state.getSlowMotionAbility().getRemainingDuration();
        double pulseAlpha = 0.45 + 0.25 * Math.sin(uiPulseClock * 2.5);

        gc.save();
        gc.setStroke(Color.color(0.22, 0.94, 1.0, pulseAlpha));
        gc.setLineWidth(3.5);
        gc.strokeRect(GameConfig.PLAY_LEFT + 2, GameConfig.PLAY_TOP + 2,
                GameConfig.PLAY_RIGHT - GameConfig.PLAY_LEFT - 4,
                GameConfig.PLAY_BOTTOM - GameConfig.PLAY_TOP - 4);

        String badge = String.format("SLOW MOTION 0.5x  [ %.1fs ]", rem);
        gc.setFill(Color.color(0.04, 0.16, 0.26, 0.82));
        gc.fillRoundRect(160, 38, 240, 28, 10, 10);
        gc.setStroke(Color.web("#48f0ff"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(160, 38, 240, 28, 10, 10);

        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#7ef9ff"));
        gc.fillText(badge, 280, 57);
        gc.restore();
    }

    private void renderPromptsAndStatus(GraphicsContext gc, GameState state) {
        if (state.getStatus() == GameStatus.PLAYING) {
            boolean allDocked = !state.getBalls().isEmpty()
                    && state.getBalls().stream().allMatch(Ball::isDockedToPaddle);
            if (allDocked) {
                double alpha = 0.55 + 0.40 * Math.sin(uiPulseClock * 1.8);
                gc.save();
                gc.setGlobalAlpha(alpha);
                gc.setFont(PROMPT_FONT);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3);
                gc.strokeText("PRESS [SPACE] TO LAUNCH BALL", getWidth() * 0.5, 610);
                gc.setFill(Color.web("#64f4ff"));
                gc.fillText("PRESS [SPACE] TO LAUNCH BALL", getWidth() * 0.5, 610);
                gc.restore();
            }
        } else if (state.getStatus() == GameStatus.PAUSED) {
            gc.save();
            gc.setFill(Color.color(0.03, 0.03, 0.08, 0.72));
            gc.fillRect(GameConfig.PLAY_LEFT, GameConfig.PLAY_TOP,
                    GameConfig.PLAY_RIGHT - GameConfig.PLAY_LEFT,
                    GameConfig.PLAY_BOTTOM - GameConfig.PLAY_TOP);
            gc.setFont(BANNER_FONT);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFill(Color.web("#ffd166"));
            gc.fillText("GAME PAUSED", getWidth() * 0.5, getHeight() * 0.47);
            gc.setFont(PROMPT_FONT);
            gc.setFill(Color.web("#e0f7ff"));
            gc.fillText("Press [P] or [ESC] to Resume", getWidth() * 0.5, getHeight() * 0.52);
            gc.restore();
        }
    }
}
