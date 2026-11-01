package se233.project2.model.state;

/**
 * High-level lifecycle states of a game run.
 */
public enum GameStatus {
    MENU,
    PLAYING,
    PAUSED,
    STAGE_CLEAR,
    UPGRADE_SELECTION,
    GAME_OVER,
    VICTORY
}
