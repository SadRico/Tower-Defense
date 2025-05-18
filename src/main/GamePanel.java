package main;

import main.model.Enemy;
import main.model.Projectile;
import main.model.Tower;
import main.util.Constants;

import javax.swing.*;
import java.awt.*;

// Zeichenfläche für das Spiel, erbt von JPanel
public class GamePanel extends JPanel {
    private TowerDefense gameInstance; // Referenz auf die Hauptspielinstanz

    // (Konstruktor) Erhält Instanz des Spiels + Hintergrundfarbe
    public GamePanel(TowerDefense gameInstance, Controller controller) {
        this.gameInstance = gameInstance;
        setBackground(Color.WHITE); // Hintergrundfarbe des Spielfelds

        // Mausklick-Listener zum Platzieren von Türmen von der Controllerklasse
        addMouseListener(controller.createTowerPlacementListener());
    }

    // Zeichnet das Spielfeld/Spielobjekte
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Hintergrund löschen

        // Raster zeichnen
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < Constants.GRID_WIDTH; i++) {
            g.drawLine(i * Constants.CELL_SIZE, 0, i * Constants.CELL_SIZE, Constants.HEIGHT); // Vertikale Linien
        }
        for (int i = 0; i < Constants.GRID_HEIGHT; i++) {
            g.drawLine(0, i * Constants.CELL_SIZE, Constants.WIDTH, i * Constants.CELL_SIZE); // Horizontale Linien
        }

        // Start- und Endpunkt aus Hauptspielinstanz holen
        Point startPoint = gameInstance.getStartPoint();
        Point endPoint = gameInstance.getEndPoint();

        // Startpunkt grün markieren
        g.setColor(Color.GREEN);
        g.fillRect(startPoint.x * Constants.CELL_SIZE, startPoint.y * Constants.CELL_SIZE,
                Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Endpunkt rot markieren
        g.setColor(Color.RED);
        g.fillRect(endPoint.x * Constants.CELL_SIZE, endPoint.y * Constants.CELL_SIZE,
                Constants.CELL_SIZE, Constants.CELL_SIZE);

        // Türme zeichnen
        for (Tower tower : gameInstance.getTowers()) {
            tower.draw(g);
        }

        // Gegner zeichnen
        for (Enemy enemy : gameInstance.getEnemies()) {
            enemy.draw(g);
        }

        // Projektile zeichnen
        for (Projectile projectile : gameInstance.getProjectiles()) {
            projectile.draw(g);
        }
    }
}