package gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameWindow extends Stage {
    private final int WIDTH = 800;  // Breite des Spielfensters
    private final int HEIGHT = 600; // Höhe des Spielfensters
    private final int TILE_SIZE = 50; // Größe der Gegner und Türme (hier als Quadrat)
    private List<Enemy> enemies; // Liste der Gegner
    private List<Tower> towers;  // Liste der Türme

    public GameWindow() {
        // Fenster für das Spiel erstellen
        this.setTitle("Tower Defense - Game");
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);

        // Canvas erstellen und zur Szene hinzufügen
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        // Spiellogik: Gegner und Türme initialisieren
        this.enemies = new ArrayList<>();
        this.towers = new ArrayList<>();
        initializeEnemies();
        initializeTowers();

        // Start Game: Eine Schleife für das Spiel (mit Bewegungen der Gegner und Schießen der Türme)
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50); // Eine kleine Verzögerung, um die Bewegungen flüssiger zu machen
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateGame(gc); // Spiellogik aufrufen
            }
        }).start();

        // Szene und Fenster konfigurieren
        Scene scene = new Scene(root);
        this.setScene(scene);
        this.show();
    }

    // Gegner initialisieren (ein Beispiel für 3 Gegner)
    private void initializeEnemies() {
        enemies.add(new Enemy(0, 0, 100)); // Erster Gegner
        enemies.add(new Enemy(100, 0, 100)); // Zweiter Gegner
        enemies.add(new Enemy(200, 0, 100)); // Dritter Gegner
    }

    // Türme initialisieren (ein Beispiel für einen Turm)
    private void initializeTowers() {
        towers.add(new Tower(150, 150)); // Ein Turm auf dem Spielfeld
    }

    // Spiellogik: Gegner bewegen, Türme schießen und alles zeichnen
    private void updateGame(GraphicsContext gc) {
        // Spielfeld löschen
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Hintergrund zeichnen
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Gegner bewegen
        for (Enemy enemy : enemies) {
            enemy.move();  // Gegner bewegen
            gc.setFill(Color.RED);
            gc.fillRect(enemy.getX(), enemy.getY(), TILE_SIZE, TILE_SIZE);  // Gegner als rotes Quadrat zeichnen
        }

        // Türme schießen
        for (Tower tower : towers) {
            tower.shoot(enemies); // Turm schießt auf die Gegner
            gc.setFill(Color.BLUE);
            gc.fillRect(tower.getX(), tower.getY(), TILE_SIZE, TILE_SIZE); // Turm als blaues Quadrat zeichnen
        }
    }

    // Die Gegnerklasse
    public static class Enemy {
        private double x, y, health;
        private int currentTargetIndex;
        private Point2D[] path; // Die Wegpunkte, denen der Gegner folgt

        public Enemy(double x, double y, double health) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.currentTargetIndex = 0;

            // Beispielhafte Wegpunkte für den Gegner
            path = new Point2D[] {
                    new Point2D(-10, -10),
                    new Point2D(100, 0),
                    new Point2D(100, 100),
                    new Point2D(200, 100),
                    new Point2D(200, 200),
                    new Point2D(300, 200),
                    new Point2D(200, 450),
                    new Point2D(600, 200),
                    new Point2D(700, 500),
                    new Point2D(800, 600)
            };
        }

        // Gegner bewegen sich entlang der Wegpunkte
        public void move() {
            if (currentTargetIndex < path.length) {
                Point2D target = path[currentTargetIndex];

                // Berechne die Richtung zum Ziel
                double dx = target.getX() - this.x;
                double dy = target.getY() - this.y;

                // Normalisiere den Vektor (damit der Gegner immer mit gleicher Geschwindigkeit bewegt wird)
                double distance = Math.sqrt(dx * dx + dy * dy);
                dx /= distance;
                dy /= distance;

                // Bewegung des Gegners in Richtung des Zielpunkts
                this.x += dx * 5; // Geschwindigkeit des Gegners (5 Pixel pro Bewegung)
                this.y += dy * 5;

                // Wenn der Gegner das Ziel erreicht, zum nächsten Wegpunkt wechseln
                if (Math.abs(this.x - target.getX()) < 5 && Math.abs(this.y - target.getY()) < 5) {
                    currentTargetIndex++;
                }
            }
        }

        // Getter für die Position und die Gesundheit des Gegners
        public double getX() { return x; }
        public double getY() { return y; }
        public double getHealth() { return health; }
        public void takeDamage(double damage) {
            this.health -= damage;
        }
    }

    // Die Tower-Klasse (Turm, der auf Gegner schießt)
    public static class Tower {
        private double x, y;
        private static final double RANGE = 200; // Reichweite des Turms
        private static final double DAMAGE = 10; // Schaden, den der Turm verursacht

        public Tower(double x, double y) {
            this.x = x;
            this.y = y;
        }

        // Turm schießt auf den nächsten Gegner in Reichweite
        public void shoot(List<Enemy> enemies) {
            for (Enemy enemy : enemies) {
                double distance = Math.sqrt(Math.pow(this.x - enemy.getX(), 2) + Math.pow(this.y - enemy.getY(), 2));
                if (distance < RANGE && enemy.getHealth() > 0) {
                    enemy.takeDamage(DAMAGE); // Schaden an den Gegner anrichten
                    break; // Nur auf den ersten Gegner in Reichweite schießen
                }
            }
        }

        // Getter für die Position des Turms
        public double getX() { return x; }
        public double getY() { return y; }
    }

    // Point2D Klasse (kann auch durch die in JavaFX integrierte Point2D ersetzt werden)
    public static class Point2D {
        private double x, y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }
        public double getY() { return y; }
    }
}



