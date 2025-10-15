package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MinMax strategy variant using diffusion-based heuristic evaluation.
 * This strategy combines the MinMax algorithm with the spatial awareness
 * of the diffusion method from the Open Space strategy.
 * 
 * <p>Instead of counting mobility (number of placeable pieces), this variant
 * evaluates positions based on the connectivity and quality of open spaces
 * using iterative diffusion. This provides better spatial understanding and
 * can distinguish between positions with similar piece counts but different
 * strategic value.
 * 
 * <p>The diffusion heuristic analyzes board topology to identify well-connected
 * open regions, preferring positions that maintain flexibility for future moves.
 */
public class ComputerStrategyMinMaxDiffusion implements ComputerStrategy {

    private final int maxDepth;
    private final int diffusionIterations;
    private int nodesEvaluated;
    private long startTime;
    private static final int MAX_THINKING_TIME_MS = 15000; // 15 seconds safety limit

    /**
     * Creates a new MinMax Diffusion strategy.
     *
     * @param maxDepth The maximum search depth (1-5 recommended, 3 is default)
     * @param diffusionIterations Number of diffusion iterations for evaluation (1-3 recommended)
     */
    public ComputerStrategyMinMaxDiffusion(int maxDepth, int diffusionIterations) {
        this.maxDepth = Math.max(1, Math.min(maxDepth, 5));
        this.diffusionIterations = Math.max(1, Math.min(diffusionIterations, 3));
    }

    /**
     * Default constructor using depth 3 and 2 diffusion iterations for balanced performance.
     */
    public ComputerStrategyMinMaxDiffusion() {
        this(3, 2);
    }

    @Override
    public ComputerMove calculateMove(GameState gameState) {
        nodesEvaluated = 0;
        startTime = System.currentTimeMillis();

        List<ComputerMove> possibleMoves = findAllPossibleMoves(gameState);
        
        if (possibleMoves.isEmpty()) {
            return null;
        }

        // If only one move available, return it immediately
        if (possibleMoves.size() == 1) {
            System.out.println("MinMaxDiffusion: Only one move available, skipping search");
            return possibleMoves.get(0);
        }

        ComputerMove bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println("MinMaxDiffusion: Evaluating " + possibleMoves.size() + 
                          " possible moves at depth " + maxDepth + 
                          " with " + diffusionIterations + " diffusion iterations");

        // Evaluate each possible move
        for (ComputerMove move : possibleMoves) {
            // Check time limit
            if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME_MS) {
                System.out.println("MinMaxDiffusion: Time limit reached, using best move so far");
                break;
            }

            // Make the move in a copy of the game state
            GameState newState = new GameState(gameState);
            newState.makeMove(move.getPiece(), move.getRow(), move.getCol());

            // Evaluate this move using minimax (opponent's turn next, so minimizing)
            int score = minimax(newState, maxDepth - 1, alpha, beta, false);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
                alpha = Math.max(alpha, score);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("MinMaxDiffusion: Evaluated " + nodesEvaluated + 
                          " nodes in " + duration + "ms, best score: " + bestScore);

        return bestMove;
    }

