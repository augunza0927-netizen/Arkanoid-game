package se233.project2.exception;

/**
 * Custom exception thrown when sprite sheets, audio files, or stage layout
 * resources are missing, corrupted, or unreadable.
 */
public class AssetLoadException extends GameException {

    private final String resourcePath;

    public AssetLoadException(String resourcePath, String message) {
        super(message);
        this.resourcePath = resourcePath;
    }

    public AssetLoadException(String resourcePath, String message, Throwable cause) {
        super(message, cause);
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
