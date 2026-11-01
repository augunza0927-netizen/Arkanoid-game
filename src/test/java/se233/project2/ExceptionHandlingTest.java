package se233.project2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se233.project2.controller.StageLoader;
import se233.project2.controller.UpgradeManager;
import se233.project2.exception.AssetLoadException;
import se233.project2.exception.GameException;
import se233.project2.exception.GlobalExceptionHandler;
import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.state.ScoreManager;
import se233.project2.view.AssetManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exception classes and centralized single-place handling
 * in {@link GlobalExceptionHandler} (Rubric: Exception Handling - 1 point).
 */
class ExceptionHandlingTest {

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler.resetStats();
        GlobalExceptionHandler.setShowUiAlerts(false);
    }

    @Test
    @DisplayName("Missing asset throws AssetLoadException and invalid state operations throw InvalidGameStateException")
    void testCustomExceptionsThrownConsistently() {
        AssetManager assetManager = new AssetManager();
        AssetLoadException assetEx = assertThrows(
                AssetLoadException.class,
                () -> assetManager.loadImageResource("/se233/project2/assets/sprites/NonExistentSprite.jpg")
        );
        assertEquals("/se233/project2/assets/sprites/NonExistentSprite.jpg", assetEx.getResourcePath());
        assertInstanceOf(GameException.class, assetEx);

        StageLoader stageLoader = new StageLoader();
        assertThrows(InvalidGameStateException.class, () -> stageLoader.loadStageConfig(0));
        assertThrows(InvalidGameStateException.class, () -> stageLoader.loadStageConfig(99));

        ScoreManager scoreManager = new ScoreManager();
        assertThrows(InvalidGameStateException.class, () -> scoreManager.addPoints(-10));

        UpgradeManager upgradeManager = new UpgradeManager();
        assertThrows(InvalidGameStateException.class, () -> upgradeManager.getRandomChoices(0));
        assertThrows(InvalidGameStateException.class, () -> upgradeManager.applyUpgrade(null, null));
    }

    @Test
    @DisplayName("GlobalExceptionHandler handles each custom exception type centrally in one place")
    void testCentralizedGlobalExceptionHandler() {
        GlobalExceptionHandler.handle(new AssetLoadException("/missing.png", "Missing test sprite"));
        assertEquals(1, GlobalExceptionHandler.getHandledCount());
        assertEquals("ASSET_LOAD", GlobalExceptionHandler.getLastHandledCategory());

        GlobalExceptionHandler.handle(new InvalidGameStateException("Illegal test transition"));
        assertEquals(2, GlobalExceptionHandler.getHandledCount());
        assertEquals("INVALID_GAME_STATE", GlobalExceptionHandler.getLastHandledCategory());

        GlobalExceptionHandler.handle(new GameException("Generic domain error"));
        assertEquals(3, GlobalExceptionHandler.getHandledCount());
        assertEquals("GAME_DOMAIN", GlobalExceptionHandler.getLastHandledCategory());

        GlobalExceptionHandler.handle(new RuntimeException("Unexpected test fault"));
        assertEquals(4, GlobalExceptionHandler.getHandledCount());
        assertEquals("UNEXPECTED_SYSTEM", GlobalExceptionHandler.getLastHandledCategory());
    }
}