    /**
     * Minimax algorithm with alpha-beta pruning.
     * Uses diffusion-based heuristic for position evaluation.
     */
    private int minimax(GameState state, int depth, int alpha, int beta, 
                        boolean maximizingPlayer) {
        nodesEvaluated++;

        // Check time limit periodically
        if (nodesEvaluated % 1000 == 0 && 
            System.currentTimeMillis() - startTime > MAX_THINKING_TIME_MS) {
            return 0; // Return neutral score if time limit exceeded
        }

        // Terminal conditions
        if (depth == 0 || isTerminal(state)) {
            return evaluatePositionWithDiffusion(state, maximizingPlayer);
        }

        List<ComputerMove> moves = findAllPossibleMoves(state);
        
        // If no moves available, this is a terminal state
        if (moves.isEmpty()) {
            return evaluatePositionWithDiffusion(state, maximizingPlayer);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            
            for (ComputerMove move : moves) {
                GameState newState = new GameState(state);
                newState.makeMove(move.getPiece(), move.getRow(), move.getCol());
                
                int eval = minimax(newState, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
                // Alpha-beta pruning
                if (beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            return maxEval;
            
        } else {
            int minEval = Integer.MAX_VALUE;
            
            for (ComputerMove move : moves) {
                GameState newState = new GameState(state);
                newState.makeMove(move.getPiece(), move.getRow(), move.getCol());
                
                int eval = minimax(newState, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                
                // Alpha-beta pruning
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            return minEval;
        }
    }

    /**
     * Checks if the game state is terminal (no more moves possible or game over).
     */
    private boolean isTerminal(GameState state) {
        return state.getStatus() != GameState.GameStatus.PLAYING;
    }

    /**
     * Evaluates a game position using diffusion-based heuristic.
     * This method combines terminal state detection with spatial analysis
     * through iterative diffusion to assess position quality.
     * 
     * @param state The game state to evaluate
     * @param fromComputerPerspective True if evaluating from computer's perspective
     * @return The evaluation score (higher is better for computer)
     */
    private int evaluatePositionWithDiffusion(GameState state, boolean fromComputerPerspective) {
        // Check for terminal states first
        GameState.GameStatus status = state.getStatus();
        
        if (status != GameState.GameStatus.PLAYING) {
            // Game is over - determine winner
            if (status == GameState.GameStatus.DRAW) {
                return 0; // Draw is neutral
            }
            
            // Determine if computer won or lost (assuming Player 2 is computer)
            boolean computerWon = (status == GameState.GameStatus.PLAYER_2_WINS);
            
            // Return extreme scores for wins/losses
            if (computerWon) {
                return Integer.MAX_VALUE - 1000;
            } else {
                return Integer.MIN_VALUE + 1000;
            }
        }

        // Check if current player has any legal moves
        if (!state.hasLegalMoves()) {
            // Current player can't move = they lose
            GameState.Player currentPlayer = state.getCurrentPlayer();
            boolean computerLoses = (currentPlayer == GameState.Player.PLAYER_2);
            
            if (computerLoses) {
                return Integer.MIN_VALUE + 1000;
            } else {
                return Integer.MAX_VALUE - 1000;
            }
        }

        // Non-terminal position: use diffusion heuristic
        int diffusionScore = evaluateBoardWithDiffusion(state.getBoard());
        
        // Adjust score based on whose turn it is
        // If it's opponent's turn and they have high diffusion (good position),
        // that's bad for computer, so negate it
        GameState.Player currentPlayer = state.getCurrentPlayer();
        if (currentPlayer == GameState.Player.PLAYER_1) {
            // Opponent's turn - good position for them is bad for us
            diffusionScore = -diffusionScore;
        }
        
        return diffusionScore;
    }

    /**
     * Evaluates board quality using iterative diffusion algorithm.
     * Returns a score representing the overall "openness" and connectivity
     * of available spaces on the board.
     * 
     * @param board The board to evaluate
     * @return Total diffusion score (higher means better connected open spaces)
     */
    private int evaluateBoardWithDiffusion(Board board) {
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

        // Calculate total score by summing all values
        int totalScore = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                totalScore += evaluation[row][col];
            }
        }

        return totalScore;
    }

    /**
     * Finds all possible moves for the current player.
     * Considers all available pieces and all their valid placements.
     */
    private List<ComputerMove> findAllPossibleMoves(GameState gameState) {
        List<ComputerMove> possibleMoves = new ArrayList<>();
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        Board board = gameState.getBoard();

        for (PentominoPiece piece : availablePieces) {
            // Try all transformations of this piece
            for (PentominoPiece transformedPiece : getAllTransformations(piece)) {
                // Try all positions on the board
                for (int row = 0; row < Board.SIZE; row++) {
                    for (int col = 0; col < Board.SIZE; col++) {
                        if (board.canPlaceAt(transformedPiece, row, col)) {
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
     * Generates up to 8 different orientations through rotation and flipping.
     */
    private List<PentominoPiece> getAllTransformations(PentominoPiece piece) {
        List<PentominoPiece> transformations = new ArrayList<>();
        PentominoPiece current = piece;

        // All 4 rotations
        for (int i = 0; i < 4; i++) {
            transformations.add(current);
            current = current.rotate();
        }

        // Flip and add 4 more rotations
        current = piece.flip();
        for (int i = 0; i < 4; i++) {
            transformations.add(current);
            current = current.rotate();
        }

        return transformations;
    }

    @Override
    public String getStrategyName() {
        return "MinMax Diffusion (depth=" + maxDepth + ", diff=" + diffusionIterations + ")";
    }

    /**
     * Gets the number of nodes evaluated in the last move calculation.
     * Useful for performance analysis.
     */
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

    /**
     * Gets the number of diffusion iterations used.
     */
    public int getDiffusionIterations() {
        return diffusionIterations;
    }
}