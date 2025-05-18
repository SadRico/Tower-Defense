package main.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class DatabaseManager {
    private Connection dbConnection; // Verbindung zur Datenbank

    public DatabaseManager() {
        // Lässt Verbindung zunächst, bis initializeDatabase aufgerufen wird
    }

    // Initialisiert Datenbankverbindung und erstellt die Tabelle (falls nicht vorhanden)
    public void initializeDatabase() {
        try {
            // Verbindung zur Datenbank herstellen
            dbConnection = DriverManager.getConnection("jdbc:sqlite:tower_defense.db");
            Statement statement = dbConnection.createStatement();

            // Tabelle für Highscores erstellen, falls sie noch nicht existiert
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS highscores (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +         // Automatische ID
                            "player_name TEXT NOT NULL," +                   // Spielername darf nicht null sein
                            "score INTEGER NOT NULL," +                      // Punktestand darf nicht null sein
                            "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +    // Automatischer Zeitstempel
                            ");"
            );
            statement.close();
            System.out.println("Datenbank erfolgreich initialisiert");
        } catch (SQLException e) {

            // Konsolenausgabe + Fehlermeldung
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler bei der Datenbankinitialisierung: " + e.getMessage(),
                    "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Speichert neuen Highscore Eintrag in Datenbank
    public void saveHighscore(String playerName, int score) {
        try {
            // Vorbereitete SQL-Anweisung zur sicheren Übergabe von Parametern
            PreparedStatement pstmt = dbConnection.prepareStatement(
                    "INSERT INTO highscores (player_name, score) VALUES (?, ?)");
            pstmt.setString(1, playerName); // Spielername setzen
            pstmt.setInt(2, score);         // Punktestand setzen
            pstmt.executeUpdate();                      // Eintrag speichern
            pstmt.close();
            System.out.println("Highscore gespeichert");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim Speichern des Highscores: " + e.getMessage(),
                    "Datenbankfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Holtbeste Highscores (nach Punktzahl sortiert, begrenzt auf "limit")
    public List<String> getTopHighscores(int limit) {
        List<String> highscores = new ArrayList<>();
        try {
            Statement stmt = dbConnection.createStatement();

            // Höchste Scores zuerst
            ResultSet rs = stmt.executeQuery(
                    "SELECT player_name, score FROM highscores ORDER BY score DESC LIMIT " + limit); // "DESC" = absteigend

            // Ergebnisse in Liste einfügen
            while (rs.next()) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                highscores.add(name + ": " + score);
            }

            rs.close();
            stmt.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return highscores;
    }
}