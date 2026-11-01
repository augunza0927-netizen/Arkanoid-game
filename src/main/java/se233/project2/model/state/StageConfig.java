package se233.project2.model.state;

import se233.project2.model.brick.BrickType;

/**
 * Encapsulates configuration and brick layout blueprint for an individual stage.
 */
public class StageConfig {

    private final int stageNumber;
    private final String stageName;
    private final BrickType[][] brickGrid;
    private final double baseBallSpeedMultiplier;

    public StageConfig(int stageNumber, String stageName, BrickType[][] brickGrid, double baseBallSpeedMultiplier) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.brickGrid = brickGrid;
        this.baseBallSpeedMultiplier = baseBallSpeedMultiplier;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public BrickType[][] getBrickGrid() {
        return brickGrid;
    }

    public double getBaseBallSpeedMultiplier() {
        return baseBallSpeedMultiplier;
    }
}
