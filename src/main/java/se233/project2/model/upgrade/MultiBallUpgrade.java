package se233.project2.model.upgrade;

import se233.project2.model.state.GameState;
import se233.project2.util.GameLogger;


public class MultiBallUpgrade extends Upgrade {

    public MultiBallUpgrade() {
        super(UpgradeType.MULTI_BALL, "Multi-Ball Split",
                "Launches +1 extra ball in every stage and splits an active ball now.", 2);
    }

    @Override
    public void apply(GameState state) {
        validateState(state);
        state.incrementExtraStartingBalls();
        boolean hasUndockedBall = state.getBalls().stream().anyMatch(b -> !b.isDockedToPaddle());
        if (hasUndockedBall) {
            state.spawnExtraActiveBall(-26.0);
        }
        state.recordUpgrade(getTitle());
        GameLogger.logGameEvent("Applied Roguelike Upgrade: " + getTitle()
                + " (ExtraStartingBalls=" + state.getExtraStartingBalls() + ")");
    }
}
