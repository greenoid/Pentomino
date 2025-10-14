package de.greenoid.game.pentomino.model;

/**
 * Composite computer strategy that combines early-game and endgame strategies.
 * 
 * <p>This strategy uses a two-phase approach:
 * <ul>
 *   <li><b>Early game</b>: Uses a fast, heuristic-based strategy (default: OpenSpace)
 *       for quick moves while the board is open</li>
 *   <li><b>Endgame</b>: Switches to a thorough, tactical strategy (default: MinMax)
 *       when the board becomes constrained</li>
 * </ul>
 * 
 * <p>The switch between strategies is determined by a {@link ThresholdCalculator},
 * which can be based on various metrics such as empty squares or board openness.
 * 
 * <p>This approach provides the best of both worlds: fast early moves with minimal
 * computation, and optimal endgame play when it matters most.
 * 
 * <p>Example usage:
 * <pre>
 * // Create with default strategies and threshold
 * ComputerStrategy strategy = new ComputerStrategyComposite();
 * 
 * // Or customize each component
 * ComputerStrategy strategy = new ComputerStrategyComposite(
 *     new ComputerStrategyOpenSpace(3),
 *     new ComputerStrategyMinMax(3),
 *     new SimpleThresholdCalculator(28)
 * );
 * </pre>
 */
public class ComputerStrategyComposite implements ComputerStrategy {
    
    private final ComputerStrategy earlyGameStrategy;
    private final ComputerStrategy endGameStrategy;
    private final ThresholdCalculator thresholdCalculator;
    
    private boolean hasSwitched = false;
    private int switchedAtMove = -1;
    
    /**
     * Creates a composite strategy with specified early game, endgame strategies,
     * and threshold calculator.
     *
     * @param earlyGameStrategy Strategy to use in early game (e.g., OpenSpace)
     * @param endGameStrategy Strategy to use in endgame (e.g., MinMax)
     * @param thresholdCalculator Calculator to determine when to switch
     */
    public ComputerStrategyComposite(
            ComputerStrategy earlyGameStrategy,
            ComputerStrategy endGameStrategy,
            ThresholdCalculator thresholdCalculator) {
        
        if (earlyGameStrategy == null || endGameStrategy == null || thresholdCalculator == null) {
            throw new IllegalArgumentException("All strategy components must be non-null");
        }
        
        this.earlyGameStrategy = earlyGameStrategy;
        this.endGameStrategy = endGameStrategy;
        this.thresholdCalculator = thresholdCalculator;
    }
    
    /**
     * Creates a composite strategy with default configuration:
     * <ul>
     *   <li>Early game: ComputerStrategyOpenSpace with 3 diffusion iterations</li>
     *   <li>Endgame: ComputerStrategyMinMax with depth 3</li>
     *   <li>Threshold: SimpleThresholdCalculator with 41 empty squares (~33% board occupancy)</li>
     * </ul>
     */
    public ComputerStrategyComposite() {
        this(new ComputerStrategyOpenSpace(3),
             new ComputerStrategyMinMax(3),
             new SimpleThresholdCalculator(41));
    }
    
    @Override
    public ComputerMove calculateMove(GameState gameState) {
        // Check if we should switch strategies
        if (!hasSwitched && thresholdCalculator.shouldSwitchStrategy(gameState)) {
            hasSwitched = true;
            switchedAtMove = gameState.getMoveCount();
            
            int emptySquares = Board.SIZE * Board.SIZE -
                              gameState.getBoard().getOccupiedSquareCount();
            
            System.out.println("Composite: Switching to endgame at move " + switchedAtMove +
                             " (" + emptySquares + " empty squares)");
        }
        
        // Use appropriate strategy
        ComputerStrategy activeStrategy = hasSwitched ? endGameStrategy : earlyGameStrategy;
        
        return activeStrategy.calculateMove(gameState);
    }
    
