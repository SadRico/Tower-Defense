package db;

import java.sql.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private String dbPath;
    private Connection connection;

    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    public void connect() {
        try {
            // Da 'data' ein Package ist, gehe davon aus, dass die Datei 'game.db' im Ressourcenordner liegt
            URI dbUri = getClass().getClassLoader().getResource(dbPath).toURI();
            File dbFile = new File(dbUri);

            if (!dbFile.exists()) {
                System.err.println("Fehler: Die Datei 'game.db' existiert nicht unter dem angegebenen Pfad.");
                return;
            }

            // Jetzt können wir den richtigen URL für die Verbindung setzen
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            System.out.println("Verbindung zur Datenbank erfolgreich.");
        } catch (SQLException | NullPointerException | URISyntaxException e) {
            System.err.println("Fehler bei der Verbindung zur Datenbank:");
            e.printStackTrace();
            connection = null;
        }
    }

    public void createTables() {
        if (connection == null) {
            System.err.println("Fehler: Verbindung zur Datenbank ist null!");
            return;
        }

        // SQL zum Erstellen der Tabelle, falls sie noch nicht existiert
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "score INTEGER NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabelle 'player' wurde erstellt.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("DB Verbindung geschlossen.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Datenbankverbindung:");
            e.printStackTrace();
        }
    }

    public void insertPlayer(String name, int score) {
        if (connection == null) {
            System.err.println("Fehler: Keine Datenbankverbindung.");
            return;
        }

        String sql = "INSERT INTO player (name, score) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
            System.out.println("Spieler erfolgreich eingefügt.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Einfügen des Spielers:");
            e.printStackTrace();
        }
    }

    public List<String> getHighscores() {
        if (connection == null) {
            System.err.println("Fehler: Keine Datenbankverbindung.");
            return new ArrayList<>();
        }

        List<String> highscores = new ArrayList<>();
        String sql = "SELECT name, score FROM player ORDER BY score DESC LIMIT 10";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                highscores.add(rs.getString("name") + ": " + rs.getInt("score"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return highscores;
    }
}
