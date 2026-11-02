package se233.project2;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se233.project2.controller.CollisionManager;
import se233.project2.controller.GameController;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.brick.SpecialBrick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.entity.Ball;
import se233.project2.model.entity.LaserProjectile;
import se233.project2.model.entity.Paddle;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;

import static org.junit.jupiter.api.Assertions.*;

//Test Moving vs Bouncing
class BallBrickCollisionTest {

    private GameState state;
    private CollisionManager collisionManager;

    @BeforeEach
    void setUp() {
        state = new GameState();
        state.resetForNewRun();
        collisionManager = new CollisionManager();
    }

    @Test
    @DisplayName("Paddle moves via keyboard input and clamps cleanly at playfield walls")
    void testPaddleMovementAndBoundaryClamping() {
        GameController controller = new GameController(state);
        controller.startNewGame();
        Paddle paddle = state.getPaddle();

        controller.getInputHandler().pressKeyForTest(KeyCode.LEFT);
        controller.updateFrame(2.0);
        assertEquals(GameConfig.PLAY_LEFT, paddle.getX(), 0.001, "Paddle must clamp at left wall");

        controller.getInputHandler().releaseKeyForTest(KeyCode.LEFT);
        controller.getInputHandler().pressKeyForTest(KeyCode.RIGHT);
        controller.updateFrame(3.0);
        assertEquals(GameConfig.PLAY_RIGHT - paddle.getWidth(), paddle.getX(), 0.001, "Paddle must clamp at right wall");
        controller.shutdown();
    }

    @Test
    @DisplayName("Ball bounces off walls and paddle while separating MOVING and BOUNCING actions")
    void testBallBoundaryAndPaddleBounceAndActionSeparation() {
        Ball ball = new Ball(GameConfig.PLAY_LEFT - 2.0, 200.0, GameConfig.BALL_SIZE);
        ball.setDockedToPaddle(false);
        ball.setVelocityX(-220.0);
        ball.setVelocityY(-180.0);
        state.getBalls().add(ball);

        assertEquals(Ball.AnimationAction.MOVING, ball.getCurrentAction());


        collisionManager.processCollisions(state);
        assertTrue(ball.getVelocityX() > 0, "Velocity X should reflect rightward");
        assertEquals(Ball.AnimationAction.BOUNCING, ball.getCurrentAction(), "Bounce must trigger BOUNCING action");


        ball.update(0.25);
        assertEquals(Ball.AnimationAction.MOVING, ball.getCurrentAction(), "Ball should return to MOVING action");


        Paddle paddle = state.getPaddle();
        ball.setX(paddle.getCenterX() + 20.0);
        ball.setY(paddle.getY() - ball.getHeight() + 3.0);
        ball.setVelocityX(0.0);
        ball.setVelocityY(260.0);

        collisionManager.processCollisions(state);
        assertTrue(ball.getVelocityY() < 0, "Ball must bounce upward off paddle");
        assertTrue(ball.getVelocityX() > 0, "Hitting right side of paddle must angle ball rightward");
        assertEquals(Ball.AnimationAction.BOUNCING, ball.getCurrentAction());
    }

    @Test
    @DisplayName("Normal, Strong, and Special bricks take damage and destroy according to type")
    void testBrickDamageDestructionAndSpecialBlast() {
        NormalBrick normal = new NormalBrick(200, 200, 54, 24);
        StrongBrick strong = new StrongBrick(300, 200, 54, 24, 3);
        SpecialBrick special = new SpecialBrick(200, 300, 54, 24);
        NormalBrick neighbor = new NormalBrick(255, 300, 54, 24);

        state.getBricks().add(normal);
        state.getBricks().add(strong);
        state.getBricks().add(special);
        state.getBricks().add(neighbor);

        //Normal brick destroys in 1 hit
        Ball ball = new Ball(210, 220, GameConfig.BALL_SIZE);
        ball.setDockedToPaddle(false);
        ball.setVelocityX(0);
        ball.setVelocityY(-250);
        state.getBalls().add(ball);

        collisionManager.processCollisions(state);
        assertTrue(normal.isDestroyed(), "NormalBrick must destroy in 1 hit");
        assertTrue(ball.getVelocityY() > 0, "Non-piercing ball must bounce back from NormalBrick");

        // Strong Brick
        assertEquals(0, strong.getDamageSpriteRow());
        strong.onHit(1);
        assertFalse(strong.isDestroyed());
        assertEquals(1, strong.getDamageSpriteRow());
        strong.onHit(1);
        assertFalse(strong.isDestroyed());
        assertEquals(2, strong.getDamageSpriteRow());
        strong.onHit(1);
        assertTrue(strong.isDestroyed());

        //Special Bricks
        special.onHit(1);
        assertFalse(special.isDestroyed());
        LaserProjectile laser = new LaserProjectile(215, 310, 8, 26, 1);
        state.getLasers().add(laser);
        collisionManager.processCollisions(state);

        assertTrue(special.isDestroyed(), "Laser hit should finish off damaged SpecialBrick");
        assertTrue(neighbor.isDestroyed(), "SpecialBrick area shockwave should destroy adjacent NormalBrick");
    }

    @Test
    @DisplayName("Piercing ball passes straight through destroyed 1-HP bricks without reversing velocity")
    void testPiercingBallPassThrough() {
        NormalBrick brick = new NormalBrick(200, 200, 54, 24);
        state.getBricks().add(brick);

        Ball ball = new Ball(215, 210, GameConfig.BALL_SIZE);
        ball.setDockedToPaddle(false);
        ball.setPiercing(true);
        ball.setVelocityX(0);
        ball.setVelocityY(-300);
        state.getBalls().add(ball);

        collisionManager.processCollisions(state);
        assertTrue(brick.isDestroyed());
        assertEquals(-300.0, ball.getVelocityY(), 0.001, "Piercing ball must maintain upward velocity through destroyed brick");
    }
}
