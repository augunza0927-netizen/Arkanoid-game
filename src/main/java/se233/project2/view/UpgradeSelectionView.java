package se233.project2.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.util.GameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class UpgradeSelectionView extends StackPane {

    private final Label headerLabel = new Label("STAGE CLEARED!");
    private final Label subHeaderLabel = new Label("SELECT ONE ROGUELIKE UPGRADE FOR THE NEXT STAGE  (Click or press [1] [2] [3])");
    private final HBox cardsContainer = new HBox(20);

    private AssetManager assetManager;
    private List<Upgrade> currentChoices = new ArrayList<>();
    private Consumer<Upgrade> selectionCallback;

    public UpgradeSelectionView() {
        super();
        setPrefSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setStyle("-fx-background-color: rgba(7, 8, 20, 0.88);");
        setVisible(false);

        VBox modal = new VBox(18);
        modal.setAlignment(Pos.CENTER);
        modal.setMaxWidth(840);
        modal.setMaxHeight(580);
        modal.setPadding(new Insets(28, 32, 28, 32));
        modal.setStyle("-fx-background-color: rgba(18, 20, 44, 0.96);"
                + "-fx-border-color: #ffd166; -fx-border-width: 2.5;"
                + "-fx-border-radius: 14; -fx-background-radius: 14;");

        headerLabel.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 32));
        headerLabel.setTextFill(Color.web("#ffd166"));

        subHeaderLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        subHeaderLabel.setTextFill(Color.web("#90e0ef"));

        cardsContainer.setAlignment(Pos.CENTER);

        modal.getChildren().addAll(headerLabel, subHeaderLabel, cardsContainer);
        getChildren().add(modal);
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    //display cards
    public void displayChoices(List<Upgrade> choices, Consumer<Upgrade> onUpgradeSelected) {
        this.currentChoices = choices != null ? new ArrayList<>(choices) : List.of();
        this.selectionCallback = onUpgradeSelected;
        cardsContainer.getChildren().clear();

        for (int i = 0; i < this.currentChoices.size(); i++) {
            Upgrade upgrade = this.currentChoices.get(i);
            VBox cardNode = buildUpgradeCardNode(upgrade, i + 1);
            cardsContainer.getChildren().add(cardNode);
        }
        setVisible(true);
    }

    public void selectByIndex(int zeroBasedIndex) {
        if (!isVisible() || selectionCallback == null) {
            return;
        }
        if (zeroBasedIndex >= 0 && zeroBasedIndex < currentChoices.size()) {
            Upgrade chosen = currentChoices.get(zeroBasedIndex);
            setVisible(false);
            selectionCallback.accept(chosen);
        }
    }

    private VBox buildUpgradeCardNode(Upgrade upgrade, int keyNumber) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(235);
        box.setPrefHeight(390);
        box.getStyleClass().add("upgrade-card");

        Label keyBadge = new Label("PRESS [" + keyNumber + "]");
        keyBadge.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 11));
        keyBadge.setTextFill(Color.web("#ffd166"));
        keyBadge.setPadding(new Insets(2, 8, 2, 8));
        keyBadge.setStyle("-fx-background-color: #2b234d; -fx-background-radius: 4;");

        Image cardArt = assetManager != null ? assetManager.getUpgradeCardSprite(upgrade.getCardSpriteIndex()) : null;
        ImageView artView = new ImageView();
        if (cardArt != null) {
            artView.setImage(cardArt);
        }
        artView.setFitWidth(135);
        artView.setFitHeight(198);
        artView.setPreserveRatio(true);

        Label titleLbl = new Label(upgrade.getTitle().toUpperCase());
        titleLbl.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 15));
        titleLbl.setTextFill(Color.web("#64f4ff"));
        titleLbl.setTextAlignment(TextAlignment.CENTER);
        titleLbl.setWrapText(true);

        Label descLbl = new Label(upgrade.getDescription());
        descLbl.setFont(Font.font("Consolas", 12));
        descLbl.setTextFill(Color.web("#dbeafe"));
        descLbl.setWrapText(true);
        descLbl.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(keyBadge, artView, titleLbl, descLbl);
        box.setOnMouseClicked(e -> {
            setVisible(false);
            if (selectionCallback != null) {
                selectionCallback.accept(upgrade);
            }
        });
        return box;
    }
}
