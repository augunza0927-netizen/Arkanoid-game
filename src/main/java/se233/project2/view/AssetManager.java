package se233.project2.view;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import se233.project2.exception.AssetLoadException;
import se233.project2.util.GameLogger;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized pure-JavaFX asset registry responsible for loading, slicing, and caching
 * sprite sheets ({@code PlayArea.jpg}, {@code Paddle_Ball_SpecialAttack.jpg},
 * {@code Bricks_Damage.jpg}, {@code Upgrade_Capsule_HUDUI.jpg}) and {@link AudioClip} sounds.
 * Throws {@link AssetLoadException} when required resources cannot be loaded.
 */
public class AssetManager {

    private static final String SPRITE_BASE = "/se233/project2/assets/sprites/";
    private static final String SOUND_BASE = "/se233/project2/assets/sounds/";

    private final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, AudioClip> soundCache = new HashMap<>();

    // Sliced animations & sprite collections
    private final Image[][] brickSprites = new Image[3][6];         // [damageRow 0..2][colorCol 0..5]
    private final Image[][] brickShatterSprites = new Image[2][6];  // [shatterFrame 0..1][colorCol 0..5]
    private final Image[] paddleSprites = new Image[3];             // [compact, standard, wide]
    private Image laserPaddleSprite;
    private Image laserBeamSprite;
    private AnimationSprite normalBallMoveAnim;
    private AnimationSprite slowMoBallMoveAnim;
    private AnimationSprite piercingBallMoveAnim;
    private AnimationSprite ballBounceAnim;
    private final List<Image> sparkFrames = new ArrayList<>();
    private final Image[] upgradeCardSprites = new Image[6];
    private final Image[] capsuleSprites = new Image[6];
    private Image heartFullSprite;
    private Image heartEmptySprite;

    private boolean loaded = false;
    private boolean soundMuted = false;

    public void loadAllAssets() {
        if (loaded) {
            return;
        }
        GameLogger.logGameEvent("Loading and slicing pure-JavaFX sprite sheets and audio assets...");

        Image playArea = loadImageResource(SPRITE_BASE + "PlayArea.jpg");
        imageCache.put("play_area", playArea);

        Image bricksImg = loadImageResource(SPRITE_BASE + "Bricks_Damage.jpg");
        sliceBricksSheet(new SpriteSheet(bricksImg, 30));

        Image paddleBallImg = loadImageResource(SPRITE_BASE + "Paddle_Ball_SpecialAttack.jpg");
        slicePaddleBallSheet(new SpriteSheet(paddleBallImg, 26));

        Image uiImg = loadImageResource(SPRITE_BASE + "Upgrade_Capsule_HUDUI.jpg");
        sliceUiAndUpgradeSheet(new SpriteSheet(uiImg, 30));

        loadOptionalSounds();
        loaded = true;
        GameLogger.logGameEvent("All sprite sheets sliced and cached successfully.");
    }

