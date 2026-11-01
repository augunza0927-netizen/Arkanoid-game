package se233.project2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameController;
import se233.project2.controller.UpgradeManager;
import se233.project2.model.state.GameState;
import se233.project2.model.upgrade.ExtraLifeUpgrade;
import se233.project2.model.upgrade.MultiBallUpgrade;
import se233.project2.model.upgrade.PaddleSizeUpgrade;
import se233.project2.model.upgrade.PaddleSpeedUpgrade;
import se233.project2.model.upgrade.PiercingBallUpgrade;
import se233.project2.model.upgrade.ScoreMultiplierUpgrade;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.model.upgrade.UpgradeType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for roguelike upgrade selection and stat modifications
 * across stages (Rubric: Testing - 2 points).
 */
class UpgradeSystemTest {

    private GameState state;
    private UpgradeManager upgradeManager;

    @BeforeEach
    void setUp() {
        state = new GameState();
        state.resetForNewRun();
        upgradeManager = new UpgradeManager();
    }

    @Test
    @DisplayName("UpgradeManager draws distinct random upgrade cards")
    void testRandomUpgradeSelection() {
        List<Upgrade> choices = upgradeManager.getRandomChoices(3);
        assertEquals(3, choices.size());

        Set<UpgradeType> uniqueTypes = choices.stream()
                .map(Upgrade::getType)
                .collect(Collectors.toSet());
        assertEquals(3, uniqueTypes.size(), "All 3 presented upgrade choices must be distinct");
    }

    @Test
    @DisplayName("All 6 concrete roguelike Upgrade classes modify GameState and persist across stages")
    void testApplyAllSixUpgradesToGameState() {
        GameController controller = new GameController(state);
        controller.startNewGame();

        double initialWidth = state.getPaddle().getWidth();
        double initialSpeed = state.getPaddle().getMoveSpeed();
        int initialLives = state.getLives();

        // 1. Expand Paddle
        upgradeManager.applyUpgrade(new PaddleSizeUpgrade(), state);
        assertTrue(state.getPaddle().getWidth() > initialWidth);

        // 2. Chrono Thrusters (Paddle speed + reduced Slow-Mo cooldown)
        double initialSlowCooldown = state.getSlowMotionAbility().getCooldownSeconds();
        upgradeManager.applyUpgrade(new PaddleSpeedUpgrade(), state);
        assertTrue(state.getPaddle().getMoveSpeed() > initialSpeed);
        assertTrue(state.getSlowMotionAbility().getCooldownSeconds() < initialSlowCooldown);

        // 3. Extra Life
        upgradeManager.applyUpgrade(new ExtraLifeUpgrade(), state);
        assertEquals(initialLives + 1, state.getLives());

        // 4. Score Multiplier + Dual Laser Overdrive
        upgradeManager.applyUpgrade(new ScoreMultiplierUpgrade(), state);
        assertEquals(1.5, state.getScoreManager().getScoreMultiplier(), 0.001);
        assertTrue(state.getDualLaserAbility().isActive());

        // 5. Piercing Ball
        upgradeManager.applyUpgrade(new PiercingBallUpgrade(), state);
        assertTrue(state.isPersistentPiercing());
        assertTrue(state.getBalls().get(0).isPiercing());

        // 6. Multi-Ball Split + advance to Stage 2
        controller.selectUpgradeAndAdvance(new MultiBallUpgrade());
        assertEquals(2, state.getCurrentStageNumber(), "Selecting upgrade must advance to Stage 2");
        assertEquals(1, state.getExtraStartingBalls());
        assertTrue(state.getBalls().get(0).isPiercing(), "Piercing upgrade must persist into Stage 2");

        // Launching on Stage 2 spawns primary + 1 extra ball = 2 active balls
        controller.launchDockedBalls();
        assertEquals(2, state.getBalls().size(), "MultiBallUpgrade must launch extra ball on Stage 2");
        controller.shutdown();
    }
}
