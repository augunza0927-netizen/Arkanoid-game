package se233.project2.model.upgrade;

import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;

/**
 * Roguelike upgrade that grants +1 extra life (Card Sprite Index 0 - Heart).
 */
public class ExtraLifeUpgrade extends Upgrade {

    public ExtraLifeUpgrade() {
        super(UpgradeType.EXTRA_LIFE, "Vitality Core",
                "Grants +1 extra life for the remainder of the run (or +750 pts at max lives).", 0);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        if (state.getLives() < GameConfig.MAX_LIVES) {
            state.addLife(1);
        } else {
            state.getScoreManager().addPoints(750);
        }
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle() + " (Lives=" + state.getLives() + ")");
    }
}
