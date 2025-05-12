package gui;

import db.DatabaseManager;
import game.Game;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends Application {
    private DatabaseManager db;

    @Override
    public void start(Stage primaryStage) {
        db = new DatabaseManager("data/game.db");
        db.connect();
        db.createTables();

        Label title = new Label("Tower Defense");
        TextField nameField = new TextField();
        nameField.setPromptText("Spielername");
        Button startButton = new Button("Spiel starten");
        Button highscoreButton = new Button("Highscores anzeigen");

        startButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Bitte gib einen Spielernamen ein.");
                return;
            }
            Game game = new Game(db);
            game.startGame(name);
            showAlert("Spiel beendet. Score wurde gespeichert.");
        });

        highscoreButton.setOnAction(e -> {
            var scores = db.getHighscores();
            showAlert("Highscores:\n" + String.join("\n", scores));
        });

        VBox layout = new VBox(10, title, nameField, startButton, highscoreButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");
        Scene scene = new Scene(layout, 300, 250);

        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        db.disconnect();
    }
}
