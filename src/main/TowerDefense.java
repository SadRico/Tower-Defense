package main;

import main.model.Enemy;
import main.model.Projectile;
import main.model.Tower;
import main.database.DatabaseManager;
import main.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Hauptklasse des Spiels, erbt von JFrame (GUI Fenster)
public class TowerDefense extends JFrame {
    private GamePanel gamePanel;    // Zeichenfläche für das Spiel
    private Controller controller;  // Controller für Benutzereingaben
    private JLabel scoreLabel;      // Anzeige für den Punktestand
    private int score = 0;          // Aktueller Punktestand

    // Spielobjekte
    private List<Tower> towers = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();

    // Start-/ Endpunkt der Gegnerbewegung
    private Point startPoint = new Point(0, 5);
    private Point endPoint = new Point(19, 5);

    // Timer für Spielupdate und Gegner-Spawn
    private Timer gameTimer;
    private Timer enemySpawnTimer;

    private boolean gameOver = false; // Spielstatus
    private int enemiesDefeated = 0;  // Counter für besiegte Gegner
    private int totalEnemies = 20;    // Gesamtanzahl der Gegner in einer Runde

    private DatabaseManager dbManager; // Datenbankverwaltung für Highscores

    public TowerDefense() {
        setTitle("Tower Defense");  // Titel
        setSize(Constants.WIDTH, Constants.HEIGHT); // Fenstergröße
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Layout

        // Controller initialisiereen
        controller = new Controller(this);

        // Panel für Punktestand
        JPanel scorePanel = new JPanel();
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scorePanel.add(scoreLabel);
        add(scorePanel, BorderLayout.NORTH);

        // Spielfeld-Panel mit Controller
        gamePanel = new GamePanel(this, controller);
        add(gamePanel, BorderLayout.CENTER);

        // Initialisierung der Datenbank
        dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        // Spielstart
        startGame();

        setLocationRelativeTo(null); // Zentriert Fenster
        setVisible(true);            // Macht Window sichtbar
    }

    // Getter für Türme, Gegner, Projektile, Start-/Endpunkt und Spielstatus
    public List<Tower> getTowers() { return towers; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public Point getStartPoint() { return startPoint; }
    public Point getEndPoint() { return endPoint; }
    public boolean isGameOver() { return gameOver; }

    // Platziert einen Turm auf dem Spielfeld, sofern möglich
    public void placeTower(int gridX, int gridY) {
        boolean towerExists = false;

        // Prüft ob bereits ein Turm an der Position existiert
        for (Tower tower : towers) {
            if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                towerExists = true;
                break;
            }
        }

        // Verhindert das Platzieren auf Start-/Endpunkt
        boolean isSpecialPoint = (gridX == startPoint.x && gridY == startPoint.y) ||
                (gridX == endPoint.x && gridY == endPoint.y);

        // Turm hinzufügen, wenn möglich
        if (!towerExists && !isSpecialPoint) {
            towers.add(new Tower(gridX, gridY, Constants.CELL_SIZE));
        }
    }

    // Initialisiert das Spiel und startet die Timer
    private void startGame() {
        gameOver = false;
        score = 0;
        enemiesDefeated = 0;

        // Leere alle Spiellisten
        towers.clear();
        enemies.clear();
        projectiles.clear();
        scoreLabel.setText("Score: 0");

        // Timer für regelmäßiges Spiellogik-Update
        gameTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();          // Logik aktualisieren
                gamePanel.repaint();   // Spielfeld neu zeichnen
            }
        });
        gameTimer.start();

        // Timer für das spawnen der Gegnern
        enemySpawnTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enemies.size() + enemiesDefeated < totalEnemies) {
                    spawnEnemy(); // Neuen Gegner hinzufügen
                } else if (enemies.isEmpty() && enemiesDefeated == totalEnemies) {
                    endGame(true); // Spieler hat gewonnen
                }
            }
        });
        enemySpawnTimer.start();
    }

    // Erstellt und platziert einen neuen Gegner
    private void spawnEnemy() {
        Enemy enemy = new Enemy(
                startPoint.x * Constants.CELL_SIZE,
                startPoint.y * Constants.CELL_SIZE,
                endPoint
        );
        enemies.add(enemy);
    }

    // Hauptspiel-Logik
    private void updateGame() {
        if (gameOver) return;

        // Gegner bewegen und Status prüfen
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.move();

            // Prüft ob Gegner das Ziel erreicht hat
            if (enemy.reachedDestination(
                    endPoint.x * Constants.CELL_SIZE,
                    endPoint.y * Constants.CELL_SIZE)) {
                endGame(false); // Spiel verloren
                return;
            }

            // Entfernt tote Gegner und erhöht Punktestand
            if (enemy.getHealth() <= 0) {
                enemyIterator.remove();
                score += 100;
                enemiesDefeated++;
                scoreLabel.setText("Score: " + score);

                // Prüft auf Spielsieg
                if (enemies.isEmpty() && enemiesDefeated == totalEnemies) {
                    endGame(true);
                    return;
                }
            }
        }

        // Türme schießen auf Gegner
        for (Tower tower : towers) {
            tower.decreaseCooldown();                 // Schuss-Cooldown verringern
            Enemy target = tower.findTarget(enemies); // Ziel finden
            if (target != null && tower.canFire()) {
                Projectile projectile = tower.fireAt(target); // Projektil erzeugen
                projectiles.add(projectile);
            }
        }

        // Projektile bewegen und prüfen
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.move();

            // Kollision mit Gegner prüfen
            for (Enemy enemy : enemies) {
                if (projectile.collidesWith(enemy)) {
                    enemy.takeDamage(projectile.getDamage()); // Schaden anwenden
                    projectileIterator.remove();              // Projektil entfernen
                    break;
                }
            }

            // Entfernt Projektil, wenn außerhalb des Spielfelds
            if (projectile.isOutOfBounds(Constants.WIDTH, Constants.HEIGHT)) {
                projectileIterator.remove();
            }
        }
    }

    // Beendet das Spiel (Sieg oder Niederlage)
    private void endGame(boolean victory) {
        gameTimer.stop();
        enemySpawnTimer.stop();
        gameOver = true;

        String message = victory ?
                "Alle Gegner besiegt! Dein Score: " + score :
                "Game Over! Ein Gegner hat das Ziel erreicht. Dein Score: " + score;

        // Abfrage des Spielernamens für Highscore
        String playerName = JOptionPane.showInputDialog(this, message + "\nGib deinen Namen ein:");

        if (playerName != null && !playerName.trim().isEmpty()) {
            dbManager.saveHighscore(playerName, score); // Score wird gespei9chert

            // Highscores abrufen und anzeigen
            List<String> highscores = dbManager.getTopHighscores(5);
            StringBuilder highscoreMessage = new StringBuilder("Top 5 Highscores:\n");
            for (String highscore : highscores) {
                highscoreMessage.append(highscore).append("\n");
            }

            JOptionPane.showMessageDialog(this, highscoreMessage.toString(), "Highscores", JOptionPane.INFORMATION_MESSAGE);
        }

        // Spielneustart abfragen
        int option = JOptionPane.showConfirmDialog(this, "Möchtest du noch einmal spielen?",
                "Spiel neu starten", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            startGame();
        } else {
            System.exit(0); // Gui beenden
        }
    }

    // Einstiegspunkt der Anwendung
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefense());
    }
}