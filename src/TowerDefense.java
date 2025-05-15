import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TowerDefense extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 40;
    private static final int GRID_WIDTH = WIDTH / CELL_SIZE;
    private static final int GRID_HEIGHT = HEIGHT / CELL_SIZE;

    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private int score = 0;
    private List<Tower> towers = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private Point startPoint = new Point(0, 5);
    private Point endPoint = new Point(19, 5);
    private Timer gameTimer;
    private Timer enemySpawnTimer;
    private boolean gameOver = false;
    private int enemiesDefeated = 0;
    private int totalEnemies = 10;
    private Connection dbConnection;

    public TowerDefense() {
        setTitle("Tower Defense");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Score Panel
        JPanel scorePanel = new JPanel();
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scorePanel.add(scoreLabel);
        add(scorePanel, BorderLayout.NORTH);

        // Game Panel
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // Initialize Database
        initializeDatabase();

        // Start Game
        startGame();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            // Verbindung zur SQLite-Datenbank herstellen (wird erstellt, falls nicht vorhanden)
            dbConnection = DriverManager.getConnection("jdbc:sqlite:tower_defense.db");
            Statement statement = dbConnection.createStatement();

            // Highscore-Tabelle erstellen, falls nicht vorhanden
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS highscores (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "player_name TEXT NOT NULL," +
                            "score INTEGER NOT NULL," +
                            "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ");"
            );
            statement.close();
            System.out.println("Datenbank erfolgreich initialisiert");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler bei der Datenbankinitialisierung: " + e.getMessage(),
                    "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveHighscore(String playerName, int score) {
        try {
            PreparedStatement pstmt = dbConnection.prepareStatement(
                    "INSERT INTO highscores (player_name, score) VALUES (?, ?)");
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Highscore gespeichert");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern des Highscores: " + e.getMessage(),
                    "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> getTopHighscores(int limit) {
        List<String> highscores = new ArrayList<>();
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT player_name, score FROM highscores ORDER BY score DESC LIMIT " + limit);

            while (rs.next()) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                highscores.add(name + ": " + score);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return highscores;
    }

    private void startGame() {
        gameOver = false;
        score = 0;
        enemiesDefeated = 0;
        towers.clear();
        enemies.clear();
        projectiles.clear();
        scoreLabel.setText("Score: 0");

        // Game Loop
        gameTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
        gameTimer.start();

        // Enemy Spawner
        enemySpawnTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enemies.size() + enemiesDefeated < totalEnemies) {
                    spawnEnemy();
                } else if (enemies.isEmpty() && enemiesDefeated == totalEnemies) {
                    endGame(true);
                }
            }
        });
        enemySpawnTimer.start();
    }

    private void spawnEnemy() {
        Enemy enemy = new Enemy(startPoint.x * CELL_SIZE, startPoint.y * CELL_SIZE, endPoint);
        enemies.add(enemy);
    }

    private void updateGame() {
        if (gameOver) return;

        // Update enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.move();

            // Check if enemy reached the end
            if (enemy.reachedDestination(endPoint.x * CELL_SIZE, endPoint.y * CELL_SIZE)) {
                endGame(false);
                return;
            }

            // Check if enemy is dead
            if (enemy.getHealth() <= 0) {
                enemyIterator.remove();
                score += 100;
                enemiesDefeated++;
                scoreLabel.setText("Score: " + score);

                // Check if all enemies defeated
                if (enemies.isEmpty() && enemiesDefeated == totalEnemies) {
                    endGame(true);
                    return;
                }
            }
        }

        // Update towers and fire
        for (Tower tower : towers) {
            tower.decreaseCooldown();
            Enemy target = tower.findTarget(enemies);
            if (target != null && tower.canFire()) {
                Projectile projectile = tower.fireAt(target);
                projectiles.add(projectile);
            }
        }

        // Update projectiles
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.move();

            // Check collisions with enemies
            for (Enemy enemy : enemies) {
                if (projectile.collidesWith(enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    projectileIterator.remove();
                    break;
                }
            }

            // Remove projectiles that are out of bounds
            if (projectile.isOutOfBounds(WIDTH, HEIGHT)) {
                projectileIterator.remove();
            }
        }
    }

    private void endGame(boolean victory) {
        gameTimer.stop();
        enemySpawnTimer.stop();
        gameOver = true;

        String message = victory ?
                "Alle Gegner besiegt! Dein Score: " + score :
                "Game Over! Ein Gegner hat das Ziel erreicht. Dein Score: " + score;

        String playerName = JOptionPane.showInputDialog(this, message + "\nGib deinen Namen ein:");

        if (playerName != null && !playerName.trim().isEmpty()) {
            saveHighscore(playerName, score);

            // Show highscores
            List<String> highscores = getTopHighscores(5);
            StringBuilder highscoreMessage = new StringBuilder("Top 5 Highscores:\n");
            for (String highscore : highscores) {
                highscoreMessage.append(highscore).append("\n");
            }

            JOptionPane.showMessageDialog(this, highscoreMessage.toString(), "Highscores", JOptionPane.INFORMATION_MESSAGE);
        }

        // Restart option
        int option = JOptionPane.showConfirmDialog(this, "Möchtest du noch einmal spielen?",
                "Spiel neu starten", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            startGame();
        } else {
            System.exit(0);
        }
    }

    private class GamePanel extends JPanel {

        public GamePanel() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;

                    // Place tower at clicked position
                    int gridX = e.getX() / CELL_SIZE;
                    int gridY = e.getY() / CELL_SIZE;

                    // Check if tower already exists at this position
                    boolean towerExists = false;
                    for (Tower tower : towers) {
                        if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                            towerExists = true;
                            break;
                        }
                    }

                    // Check if position is start or end point
                    boolean isSpecialPoint = (gridX == startPoint.x && gridY == startPoint.y) ||
                            (gridX == endPoint.x && gridY == endPoint.y);

                    if (!towerExists && !isSpecialPoint) {
                        towers.add(new Tower(gridX, gridY, CELL_SIZE));
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw grid
            g.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < GRID_WIDTH; i++) {
                g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, HEIGHT);
            }
            for (int i = 0; i < GRID_HEIGHT; i++) {
                g.drawLine(0, i * CELL_SIZE, WIDTH, i * CELL_SIZE);
            }

            // Draw start and end points
            g.setColor(Color.GREEN);
            g.fillRect(startPoint.x * CELL_SIZE, startPoint.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.setColor(Color.RED);
            g.fillRect(endPoint.x * CELL_SIZE, endPoint.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            // Draw towers
            for (Tower tower : towers) {
                tower.draw(g);
            }

            // Draw enemies
            for (Enemy enemy : enemies) {
                enemy.draw(g);
            }

            // Draw projectiles
            for (Projectile projectile : projectiles) {
                projectile.draw(g);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefense());
    }
}

