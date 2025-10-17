package de.greenoid.game.pentomino.model;

/**
 * Proportional MinMax strategy that adjusts search depth based on computer's move count.
 *
 * <p>This strategy uses a depth that increases linearly with the number of computer moves made,
 * controlled by a configurable strength factor:
 * <ul>
 *   <li><b>Depth calculation</b>: depth = floor(computerMoveCount / strengthFactor)</li>
 *   <li><b>Where</b>: computerMoveCount = floor((totalMoves + 1) / 2)</li>
 *   <li><b>Computer move 1</b>: Depth 1 (at strength 1.0)</li>
 *   <li><b>Computer move 2</b>: Depth 2 (at strength 1.0)</li>
 *   <li><b>Computer move N</b>: Depth N (at strength 1.0)</li>
 * </ul>
 * 
 * <p>The strength factor allows fine-tuning of difficulty:
 * <ul>
 *   <li><b>Strength 1.0 (Hard)</b>: Full depth, strongest play</li>
 *   <li><b>Strength 1.5 (Fair)</b>: Moderate depth, balanced play</li>
 *   <li><b>Strength 2.0 (Easy)</b>: Half depth, easier opponent</li>
 * </ul>
 *
 * <p>This approach ensures that:
 * <ul>
 *   <li>Early moves are fast (shallow search)</li>
 *   <li>Late game moves are deep (thorough analysis)</li>
 *   <li>Computation time increases gradually</li>
 *   <li>Difficulty is smoothly adjustable</li>
 * </ul>
 */
public class ComputerStrategyMinMaxProportional implements ComputerStrategy {
    
    private final float strengthFactor;
    private final ComputerStrategyMinMax minMaxStrategy;
    private int currentDepth;
    private int lastMoveCount = -1;
    
    /**
     * Creates a proportional MinMax strategy with the specified strength factor.
     *
     * @param strengthFactor The strength factor (1.0 = hardest, higher = easier).
     *                       Depth = floor(computerMoveCount / strengthFactor).
     *                       Where computerMoveCount = floor((totalMoves + 1) / 2).
     *                       Recommended values: 1.0 (hard), 1.5 (fair), 2.0 (easy)
     */
    public ComputerStrategyMinMaxProportional(float strengthFactor) {
        if (strengthFactor <= 0) {
            throw new IllegalArgumentException("Strength factor must be positive");
        }
        this.strengthFactor = strengthFactor;
        // Start with depth 1 as a default
        this.minMaxStrategy = new ComputerStrategyMinMax(1);
        this.currentDepth = 1;
    }
    
    /**
     * Creates a proportional MinMax strategy with default strength (1.0 - hardest).
     */
    public ComputerStrategyMinMaxProportional() {
        this(1.0f);
    }
    
    @Override
    public ComputerMove calculateMove(GameState gameState) {
        int totalMoveCount = gameState.getMoveCount();
        
        // Calculate computer's move count: computerMoves = floor((totalMoves + 1) / 2)
        // This ensures: move 1 → computer move 1, move 3 → computer move 2, etc.
        int computerMoveCount = (totalMoveCount + 1) / 2;
        
        // Calculate depth based on computer's move count and strength factor
        // Ensure minimum depth of 1
        int calculatedDepth = Math.max(1, (int) Math.floor(computerMoveCount / strengthFactor));
        
        // Cap at reasonable maximum depth to avoid excessive computation
        int targetDepth = Math.min(calculatedDepth, 6);
        
        // Only log depth changes
        if (totalMoveCount != lastMoveCount) {
            lastMoveCount = totalMoveCount;
            currentDepth = targetDepth;
            System.out.println("Proportional MinMax (strength " + strengthFactor +
                             "): Total move " + totalMoveCount +
                             ", Computer move " + computerMoveCount +
                             " -> Depth " + currentDepth);
        }
        
        // Create a new MinMax strategy with the calculated depth if it changed
        ComputerStrategyMinMax strategy;
        if (targetDepth != currentDepth) {
            currentDepth = targetDepth;
            strategy = new ComputerStrategyMinMax(currentDepth);
        } else {
            strategy = new ComputerStrategyMinMax(currentDepth);
        }
        
        // Calculate the move using the appropriate depth
        ComputerMove move = strategy.calculateMove(gameState);
        
        // Clean up the temporary strategy
        strategy.shutdown();
        
        return move;
    }
    
    @Override
    public String getStrategyName() {
        String difficultyLabel;
        if (strengthFactor <= 1.0f) {
            difficultyLabel = "Hard";
        } else if (strengthFactor <= 1.5f) {
            difficultyLabel = "Fair";
        } else {
            difficultyLabel = "Easy";
        }
        
        return String.format("MinMax Proportional [strength=%.1f (%s), depth=%d, parallel]",
                           strengthFactor, difficultyLabel, currentDepth);
    }
    
    /**
     * Gets the current search depth being used.
     */
    public int getCurrentDepth() {
        return currentDepth;
    }
    
    /**
     * Gets the strength factor.
     */
    public float getStrengthFactor() {
        return strengthFactor;
    }
    
    /**
     * Calculates what depth would be used for a given total move count.
     * Useful for testing and analysis.
     *
     * @param totalMoveCount The total number of moves (both players)
     * @return The depth that would be used for this move count
     */
    public int getDepthForMoveCount(int totalMoveCount) {
        int computerMoveCount = (totalMoveCount + 1) / 2;
        int depth = Math.max(1, (int) Math.floor(computerMoveCount / strengthFactor));
        return Math.min(depth, 6);
    }
    
    /**
     * Shuts down the underlying executor service. Should be called when the strategy
     * is no longer needed to free up resources.
     */
    public void shutdown() {
        minMaxStrategy.shutdown();
    }
    
    /**
     * Builder class for creating customized Proportional MinMax instances.
     * 
     * <p>Example:
     * <pre>
     * ComputerStrategy strategy = new ComputerStrategyMinMaxProportional.Builder()
     *     .withStrength(1.5f)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private float strengthFactor = 1.0f;
        
        /**
         * Sets the strength factor.
         * 
         * @param strength The strength factor (1.0 = hardest, higher = easier)
         * @return This builder for chaining
         */
        public Builder withStrength(float strength) {
            if (strength <= 0) {
                throw new IllegalArgumentException("Strength must be positive");
            }
            this.strengthFactor = strength;
            return this;
        }
        
        /**
         * Sets the difficulty preset.
         * 
         * @param difficulty "hard" (1.0), "fair" (1.5), or "easy" (2.0)
         * @return This builder for chaining
         */
        public Builder withDifficulty(String difficulty) {
            switch (difficulty.toLowerCase()) {
                case "hard":
                    this.strengthFactor = 1.0f;
                    break;
                case "fair":
                    this.strengthFactor = 1.5f;
                    break;
                case "easy":
                    this.strengthFactor = 2.0f;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
            }
            return this;
        }
        
        /**
         * Builds the strategy instance.
         * 
         * @return A new ComputerStrategyMinMaxProportional instance
         */
        public ComputerStrategyMinMaxProportional build() {
            return new ComputerStrategyMinMaxProportional(strengthFactor);
        }
    }
}