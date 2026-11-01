package se233.project2.model.entity;

/**
 * Interface for game objects that possess hit points and can take damage or be destroyed.
 */
public interface Damageable {

    void takeDamage(int amount);

    int getHitPoints();

    int getMaxHitPoints();

    boolean isDestroyed();
}