    @Override
    public String getStrategyName() {
        if (hasSwitched) {
            return "Composite Strategy [ENDGAME: " + endGameStrategy.getStrategyName() + "]";
        } else {
            return "Composite Strategy [EARLY: " + earlyGameStrategy.getStrategyName() + "]";
        }
    }
    
    /**
     * Checks if the strategy has switched to endgame mode.
     * 
     * @return true if currently using endgame strategy, false if still in early game
     */
    public boolean hasSwitched() {
        return hasSwitched;
    }
    
    /**
     * Gets the move number when the switch to endgame occurred.
     * 
     * @return The move count at switch time, or -1 if not yet switched
     */
    public int getSwitchMove() {
        return switchedAtMove;
    }
    
    /**
     * Gets the early game strategy used by this composite.
     */
    public ComputerStrategy getEarlyGameStrategy() {
        return earlyGameStrategy;
    }
    
    /**
     * Gets the endgame strategy used by this composite.
     */
    public ComputerStrategy getEndGameStrategy() {
        return endGameStrategy;
    }
    
    /**
     * Gets the threshold calculator used by this composite.
     */
    public ThresholdCalculator getThresholdCalculator() {
        return thresholdCalculator;
    }
    
    /**
     * Resets the switch state. Useful for testing or if the game is reset.
     * Note: This is not typically needed during normal gameplay.
     */
    public void resetSwitchState() {
        hasSwitched = false;
        switchedAtMove = -1;
    }
    
    /**
     * Builder class for creating customized CompositeStrategy instances.
     * Provides a fluent API for configuration.
     * 
     * <p>Example:
     * <pre>
     * ComputerStrategy strategy = new ComputerStrategyComposite.Builder()
     *     .withEarlyGameStrategy(new ComputerStrategyOpenSpace(4))
     *     .withEndGameStrategy(new ComputerStrategyMinMax(4))
     *     .withThreshold(new SimpleThresholdCalculator(30))
     *     .build();
     * </pre>
     */
    public static class Builder {
        private ComputerStrategy earlyGameStrategy;
        private ComputerStrategy endGameStrategy;
        private ThresholdCalculator thresholdCalculator;
        
        /**
         * Sets the early game strategy.
         */
        public Builder withEarlyGameStrategy(ComputerStrategy strategy) {
            this.earlyGameStrategy = strategy;
            return this;
        }
        
        /**
         * Sets the endgame strategy.
         */
        public Builder withEndGameStrategy(ComputerStrategy strategy) {
            this.endGameStrategy = strategy;
            return this;
        }
        
        /**
         * Sets the threshold calculator.
         */
        public Builder withThreshold(ThresholdCalculator calculator) {
            this.thresholdCalculator = calculator;
            return this;
        }
        
        /**
         * Sets a simple threshold based on empty squares.
         * 
         * @param emptySquares The threshold for empty squares
         */
        public Builder withSimpleThreshold(int emptySquares) {
            this.thresholdCalculator = new SimpleThresholdCalculator(emptySquares);
            return this;
        }
        
        /**
         * Sets a diffusion-based threshold.
         * 
         * @param maxOpenness The maximum openness threshold
         * @param iterations Number of diffusion iterations
         */
        public Builder withDiffusionThreshold(int maxOpenness, int iterations) {
            this.thresholdCalculator = new DiffusionThresholdCalculator(maxOpenness, iterations);
            return this;
        }
        
        /**
         * Builds the composite strategy with the configured components.
         * Uses defaults for any components not explicitly set.
         */
        public ComputerStrategyComposite build() {
            // Use defaults if not specified
            ComputerStrategy early = earlyGameStrategy != null ? 
                earlyGameStrategy : new ComputerStrategyOpenSpace(3);
            
            ComputerStrategy end = endGameStrategy != null ? 
                endGameStrategy : new ComputerStrategyMinMax(3);
            
            ThresholdCalculator threshold = thresholdCalculator != null ? 
                thresholdCalculator : new SimpleThresholdCalculator(28);
            
            return new ComputerStrategyComposite(early, end, threshold);
        }
    }
}