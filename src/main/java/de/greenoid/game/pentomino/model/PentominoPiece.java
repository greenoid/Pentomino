package de.greenoid.game.pentomino.model;

import java.awt.Color;
import java.util.*;

/**
 * Represents a pentomino piece with its shape, color, and transformations.
 */
public class PentominoPiece {
    public enum PieceType {
        I, L, P, N, T, U, V, W, X, Z, F, Y
    }

    private final PieceType type;
    private final Color color;
    private final List<Point> shape;
    private final String name;

    public PentominoPiece(PieceType type, Color color, List<Point> shape, String name) {
        this.type = type;
        this.color = color;
        this.shape = new ArrayList<>(shape);
        this.name = name;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public List<Point> getShape() {
        return new ArrayList<>(shape);
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a new piece with the shape rotated by 90 degrees clockwise.
     */
    public PentominoPiece rotate() {
        List<Point> rotatedShape = new ArrayList<>();
        for (Point point : shape) {
            rotatedShape.add(point.rotate(1));
        }
        return new PentominoPiece(type, color, rotatedShape, name);
    }

    /**
     * Creates a new piece with the shape flipped vertically.
     */
    public PentominoPiece flip() {
        List<Point> flippedShape = new ArrayList<>();
        for (Point point : shape) {
            flippedShape.add(point.flip());
        }
        return new PentominoPiece(type, color, flippedShape, name);
    }

    /**
     * Gets all possible transformations (rotations and flips) of this piece.
     */
    public Set<PentominoPiece> getAllTransformations() {
        Set<PentominoPiece> transformations = new HashSet<>();
        PentominoPiece current = this;

        // All 4 rotations
        for (int i = 0; i < 4; i++) {
            transformations.add(current);
            PentominoPiece flipped = current.flip();
            transformations.add(flipped);

            // Add rotations of the flipped piece
            for (int j = 0; j < 3; j++) {
                flipped = flipped.rotate();
                transformations.add(flipped);
            }

            current = current.rotate();
        }

        return transformations;
    }

    /**
     * Normalizes the piece by translating it to start at (0,0).
     */
    public PentominoPiece normalize() {
        int minX = shape.stream().mapToInt(Point::getX).min().orElse(0);
        int minY = shape.stream().mapToInt(Point::getY).min().orElse(0);

        List<Point> normalizedShape = new ArrayList<>();
        for (Point point : shape) {
            normalizedShape.add(new Point(point.getX() - minX, point.getY() - minY));
        }

        return new PentominoPiece(type, color, normalizedShape, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PentominoPiece that = (PentominoPiece) obj;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    /**
     * Creates all 12 standard pentomino pieces with their correct shapes and colors.
     */
    public static List<PentominoPiece> createAllPieces() {
        List<PentominoPiece> pieces = new ArrayList<>();

        // F-piece (red) - from SVG coordinates: rects at (125,225), (125,250), (125,275), (150,225), (100,250)
        pieces.add(new PentominoPiece(PieceType.F, new Color(255, 0, 0),
            Arrays.asList(new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,0), new Point(-1,1)), "F"));

        // I-piece (dark blue) - from SVG: vertical line at x=200, y=175-275
        pieces.add(new PentominoPiece(PieceType.I, new Color(0, 0, 139),
            Arrays.asList(new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3), new Point(0,4)), "I"));

        // L-piece (light brown) - from SVG: rects at (250,200), (250,225), (250,250), (250,275), (275,275)
        pieces.add(new PentominoPiece(PieceType.L, new Color(210, 180, 140),
            Arrays.asList(new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3), new Point(1,3)), "L"));

        // N-piece (dark brown) - 3 squares in a line and 2 squares in a second line, end of first line adjacent to begin of second line
        pieces.add(new PentominoPiece(PieceType.N, new Color(139, 69, 19),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(2,0), new Point(2,1), new Point(3,1)), "N"));

        // P-piece (dark grey) - from SVG: rects at (450,225), (450,250), (450,275), (475,225), (475,250)
        pieces.add(new PentominoPiece(PieceType.P, new Color(105, 105, 105),
            Arrays.asList(new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,0), new Point(1,1)), "P"));

        // T-piece (light grey) - from SVG: rects at (525,225), (550,225), (575,225), (550,250), (550,275)
        pieces.add(new PentominoPiece(PieceType.T, new Color(211, 211, 211),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(2,0), new Point(1,1), new Point(1,2)), "T"));

        // U-piece (dark brown) - from SVG: rects at (100,350), (100,375), (125,375), (150,375), (150,350)
        pieces.add(new PentominoPiece(PieceType.U, new Color(139, 69, 19),
            Arrays.asList(new Point(0,0), new Point(0,1), new Point(1,1), new Point(2,1), new Point(2,0)), "U"));

        // V-piece (orange) - from SVG with transform matrix (rotated)
        pieces.add(new PentominoPiece(PieceType.V, new Color(255, 165, 0),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(2,0), new Point(2,1), new Point(2,2)), "V"));

        // W-piece (black) - from SVG with transform matrix (rotated)
        pieces.add(new PentominoPiece(PieceType.W, new Color(0, 0, 0),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(1,1), new Point(2,1), new Point(2,2)), "W"));

        // X-piece (light green) - from SVG with transform matrix (rotated)
        pieces.add(new PentominoPiece(PieceType.X, new Color(144, 238, 144),
            Arrays.asList(new Point(1,0), new Point(0,1), new Point(1,1), new Point(2,1), new Point(1,2)), "X"));

        // Y-piece (dark green) - a line of four squares and one square adjacent at the second square of the first line
        pieces.add(new PentominoPiece(PieceType.Y, new Color(0, 100, 0),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0), new Point(1,1)), "Y"));

        // Z-piece (yellow) - from SVG: rects at (550,325), (575,325), (575,350), (575,375), (600,375)
        pieces.add(new PentominoPiece(PieceType.Z, new Color(255, 255, 0),
            Arrays.asList(new Point(0,0), new Point(1,0), new Point(1,1), new Point(1,2), new Point(2,2)), "Z"));

        return pieces;
    }
}