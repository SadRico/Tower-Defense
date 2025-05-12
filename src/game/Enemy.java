package game;

public class Enemy {
    private int health;

    public Enemy(int health) {
        this.health = health;
    }

    public void takeDamage(int damage) {
        health -= damage;
        System.out.println("Gegner nimmt " + damage + " Schaden, verbleibende HP: " + health);
        if (health <= 0) {
            System.out.println("Gegner besiegt!");
        }
    }
}
