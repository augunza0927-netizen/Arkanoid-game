package se233.project2.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se233.project2.model.ability.DualLaserAbility;
import se233.project2.model.ability.SlowMotionAbility;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;

import java.util.List;

/**
 * Pure JavaFX heads-up display (HUD) panel showing live score, multiplier, remaining lives,
 * stage progress, Dual Laser special attack status, and the 5-second Slow Motion ability bar.
 */
public class HudPane extends HBox {

    private final Label stageTitleLabel = new Label("STAGE 1 / 5");
    private final Label stageSubLabel = new Label("NEON OUTPOST  •  Bricks: 0");
    private final Label scoreValueLabel = new Label("000000");
    private final Label highScoreLabel = new Label("HIGH: 000000");
    private final Label multiplierLabel = new Label("MULT: 1.0x");

    private final HBox heartsBox = new HBox(6);

    // Slow Motion Ability UI (Additional Stunning Feature)
    private final ImageView slowMoIconView = new ImageView();
    private final Label slowMoStateLabel = new Label("READY (0.5x / 5.0s)");
    private final ProgressBar slowMoBar = new ProgressBar(1.0);
    private final Button slowMoTriggerBtn = new Button("SLOW MOTION [SHIFT / S]");

    // Dual Laser Special Attack UI
    private final ImageView laserIconView = new ImageView();
    private final Label laserStateLabel = new Label("READY (8.0s)");
    private final ProgressBar laserBar = new ProgressBar(1.0);
    private final Button laserTriggerBtn = new Button("DUAL LASER [F]");

    // Applied Roguelike Upgrades display
    private final FlowPane upgradesFlow = new FlowPane(6, 6);

    private final Button muteButton = new Button("SOUND: ON");
    private final Button pauseButton = new Button("PAUSE [P]");
    private final Button menuButton = new Button("MENU");

    private AssetManager assetManager;
    private int lastRenderedLives = -1;
    private int lastRenderedUpgradeCount = -1;

    public HudPane() {
        super();
        setPrefSize(GameConfig.HUD_WIDTH, GameConfig.WINDOW_HEIGHT);
        setMinWidth(GameConfig.HUD_WIDTH);
        setMaxWidth(GameConfig.HUD_WIDTH);
        setStyle("-fx-background-color: linear-gradient(to bottom, #131129, #0b0a18);"
                + "-fx-border-color: #2f3b69; -fx-border-width: 0 0 0 2;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 16, 16, 16));
        content.setPrefWidth(GameConfig.HUD_WIDTH);
        HBox.setHgrow(content, Priority.ALWAYS);

        // 1. Header & Stage Card
        VBox stageBox = createPanelCard("#38d9ff");
        stageTitleLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 19));
        stageTitleLabel.setTextFill(Color.web("#64f4ff"));
        stageSubLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        stageSubLabel.setTextFill(Color.web("#b8c7e0"));
        stageBox.getChildren().addAll(stageTitleLabel, stageSubLabel);

