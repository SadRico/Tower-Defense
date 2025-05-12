package db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;
    private String dbPath;

    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    // Verbindung zur Datenbank herstellen
    public void connect() {
        try {
            File dbFile = new File(dbPath);

            // Überprüfen, ob der Ordner existiert, andernfalls erstellen
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();  // Ordner erstellen, falls nicht vorhanden
            }

            // Überprüfen, ob die Datenbankdatei existiert und ggf. erstellen
            if (!dbFile.exists()) {
                dbFile.createNewFile(); // Neue Datei erstellen
            }

            // Verbindung zur SQLite-Datenbank herstellen
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(true); // Automatische Bestätigung der Transaktionen
            System.out.println("Verbindung zur Datenbank erfolgreich.");
        } catch (SQLException | IOException e) {
            System.err.println("Fehler bei der Verbindung zur Datenbank: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tabelle erstellen
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

        try (var stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabelle 'player' wurde erstellt.");
        } catch (SQLException e) {
            System.err.println("Fehler bei der Erstellung der Tabelle:");
            e.printStackTrace();
        }
    }

    // Spieler in die Datenbank einfügen
    public void insertPlayer(String name, int score) {
        if (connection == null) {
            System.err.println("Fehler: Verbindung zur Datenbank ist null!");
            return;
        }

        String insertSQL = "INSERT INTO player (name, score) VALUES (?, ?)";

        try (var pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
            System.out.println("Spieler erfolgreich eingefügt.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Einfügen des Spielers:");
            e.printStackTrace();
        }
    }

    // Verbindung zur Datenbank schließen
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("DB Verbindung geschlossen.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Verbindung:");
            e.printStackTrace();
        }
    }
}
