import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameUI extends JFrame {
    private JPanel gamePanel;
    private JLabel scoreLabel;

    public GameUI() {
        setTitle("Tower Defense");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel für Score
        JPanel topPanel = new JPanel();
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(scoreLabel);
        add(topPanel, BorderLayout.NORTH);

        // Game Panel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Spiellogik-Rendering hier
            }
        };
        gamePanel.setBackground(Color.WHITE);
        add(gamePanel, BorderLayout.CENTER);

        // Info Panel (optional)
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("Klicke, um Türme zu platzieren");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void displayHighscoreDialog(int finalScore) {
        String name = JOptionPane.showInputDialog(this,
                "Spiel beendet! Dein Score: " + finalScore + "\nGib deinen Namen ein:");

        if (name != null && !name.trim().isEmpty()) {
            // Name und Score in Datenbank speichern
            saveHighscore(name, finalScore);

            // Highscores anzeigen
            showHighscores();
        }
    }

    private void saveHighscore(String name, int score) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:tower_defense.db")) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO highscores (player_name, score) VALUES (?, ?)");
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern des Highscores: " + e.getMessage());
        }
    }

    private void showHighscores() {
        StringBuilder message = new StringBuilder("Top 5 Highscores:\n\n");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:tower_defense.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT player_name, score FROM highscores ORDER BY score DESC LIMIT 5");

            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("player_name");
                int score = rs.getInt("score");
                message.append(rank++).append(". ").append(name).append(": ").append(score).append("\n");
            }

            JOptionPane.showMessageDialog(this, message.toString(), "Highscores", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Laden der Highscores: " + e.getMessage());
        }
    }
}