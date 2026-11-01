package se233.project2.exception;

/**
 * Custom exception thrown when an illegal game state transition, invalid stage
 * configuration, or invalid upgrade/ability operation occurs.
 */
public class InvalidGameStateException extends GameException {

    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
