import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    // Spielkonstanten
    private static final int CELL_SIZE = 40;

    // Spielzustand
    private int score = 0;
    private boolean gameOver = false;
    private int enemiesDefeated = 0;
    private int totalEnemies = 10;

    // Spielobjekte
    private List<Tower> towers = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();

    // Start- und Endpunkte
    private Point startPoint = new Point(0, 5);
    private Point endPoint = new Point(19, 5);

    public void update() {
        if (gameOver) return;

        // Projektile bewegen und Kollisionen prüfen
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.move();

            boolean hit = false;
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (projectile.collidesWith(enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    hit = true;

                    if (enemy.getHealth() <= 0) {
                        enemies.remove(j);
                        score += 100;
                        enemiesDefeated++;
                    }
                    break;
                }
            }

            if (hit || projectile.isOutOfBounds(800, 600)) {
                projectiles.remove(i);
            }
        }

        // Gegner bewegen
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.move();

            if (enemy.reachedDestination(endPoint.x * CELL_SIZE, endPoint.y * CELL_SIZE)) {
                gameOver = true;
                return;
            }
        }

        // Spiel beenden, wenn alle Gegner besiegt wurden
        if (enemies.isEmpty() && enemiesDefeated == totalEnemies) {
            gameOver = true;
        }
    }

    public void placeTower(int x, int y) {
        int gridX = x / CELL_SIZE;
        int gridY = y / CELL_SIZE;

        // Prüfen, ob bereits ein Turm an dieser Position ist
        for (Tower tower : towers) {
            if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                return;
            }
        }

        // Prüfen, ob Position Start- oder Endpunkt ist
        if ((gridX == startPoint.x && gridY == startPoint.y) ||
                (gridX == endPoint.x && gridY == endPoint.y)) {
            return;
        }

        // Neuen Turm platzieren
        towers.add(new Tower(gridX, gridY, CELL_SIZE));
    }

    public void spawnEnemy() {
        if (enemies.size() + enemiesDefeated < totalEnemies) {
            Enemy enemy = new Enemy(startPoint.x * CELL_SIZE, startPoint.y * CELL_SIZE, endPoint);
            enemies.add(enemy);
        }
    }

    public void draw(Graphics g) {
        // Spielfeld zeichnen
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 800/CELL_SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, 600);
        }
        for (int i = 0; i < 600/CELL_SIZE; i++) {
            g.drawLine(0, i * CELL_SIZE, 800, i * CELL_SIZE);
        }

        // Start- und Endpunkte zeichnen
        g.setColor(Color.GREEN);
        g.fillRect(startPoint.x * CELL_SIZE, startPoint.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.RED);
        g.fillRect(endPoint.x * CELL_SIZE, endPoint.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Türme zeichnen
        for (Tower tower : towers) {
            tower.draw(g);
        }

        // Gegner zeichnen
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }

        // Projektile zeichnen
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }
    }

    // Getter und Setter
    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isVictory() {
        return gameOver && enemies.isEmpty() && enemiesDefeated == totalEnemies;
    }

    public void reset() {
        score = 0;
        gameOver = false;
        enemiesDefeated = 0;
        towers.clear();
        enemies.clear();
        projectiles.clear();
    }
}