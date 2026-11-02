package se233.project2.model.effect;


public class BounceEffect extends VisualEffect {

    public BounceEffect(double x, double y, double size) {
        super(x - size * 0.5, y - size * 0.5, size, size, 0.22);
    }
}
