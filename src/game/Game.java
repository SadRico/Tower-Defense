package game;

import db.DatabaseManager;

public class Game {
    private DatabaseManager db;

    public Game(DatabaseManager db) {
        this.db = db;
    }

    public void startGame(String playerName) {
        System.out.println("Spiel gestartet für: " + playerName);

        // Beispielspielverlauf (kann durch echte Logik ersetzt werden)
        Enemy enemy = new Enemy(100);
        Tower tower = new Tower(10, 10, 25);

        for (int i = 0; i < 5; i++) {
            tower.attack(enemy);
        }

        int score = (int)(Math.random() * 1000);
        System.out.println("Spiel beendet. Score: " + score);
        db.insertPlayer(playerName, score);
    }
}
