package main.model;

import main.util.Constants;

import java.awt.*;

public class Enemy {
    private double x, y;                     // Aktuelle Position des Gegners (mit Kommazahlen für glattere Bewegung)
    private double speed = 3.0;              // Bewegungsgeschwindigkeit pro Update
    private int size = 30;                   // Größe (Durchmesser) des Gegners
    private int health = 50;                 // Lebenspunkte
    private Point destination;              // Zielpunkt (Endpunkt des Pfades)

    // (Konstruktor) setzt Startposition und Zielpunkt
    public Enemy(int x, int y, Point destination) {
        this.x = x;
        this.y = y;
        this.destination = destination;
    }

    // Getter für Position
    public double getX() {
        return x;
    }
    // Getter für Position
    public double getY() {
        return y;
    }

    // Getter für Größe
    public int getSize() {
        return size;
    }
    // Getter für Lebenspunkte
    public int getHealth() {
        return health;
    }

    // Getter für Schaden zuzufügen
    public void takeDamage(int damage) {
        health -= damage;
    }

    // Bewegung in Richtung Zielpunkt
    public void move() {
        double targetX = destination.x * Constants.CELL_SIZE; // Zielkoordinaten in Pixel
        double targetY = destination.y * Constants.CELL_SIZE; // Zielkoordinaten in Pixel

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy); // Entfernung zum Ziel

        // Nur bewegen, wenn Ziel noch nicht erreicht ist
        if (distance > speed) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    // Überprüfut ob Ziel erreicht wurde (Abstand kleiner als halbe Größe)
    public boolean reachedDestination(int destX, int destY) {
        double distance = Math.sqrt(
                Math.pow(destX - x, 2) + Math.pow(destY - y, 2)
        );
        return distance < size / 2;
    }

    // Gegner zeichnen
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int)x, (int)y, size, size); // Körper

        // Lebensbalken zeichnen
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y - 10, size, 5); // Rahmen
        g.setColor(Color.GREEN); // Farbe
        int healthWidth = (int)((health / 50.0) * size); // Füllung passend zu health
        g.fillRect((int)x, (int)y - 10, healthWidth, 5);
    }
}