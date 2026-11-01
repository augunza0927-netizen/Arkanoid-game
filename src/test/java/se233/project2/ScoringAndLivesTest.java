package se233.project2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameController;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.entity.Ball;
import se233.project2.model.state.GameState;
import se233.project2.model.state.GameStatus;
import se233.project2.model.state.ScoreManager;
import se233.project2.util.GameConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for score calculation, multipliers, losing lives, multi-ball survival,
 * stage clear, and Game Over transitions (Rubric: Testing - 2 points).
 */
class ScoringAndLivesTest {

    private GameState state;
    private GameController controller;

    @BeforeEach
    void setUp() {
        state = new GameState();
        controller = new GameController(state);
        controller.startNewGame();
    }

    @Test
    @DisplayName("Destroying bricks increments score with active multiplier and tracks high score")
    void testScoreIncrementAndMultiplier() {
        ScoreManager sm = state.getScoreManager();
        assertEquals(0, sm.getCurrentScore());

        sm.addPoints(100);
        assertEquals(100, sm.getCurrentScore());

        sm.increaseMultiplier(0.5); // 1.5x
        sm.addPoints(200);          // +300
        assertEquals(400, sm.getCurrentScore());
        assertEquals(400, sm.getHighScore());

        sm.reset();
        assertEquals(0, sm.getCurrentScore());
        assertEquals(1.0, sm.getScoreMultiplier(), 0.001);
        assertEquals(400, sm.getHighScore(), "High score should persist across run resets");
        controller.shutdown();
    }

    @Test
    @DisplayName("Multi-ball loses one ball without losing a life; losing all balls decrements life and triggers Game Over at 0")
    void testLoseLifeMultiBallAndGameOver() {
        assertEquals(GameConfig.INITIAL_LIVES, state.getLives());

        // Launch primary ball and add a second active ball
        controller.launchDockedBalls();
        Ball secondBall = state.spawnExtraActiveBall(-25.0);
        assertEquals(2, state.getBalls().size());

        // Drop only the first ball below PLAY_BOTTOM -> life must NOT decrease yet
        Ball firstBall = state.getBalls().get(0);
        firstBall.setY(GameConfig.PLAY_BOTTOM + 20.0);
        secondBall.setY(300.0);
        controller.updateFrame(0.016);

        assertEquals(GameConfig.INITIAL_LIVES, state.getLives(), "Life should not be lost while another ball remains in play");
        assertEquals(1, state.getBalls().size());

        // Drop the remaining ball 3 times to reach 0 lives and GAME_OVER
        for (int expectedLives = GameConfig.INITIAL_LIVES - 1; expectedLives >= 0; expectedLives--) {
            controller.launchDockedBalls();
            for (Ball b : state.getBalls()) {
                b.setY(GameConfig.PLAY_BOTTOM + 25.0);
            }
            controller.updateFrame(0.016);
            assertEquals(expectedLives, state.getLives());
        }

        assertEquals(GameStatus.GAME_OVER, state.getStatus(), "Game status must become GAME_OVER at 0 lives");
        controller.shutdown();
    }

    @Test
    @DisplayName("Clearing all bricks in Stage 1 enters UPGRADE_SELECTION; clearing final stage enters VICTORY")
    void testStageClearAndVictoryTransitions() {
        state.getBricks().clear();
        NormalBrick lastBrick = new NormalBrick(200, 200, 54, 24);
        lastBrick.onHit(1);
        state.getBricks().add(lastBrick);

        controller.updateFrame(0.016);
        assertEquals(GameStatus.UPGRADE_SELECTION, state.getStatus(), "Clearing stage 1 should pause for upgrade selection");

        // Advance to final stage and clear all bricks -> VICTORY
        controller.loadStage(GameConfig.TOTAL_STAGES);
        state.getBricks().forEach(b -> b.onHit(10));
        controller.updateFrame(0.016);
        assertEquals(GameStatus.VICTORY, state.getStatus(), "Clearing final stage must trigger VICTORY");
        controller.shutdown();
    }
}
