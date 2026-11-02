package se233.project2.util;


public final class GameConfig {

    public static final String GAME_TITLE = "CYBER ARKANOID: ROGUELIKE";

    // Window & Playfield Layout
    public static final double PLAYFIELD_WIDTH = 560.0;
    public static final double PLAYFIELD_HEIGHT = 750.0;
    public static final double HUD_WIDTH = 360.0;
    public static final double WINDOW_WIDTH = PLAYFIELD_WIDTH + HUD_WIDTH;
    public static final double WINDOW_HEIGHT = PLAYFIELD_HEIGHT;

    // Inner Play Area
    public static final double PLAY_LEFT = 27.0;
    public static final double PLAY_RIGHT = 533.0;
    public static final double PLAY_TOP = 27.0;
    public static final double PLAY_BOTTOM = 750.0;

    public static final int INITIAL_LIVES = 3;
    public static final int MAX_LIVES = 6;
    public static final int TOTAL_STAGES = 5;

    public static final double PADDLE_DEFAULT_WIDTH = 104.0;
    public static final double PADDLE_MIN_WIDTH = 72.0;
    public static final double PADDLE_MAX_WIDTH = 180.0;
    public static final double PADDLE_HEIGHT = 24.0;
    public static final double PADDLE_Y = 696.0;
    public static final double PADDLE_DEFAULT_SPEED = 430.0;
    public static final double PADDLE_MAX_SPEED = 740.0;

    public static final double BALL_SIZE = 18.0;
    public static final double BALL_BASE_SPEED = 350.0;
    public static final double BALL_MAX_SPEED = 560.0;

    public static final double SLOW_MOTION_SPEED_FACTOR = 0.5;
    public static final double SLOW_MOTION_DURATION_SECONDS = 5.0;
    public static final double SLOW_MOTION_COOLDOWN_SECONDS = 10.0;

    public static final double DUAL_LASER_DURATION_SECONDS = 8.0;
    public static final double DUAL_LASER_COOLDOWN_SECONDS = 14.0;
    public static final double DUAL_LASER_FIRE_INTERVAL = 0.26;
    public static final double LASER_WIDTH = 8.0;
    public static final double LASER_HEIGHT = 26.0;
    public static final double LASER_SPEED = 640.0;


    public static final double CAPSULE_WIDTH = 26.0;
    public static final double CAPSULE_HEIGHT = 44.0;
    public static final double CAPSULE_FALL_SPEED = 165.0;

    private GameConfig() {

    }
}
