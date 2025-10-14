package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced computer strategy using the minimax algorithm with alpha-beta pruning.
 * This strategy searches the game tree to find optimal moves by considering
 * potential opponent responses. It's most effective in endgame situations where
 * the search space is manageable.
 * 
 * <p>The algorithm uses a mobility-based heuristic to evaluate non-terminal positions,
 * preferring positions where the computer has more placement options than the opponent.
 */
public class ComputerStrategyMinMax implements ComputerStrategy {

    private final int maxDepth;
    private int nodesEvaluated;
    private long startTime;
    private static final int MAX_THINKING_TIME_MS = 15000; // 15 seconds safety limit

    /**
     * Creates a new MinMax strategy with the specified search depth.
     *
     * @param maxDepth The maximum search depth (1-5 recommended, 3 is default).
     *                 Higher depth = stronger play but slower computation.
     *                 Depth 1: Only considers immediate moves
     *                 Depth 2: Considers computer move + opponent response
     *                 Depth 3: Computer + opponent + computer (recommended)
     */
    public ComputerStrategyMinMax(int maxDepth) {
        this.maxDepth = Math.max(1, Math.min(maxDepth, 5));
    }

    /**
     * Default constructor using depth 3 for balanced performance.
     */
    public ComputerStrategyMinMax() {
        this(3);
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
            System.out.println("MinMax: Only one move available, skipping search");
            return possibleMoves.get(0);
        }

        ComputerMove bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println("MinMax: Evaluating " + possibleMoves.size() + 
                          " possible moves at depth " + maxDepth);

        // Evaluate each possible move
        for (ComputerMove move : possibleMoves) {
            // Check time limit
            if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME_MS) {
                System.out.println("MinMax: Time limit reached, using best move so far");
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
        System.out.println("MinMax: Evaluated " + nodesEvaluated + 
                          " nodes in " + duration + "ms, best score: " + bestScore);

        return bestMove;
    }

    /**
     * Minimax algorithm with alpha-beta pruning.
     * 
     * @param state The current game state
     * @param depth Remaining search depth
     * @param alpha Best score maximizer can guarantee
     * @param beta Best score minimizer can guarantee
     * @param maximizingPlayer True if it's the computer's turn (maximizing)
     * @return The evaluation score for this position
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
            return evaluatePosition(state, maximizingPlayer);
        }

        List<ComputerMove> moves = findAllPossibleMoves(state);
        
        // If no moves available, this is a terminal state
        if (moves.isEmpty()) {
            return evaluatePosition(state, maximizingPlayer);
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
     * Evaluates a game position using a mobility-based heuristic.
     * Returns positive scores for positions favorable to the computer,
     * negative scores for positions favorable to the opponent.
     * 
     * @param state The game state to evaluate
     * @param fromComputerPerspective True if evaluating from computer's perspective
     * @return The evaluation score (higher is better for computer)
     */
    private int evaluatePosition(GameState state, boolean fromComputerPerspective) {
        // Check for terminal states first
        GameState.GameStatus status = state.getStatus();
        
        if (status != GameState.GameStatus.PLAYING) {
            // Game is over - determine winner
            if (status == GameState.GameStatus.DRAW) {
                return 0; // Draw is neutral
            }
            
            // Determine if computer won or lost
            // Assuming Player 2 is the computer (adjust if needed)
            boolean computerWon = (status == GameState.GameStatus.PLAYER_2_WINS);
            
            // Return extreme scores for wins/losses
            if (computerWon) {
                return Integer.MAX_VALUE - 1000; // Very high but not max (for depth tracking)
            } else {
                return Integer.MIN_VALUE + 1000; // Very low but not min
            }
        }

        // Check if current player has any legal moves
        if (!state.hasLegalMoves()) {
            // Current player can't move = they lose
            // We need to determine if that's the computer or opponent
            GameState.Player currentPlayer = state.getCurrentPlayer();
            boolean computerLoses = (currentPlayer == GameState.Player.PLAYER_2);
            
            if (computerLoses) {
                return Integer.MIN_VALUE + 1000;
            } else {
                return Integer.MAX_VALUE - 1000;
            }
        }

        // Non-terminal position: use mobility heuristic
        // Count how many pieces each player can still place
        int mobilityScore = countLegalMoves(state);
        
        // Add secondary factors for better evaluation
        int positionScore = evaluatePositionalFactors(state);
        
        // Combine scores (mobility is primary factor)
        return mobilityScore * 10 + positionScore;
    }

    /**
     * Counts the number of pieces that can still be placed (mobility metric).
     * Returns positive value if computer has more options, negative if opponent has more.
     */
    private int countLegalMoves(GameState state) {
        GameState.Player originalPlayer = state.getCurrentPlayer();
        int computerMobility = 0;
        int opponentMobility = 0;

        // Count mobility for each piece
        for (PentominoPiece piece : state.getAvailablePieces()) {
            if (state.getBoard().hasLegalMove(piece)) {
                // Check which player this would be for
                // This is a simplification - pieces are shared
                // So we count it as general mobility
                computerMobility++;
            }
        }

        // For a more accurate assessment, we could simulate the opponent's turn
        // but that would be expensive. For now, we use the current mobility
        // as a proxy for both players.
        
        // The player whose turn it is has the advantage
        if (originalPlayer == GameState.Player.PLAYER_2) {
            // Computer's turn - having moves is good
            return computerMobility;
        } else {
            // Opponent's turn - having moves is bad for computer
            return -computerMobility;
        }
    }

    /**
     * Evaluates secondary positional factors.
     * Returns a small score bonus based on board state.
     */
    private int evaluatePositionalFactors(GameState state) {
        int score = 0;
        Board board = state.getBoard();
        
        // Prefer having more empty squares (more flexibility)
        int emptySquares = Board.SIZE * Board.SIZE - board.getOccupiedSquareCount();
        score += emptySquares;
        
        return score;
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
        return "MinMax Strategy (depth=" + maxDepth + ")";
    }

    /**
     * Gets the number of nodes evaluated in the last move calculation.
     * Useful for performance analysis.
     */
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }
}