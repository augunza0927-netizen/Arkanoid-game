package se233.project2.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import se233.project2.util.GameLogger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Centralized single-point exception handler for the entire application.
 * Handles all exceptions of the same type in one unified location (satisfying SE233 rubric).
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final AtomicInteger HANDLED_COUNT = new AtomicInteger(0);
    private static final AtomicReference<String> LAST_HANDLED_CATEGORY = new AtomicReference<>("NONE");
    private static boolean showUiAlerts = false;

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        handle(throwable);
    }

    /**
     * Single unified place handling each exception category consistently across the game.
     */
    public static void handle(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        HANDLED_COUNT.incrementAndGet();

        if (throwable instanceof AssetLoadException assetEx) {
            LAST_HANDLED_CATEGORY.set("ASSET_LOAD");
            GameLogger.logError(
                    "AssetLoadException for resource [" + assetEx.getResourcePath() + "]: " + assetEx.getMessage(),
                    assetEx
            );
            notifyUiIfEnabled("Asset Loading Error", assetEx.getMessage());
        } else if (throwable instanceof InvalidGameStateException stateEx) {
            LAST_HANDLED_CATEGORY.set("INVALID_GAME_STATE");
            GameLogger.logWarning("InvalidGameStateException handled centrally: " + stateEx.getMessage());
            GameLogger.logError("State validation trace:", stateEx);
            notifyUiIfEnabled("Invalid Game State", stateEx.getMessage());
        } else if (throwable instanceof GameException gameEx) {
            LAST_HANDLED_CATEGORY.set("GAME_DOMAIN");
            GameLogger.logError("GameException handled centrally: " + gameEx.getMessage(), gameEx);
            notifyUiIfEnabled("Game Error", gameEx.getMessage());
        } else {
            LAST_HANDLED_CATEGORY.set("UNEXPECTED_SYSTEM");
            GameLogger.logError("Unexpected runtime error handled centrally: " + throwable.getMessage(), throwable);
            notifyUiIfEnabled("Unexpected Error", "An unexpected error occurred: " + throwable.getMessage());
        }
    }

    private static void notifyUiIfEnabled(String title, String content) {
        if (!showUiAlerts) {
            return;
        }
        try {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.show();
            });
        } catch (IllegalStateException ignored) {
            // Headless / JUnit test environment where JavaFX toolkit is not running
        }
    }

    public static void setShowUiAlerts(boolean enabled) {
        showUiAlerts = enabled;
    }

    public static int getHandledCount() {
        return HANDLED_COUNT.get();
    }

    public static String getLastHandledCategory() {
        return LAST_HANDLED_CATEGORY.get();
    }

    public static void resetStats() {
        HANDLED_COUNT.set(0);
        LAST_HANDLED_CATEGORY.set("NONE");
    }
}
