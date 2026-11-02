package se233.project2.model.effect;


public class FloatingScoreText extends VisualEffect {

    private final String text;
    private final String colorHex;

    public FloatingScoreText(double x, double y, String text) {
        this(x, y, text, "#ffe66d");
    }

    public FloatingScoreText(double x, double y, String text, String colorHex) {
        super(x, y, 0, 0, 0.85);
        this.text = text;
        this.colorHex = colorHex != null ? colorHex : "#ffe66d";
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        this.y -= 38.0 * deltaTime;
    }

    public String getText() {
        return text;
    }

    public String getColorHex() {
        return colorHex;
    }
}
