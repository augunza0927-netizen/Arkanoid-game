package se233.project2.model.brick;


public class SpecialBrick extends Brick {

    public SpecialBrick(double x, double y, double width, double height) {
        super(x, y, width, height, BrickType.SPECIAL, 2, 350, 3);
    }

    @Override
    public void onHit(int damage) {
        takeDamage(damage);
    }

    @Override
    public double getCapsuleDropChance() {
        return 0.45;
    }

    @Override
    public boolean triggersAreaBlastOnDestroy() {
        return true;
    }
}