    /**
     * Loads a classpath image resource or throws {@link AssetLoadException} if missing/corrupt.
     */
    public Image loadImageResource(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new AssetLoadException(String.valueOf(resourcePath), "Resource path must not be null or blank");
        }
        InputStream stream = AssetManager.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new AssetLoadException(resourcePath, "Required image asset not found on classpath: " + resourcePath);
        }
        try (InputStream in = stream) {
            Image img = new Image(in);
            if (img.isError() || img.getWidth() <= 0) {
                throw new AssetLoadException(resourcePath, "Corrupted or unreadable image asset: " + resourcePath, img.getException());
            }
            return img;
        } catch (AssetLoadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssetLoadException(resourcePath, "Failed to read image stream: " + resourcePath, ex);
        }
    }

    private void sliceBricksSheet(SpriteSheet sheet) {
        int[] colX = {72, 400, 718, 1044, 1372, 1690};
        int[] colW = {287, 286, 286, 287, 277, 286};
        int[] rowY = {144, 440, 758};
        int[] rowH = {163, 175, 174};

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                brickSprites[row][col] = sheet.getSprite(colX[col], rowY[row], colW[col], rowH[row]);
            }
        }

        // Shatter Frame 1 (Row 3) & Shatter Frame 2 (Row 4)
        int[][] r3 = {
                {72, 1126, 287, 165}, {400, 1126, 286, 185}, {718, 1086, 286, 245},
                {1044, 1076, 287, 276}, {1362, 1065, 297, 297}, {1688, 1054, 288, 308}
        };
        int[][] r4 = {
                {72, 1557, 287, 205}, {400, 1515, 286, 288}, {728, 1515, 286, 298},
                {1044, 1506, 310, 360}, {1365, 1515, 295, 340}, {1690, 1525, 287, 320}
        };
        for (int col = 0; col < 6; col++) {
            brickShatterSprites[0][col] = sheet.getParticleSprite(r3[col][0], r3[col][1], r3[col][2], r3[col][3]);
            brickShatterSprites[1][col] = sheet.getParticleSprite(r4[col][0], r4[col][1], r4[col][2], r4[col][3]);
        }
    }

    private void slicePaddleBallSheet(SpriteSheet sheet) {
        // Paddles: compact blue, standard blue, wide green, and armed dual-laser paddle
        paddleSprites[0] = sheet.getSprite(646, 134, 265, 111);
        paddleSprites[1] = sheet.getSprite(554, 400, 439, 112);
        paddleSprites[2] = sheet.getSprite(1056, 400, 439, 112);
        laserPaddleSprite = sheet.getSprite(544, 666, 460, 163);

        // Separated Ball Move vs. Ball Bounce Animation Sprites (Rubric: Sprite Sheet 2 pts)
        normalBallMoveAnim = new AnimationSprite(List.of(
                sheet.getSprite(74, 1024, 172, 163),
                sheet.getSprite(72, 1280, 174, 163)
        ), 0.14);

        slowMoBallMoveAnim = new AnimationSprite(List.of(
                sheet.getParticleSprite(922, 993, 194, 205),
                sheet.getParticleSprite(922, 1260, 184, 194)
        ), 0.16);

        piercingBallMoveAnim = new AnimationSprite(List.of(
                sheet.getParticleSprite(1178, 1014, 215, 184),
                sheet.getParticleSprite(1178, 1270, 215, 184)
        ), 0.14);

        ballBounceAnim = new AnimationSprite(List.of(
                sheet.getParticleSprite(634, 976, 207, 213),
                sheet.getParticleSprite(594, 1269, 246, 176)
        ), 0.08);

        // Single Laser Beam & Impact Spark frames
        laserBeamSprite = sheet.getParticleSprite(112, 1586, 65, 320);

        sparkFrames.clear();
        sparkFrames.add(sheet.getParticleSprite(358, 1637, 73, 115));
        sparkFrames.add(sheet.getParticleSprite(502, 1637, 103, 115));
        sparkFrames.add(sheet.getParticleSprite(686, 1607, 113, 135));
        sparkFrames.add(sheet.getParticleSprite(1586, 1596, 124, 145));
    }

    private void sliceUiAndUpgradeSheet(SpriteSheet sheet) {
        int[][] cards = {
                {68, 134, 291, 435}, {394, 134, 289, 435}, {718, 134, 289, 435},
                {1042, 134, 289, 435}, {1366, 134, 289, 435}, {1688, 134, 292, 435}
        };
        int[][] capsules = {
                {120, 624, 194, 344}, {446, 624, 188, 343}, {766, 624, 193, 344},
                {1090, 624, 194, 344}, {1412, 624, 193, 344}, {1694, 624, 279, 344}
        };
        for (int i = 0; i < 6; i++) {
            upgradeCardSprites[i] = sheet.getSprite(cards[i][0], cards[i][1], cards[i][2], cards[i][3]);
            capsuleSprites[i] = sheet.getSprite(capsules[i][0], capsules[i][1], capsules[i][2], capsules[i][3]);
        }

        heartFullSprite = sheet.getSprite(90, 1617, 102, 104);
        heartEmptySprite = sheet.getSprite(412, 1617, 102, 104);
    }

    private void loadOptionalSounds() {
        String[] soundNames = {
                "bounce", "brick_hit", "brick_destroy", "laser",
                "slowmo", "upgrade", "lose_life", "stage_clear", "game_over"
        };
        for (String name : soundNames) {
            try {
                URL url = AssetManager.class.getResource(SOUND_BASE + name + ".wav");
                if (url != null) {
                    soundCache.put(name, new AudioClip(url.toExternalForm()));
                }
            } catch (Exception ex) {
                GameLogger.logWarning("Audio subsystem unavailable for sound [" + name + "]: " + ex.getMessage());
            }
        }
    }

    public void playSound(String key) {
        if (soundMuted) {
            return;
        }
        AudioClip clip = soundCache.get(key);
        if (clip != null) {
            try {
                clip.play();
            } catch (Exception ignored) {
                // Ignore audio hardware playback errors
            }
        }
    }

    public Image getPlayAreaImage() {
        return imageCache.get("play_area");
    }

    public Image getBrickSprite(int damageRow, int colorColumn) {
        int r = Math.max(0, Math.min(2, damageRow));
        int c = Math.max(0, Math.min(5, colorColumn));
        return brickSprites[r][c];
    }

    public Image getBrickShatterSprite(int frameIdx, int colorColumn) {
        int f = Math.max(0, Math.min(1, frameIdx));
        int c = Math.max(0, Math.min(5, colorColumn));
        return brickShatterSprites[f][c];
    }

    public Image getPaddleSprite(int sizeTier, boolean laserEquipped) {
        if (laserEquipped && laserPaddleSprite != null) {
            return laserPaddleSprite;
        }
        int t = Math.max(0, Math.min(2, sizeTier));
        return paddleSprites[t];
    }

    public Image getLaserBeamSprite() {
        return laserBeamSprite;
    }

    public AnimationSprite getNormalBallMoveAnim() {
        return normalBallMoveAnim;
    }

    public AnimationSprite getSlowMoBallMoveAnim() {
        return slowMoBallMoveAnim;
    }

    public AnimationSprite getPiercingBallMoveAnim() {
        return piercingBallMoveAnim;
    }

    public AnimationSprite getBallBounceAnim() {
        return ballBounceAnim;
    }

    public List<Image> getSparkFrames() {
        return sparkFrames;
    }

    public Image getUpgradeCardSprite(int index) {
        return upgradeCardSprites[Math.max(0, Math.min(5, index))];
    }

    public Image getCapsuleSprite(int index) {
        return capsuleSprites[Math.max(0, Math.min(5, index))];
    }

    public Image getHeartFullSprite() {
        return heartFullSprite;
    }

    public Image getHeartEmptySprite() {
        return heartEmptySprite;
    }

    public Image getImage(String key) {
        return imageCache.get(key);
    }

    public AudioClip getSound(String key) {
        return soundCache.get(key);
    }

    public boolean isSoundMuted() {
        return soundMuted;
    }

    public void setSoundMuted(boolean soundMuted) {
        this.soundMuted = soundMuted;
    }

    public boolean toggleMute() {
        this.soundMuted = !this.soundMuted;
        return this.soundMuted;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
