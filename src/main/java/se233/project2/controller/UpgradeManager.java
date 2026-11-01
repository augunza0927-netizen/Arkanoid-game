package se233.project2.controller;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.state.GameState;
import se233.project2.model.upgrade.ExtraLifeUpgrade;
import se233.project2.model.upgrade.MultiBallUpgrade;
import se233.project2.model.upgrade.PaddleSizeUpgrade;
import se233.project2.model.upgrade.PaddleSpeedUpgrade;
import se233.project2.model.upgrade.PiercingBallUpgrade;
import se233.project2.model.upgrade.ScoreMultiplierUpgrade;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.util.GameLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the pool of roguelike upgrades, draws random distinct upgrade choices upon
 * stage completion, and applies the player's selected upgrade to {@link GameState}.
 */
public class UpgradeManager {

    private List<Upgrade> createFullPool() {
        List<Upgrade> pool = new ArrayList<>();
        pool.add(new PaddleSizeUpgrade());
        pool.add(new PaddleSpeedUpgrade());
        pool.add(new MultiBallUpgrade());
        pool.add(new ExtraLifeUpgrade());
        pool.add(new PiercingBallUpgrade());
        pool.add(new ScoreMultiplierUpgrade());
        return pool;
    }

    public List<Upgrade> getRandomChoices(int count) {
        if (count <= 0) {
            throw new InvalidGameStateException("Requested upgrade choice count must be positive: " + count);
        }
        List<Upgrade> pool = createFullPool();
        Collections.shuffle(pool);
        int limit = Math.min(count, pool.size());
        return new ArrayList<>(pool.subList(0, limit));
    }

    public void applyUpgrade(Upgrade selectedUpgrade, GameState state) {
        if (selectedUpgrade == null) {
            throw new InvalidGameStateException("Selected upgrade must not be null");
        }
        if (state == null) {
            throw new InvalidGameStateException("Target GameState must not be null");
        }
        GameLogger.logPlayerAction("Selected roguelike upgrade card: " + selectedUpgrade.getTitle());
        selectedUpgrade.apply(state);
    }
}
