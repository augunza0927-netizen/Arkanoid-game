package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;


public interface Collidable {

    Rectangle2D getCollisionBounds();

    boolean intersects(Collidable other);
}
