package se233.project2.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Pure JavaFX keyboard event handler.
 * Tracks held movement keys and dispatches single-press action triggers
 * (ball launch, Dual Laser activation, 5-second Slow Motion button, pause, card selection).
 */
public class InputHandler {

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private Consumer<KeyCode> onKeyTriggered;

    public void attachToScene(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            boolean firstPress = activeKeys.add(code);
            if (firstPress && onKeyTriggered != null) {
                onKeyTriggered.accept(code);
            }
            if (code == KeyCode.SPACE || code == KeyCode.LEFT || code == KeyCode.RIGHT
                    || code == KeyCode.UP || code == KeyCode.DOWN) {
                event.consume();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            activeKeys.remove(event.getCode());
        });
    }

    public void setOnKeyTriggered(Consumer<KeyCode> onKeyTriggered) {
        this.onKeyTriggered = onKeyTriggered;
    }

    public boolean isMovingLeft() {
        return activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A);
    }

    public boolean isMovingRight() {
        return activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
    }

    public boolean isFiringHeld() {
        return activeKeys.contains(KeyCode.SPACE)
                || activeKeys.contains(KeyCode.W)
                || activeKeys.contains(KeyCode.UP);
    }

    public boolean isKeyPressed(KeyCode keyCode) {
        return activeKeys.contains(keyCode);
    }

    public void pressKeyForTest(KeyCode keyCode) {
        boolean first = activeKeys.add(keyCode);
        if (first && onKeyTriggered != null) {
            onKeyTriggered.accept(keyCode);
        }
    }

    public void releaseKeyForTest(KeyCode keyCode) {
        activeKeys.remove(keyCode);
    }

    public void clear() {
        activeKeys.clear();
    }
}
