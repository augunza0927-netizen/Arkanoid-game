package se233.project2.controller;

import se233.project2.exception.InvalidGameStateException;
import se233.project2.model.brick.BonusBrick;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickType;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.brick.SpecialBrick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.state.StageConfig;
import se233.project2.util.GameConfig;

import java.util.ArrayList;
import java.util.List;

public class StageLoader {

    private static final int COLS = 8;
    private static final double BRICK_WIDTH = 54.0;
    private static final double BRICK_HEIGHT = 24.0;
    private static final double H_GAP = 5.0;
    private static final double V_GAP = 6.0;
    private static final double GRID_START_X = GameConfig.PLAY_LEFT + 19.5;
    private static final double GRID_START_Y = 82.0;

    public StageConfig loadStageConfig(int stageNumber) {
        if (stageNumber <= 0 || stageNumber > GameConfig.TOTAL_STAGES) {
            throw new InvalidGameStateException("Invalid stage number requested: " + stageNumber);
        }

        BrickType N = BrickType.NORMAL;
        BrickType S = BrickType.STRONG;
        BrickType B = BrickType.BONUS;
        BrickType X = BrickType.SPECIAL;

        return switch (stageNumber) {
            case 1 -> new StageConfig(1, "Neon Outpost", new BrickType[][]{
                    {N, N, B, N, N, B, N, N},
                    {N, N, N, N, N, N, N, N},
                    {null, S, N, X, X, N, S, null},
                    {N, N, N, N, N, N, N, N}
            }, 1.00);

            case 2 -> new StageConfig(2, "Fortress Grid", new BrickType[][]{
                    {S, B, S, N, N, S, B, S},
                    {N, N, X, N, N, X, N, N},
                    {S, N, S, B, B, S, N, S},
                    {N, N, N, S, S, N, N, N},
                    {null, N, N, N, N, N, N, null}
            }, 1.07);

            case 3 -> new StageConfig(3, "Twin Reactors", new BrickType[][]{
                    {null, B, S, null, null, S, B, null},
                    {S, X, S, N, N, S, X, S},
                    {N, S, B, N, N, B, S, N},
                    {N, N, N, X, X, N, N, N},
                    {S, N, N, S, S, N, N, S}
            }, 1.13);

            case 4 -> new StageConfig(4, "Cyber Citadel", new BrickType[][]{
                    {B, S, S, B, B, S, S, B},
                    {S, X, N, S, S, N, X, S},
                    {S, N, B, X, X, B, N, S},
                    {N, S, S, N, N, S, S, N},
                    {N, N, S, N, N, S, N, N},
                    {null, S, N, N, N, N, S, null}
            }, 1.19);

            default -> new StageConfig(5, "Core Finale", new BrickType[][]{
                    {B, B, S, B, B, S, B, B},
                    {S, X, S, X, X, S, X, S},
                    {S, S, B, S, S, B, S, S},
                    {N, X, S, B, B, S, X, N},
                    {S, N, S, S, S, S, N, S},
                    {N, S, N, X, X, N, S, N}
            }, 1.25);
        };
    }

    //Define state
    public List<Brick> instantiateBricks(StageConfig config) {
        if (config == null || config.getBrickGrid() == null) {
            throw new InvalidGameStateException("StageConfig or brickGrid must not be null");
        }

        List<Brick> bricks = new ArrayList<>();
        BrickType[][] grid = config.getBrickGrid();
        int stage = config.getStageNumber();

        for (int r = 0; r < grid.length; r++) {
            BrickType[] row = grid[r];
            for (int c = 0; c < Math.min(COLS, row.length); c++) {
                BrickType type = row[c];
                if (type == null) {
                    continue;
                }
                double bx = GRID_START_X + c * (BRICK_WIDTH + H_GAP);
                double by = GRID_START_Y + r * (BRICK_HEIGHT + V_GAP);

                Brick brick = switch (type) {
                    case NORMAL -> {
                        int colorCol = ((r + c) % 2 == 0) ? 0 : 2; // Red or Green
                        yield new NormalBrick(bx, by, BRICK_WIDTH, BRICK_HEIGHT, colorCol);
                    }
                    case STRONG -> {
                        int durability = (stage >= 4 && r <= 2) ? 3 : 2;
                        yield new StrongBrick(bx, by, BRICK_WIDTH, BRICK_HEIGHT, durability);
                    }
                    case BONUS -> new BonusBrick(bx, by, BRICK_WIDTH, BRICK_HEIGHT);
                    case SPECIAL -> new SpecialBrick(bx, by, BRICK_WIDTH, BRICK_HEIGHT);
                };
                bricks.add(brick);
            }
        }
        return bricks;
    }
}
