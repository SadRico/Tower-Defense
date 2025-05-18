package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Controller {
    private TowerDefense gameInstance; // Referenz auf die Hauptspielinstanz

    public Controller(TowerDefense gameInstance) {
        this.gameInstance = gameInstance;
    }

    // Erstellt einen MouseAdapter für das GamePanel
    public MouseAdapter createTowerPlacementListener() {
        return new MouseAdapter() { // @return MouseAdapter für die Platzierung von Türmen
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameInstance.isGameOver()) return; // Kein Platzieren nach Spielende

                // Umrechnen von Pixelposition in Gridkoordinaten
                int gridX = e.getX() / main.util.Constants.CELL_SIZE;
                int gridY = e.getY() / main.util.Constants.CELL_SIZE;

                // Turm an geklickter Position platzieren
                gameInstance.placeTower(gridX, gridY);
            }
        };
    }
}