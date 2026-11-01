package se233.project2.model.state;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.util.GameLogger;

/**
 * Manages player score, score multipliers (upgradable via roguelike cards),
 * and high-score tracking.
 */
public class ScoreManager {

    private int currentScore = 0;
    private int highScore = 0;
    private double scoreMultiplier = 1.0;

    public void addPoints(int basePoints) {
        awardPoints(basePoints);
    }

    public int awardPoints(int basePoints) {
        if (basePoints < 0) {
            throw new InvalidGameStateException("Cannot add negative base points: " + basePoints);
        }
        if (basePoints == 0) {
            return 0;
        }
        int awarded = (int) Math.round(basePoints * scoreMultiplier);
        currentScore += awarded;
        if (currentScore > highScore) {
            highScore = currentScore;
        }
        GameLogger.logScoreChange(basePoints, scoreMultiplier, awarded, currentScore);
        return awarded;
    }

    public void increaseMultiplier(double delta) {
        if (delta <= 0.0) {
            throw new InvalidGameStateException("Multiplier increment must be positive: " + delta);
        }
        this.scoreMultiplier = Math.round((this.scoreMultiplier + delta) * 100.0) / 100.0;
    }

    public void reset() {
        this.currentScore = 0;
        this.scoreMultiplier = 1.0;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getHighScore() {
        return highScore;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public void setScoreMultiplier(double scoreMultiplier) {
        if (scoreMultiplier <= 0.0) {
            throw new InvalidGameStateException("Score multiplier must be positive: " + scoreMultiplier);
        }
        this.scoreMultiplier = scoreMultiplier;
    }
}
