package main.model;

import java.awt.*;
import java.util.List;

public class Tower {
    private int gridX, gridY;           // Gitterkoordinaten des Turms
    private int x, y;                   // Pixelposition (Gitterkoordinaten)
    private int size;                   // Größe des Turms in Pixel
    private int range = 150;           // Reichweite in der Gegner erkannt werden
    private int cooldown = 0;          // Aktueller Cooldown-Zähler (Feuerrate)
    private int cooldownMax = 20;      // Maximale Cooldown-Zeit in Spielzyklen (Feuerrate)

    // Setzt Gitter und Pixelposition sowie Turmgröße
    public Tower(int gridX, int gridY, int cellSize) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.x = gridX * cellSize;
        this.y = gridY * cellSize;
        this.size = cellSize;
    }

    // Getter für Gitterposition
    public int getGridX() {
        return gridX;
    }

    // Getter für Gitterposition
    public int getGridY() {
        return gridY;
    }

    // Verringert den Cooldown pro Spielzyklus
    public void decreaseCooldown() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    // Gibt true zurück, wenn Turm bereit zum Schießen ist
    public boolean canFire() {
        return cooldown == 0;
    }

    // Sucht das erste Ziel innerhalb der Reichweite
    public Enemy findTarget(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            double distance = Math.sqrt(
                    Math.pow(enemy.getX() + enemy.getSize()/2 - (x + size/2), 2) +
                            Math.pow(enemy.getY() + enemy.getSize()/2 - (y + size/2), 2)
            );

            if (distance <= range) {
                return enemy; // Erstes Ziel in Reichweite
            }
        }
        return null; // Kein Ziel gefunden
    }

    // Erzeugt ein Projektil auf den anvisierten Gegner, setzt dann Cooldown zurück
    public Projectile fireAt(Enemy enemy) {
        cooldown = cooldownMax;
        return new Projectile(
                x + size/2,                         // Mittelpunkt des Turms
                y + size/2,
                enemy.getX() + enemy.getSize()/2,   // Mittelpunkt des Gegners
                enemy.getY() + enemy.getSize()/2,
                10 // Schaden
        );
    }

    // Zeichnet den Turm und Reichweitenkreis
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, size, size);

        // Reichweitenindikator
        g.setColor(new Color(0, 0, 255, 20));
        g.fillOval(x + size/2 - range, y + size/2 - range, range*2, range*2);
    }
}