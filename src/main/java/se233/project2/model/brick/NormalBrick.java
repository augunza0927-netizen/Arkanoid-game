package se233.project2.model.brick;


public class NormalBrick extends Brick {

    public NormalBrick(double x, double y, double width, double height) {
        this(x, y, width, height, 0);
    }

    public NormalBrick(double x, double y, double width, double height, int colorColumn) {
        super(x, y, width, height, BrickType.NORMAL, 1, 100, colorColumn);
    }

    @Override
    public void onHit(int damage) {
        takeDamage(damage);
    }

    @Override
    public double getCapsuleDropChance() {
        return 0.14;
    }
}
