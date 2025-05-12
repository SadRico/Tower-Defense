package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainWindow extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Button zum Starten des Spiels
        Button btnStartGame = new Button("Start Game");
        btnStartGame.setOnAction(e -> startGame());

        // Layout
        StackPane root = new StackPane();
        root.getChildren().add(btnStartGame);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame() {
        // Neues Fenster für das Spiel öffnen
        GameWindow gameWindow = new GameWindow();
    }
}

