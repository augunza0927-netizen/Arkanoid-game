package se233.project2.exception;

public class InvalidGameStateException extends GameException {

    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
//stgld