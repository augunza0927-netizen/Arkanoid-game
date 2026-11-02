package se233.project2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import se233.project2.controller.GameController;
import se233.project2.exception.GlobalExceptionHandler;
import se233.project2.model.state.GameState;
import se233.project2.util.GameConfig;
import se233.project2.util.GameLogger;
import se233.project2.view.AssetManager;
import se233.project2.view.GameCanvas;
import se233.project2.view.GameOverView;
import se233.project2.view.HudPane;
import se233.project2.view.StartMenuView;
import se233.project2.view.UpgradeSelectionView;

import java.net.URL;


public class MainApp extends Application {

    private GameController gameController;

    @Override
    public void start(Stage primaryStage) {//
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());//exception
        GlobalExceptionHandler.setShowUiAlerts(true);

        try {
            GameLogger.logGameEvent("Bootstrapping " + GameConfig.GAME_TITLE + " (JavaFX 17)");

            AssetManager assetManager = new AssetManager();
            assetManager.loadAllAssets();

            GameState gameState = new GameState();
            gameController = new GameController(gameState);

            GameCanvas gameCanvas = new GameCanvas(GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
            HudPane hudPane = new HudPane();
            StartMenuView startMenuView = new StartMenuView();
            UpgradeSelectionView upgradeSelectionView = new UpgradeSelectionView();
            GameOverView gameOverView = new GameOverView();

            startMenuView.setOnExit(Platform::exit);

            HBox playAreaAndHud = new HBox(gameCanvas, hudPane);
            StackPane rootStack = new StackPane(
                    playAreaAndHud,
                    upgradeSelectionView,
                    gameOverView,
                    startMenuView
            );

            Scene scene = new Scene(rootStack, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            URL cssUrl = MainApp.class.getResource("/se233/project2/styles/application.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            gameController.bindViews(
                    assetManager,
                    gameCanvas,
                    hudPane,
                    startMenuView,
                    upgradeSelectionView,
                    gameOverView
            );
            gameController.getInputHandler().attachToScene(scene);

            //Load assets
            primaryStage.setTitle(GameConfig.GAME_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            gameController.startGameLoop();
        } catch (Throwable t) {
            GlobalExceptionHandler.handle(t);
        }
    }

    @Override
    public void stop() {
        if (gameController != null) {
            gameController.shutdown();
        }
        GameLogger.logGameEvent("Application terminated cleanly.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
