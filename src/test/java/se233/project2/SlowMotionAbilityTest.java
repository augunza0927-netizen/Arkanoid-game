package se233.project2;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameController;
import se233.project2.model.ability.SlowMotionAbility;
import se233.project2.model.entity.Ball;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the custom Slow Motion feature:
 * button press halves ball speed (0.5x) for exactly 5.0 seconds and restores normal speed.
 */
class SlowMotionAbilityTest {

    private GameState state;
    private GameController controller;

    @BeforeEach
    void setUp() {
        state = new GameState();
        controller = new GameController(state);
        controller.startNewGame();
    }

    @Test
    @DisplayName("Pressing Slow Motion key halves ball speed (0.5x) for 5.0 seconds then restores 1.0x speed")
    void testSlowMotionHalvesBallSpeedForFiveSeconds() {
        Ball ball = state.getBalls().get(0);
        ball.setDockedToPaddle(false);
        ball.setX(280.0);
        ball.setY(500.0);
        ball.setVelocityX(0.0);
        ball.setVelocityY(-200.0);

        // Measure 0.5s displacement at normal 1.0x speed -> moves 100px upward
        double startY = ball.getY();
        ball.update(0.5);
        double normalDisplacement = startY - ball.getY();
        assertEquals(100.0, normalDisplacement, 0.01);

        // Trigger Slow Motion via keyboard button press (SHIFT)
        controller.getInputHandler().pressKeyForTest(KeyCode.SHIFT);
        SlowMotionAbility slowMo = state.getSlowMotionAbility();

        assertTrue(slowMo.isActive(), "Slow Motion must become active on SHIFT key press");
        assertEquals(0.5, ball.getSpeedMultiplier(), 0.001, "Ball speed multiplier must be halved to 0.5x");
        assertEquals(5.0, slowMo.getRemainingDuration(), 0.001, "Slow Motion duration must start at 5.0 seconds");

        // Measure 0.5s displacement during Slow Motion -> moves only 50px (half of 100px!)
        double slowStartY = ball.getY();
        ball.update(0.5);
        double slowDisplacement = slowStartY - ball.getY();
        assertEquals(50.0, slowDisplacement, 0.01, "Ball must travel half distance per second during Slow Motion");

        // Advance timer by 4.9 seconds -> still active!
        slowMo.update(4.9, state);
        assertTrue(slowMo.isActive(), "Slow Motion must still be active at 4.9s");
        assertEquals(0.5, ball.getSpeedMultiplier(), 0.001);

        // Advance final 0.1 seconds (total 5.0 seconds) -> automatically expires and restores 1.0x speed!
        slowMo.update(0.1, state);
        assertFalse(slowMo.isActive(), "Slow Motion must deactivate at 5.0s");
        assertEquals(1.0, ball.getSpeedMultiplier(), 0.001, "Ball speed must restore to 1.0x after 5.0s");
        controller.shutdown();
    }

    @Test
    @DisplayName("Slow Motion cannot be re-activated while active or on cooldown, until cooldown finishes")
    void testSlowMotionCooldownConstraint() {
        SlowMotionAbility slowMo = state.getSlowMotionAbility();

        assertTrue(controller.triggerSlowMotion());
        assertFalse(controller.triggerSlowMotion(), "Cannot re-trigger while already active");

        // Expire 5.0s active window -> enters 10.0s cooldown
        slowMo.update(GameConfig.SLOW_MOTION_DURATION_SECONDS, state);
        assertFalse(slowMo.isActive());
        assertTrue(slowMo.getRemainingCooldown() > 0.0);
        assertFalse(controller.triggerSlowMotion(), "Cannot re-trigger while on cooldown");

        // Finish 10.0s cooldown -> ready again!
        slowMo.update(GameConfig.SLOW_MOTION_COOLDOWN_SECONDS, state);
        assertEquals(0.0, slowMo.getRemainingCooldown(), 0.001);
        assertTrue(controller.triggerSlowMotion(), "Must activate again after cooldown finishes");
        controller.shutdown();
    }
}