        // 2. Score & Multiplier Card
        VBox scoreBox = createPanelCard("#ffd166");
        Label scoreTitle = createSectionCaption("SCORE TELEMETRY", "#ffd166");
        scoreValueLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 30));
        scoreValueLabel.setTextFill(Color.web("#fff3b0"));

        HBox subScoreRow = new HBox(12);
        highScoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        highScoreLabel.setTextFill(Color.web("#9fb3c8"));
        multiplierLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 13));
        multiplierLabel.setTextFill(Color.web("#ff70a6"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        subScoreRow.getChildren().addAll(highScoreLabel, spacer, multiplierLabel);
        scoreBox.getChildren().addAll(scoreTitle, scoreValueLabel, subScoreRow);

        // 3. Lives Card
        VBox livesBox = createPanelCard("#ff4d6d");
        Label livesTitle = createSectionCaption("HULL INTEGRITY (LIVES)", "#ff758f");
        heartsBox.setAlignment(Pos.CENTER_LEFT);
        livesBox.getChildren().addAll(livesTitle, heartsBox);

        // 4. Slow Motion Ability Card (New Feature)
        VBox slowMoBox = createPanelCard("#48f0ff");
        HBox slowHeader = new HBox(8);
        slowHeader.setAlignment(Pos.CENTER_LEFT);
        slowMoIconView.setFitWidth(24);
        slowMoIconView.setFitHeight(36);
        slowMoIconView.setPreserveRatio(true);
        VBox slowTitles = new VBox(2);
        Label slowTitle = createSectionCaption("NEW FEATURE: TIME DILATION", "#48f0ff");
        slowMoStateLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        slowMoStateLabel.setTextFill(Color.web("#e0fbfc"));
        slowTitles.getChildren().addAll(slowTitle, slowMoStateLabel);
        slowHeader.getChildren().addAll(slowMoIconView, slowTitles);

        slowMoBar.setMaxWidth(Double.MAX_VALUE);
        slowMoBar.setPrefHeight(12);
        slowMoBar.setStyle("-fx-accent: #48f0ff;");

        slowMoTriggerBtn.getStyleClass().add("ability-button");
        slowMoTriggerBtn.setMaxWidth(Double.MAX_VALUE);
        slowMoTriggerBtn.setFocusTraversable(false);
        slowMoBox.getChildren().addAll(slowHeader, slowMoBar, slowMoTriggerBtn);

        // 5. Dual Laser Special Attack Card
        VBox laserBox = createPanelCard("#ff6b6b");
        HBox laserHeader = new HBox(8);
        laserHeader.setAlignment(Pos.CENTER_LEFT);
        laserIconView.setFitWidth(24);
        laserIconView.setFitHeight(36);
        laserIconView.setPreserveRatio(true);
        VBox laserTitles = new VBox(2);
        Label laserTitle = createSectionCaption("SPECIAL ATTACK: DUAL LASER", "#ff8fa3");
        laserStateLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        laserStateLabel.setTextFill(Color.web("#ffe5ec"));
        laserTitles.getChildren().addAll(laserTitle, laserStateLabel);
        laserHeader.getChildren().addAll(laserIconView, laserTitles);

        laserBar.setMaxWidth(Double.MAX_VALUE);
        laserBar.setPrefHeight(12);
        laserBar.setStyle("-fx-accent: #ff5d8f;");

        laserTriggerBtn.getStyleClass().add("ability-button");
        laserTriggerBtn.setMaxWidth(Double.MAX_VALUE);
        laserTriggerBtn.setFocusTraversable(false);
        laserBox.getChildren().addAll(laserHeader, laserBar, laserTriggerBtn);

        // 6. Active Roguelike Upgrades Card
        VBox upgradesBox = createPanelCard("#9b5de5");
        Label upgTitle = createSectionCaption("ROGUELIKE UPGRADES", "#c77dff");
        upgradesFlow.setPrefWrapLength(300);
        Label noneLabel = createUpgradeTag("None yet");
        upgradesFlow.getChildren().add(noneLabel);
        upgradesBox.getChildren().addAll(upgTitle, upgradesFlow);

        // 7. Controls Legend & Footer Buttons
        Label controlsHint = new Label(
                "Move: [A/D] or [←/→]  |  Launch/Fire: [SPACE / W]\n"
                + "Slow-Mo (0.5x 5s): [SHIFT / S]  |  Laser: [F]"
        );
        controlsHint.setFont(Font.font("Consolas", 10.5));
        controlsHint.setTextFill(Color.web("#8fa3bf"));

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        HBox actionRow = new HBox(8);
        for (Button btn : List.of(muteButton, pauseButton, menuButton)) {
            btn.getStyleClass().add("ability-button");
            btn.setFocusTraversable(false);
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
        }
        actionRow.getChildren().addAll(muteButton, pauseButton, menuButton);

        content.getChildren().addAll(
                stageBox, scoreBox, livesBox, slowMoBox, laserBox, upgradesBox,
                bottomSpacer, controlsHint, actionRow
        );
        getChildren().add(content);
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        if (assetManager != null && assetManager.isLoaded()) {
            slowMoIconView.setImage(assetManager.getCapsuleSprite(4));
            laserIconView.setImage(assetManager.getCapsuleSprite(3));
        }
    }

    public void setOnTriggerSlowMotion(Runnable action) {
        slowMoTriggerBtn.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnTriggerDualLaser(Runnable action) {
        laserTriggerBtn.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnToggleMute(Runnable action) {
        muteButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnTogglePause(Runnable action) {
        pauseButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnReturnToMenu(Runnable action) {
        menuButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void updateMuteLabel(boolean muted) {
        muteButton.setText(muted ? "SOUND: OFF" : "SOUND: ON");
    }

    public void refresh(GameState state) {
        if (state == null) {
            return;
        }

        stageTitleLabel.setText(String.format("STAGE %d / %d", state.getCurrentStageNumber(), GameConfig.TOTAL_STAGES));
        stageSubLabel.setText(String.format("%s  •  Bricks: %d",
                state.getCurrentStageName().toUpperCase(), state.getRemainingBrickCount()));

        scoreValueLabel.setText(String.format("%06d", state.getScoreManager().getCurrentScore()));
        highScoreLabel.setText(String.format("HIGH: %06d", state.getScoreManager().getHighScore()));
        multiplierLabel.setText(String.format("MULT: %.1fx", state.getScoreManager().getScoreMultiplier()));

        refreshHearts(state.getLives());
        refreshSlowMotion(state.getSlowMotionAbility());
        refreshDualLaser(state.getDualLaserAbility());
        refreshUpgradesList(state.getAppliedUpgrades());
    }

    private void refreshHearts(int lives) {
        if (lives == lastRenderedLives) {
            return;
        }
        lastRenderedLives = lives;
        heartsBox.getChildren().clear();

        Image fullHeart = assetManager != null ? assetManager.getHeartFullSprite() : null;
        Image emptyHeart = assetManager != null ? assetManager.getHeartEmptySprite() : null;

        int slots = Math.max(GameConfig.INITIAL_LIVES, lives);
        for (int i = 0; i < slots; i++) {
            boolean filled = i < lives;
            if (fullHeart != null && emptyHeart != null) {
                ImageView iv = new ImageView(filled ? fullHeart : emptyHeart);
                iv.setFitWidth(26);
                iv.setFitHeight(26);
                iv.setPreserveRatio(true);
                heartsBox.getChildren().add(iv);
            } else {
                Label heartLbl = new Label(filled ? "♥" : "♡");
                heartLbl.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
                heartLbl.setTextFill(filled ? Color.web("#ff4d6d") : Color.web("#4a4e69"));
                heartsBox.getChildren().add(heartLbl);
            }
        }
    }

    private void refreshSlowMotion(SlowMotionAbility slowMo) {
        if (slowMo.isActive()) {
            double rem = slowMo.getRemainingDuration();
            slowMoStateLabel.setText(String.format("ACTIVE 0.5x SPEED (%.1fs)", rem));
            slowMoStateLabel.setTextFill(Color.web("#7ef9ff"));
            slowMoBar.setProgress(Math.max(0.0, Math.min(1.0, rem / slowMo.getMaxDurationSeconds())));
            slowMoTriggerBtn.setDisable(true);
        } else if (slowMo.getRemainingCooldown() > 0.01) {
            double cd = slowMo.getRemainingCooldown();
            slowMoStateLabel.setText(String.format("RECHARGING (%.1fs)", cd));
            slowMoStateLabel.setTextFill(Color.web("#9fb3c8"));
            double progress = 1.0 - (cd / slowMo.getCooldownSeconds());
            slowMoBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            slowMoTriggerBtn.setDisable(true);
        } else {
            slowMoStateLabel.setText("READY  •  0.5x BALL SPEED (5.0s)");
            slowMoStateLabel.setTextFill(Color.web("#64dfdf"));
            slowMoBar.setProgress(1.0);
            slowMoTriggerBtn.setDisable(false);
        }
    }

    private void refreshDualLaser(DualLaserAbility laser) {
        if (laser.isActive()) {
            double rem = laser.getRemainingDuration();
            laserStateLabel.setText(String.format("CANNONS ARMED (%.1fs)", rem));
            laserStateLabel.setTextFill(Color.web("#ffccd5"));
            laserBar.setProgress(Math.max(0.0, Math.min(1.0, rem / laser.getMaxDurationSeconds())));
            laserTriggerBtn.setDisable(true);
        } else if (laser.getRemainingCooldown() > 0.01) {
            double cd = laser.getRemainingCooldown();
            laserStateLabel.setText(String.format("COOLDOWN (%.1fs)", cd));
            laserStateLabel.setTextFill(Color.web("#9fb3c8"));
            double progress = 1.0 - (cd / laser.getCooldownSeconds());
            laserBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            laserTriggerBtn.setDisable(true);
        } else {
            laserStateLabel.setText("READY  •  TWIN CANNONS (8.0s)");
            laserStateLabel.setTextFill(Color.web("#ff8fa3"));
            laserBar.setProgress(1.0);
            laserTriggerBtn.setDisable(false);
        }
    }

    private void refreshUpgradesList(List<String> upgrades) {
        if (upgrades.size() == lastRenderedUpgradeCount) {
            return;
        }
        lastRenderedUpgradeCount = upgrades.size();
        upgradesFlow.getChildren().clear();
        if (upgrades.isEmpty()) {
            upgradesFlow.getChildren().add(createUpgradeTag("Clear stage to draft cards"));
            return;
        }
        for (String upg : upgrades) {
            upgradesFlow.getChildren().add(createUpgradeTag(upg));
        }
    }

    private VBox createPanelCard(String borderHex) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: rgba(24, 27, 52, 0.90);"
                + "-fx-border-color: " + borderHex + ";"
                + "-fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7;");
        return box;
    }

    private Label createSectionCaption(String text, String colorHex) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(colorHex));
        return lbl;
    }

    private Label createUpgradeTag(String text) {
        Label tag = new Label(text);
        tag.setFont(Font.font("Consolas", FontWeight.BOLD, 10.5));
        tag.setTextFill(Color.web("#f3e8ff"));
        tag.setPadding(new Insets(3, 7, 3, 7));
        tag.setStyle("-fx-background-color: #2c1e4a; -fx-border-color: #9b5de5;"
                + "-fx-border-radius: 4; -fx-background-radius: 4;");
        return tag;
    }
}
