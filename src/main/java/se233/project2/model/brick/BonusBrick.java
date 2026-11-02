package se233.project2.model.brick;


public class BonusBrick extends Brick {

    public BonusBrick(double x, double y, double width, double height) {
        super(x, y, width, height, BrickType.BONUS, 1, 500, 5);
    }

    @Override
    public void onHit(int damage) {
        takeDamage(damage);
    }

    @Override
    public double getCapsuleDropChance() {
        return 0.72;
    }
}
