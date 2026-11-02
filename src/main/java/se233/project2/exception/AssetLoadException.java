package se233.project2.exception;


public class AssetLoadException extends GameException {

    private final String resourcePath;//Store rsp to assetmanager/SpriteSheet

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
