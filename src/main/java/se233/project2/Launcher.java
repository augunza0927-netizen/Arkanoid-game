package se233.project2;

/**
 * Standalone entry point for packaging and running as an executable fat JAR
 * without requiring explicit JavaFX module-path flags on the command line.
 */
public final class Launcher {

    private Launcher() {
        // Entry point utility class
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
