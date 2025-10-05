package de.greenoid.game.pentomino.model;

/**
 * Represents a 2D coordinate point on the game board.
 */
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point add(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public Point rotate(int times) {
        Point result = this;
        for (int i = 0; i < times; i++) {
            result = new Point(result.y, -result.x);
        }
        return result;
    }

    public Point flip() {
        return new Point(x, -y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}