package se233.project2.exception;

/**
 * Custom base domain exception for the Arkanoid Roguelike project.
 * Satisfies the SE233 rubric requirement to define custom exception classes
 * and group related errors for centralized handling.
 */
public class GameException extends RuntimeException {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
