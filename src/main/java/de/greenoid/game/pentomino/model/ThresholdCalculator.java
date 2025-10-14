package de.greenoid.game.pentomino.model;

/**
 * Interface for calculating when a composite strategy should switch
 * from early-game to endgame strategy.
 * 
 * <p>Different implementations can use various metrics such as:
 * <ul>
 *   <li>Number of empty squares remaining</li>
 *   <li>Board openness/diffusion values</li>
 *   <li>Number of pieces remaining</li>
 *   <li>Move count</li>
 * </ul>
 */
public interface ThresholdCalculator {
    
    /**
     * Determines if the composite strategy should switch from early game
     * to endgame strategy based on the current game state.
     *
     * @param gameState The current game state to evaluate
     * @return true if strategy should switch to endgame strategy, false otherwise
     */
    boolean shouldSwitchStrategy(GameState gameState);
    
    /**
     * Gets a description of this threshold calculation method.
     * 
     * @return A human-readable description
     */
    String getDescription();
}