package se233.project2.model.upgrade;

import se233.project2.model.entity.Ball;
import se233.project2.model.state.GameState;
import se233.project2.util.GameLogger;

/**
 * Roguelike upgrade allowing balls to pierce straight through destroyed bricks (Card Sprite Index 5 - Pierce).
 */
public class PiercingBallUpgrade extends Upgrade {

    public PiercingBallUpgrade() {
        super(UpgradeType.PIERCING_BALL, "Phantom Pierce",
                "Balls become Phantom Orbs that slice straight through destroyed bricks!", 5);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        state.setPersistentPiercing(true);
        for (Ball ball : state.getBalls()) {
            ball.setPiercing(true);
        }
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle());
    }
}
