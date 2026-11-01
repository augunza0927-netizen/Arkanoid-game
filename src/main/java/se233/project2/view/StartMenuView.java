package se233.project2.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se233.project2.util.GameConfig;

/**
 * Pure JavaFX Start Menu screen (Use Case 3.1: Start Game).
 */
public class StartMenuView extends StackPane {

    private final Button startButton = new Button("▶  START NEW RUN");
    private final Button soundButton = new Button("SOUND: ON");
    private final Button exitButton = new Button("EXIT GAME");

    public StartMenuView() {
        super();
        setPrefSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setStyle("-fx-background-color: radial-gradient(center 50% 35%, radius 75%, #1d1542, #090814);");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(680);
        card.setMaxHeight(610);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setStyle("-fx-background-color: rgba(16, 18, 38, 0.92);"
                + "-fx-border-color: #3be8ff; -fx-border-width: 2.5;"
                + "-fx-border-radius: 14; -fx-background-radius: 14;");

        DropShadow neonGlow = new DropShadow();
        neonGlow.setColor(Color.web("#3be8ff"));
        neonGlow.setRadius(22);

        Label titleLabel = new Label("CYBER ARKANOID");
        titleLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 46));
        titleLabel.setTextFill(Color.web("#64f4ff"));
        titleLabel.setEffect(neonGlow);

        Label subTitle = new Label("ROGUELIKE EDITION  •  SE 233 TERM PROJECT II");
        subTitle.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        subTitle.setTextFill(Color.web("#ff70a6"));

        // Feature Highlights Row
        HBox featuresRow = new HBox(12);
        featuresRow.setAlignment(Pos.CENTER);
        featuresRow.getChildren().addAll(
                createFeaturePill("5 ESCALATING STAGES", "Normal, Strong, Bonus & Explosive Bricks", "#ffd166"),
                createFeaturePill("ROGUELIKE DRAFT", "6 Run-Altering Card Upgrades & Capsules", "#c77dff"),
                createFeaturePill("TIME DILATION", "Press [SHIFT / S] for 0.5x Speed (5.0s)", "#48f0ff")
        );

        // Controls Guide Box
        VBox controlsBox = new VBox(6);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(12, 18, 12, 18));
        controlsBox.setStyle("-fx-background-color: rgba(10, 14, 28, 0.85);"
                + "-fx-border-color: #2f3e6e; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label controlsHeader = new Label("ARCADE CONTROLS");
        controlsHeader.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 13));
        controlsHeader.setTextFill(Color.web("#ffd166"));

        Label controlsBody = new Label(
                "[A / D] or [← / →]  Move Paddle        [SPACE]  Launch Ball / Fire Laser\n"
                + "[SHIFT] or [S]      Slow Motion (0.5x Ball Speed for 5 Seconds)\n"
                + "[F]                 Activate Dual Laser Special Attack    [P / ESC]  Pause"
        );
        controlsBody.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        controlsBody.setTextFill(Color.web("#d6e6ff"));
        controlsBody.setTextAlignment(TextAlignment.CENTER);
        controlsBox.getChildren().addAll(controlsHeader, controlsBody);

        // Buttons
        startButton.getStyleClass().add("arcade-button");
        startButton.setPrefWidth(280);
        startButton.setStyle("-fx-font-size: 18px;");

        HBox secondaryBtns = new HBox(14);
        secondaryBtns.setAlignment(Pos.CENTER);
        soundButton.getStyleClass().add("arcade-button");
        exitButton.getStyleClass().add("arcade-button");
        soundButton.setPrefWidth(155);
        exitButton.setPrefWidth(155);
        secondaryBtns.getChildren().addAll(soundButton, exitButton);

        card.getChildren().addAll(titleLabel, subTitle, featuresRow, controlsBox, startButton, secondaryBtns);
        getChildren().add(card);
    }

    private VBox createFeaturePill(String heading, String detail, String accentHex) {
        VBox pill = new VBox(5);
        pill.setAlignment(Pos.TOP_CENTER);
        pill.setPrefWidth(190);
        pill.setPadding(new Insets(10));
        pill.setStyle("-fx-background-color: rgba(25, 28, 56, 0.90);"
                + "-fx-border-color: " + accentHex + "; -fx-border-width: 1.5;"
                + "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label h = new Label(heading);
        h.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 11.5));
        h.setTextFill(Color.web(accentHex));

        Label d = new Label(detail);
        d.setFont(Font.font("Consolas", 10.5));
        d.setTextFill(Color.web("#cbd5e1"));
        d.setWrapText(true);
        d.setTextAlignment(TextAlignment.CENTER);
        pill.getChildren().addAll(h, d);
        return pill;
    }

    public void setOnStartGame(Runnable action) {
        startButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnToggleSound(Runnable action) {
        soundButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnExit(Runnable action) {
        exitButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void updateSoundButton(boolean muted) {
        soundButton.setText(muted ? "SOUND: OFF" : "SOUND: ON");
    }
}
