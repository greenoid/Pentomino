package de.greenoid.game.pentomino.model;

import java.util.List;

/**
 * Interface for computer player strategies in the Pentomino game.
 * Different implementations can provide various levels of AI difficulty.
 */
public interface ComputerStrategy {

    /**
     * Calculates the best move for the computer player given the current game state.
     *
     * @param gameState The current state of the game
     * @return A ComputerMove representing the chosen move, or null if no moves are possible
     */
    ComputerMove calculateMove(GameState gameState);

    /**
     * Gets the name of this strategy for display purposes.
     *
     * @return The strategy name
     */
    String getStrategyName();

    /**
     * Represents a move calculated by a computer strategy.
     */
    class ComputerMove {
        private final PentominoPiece piece;
        private final int row;
        private final int col;

        public ComputerMove(PentominoPiece piece, int row, int col) {
            this.piece = piece;
            this.row = row;
            this.col = col;
        }

        public PentominoPiece getPiece() {
            return piece;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public String toString() {
            return "ComputerMove{" +
                    "piece=" + piece +
                    ", row=" + row +
                    ", col=" + col +
                    '}';
        }
    }
}