class Tower {
    private int gridX, gridY;
    private int x, y;
    private int size;
    private int range = 150;
    private int cooldown = 0;
    private int cooldownMax = 20;

    public Tower(int gridX, int gridY, int cellSize) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.x = gridX * cellSize;
        this.y = gridY * cellSize;
        this.size = cellSize;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void decreaseCooldown() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public boolean canFire() {
        return cooldown == 0;
    }

    public Enemy findTarget(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            double distance = Math.sqrt(
                    Math.pow(enemy.getX() + enemy.getSize()/2 - (x + size/2), 2) +
                            Math.pow(enemy.getY() + enemy.getSize()/2 - (y + size/2), 2)
            );

            if (distance <= range) {
                return enemy;
            }
        }
        return null;
    }

    public Projectile fireAt(Enemy enemy) {
        cooldown = cooldownMax;
        return new Projectile(
                x + size/2,
                y + size/2,
                enemy.getX() + enemy.getSize()/2,
                enemy.getY() + enemy.getSize()/2,
                10 // damage
        );
    }
    // Tower Range Indikator
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, size, size);

        g.setColor(new Color(0, 0, 255, 50));
        g.fillOval(x + size/2 - range, y + size/2 - range, range*2, range*2);

    }
}

class Enemy {
    private double x, y;
    private double speed = 3.0;
    private int size = 30;
    private int health = 50;
    private Point destination;

    public Enemy(int x, int y, Point destination) {
        this.x = x;
        this.y = y;
        this.destination = destination;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public void move() {
        double targetX = destination.x * 40; // CELL_SIZE
        double targetY = destination.y * 40; // CELL_SIZE

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance > speed) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    public boolean reachedDestination(int destX, int destY) {
        double distance = Math.sqrt(
                Math.pow(destX - x, 2) + Math.pow(destY - y, 2)
        );
        return distance < size/2;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int)x, (int)y, size, size);

        // Draw health bar
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y - 10, size, 5);
        g.setColor(Color.GREEN);
        int healthWidth = (int)((health / 50.0) * size);
        g.fillRect((int)x, (int)y - 10, healthWidth, 5);
    }
}

class Projectile {
    private double x, y;
    private double targetX, targetY;
    private double speed = 10.0;
    private double dx, dy;
    private int size = 8;
    private int damage;

    public Projectile(int startX, int startY, double targetX, double targetY, int damage) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;

        double angle = Math.atan2(targetY - startY, targetX - startX);
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    public int getDamage() {
        return damage;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public boolean collidesWith(Enemy enemy) {
        double distance = Math.sqrt(
                Math.pow(enemy.getX() + enemy.getSize()/2 - x, 2) +
                        Math.pow(enemy.getY() + enemy.getSize()/2 - y, 2)
        );

        return distance < (enemy.getSize()/2 + size/2);
    }

    public boolean isOutOfBounds(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval((int)x - size/2, (int)y - size/2, size, size);
    }
}