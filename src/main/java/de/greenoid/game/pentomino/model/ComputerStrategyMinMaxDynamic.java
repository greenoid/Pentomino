package de.greenoid.game.pentomino.model;

/**
 * Dynamic MinMax strategy that adjusts search depth based on game phase.
 * 
 * <p>This strategy uses progressively deeper searches as the game progresses
 * and the board becomes more constrained:
 * <ul>
 *   <li><b>Early game</b>: Depth 2 (fast, when board is wide open)</li>
 *   <li><b>Mid game</b>: Depth 3 (balanced, when board is moderately filled)</li>
 *   <li><b>End game</b>: Depth 4 (deep analysis, when board is highly constrained)</li>
 * </ul>
 * 
 * <p>The depth increases automatically as the number of empty squares decreases,
 * providing optimal balance between move quality and computation time throughout the game.
 * 
 * <p>Default thresholds (optimized from gameplay testing):
 * <ul>
 *   <li>Switch to depth 3: when empty squares ≤ 32 (~50% occupancy)</li>
 *   <li>Switch to depth 4: when empty squares ≤ 18 (~72% occupancy)</li>
 * </ul>
 *
 * <p>These thresholds ensure move counts are manageable at each depth:
 * <ul>
 *   <li>At 32 empty: typically 150-350 moves, depth 3 takes <3s</li>
 *   <li>At 18 empty: typically <10 moves, depth 4 is instant</li>
 * </ul>
 */
public class ComputerStrategyMinMaxDynamic implements ComputerStrategy {
    
    private final int depthEarly;       // Early game depth (default: 2)
    private final int depthMid;         // Mid game depth (default: 3)
    private final int depthEnd;         // End game depth (default: 4)
    private final int thresholdMid;     // Switch to mid game (default: 41 squares)
    private final int thresholdEnd;     // Switch to end game (default: 21 squares)
    
    private final ComputerStrategyMinMax earlyStrategy;
    private final ComputerStrategyMinMax midStrategy;
    private final ComputerStrategyMinMax endStrategy;
    
    private int currentDepth;
    private boolean switchedToMid = false;
    private boolean switchedToEnd = false;
    private int midSwitchMove = -1;
    private int endSwitchMove = -1;
    
    /**
     * Creates a dynamic MinMax strategy with specified depths and thresholds.
     *
     * @param depthEarly Depth for early game (default: 2)
     * @param depthMid Depth for mid game (default: 3)
     * @param depthEnd Depth for end game (default: 4)
     * @param thresholdMid Switch to mid game when empty ≤ this (default: 41)
     * @param thresholdEnd Switch to end game when empty ≤ this (default: 21)
     */
    public ComputerStrategyMinMaxDynamic(int depthEarly, int depthMid, int depthEnd,
                                         int thresholdMid, int thresholdEnd) {
        this.depthEarly = depthEarly;
        this.depthMid = depthMid;
        this.depthEnd = depthEnd;
        this.thresholdMid = thresholdMid;
        this.thresholdEnd = thresholdEnd;
        
        // Create MinMax strategies for each phase
        this.earlyStrategy = new ComputerStrategyMinMax(depthEarly);
        this.midStrategy = new ComputerStrategyMinMax(depthMid);
        this.endStrategy = new ComputerStrategyMinMax(depthEnd);
        
        this.currentDepth = depthEarly;
    }
    
    /**
     * Creates a dynamic MinMax strategy with default configuration:
     * <ul>
     *   <li>Early game: Depth 2, switch at 32 empty squares (~50% occupancy)</li>
     *   <li>Mid game: Depth 3, switch at 18 empty squares (~72% occupancy)</li>
     *   <li>End game: Depth 4</li>
     * </ul>
     *
     * <p>These thresholds are optimized from gameplay testing to ensure:
     * <ul>
     *   <li>Depth 2 handles high move counts (>500 moves) efficiently</li>
     *   <li>Depth 3 kicks in when moves reduce to ~150-350 (fast enough)</li>
     *   <li>Depth 4 only engages when very few moves remain (<10)</li>
     * </ul>
     */
    public ComputerStrategyMinMaxDynamic() {
        this(2, 3, 4, 32, 18);
    }
    
    @Override
    public ComputerMove calculateMove(GameState gameState) {
        int emptySquares = Board.SIZE * Board.SIZE -
                          gameState.getBoard().getOccupiedSquareCount();
        int moveCount = gameState.getMoveCount();
        
        // Determine current phase and switch if needed
        GamePhase phase = determinePhase(emptySquares, moveCount);
        
        // Select appropriate strategy based on phase
        ComputerStrategyMinMax activeStrategy;
        switch (phase) {
            case EARLY:
                activeStrategy = earlyStrategy;
                currentDepth = depthEarly;
                break;
            case MID:
                activeStrategy = midStrategy;
                currentDepth = depthMid;
                break;
            case END:
                activeStrategy = endStrategy;
                currentDepth = depthEnd;
                break;
            default:
                activeStrategy = earlyStrategy;
                currentDepth = depthEarly;
        }
        
        return activeStrategy.calculateMove(gameState);
    }
    
