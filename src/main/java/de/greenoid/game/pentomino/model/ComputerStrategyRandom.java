package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Random computer strategy that finds all possible moves and selects one randomly.
 * This provides a basic computer opponent for testing and casual play.
 */
public class ComputerStrategyRandom implements ComputerStrategy {

    private final Random random = new Random();

    @Override
    public ComputerMove calculateMove(GameState gameState) {
        List<ComputerMove> possibleMoves = findAllPossibleMoves(gameState);

        if (possibleMoves.isEmpty()) {
            return null; // No possible moves
        }

        // Select a random move from all possible moves
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    @Override
    public String getStrategyName() {
        return "Random Strategy";
    }

    /**
     * Finds all possible moves for the current player given the game state.
     * Considers all pieces, all positions, and all transformations.
     *
     * @param gameState The current game state
     * @return List of all possible moves
     */
    private List<ComputerMove> findAllPossibleMoves(GameState gameState) {
        List<ComputerMove> possibleMoves = new ArrayList<>();
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();

        for (PentominoPiece piece : availablePieces) {
            // Try all transformations of this piece
            for (PentominoPiece transformedPiece : getAllTransformations(piece)) {
                // Try all positions on the board
                for (int row = 0; row < Board.SIZE; row++) {
                    for (int col = 0; col < Board.SIZE; col++) {
                        if (gameState.getBoard().canPlaceAt(transformedPiece, row, col)) {
                            possibleMoves.add(new ComputerMove(transformedPiece, row, col));
                        }
                    }
                }
            }
        }

        return possibleMoves;
    }

    /**
     * Gets all possible transformations (rotations and flips) of a piece.
     * This is more efficient than generating them during move search.
     *
     * @param piece The original piece
     * @return List of all 8 possible transformations
     */
    private List<PentominoPiece> getAllTransformations(PentominoPiece piece) {
        List<PentominoPiece> transformations = new ArrayList<>();
        PentominoPiece current = piece;

        // All 4 rotations
        for (int i = 0; i < 4; i++) {
            transformations.add(current);
            PentominoPiece flipped = current.flip();
            transformations.add(flipped);

            // Add rotations of the flipped piece (except the original flip)
            for (int j = 0; j < 3; j++) {
                flipped = flipped.rotate();
                transformations.add(flipped);
            }

            current = current.rotate();
        }

        return transformations;
    }
}