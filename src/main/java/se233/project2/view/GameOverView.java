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


public class GameOverView extends StackPane {

    private final VBox modalBox = new VBox(18);
    private final Label bannerLabel = new Label("GAME OVER");
    private final Label subtitleLabel = new Label("All lives lost — Your cyber run has ended.");
    private final Label statsLabel = new Label();
    private final Button playAgainButton = new Button("↻  PLAY AGAIN");
    private final Button mainMenuButton = new Button("MAIN MENU");

    public GameOverView() {
        super();
        setPrefSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setStyle("-fx-background-color: rgba(6, 7, 18, 0.88);");
        setVisible(false);

        modalBox.setAlignment(Pos.CENTER);
        modalBox.setMaxWidth(540);
        modalBox.setMaxHeight(410);
        modalBox.setPadding(new Insets(32, 36, 32, 36));

        bannerLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 40));
        subtitleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setWrapText(true);

        statsLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        statsLabel.setTextFill(Color.web("#f8fafc"));
        statsLabel.setTextAlignment(TextAlignment.CENTER);
        statsLabel.setPadding(new Insets(14, 24, 14, 24));
        statsLabel.setStyle("-fx-background-color: rgba(12, 16, 34, 0.90);"
                + "-fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER);
        playAgainButton.getStyleClass().add("arcade-button");
        mainMenuButton.getStyleClass().add("arcade-button");
        playAgainButton.setPrefWidth(190);
        mainMenuButton.setPrefWidth(170);
        btnRow.getChildren().addAll(playAgainButton, mainMenuButton);

        modalBox.getChildren().addAll(bannerLabel, subtitleLabel, statsLabel, btnRow);
        getChildren().add(modalBox);
    }

    //cleary 5 stages=victory
    public void showResult(boolean victory, int finalScore, int stageReached) {
        String accent = victory ? "#ffd166" : "#ff4d6d";
        modalBox.setStyle("-fx-background-color: rgba(18, 20, 42, 0.96);"
                + "-fx-border-color: " + accent + "; -fx-border-width: 2.5;"
                + "-fx-border-radius: 14; -fx-background-radius: 14;");

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(accent));
        glow.setRadius(18);
        bannerLabel.setEffect(glow);

        if (victory) {//GameStatus.VICTORY
            bannerLabel.setText("VICTORY!");
            bannerLabel.setTextFill(Color.web("#ffd166"));
            subtitleLabel.setText("All " + GameConfig.TOTAL_STAGES + " Cyber Fortresses Cleared — Master Run Complete!");
            subtitleLabel.setTextFill(Color.web("#90e0ef"));
        } else {
            bannerLabel.setText("GAME OVER");
            bannerLabel.setTextFill(Color.web("#ff4d6d"));
            subtitleLabel.setText("Hull integrity depleted — Better roguelike synergies await next run!");
            subtitleLabel.setTextFill(Color.web("#fbcfe8"));
        }

        statsLabel.setText(String.format(
                "FINAL SCORE   :  %06d\nSTAGE REACHED :  STAGE %d / %d",
                finalScore, stageReached, GameConfig.TOTAL_STAGES
        ));
        setVisible(true);
    }

    public void setOnPlayAgain(Runnable action) {
        playAgainButton.setOnAction(e -> {
            setVisible(false);
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnMainMenu(Runnable action) {
        mainMenuButton.setOnAction(e -> {
            setVisible(false);
            if (action != null) {
                action.run();
            }
        });
    }
}
