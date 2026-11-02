package se233.project2.model.effect;

import se233.project2.model.entity.GameObject;


public abstract class VisualEffect extends GameObject {

    protected double elapsedSeconds;
    protected final double durationSeconds;

    protected VisualEffect(double x, double y, double width, double height, double durationSeconds) {
        super(x, y, width, height);
        this.durationSeconds = Math.max(0.05, durationSeconds);
    }

    @Override
    public void update(double deltaTime) {
        elapsedSeconds += deltaTime;
        if (isFinished()) {
            active = false;
        }
    }

    public double getProgress() {
        return Math.min(1.0, Math.max(0.0, elapsedSeconds / durationSeconds));
    }

    public int getFrameIndex(int totalFrames) {
        if (totalFrames <= 1) {
            return 0;
        }
        int idx = (int) (getProgress() * totalFrames);
        return Math.min(totalFrames - 1, Math.max(0, idx));
    }

    public boolean isFinished() {
        return elapsedSeconds >= durationSeconds;
    }
}
