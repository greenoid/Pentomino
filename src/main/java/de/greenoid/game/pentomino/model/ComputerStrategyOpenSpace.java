package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advanced computer strategy that uses iterative diffusion to evaluate board positions.
 * This strategy prefers moves that keep more future placement options open by analyzing
 * the "openness" of board positions through a diffusion algorithm.
 */
public class ComputerStrategyOpenSpace implements ComputerStrategy {

    private final int diffusionIterations;
    private final Random random = new Random();

    /**
     * Creates a new Open Space strategy with the specified number of diffusion iterations.
     *
     * @param diffusionIterations Number of diffusion iterations to perform (1-5 recommended).
     *                           Higher values consider broader board patterns but are slower.
     *                           Recommended: 3 for balanced performance.
     */
    public ComputerStrategyOpenSpace(int diffusionIterations) {
        this.diffusionIterations = Math.max(1, diffusionIterations);
    }

    @Override
    public ComputerMove calculateMove(GameState gameState) {
        // Evaluate board openness using iterative diffusion
        int[][] evaluation = evaluateBoardOpenness(gameState.getBoard());

        // Find all possible moves
        List<ComputerMove> possibleMoves = findAllPossibleMoves(gameState);

        if (possibleMoves.isEmpty()) {
            return null; // No possible moves
        }

        // Score all moves and find the best ones
        int bestScore = Integer.MIN_VALUE;
        List<ComputerMove> bestMoves = new ArrayList<>();

        for (ComputerMove move : possibleMoves) {
            int score = scorePossibleMove(move, evaluation);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        // Random selection among best moves (for variety and unpredictability)
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    @Override
    public String getStrategyName() {
        return "Open Space Strategy (" + diffusionIterations + " iterations)";
    }

    /**
     * Evaluates board positions using iterative diffusion algorithm.
     * Each position gets a value based on how much "open space" is around it.
     *
     * @param board The current game board
     * @return 8x8 grid where higher values indicate more desirable positions
     */
    private int[][] evaluateBoardOpenness(Board board) {
        // Initialize evaluation grid: 1 for free positions, 0 for occupied
        int[][] evaluation = new int[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                evaluation[row][col] = board.isOccupied(row, col) ? 0 : 1;
            }
        }

        // Perform iterative diffusion
        for (int iteration = 0; iteration < diffusionIterations; iteration++) {
            int[][] newEvaluation = new int[Board.SIZE][Board.SIZE];

            for (int row = 0; row < Board.SIZE; row++) {
                for (int col = 0; col < Board.SIZE; col++) {
                    // Sum values of all 8 neighbors
                    int sum = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) {
                                continue; // Skip the center cell itself
                            }

                            int neighborRow = row + dr;
                            int neighborCol = col + dc;

                            // Check if neighbor is within bounds
                            if (neighborRow >= 0 && neighborRow < Board.SIZE &&
                                neighborCol >= 0 && neighborCol < Board.SIZE) {
                                sum += evaluation[neighborRow][neighborCol];
                            }
                        }
                    }
                    newEvaluation[row][col] = sum;
                }
            }

            evaluation = newEvaluation;
        }

        return evaluation;
    }

    /**
     * Calculates the score for a possible move by summing the evaluation values
     * at all positions the piece would occupy.
     *
     * @param move The move to score
     * @param evaluation The board evaluation grid
     * @return The total score for this move (higher is better)
     */
    private int scorePossibleMove(ComputerMove move, int[][] evaluation) {
        int score = 0;
        PentominoPiece piece = move.getPiece();
        int startRow = move.getRow();
        int startCol = move.getCol();

        // Sum evaluation values for all 5 squares of the pentomino piece
        for (Point point : piece.getShape()) {
            int row = startRow + point.getY();
            int col = startCol + point.getX();

            // Add the evaluation value at this position
            if (row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE) {
                score += evaluation[row][col];
            }
        }

        return score;
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
     * Generates all 8 possible orientations through rotation and flipping.
     *
     * @param piece The original piece
     * @return List of all transformations
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