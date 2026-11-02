package se233.project2.model.entity;


public interface Movable {

    void move(double deltaTime);

    double getVelocityX();

    void setVelocityX(double velocityX);

    double getVelocityY();

    void setVelocityY(double velocityY);
}
