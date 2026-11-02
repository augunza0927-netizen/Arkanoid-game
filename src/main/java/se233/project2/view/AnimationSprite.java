package se233.project2.view;

import javafx.scene.image.Image;

import java.util.List;


public class AnimationSprite {

    private final List<Image> frames;
    private final double frameDurationSeconds;
    private double elapsedTime = 0.0;
    private int currentFrameIndex = 0;

    public AnimationSprite(List<Image> frames, double frameDurationSeconds) {
        this.frames = frames;
        this.frameDurationSeconds = Math.max(0.02, frameDurationSeconds);
    }

    public void update(double deltaTime) {
        if (frames == null || frames.isEmpty()) {
            return;
        }
        elapsedTime += deltaTime;
        while (elapsedTime >= frameDurationSeconds) {
            elapsedTime -= frameDurationSeconds;
            currentFrameIndex = (currentFrameIndex + 1) % frames.size();
        }
    }

    public Image getFrameAtTime(double totalSeconds) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int idx = (int) (Math.abs(totalSeconds) / frameDurationSeconds) % frames.size();
        return frames.get(idx);
    }

    public Image getCurrentFrame() {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        return frames.get(currentFrameIndex % frames.size());
    }

    public List<Image> getFrames() {
        return frames;
    }

    public void reset() {
        this.elapsedTime = 0.0;
        this.currentFrameIndex = 0;
    }
}
