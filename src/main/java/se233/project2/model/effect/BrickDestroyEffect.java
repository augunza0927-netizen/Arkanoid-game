package se233.project2.model.effect;

public class BrickDestroyEffect extends VisualEffect {

    private final int colorColumn;

    public BrickDestroyEffect(double x, double y, double width, double height) {
        this(x, y, width, height, 0);
    }

    public BrickDestroyEffect(double x, double y, double width, double height, int colorColumn) {
        super(x, y, width, height, 0.36);
        this.colorColumn = Math.max(0, Math.min(5, colorColumn));
    }

    public int getColorColumn() {
        return colorColumn;
    }
}
