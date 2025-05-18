package main.model;

import java.awt.*;

public class Projectile {
    private double x, y;                    // Aktuelle Position des Projektils
    private double speed = 10.0;            // Geschwindigkeit des Projektils
    private double dx, dy;                  // Bewegungsrichtung (mit Speed multipliziert)
    private int size = 8;                   // Durchmesser des Projektils
    private int damage;                     // Schaden vom Projektil

    // (Konstruktor) Berechnet Flugrichtung basierend auf Start- und Zielposition
    public Projectile(int startX, int startY, double targetX, double targetY, int damage) {
        this.x = startX;
        this.y = startY;
        this.damage = damage;

        // Berechnung des Winkels und der Geschwindigkeit
        double angle = Math.atan2(targetY - startY, targetX - startX);
        dx = Math.cos(angle) * speed;
        dy = Math.sin(angle) * speed;
    }

    // Getter für Schaden
    public int getDamage() {
        return damage;
    }

    // Bewegung in Richtung anvisierten Gegner
    public void move() {
        x += dx;
        y += dy;
    }

    // Kollisionsprüfung mit einem Gegner (kreisbasiert)
    public boolean collidesWith(Enemy enemy) {
        double distance = Math.sqrt(
                Math.pow(enemy.getX() + enemy.getSize()/2 - x, 2) +
                        Math.pow(enemy.getY() + enemy.getSize()/2 - y, 2)
        );

        // Kollidiert, wenn Summe der Radien größer als Abstand ist
        return distance < (enemy.getSize()/2 + size/2);
    }

    // Prüfung, ob das Projektil OOB ist
    public boolean isOutOfBounds(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    // Zeichnen des Projektils
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval((int)x - size/2, (int)y - size/2, size, size); // Zentrum auf Position
    }
}