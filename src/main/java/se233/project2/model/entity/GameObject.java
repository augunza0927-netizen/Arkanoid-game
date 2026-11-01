package se233.project2.model.entity;

import javafx.geometry.Rectangle2D;

/**
 * Abstract root base class for all spatial 2D entities in the play area.
 * Demonstrates OOP inheritance and encapsulation of spatial coordinates and dimensions.
 */
public abstract class GameObject {

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected boolean active;

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = true;
    }

    public abstract void update(double deltaTime);

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public double getCenterX() {
        return x + width * 0.5;
    }

    public double getCenterY() {
        return y + height * 0.5;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
