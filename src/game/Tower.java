package game;

public class Tower {
    private int x, y;
    private int damage;

    public Tower(int x, int y, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
    }

    public void attack(Enemy enemy) {
        enemy.takeDamage(damage);
    }
}