    /**
     * Determines the current game phase based on empty squares.
     */
    private GamePhase determinePhase(int emptySquares, int moveCount) {
        // Check for end game first (most constrained)
        if (emptySquares <= thresholdEnd) {
            if (!switchedToEnd) {
                switchedToEnd = true;
                endSwitchMove = moveCount;
                System.out.println("Dynamic MinMax: Switching to END GAME (depth " + depthEnd +
                                 ") at move " + endSwitchMove);
            }
            return GamePhase.END;
        }
        
        // Check for mid game
        if (emptySquares <= thresholdMid) {
            if (!switchedToMid) {
                switchedToMid = true;
                midSwitchMove = moveCount;
                System.out.println("Dynamic MinMax: Switching to MID GAME (depth " + depthMid +
                                 ") at move " + midSwitchMove);
            }
            return GamePhase.MID;
        }
        
        // Still in early game
        return GamePhase.EARLY;
    }
    
    @Override
    public String getStrategyName() {
        String phaseName;
        if (switchedToEnd) {
            phaseName = "END (depth " + depthEnd + ")";
        } else if (switchedToMid) {
            phaseName = "MID (depth " + depthMid + ")";
        } else {
            phaseName = "EARLY (depth " + depthEarly + ")";
        }
        return "MinMax Dynamic [" + phaseName + ", parallel]";
    }
    
    /**
     * Gets the current search depth being used.
     */
    public int getCurrentDepth() {
        return currentDepth;
    }
    
    /**
     * Gets the current game phase.
     */
    public GamePhase getCurrentPhase() {
        if (switchedToEnd) return GamePhase.END;
        if (switchedToMid) return GamePhase.MID;
        return GamePhase.EARLY;
    }
    
    /**
     * Checks if strategy has switched to mid game.
     */
    public boolean hasSwitchedToMid() {
        return switchedToMid;
    }
    
    /**
     * Checks if strategy has switched to end game.
     */
    public boolean hasSwitchedToEnd() {
        return switchedToEnd;
    }
    
    /**
     * Gets the move number when mid game switch occurred.
     */
    public int getMidSwitchMove() {
        return midSwitchMove;
    }
    
    /**
     * Gets the move number when end game switch occurred.
     */
    public int getEndSwitchMove() {
        return endSwitchMove;
    }
    
    /**
     * Shuts down all underlying executor services. Should be called when the strategy
     * is no longer needed to free up resources.
     */
    public void shutdown() {
        earlyStrategy.shutdown();
        midStrategy.shutdown();
        endStrategy.shutdown();
    }
    
    /**
     * Represents the three game phases.
     */
    public enum GamePhase {
        EARLY,  // Start of game, depth 2
        MID,    // Middle game, depth 3
        END     // End game, depth 4
    }
    
    /**
     * Builder class for creating customized Dynamic MinMax instances.
     * 
     * <p>Example:
     * <pre>
     * ComputerStrategy strategy = new ComputerStrategyMinMaxDynamic.Builder()
     *     .withEarlyDepth(2)
     *     .withMidDepth(3)
     *     .withEndDepth(5)
     *     .withMidThreshold(40)
     *     .withEndThreshold(20)
     *     .build();
     * </pre>
     */
    public static class Builder {
        private int depthEarly = 2;
        private int depthMid = 3;
        private int depthEnd = 4;
        private int thresholdMid = 32;  // Optimized: avoids 700+ moves at depth 3
        private int thresholdEnd = 18;   // Optimized: ensures <10 moves at depth 4
        
        public Builder withEarlyDepth(int depth) {
            this.depthEarly = depth;
            return this;
        }
        
        public Builder withMidDepth(int depth) {
            this.depthMid = depth;
            return this;
        }
        
        public Builder withEndDepth(int depth) {
            this.depthEnd = depth;
            return this;
        }
        
        public Builder withMidThreshold(int threshold) {
            this.thresholdMid = threshold;
            return this;
        }
        
        public Builder withEndThreshold(int threshold) {
            this.thresholdEnd = threshold;
            return this;
        }
        
        public ComputerStrategyMinMaxDynamic build() {
            return new ComputerStrategyMinMaxDynamic(depthEarly, depthMid, depthEnd,
                                                     thresholdMid, thresholdEnd);
        }
    }
}