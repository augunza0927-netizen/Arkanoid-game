package se233.project2.model.upgrade;

import se233.project2.model.state.GameState;
import se233.project2.util.GameLogger;


public class PaddleSpeedUpgrade extends Upgrade {

    public PaddleSpeedUpgrade() {
        super(UpgradeType.PADDLE_SPEED, "Chrono Thrusters",
                "Increases paddle speed by +22% and cuts Slow Motion cooldown by 2.0s.", 4);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        if (state.getPaddle() != null) {
            state.getPaddle().increaseSpeed(1.22);
        }
        state.getSlowMotionAbility().reduceBaseCooldown(2.0);
        state.getSlowMotionAbility().refreshCooldown();
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle());
    }
}
