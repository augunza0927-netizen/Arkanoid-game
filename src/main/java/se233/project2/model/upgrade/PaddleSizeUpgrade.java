package se233.project2.model.upgrade;

import se233.project2.model.state.GameState;
import se233.project2.util.GameLogger;


public class PaddleSizeUpgrade extends Upgrade {

    public PaddleSizeUpgrade() {
        super(UpgradeType.PADDLE_SIZE, "Expand Paddle",
                "Expands paddle width by +25% for easier ball returns.", 1);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        if (state.getPaddle() != null) {
            state.getPaddle().expandWidth(1.25);
        }
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle());
    }
}
