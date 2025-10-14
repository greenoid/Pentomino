package de.greenoid.game.pentomino.model;

/**
 * Advanced threshold calculator based on board openness using iterative diffusion.
 * 
 * <p>This calculator uses the same diffusion algorithm as ComputerStrategyOpenSpace
 * to evaluate board positions. It triggers a strategy switch when the maximum
 * openness value falls below a threshold, indicating that the board has become
 * sufficiently constrained for endgame analysis.
 * 
 * <p>This approach is more adaptive than simply counting empty squares, as it
 * considers the topology and connectivity of available spaces. A low maximum
 * openness value indicates that the board has many isolated or hard-to-reach areas,
 * making it a good time to switch to more thorough tactical analysis.
 * 
 * <p>Recommended threshold values:
 * <ul>
 *   <li>40-50: Earlier switch, sensitive to board fragmentation</li>
 *   <li>50-60: Balanced (default)</li>
 *   <li>60-80: Later switch, only switches when board is very constrained</li>
 * </ul>
 */
public class DiffusionThresholdCalculator implements ThresholdCalculator {
    
    private final int maxOpennessThreshold;
    private final int diffusionIterations;
    
    /**
     * Creates a diffusion-based threshold calculator.
     *
     * @param maxOpennessThreshold Switch when max openness <= this value.
     *                            Recommended: 50 for 8x8 board
     * @param diffusionIterations Number of diffusion iterations to perform.
     *                           Higher values consider broader patterns.
     *                           Recommended: 3 for balanced performance
     */
    public DiffusionThresholdCalculator(int maxOpennessThreshold, int diffusionIterations) {
        this.maxOpennessThreshold = Math.max(0, maxOpennessThreshold);
        this.diffusionIterations = Math.max(1, Math.min(diffusionIterations, 5));
    }
    
    /**
     * Default constructor using threshold of 50 and 3 diffusion iterations.
     */
    public DiffusionThresholdCalculator() {
        this(50, 3);
    }
    
    @Override
    public boolean shouldSwitchStrategy(GameState gameState) {
        int[][] evaluation = evaluateBoardOpenness(gameState.getBoard());
        int maxOpenness = getMaxValue(evaluation);
        
        return maxOpenness <= maxOpennessThreshold;
    }
    
    @Override
    public String getDescription() {
        return "Diffusion threshold (switch at max openness <= " + 
               maxOpennessThreshold + ", " + diffusionIterations + " iterations)";
    }
    
    /**
     * Evaluates board positions using iterative diffusion algorithm.
     * This is the same algorithm used by ComputerStrategyOpenSpace.
     * 
     * @param board The current game board
     * @return 8x8 grid where higher values indicate more desirable/open positions
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
     * Finds the maximum value in the evaluation grid.
     * 
     * @param evaluation The board evaluation grid
     * @return The maximum openness value
     */
    private int getMaxValue(int[][] evaluation) {
        int maxValue = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                maxValue = Math.max(maxValue, evaluation[row][col]);
            }
        }
        return maxValue;
    }
    
    /**
     * Gets the threshold value used by this calculator.
     */
    public int getThreshold() {
        return maxOpennessThreshold;
    }
    
    /**
     * Gets the number of diffusion iterations used.
     */
    public int getDiffusionIterations() {
        return diffusionIterations;
    }
}