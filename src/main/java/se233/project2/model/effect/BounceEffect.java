package se233.project2.model.effect;

/**
 * Visual spark/impact animation triggered when a ball or laser hits walls, paddle, or bricks.
 * Supports separation of move and ball bouncing actions per the Sprite Sheet rubric.
 */
public class BounceEffect extends VisualEffect {

    public BounceEffect(double x, double y, double size) {
        super(x - size * 0.5, y - size * 0.5, size, size, 0.22);
    }
}
