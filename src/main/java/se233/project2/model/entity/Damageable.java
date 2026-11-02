package se233.project2.model.entity;


public interface Damageable {

    void takeDamage(int amount);

    int getHitPoints();

    int getMaxHitPoints();

    boolean isDestroyed();
}
