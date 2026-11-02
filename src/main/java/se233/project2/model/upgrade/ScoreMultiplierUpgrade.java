package se233.project2.model.upgrade;

import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;


public class ScoreMultiplierUpgrade extends Upgrade {

    public ScoreMultiplierUpgrade() {
        super(UpgradeType.SCORE_MULTIPLIER, "Laser Overdrive",
                "Boosts score multiplier by +0.5x and charges Dual Laser for the next stage!", 3);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        state.getScoreManager().increaseMultiplier(0.5);
        state.getDualLaserAbility().forceActivate(state, GameConfig.DUAL_LASER_DURATION_SECONDS);
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle()
                + " (Multiplier=" + state.getScoreManager().getScoreMultiplier() + "x)");
    }
}
