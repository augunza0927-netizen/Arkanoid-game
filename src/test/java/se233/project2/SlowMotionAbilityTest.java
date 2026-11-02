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

        //try 0.5
        double startY = ball.getY();
        ball.update(0.5);
        double normalDisplacement = startY - ball.getY();
        assertEquals(100.0, normalDisplacement, 0.01);

        //Test SHIFT
        controller.getInputHandler().pressKeyForTest(KeyCode.SHIFT);
        SlowMotionAbility slowMo = state.getSlowMotionAbility();

        assertTrue(slowMo.isActive(), "Slow Motion must become active on SHIFT key press");
        assertEquals(0.5, ball.getSpeedMultiplier(), 0.001, "Ball speed multiplier must be halved to 0.5x");
        assertEquals(5.0, slowMo.getRemainingDuration(), 0.001, "Slow Motion duration must start at 5.0 seconds");

        slowMo.update(4.9, state);
        assertTrue(slowMo.isActive(), "Slow Motion must still be active at 4.9s");
        assertEquals(0.5, ball.getSpeedMultiplier(), 0.001);

        slowMo.update(0.1, state);
        assertFalse(slowMo.isActive(), "Slow Motion must deactivate at 5.0s");
        assertEquals(1.0, ball.getSpeedMultiplier(), 0.001, "Ball speed must restore to 1.0x after 5.0s");
        controller.shutdown();
    }

}
