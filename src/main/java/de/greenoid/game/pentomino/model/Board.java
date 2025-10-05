package de.greenoid.game.pentomino.model;

import java.util.Arrays;

/**
 * Represents the 8x8 game board for the Pentomino game.
 */
public class Board {
    public static final int SIZE = 8;
    private final PentominoPiece[][] board;
    private final boolean[][] occupied;

    public Board() {
        this.board = new PentominoPiece[SIZE][SIZE];
        this.occupied = new boolean[SIZE][SIZE];
    }

    /**
     * Creates a copy of another board.
     */
    public Board(Board other) {
        this.board = new PentominoPiece[SIZE][SIZE];
        this.occupied = new boolean[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(other.board[row], 0, this.board[row], 0, SIZE);
            System.arraycopy(other.occupied[row], 0, this.occupied[row], 0, SIZE);
        }
    }

    /**
     * Checks if a position on the board is within bounds.
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * Checks if a position on the board is occupied.
     */
    public boolean isOccupied(int row, int col) {
        if (!isValidPosition(row, col)) {
            return true; // Treat out-of-bounds as occupied
        }
        return occupied[row][col];
    }

    /**
     * Gets the piece at a specific position.
     */
    public PentominoPiece getPiece(int row, int col) {
        if (!isValidPosition(row, col)) {
            return null;
        }
        return board[row][col];
    }

    /**
     * Attempts to place a piece on the board at the specified position.
     * Returns true if successful, false if the placement is invalid.
     */
    public boolean placePiece(PentominoPiece piece, int startRow, int startCol) {
        // Check if all squares of the piece can be placed
        for (Point point : piece.getShape()) {
            int row = startRow + point.getY();
            int col = startCol + point.getX();

            if (!isValidPosition(row, col) || isOccupied(row, col)) {
                return false;
            }
        }

        // Place the piece
        for (Point point : piece.getShape()) {
            int row = startRow + point.getY();
            int col = startCol + point.getX();
            board[row][col] = piece;
            occupied[row][col] = true;
        }

        return true;
    }

    /**
     * Removes a piece from the board. Used for undoing moves or checking validity.
     */
    public void removePiece(PentominoPiece piece, int startRow, int startCol) {
        for (Point point : piece.getShape()) {
            int row = startRow + point.getY();
            int col = startCol + point.getX();

            if (isValidPosition(row, col)) {
                board[row][col] = null;
                occupied[row][col] = false;
            }
        }
    }

    /**
     * Checks if there are any legal moves available for the given piece.
     */
    public boolean hasLegalMove(PentominoPiece piece) {
        // Try all possible positions and transformations
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Try the piece in its current orientation
                if (canPlaceAt(piece, row, col)) {
                    return true;
                }

                // Try all rotations
                PentominoPiece rotated = piece;
                for (int rotation = 0; rotation < 3; rotation++) {
                    rotated = rotated.rotate();
                    if (canPlaceAt(rotated, row, col)) {
                        return true;
                    }
                }

                // Try flipped version
                PentominoPiece flipped = piece.flip();
                if (canPlaceAt(flipped, row, col)) {
                    return true;
                }

                // Try rotations of flipped version
                for (int rotation = 0; rotation < 3; rotation++) {
                    flipped = flipped.rotate();
                    if (canPlaceAt(flipped, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a piece can be placed at a specific position without actually placing it.
     * Used by computer strategies and other external classes.
     */
    public boolean canPlaceAt(PentominoPiece piece, int startRow, int startCol) {
        for (Point point : piece.getShape()) {
            int row = startRow + point.getY();
            int col = startCol + point.getX();

            if (!isValidPosition(row, col) || isOccupied(row, col)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the number of occupied squares on the board.
     */
    public int getOccupiedSquareCount() {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (occupied[row][col]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if the board is completely full.
     */
    public boolean isFull() {
        return getOccupiedSquareCount() == SIZE * SIZE;
    }

    /**
     * Clears the board completely.
     */
    public void clear() {
        for (int row = 0; row < SIZE; row++) {
            Arrays.fill(board[row], null);
            Arrays.fill(occupied[row], false);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board state:\n");
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (occupied[row][col]) {
                    sb.append(board[row][col].getType().toString().charAt(0)).append(" ");
                } else {
                    sb.append(". ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}