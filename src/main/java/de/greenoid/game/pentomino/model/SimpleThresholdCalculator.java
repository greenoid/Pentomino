package de.greenoid.game.pentomino.model;

/**
 * Simple threshold calculator based on the number of empty squares remaining.
 * 
 * <p>This calculator triggers a strategy switch when the number of empty squares
 * on the board falls below a specified threshold. This is useful for switching
 * from a fast early-game strategy to a more thorough endgame strategy when
 * the board becomes constrained.
 * 
 * <p>Recommended threshold values for an 8x8 board:
 * <ul>
 *   <li>24: Earlier switch, more time for endgame analysis</li>
 *   <li>28: Balanced, switches around 50% board coverage (default)</li>
 *   <li>30-32: Later switch, reduces total endgame computation time</li>
 * </ul>
 */
public class SimpleThresholdCalculator implements ThresholdCalculator {
    
    private final int thresholdSquares;
    
    /**
     * Creates a threshold calculator based on empty squares.
     *
     * @param thresholdSquares Switch when empty squares <= this value.
     *                         Recommended: 28 for 8x8 board (roughly 3 pieces remaining)
     */
    public SimpleThresholdCalculator(int thresholdSquares) {
        this.thresholdSquares = Math.max(0, thresholdSquares);
    }
    
    /**
     * Default constructor using threshold of 41 squares.
     * This triggers when ~33% of the board is occupied, providing
     * ample time for MinMax endgame analysis. Typically around move 10-14 on an 8x8 board.
     */
    public SimpleThresholdCalculator() {
        this(41);
    }
    
    @Override
    public boolean shouldSwitchStrategy(GameState gameState) {
        int totalSquares = Board.SIZE * Board.SIZE;
        int occupiedSquares = gameState.getBoard().getOccupiedSquareCount();
        int emptySquares = totalSquares - occupiedSquares;
        
        return emptySquares <= thresholdSquares;
    }
    
    @Override
    public String getDescription() {
        return "Simple threshold (switch at " + thresholdSquares + " empty squares)";
    }
    
    /**
     * Gets the threshold value used by this calculator.
     */
    public int getThreshold() {
        return thresholdSquares;
    }
}