package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;

/**
 * Interface defining collision bounds and intersection checks using JavaFX Rectangle2D.
 */
public interface Collidable {

    Rectangle2D getCollisionBounds();

    boolean intersects(Collidable other);
}
