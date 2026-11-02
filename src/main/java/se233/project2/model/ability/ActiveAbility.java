package se233.project2.model.ability;

import se233.project2.model.state.GameState;


public abstract class ActiveAbility {

    protected static final double EPSILON = 1e-7;

    protected final String name;
    protected final double maxDurationSeconds;
    protected double cooldownSeconds;
    protected double remainingDuration;
    protected double remainingCooldown;
    protected boolean active;

    protected ActiveAbility(String name, double maxDurationSeconds, double cooldownSeconds) {
        this.name = name;
        this.maxDurationSeconds = maxDurationSeconds;
        this.cooldownSeconds = cooldownSeconds;
    }

    public abstract boolean activate(GameState state);

    public abstract void update(double deltaTime, GameState state);

    public abstract void deactivate(GameState state);

    public boolean canActivate() {
        return !active && remainingCooldown <= EPSILON;
    }

    public void refreshCooldown() {
        this.remainingCooldown = 0.0;
    }

    public void reduceBaseCooldown(double seconds) {
        this.cooldownSeconds = Math.max(3.0, this.cooldownSeconds - Math.max(0.0, seconds));
        this.remainingCooldown = Math.min(this.remainingCooldown, this.cooldownSeconds);
    }

    public void reset() {
        this.active = false;
        this.remainingDuration = 0.0;
        this.remainingCooldown = 0.0;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public double getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public double getRemainingDuration() {
        return remainingDuration;
    }

    public double getRemainingCooldown() {
        return remainingCooldown;
    }
}
