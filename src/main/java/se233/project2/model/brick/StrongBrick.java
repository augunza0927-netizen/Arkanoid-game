package se233.project2.model.brick;

/**
 * Multi-hit reinforced brick that transitions through progressive crack sprite states.
 * Uses Blue (col 1 for 2-HP) or Silver (col 4 for 3+-HP) sprites from Bricks_Damage.jpg.
 */
public class StrongBrick extends Brick {

    public StrongBrick(double x, double y, double width, double height, int durability) {
        this(x, y, width, height, durability, durability >= 3 ? 4 : 1);
    }

    public StrongBrick(double x, double y, double width, double height, int durability, int colorColumn) {
        super(x, y, width, height, BrickType.STRONG, Math.max(2, durability), 250, colorColumn);
    }

    @Override
    public void onHit(int damage) {
        takeDamage(damage);
    }

    @Override
    public double getCapsuleDropChance() {
        return 0.25;
    }
}
