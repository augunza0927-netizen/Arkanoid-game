package se233.project2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multi-level logging utility backed by Apache Log4j 2.
 * Logs player actions, game events, scoring calculations, warnings,
 * and exceptions at distinct log levels (TRACE, DEBUG, INFO, WARN, ERROR).
 */
public final class GameLogger {

    private static final Logger LOGGER = LogManager.getLogger(GameLogger.class);

    private GameLogger() {
        // Utility class
    }

    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    public static void logGameEvent(String message) {
        LOGGER.info("[EVENT] {}", message);
    }

    public static void logPlayerAction(String action) {
        LOGGER.debug("[ACTION] {}", action);
    }

    public static void logScoreChange(int basePoints, double multiplier, int awardedPoints, int totalScore) {
        LOGGER.trace("[SCORE-CALC] base={} x mult={} -> +{}", basePoints, multiplier, awardedPoints);
        LOGGER.info("[SCORE] +{} pts (Total Score: {})", awardedPoints, totalScore);
    }

    public static void logScoreChange(int delta, int totalScore) {
        LOGGER.info("[SCORE] +{} pts (Total Score: {})", delta, totalScore);
    }

    public static void logWarning(String message) {
        LOGGER.warn("[WARN] {}", message);
    }

    public static void logError(String message, Throwable throwable) {
        LOGGER.error("[ERROR] {}", message, throwable);
    }
}
