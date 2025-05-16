package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Controller-Klasse zur Steuerung der Benutzereingaben
 */
public class Controller {
    private TowerDefense gameInstance; // Referenz auf die Hauptspielinstanz

    /**
     * Konstruktor: Initialisiert den Controller mit Referenz auf die Hauptspielinstanz
     * @param gameInstance Referenz auf das Hauptspiel
     */
    public Controller(TowerDefense gameInstance) {
        this.gameInstance = gameInstance;
    }
    
    /**
     * Erstellt und liefert einen MouseAdapter für das GamePanel
     * @return MouseAdapter für die Platzierung von Türmen
     */
    public MouseAdapter createTowerPlacementListener() {
        return new MouseAdapter() {
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