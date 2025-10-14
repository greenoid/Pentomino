# Implementation Plan: MinMax and Composite Strategies

## Quick Reference

This document provides a step-by-step implementation plan for the two new computer strategies.

## Implementation Order

### Phase 1: MinMax Foundation (Priority: HIGH)
**Estimated Time**: 4-6 hours

1. **Create ComputerStrategyMinMax.java**
   - Implement basic structure with ComputerStrategy interface
   - Add configurable depth parameter (default: 3)
   - Implement findAllPossibleMoves() method (reuse from Random/OpenSpace)

2. **Implement Minimax Core**
   - Create minimax() recursive method
   - Handle terminal state detection
   - Return best move for computer

3. **Add Heuristic Evaluation**
   - Implement mobility-based heuristic (recommended first approach)
   - Count legal moves for both players
   - Return difference as score

4. **Add Alpha-Beta Pruning**
   - Add alpha and beta parameters
   - Implement cutoff logic
   - Test pruning effectiveness

### Phase 2: MinMax Optimization (Priority: MEDIUM)
**Estimated Time**: 2-3 hours

1. **Move Ordering**
   - Quick sort moves by simple heuristic before full evaluation
   - Improves pruning efficiency

2. **Performance Monitoring**
   - Add timing metrics
   - Count nodes evaluated
   - Verify <10 second constraint

3. **Depth Adjustment**
   - Test with depths 1-4
   - Document performance trade-offs

### Phase 3: Composite Strategy (Priority: HIGH)
**Estimated Time**: 3-4 hours

1. **Create ThresholdCalculator Interface**
   ```java
   public interface ThresholdCalculator {
       boolean shouldSwitchStrategy(GameState gameState);
   }
   ```

2. **Implement SimpleThresholdCalculator**
   - Count empty squares
   - Compare against threshold (default: 28)

3. **Create ComputerStrategyComposite**
   - Hold references to early/end strategies
   - Implement switch logic
   - Add logging for debugging

4. **Test Strategy Switching**
   - Verify correct switching point
   - Monitor performance

### Phase 4: Advanced Features (Priority: LOW)
**Estimated Time**: 2-4 hours

1. **DiffusionThresholdCalculator** (optional)
   - Reuse diffusion from OpenSpace
   - Monitor max openness value

2. **Transposition Table** (optional)
   - Cache evaluated positions
   - Implement Zobrist hashing

## Code Snippets

### MinMax Basic Structure

```java
package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;

public class ComputerStrategyMinMax implements ComputerStrategy {
    private final int maxDepth;
    private int nodesEvaluated;
    
    public ComputerStrategyMinMax(int maxDepth) {
        this.maxDepth = Math.max(1, Math.min(maxDepth, 5));
    }
    
    @Override
    public ComputerMove calculateMove(GameState gameState) {
        nodesEvaluated = 0;
        long startTime = System.currentTimeMillis();
        
        List<ComputerMove> possibleMoves = findAllPossibleMoves(gameState);
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        ComputerMove bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (ComputerMove move : possibleMoves) {
            GameState newState = new GameState(gameState);
            newState.makeMove(move.getPiece(), move.getRow(), move.getCol());
            
            int score = minimax(newState, maxDepth - 1, 
                               Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("MinMax evaluated " + nodesEvaluated + 
                          " nodes in " + duration + "ms");
        
        return bestMove;
    }
    
    private int minimax(GameState state, int depth, int alpha, int beta, 
                        boolean maximizingPlayer) {
        nodesEvaluated++;
        
        // Terminal conditions
        if (depth == 0 || !state.hasLegalMoves()) {
            return evaluatePosition(state);
        }
        
        List<ComputerMove> moves = findAllPossibleMoves(state);
        
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (ComputerMove move : moves) {
                GameState newState = new GameState(state);
                newState.makeMove(move.getPiece(), move.getRow(), move.getCol());
                
                int eval = minimax(newState, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                
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
                
                if (beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            return minEval;
        }
    }
    
    private int evaluatePosition(GameState state) {
        // If terminal, return extreme scores
        if (!state.hasLegalMoves()) {
            // Current player can't move = they lose
            // If it's computer's turn, that's bad
            // If it's opponent's turn, that's good
            return (state.getCurrentPlayer() == Player.PLAYER_2) ? 
                   Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        }
        
        // Non-terminal: use mobility heuristic
        int computerMoves = countLegalMoves(state);
        
        // Simulate opponent's turn
        GameState oppState = new GameState(state);
        oppState.checkCurrentPlayerMoves(); // Switch to opponent
        int opponentMoves = countLegalMoves(oppState);
        
        return computerMoves - opponentMoves;
    }
    
    private int countLegalMoves(GameState state) {
        int count = 0;
        for (PentominoPiece piece : state.getAvailablePieces()) {
            if (state.getBoard().hasLegalMove(piece)) {
                count++;
            }
        }
        return count;
    }
    
    private List<ComputerMove> findAllPossibleMoves(GameState gameState) {
        // Reuse implementation from ComputerStrategyRandom or OpenSpace
        // ... (implementation details)
    }
    
    @Override
    public String getStrategyName() {
        return "MinMax Strategy (depth=" + maxDepth + ")";
    }
}
```

### Composite Strategy Structure

```java
package de.greenoid.game.pentomino.model;

public class ComputerStrategyComposite implements ComputerStrategy {
    private final ComputerStrategy earlyGameStrategy;
    private final ComputerStrategy endGameStrategy;
    private final ThresholdCalculator thresholdCalculator;
    
    private boolean hasSwitched = false;
    private int switchedAtMove = -1;
    
    public ComputerStrategyComposite(
            ComputerStrategy earlyGameStrategy,
            ComputerStrategy endGameStrategy,
            ThresholdCalculator thresholdCalculator) {
        this.earlyGameStrategy = earlyGameStrategy;
        this.endGameStrategy = endGameStrategy;
        this.thresholdCalculator = thresholdCalculator;
    }
    
    // Convenience constructor with defaults
    public ComputerStrategyComposite() {
        this(new ComputerStrategyOpenSpace(3),
             new ComputerStrategyMinMax(3),
             new SimpleThresholdCalculator(28));
    }
    
    @Override
    public ComputerMove calculateMove(GameState gameState) {
        // Check if we should switch strategies
        if (!hasSwitched && thresholdCalculator.shouldSwitchStrategy(gameState)) {
            hasSwitched = true;
            switchedAtMove = gameState.getMoveCount();
            System.out.println("Composite: Switching to endgame strategy at move " 
                             + switchedAtMove);
        }
        
        // Use appropriate strategy
        ComputerStrategy activeStrategy = hasSwitched ? 
            endGameStrategy : earlyGameStrategy;
        
        String phase = hasSwitched ? "endgame" : "early game";
        System.out.println("Composite: Using " + activeStrategy.getStrategyName() 
                         + " (" + phase + ")");
        
        return activeStrategy.calculateMove(gameState);
    }
    
    @Override
    public String getStrategyName() {
        if (hasSwitched) {
            return "Composite Strategy (using " + endGameStrategy.getStrategyName() + ")";
        } else {
            return "Composite Strategy (using " + earlyGameStrategy.getStrategyName() + ")";
        }
    }
    
    public boolean hasSwitched() {
        return hasSwitched;
    }
    
    public int getSwitchMove() {
        return switchedAtMove;
    }
}
```

### Threshold Calculator Interface

```java
package de.greenoid.game.pentomino.model;

public interface ThresholdCalculator {
    /**
     * Determines if the composite strategy should switch from early game
     * to endgame strategy.
     *
     * @param gameState The current game state
     * @return true if strategy should switch, false otherwise
     */
    boolean shouldSwitchStrategy(GameState gameState);
}
```

### Simple Threshold Implementation

```java
package de.greenoid.game.pentomino.model;

public class SimpleThresholdCalculator implements ThresholdCalculator {
    private final int thresholdSquares;
    
    /**
     * Creates a threshold calculator based on empty squares.
     *
     * @param thresholdSquares Switch when empty squares <= this value
     *                        Recommended: 28 for 8x8 board
     */
    public SimpleThresholdCalculator(int thresholdSquares) {
        this.thresholdSquares = thresholdSquares;
    }
    
    @Override
    public boolean shouldSwitchStrategy(GameState gameState) {
        int emptySquares = Board.SIZE * Board.SIZE - 
                          gameState.getBoard().getOccupiedSquareCount();
        return emptySquares <= thresholdSquares;
    }
}
```

## Testing Checklist

### Unit Tests (ComputerStrategyMinMaxTest.java)
- [ ] Test depth parameter validation
- [ ] Test terminal state detection (win/loss/draw)
- [ ] Test heuristic evaluation returns correct sign
- [ ] Test alpha-beta pruning reduces nodes
- [ ] Test performance under 10 seconds for depth=3

### Unit Tests (ComputerStrategyCompositeTest.java)
- [ ] Test initial strategy is early game
- [ ] Test switching occurs at correct threshold
- [ ] Test switch happens only once
- [ ] Test both strategies are called appropriately

### Integration Tests
- [ ] Full game: MinMax vs Random
- [ ] Full game: Composite vs OpenSpace
- [ ] Full game: Composite vs Random
- [ ] Performance: Monitor average move time
- [ ] Behavior: Verify MinMax never misses obvious wins

## Performance Targets

| Metric | Target | Measurement |
|--------|--------|-------------|
| MinMax move time (depth=3) | <10 seconds | Average over 20 moves |
| Composite early phase | <1 second | Average per move |
| Composite end phase | <10 seconds | Average per move |
| Switch point | Move 18-22 | In typical 8x8 game |
| Memory usage | <100MB | During MinMax search |

## Configuration Recommendations

### Development/Testing
```java
// Fast, for testing logic
ComputerStrategyMinMax minmax = new ComputerStrategyMinMax(2);
ComputerStrategyComposite composite = new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(2),
    new ComputerStrategyMinMax(2),
    new SimpleThresholdCalculator(24)
);
```

### Production/Gameplay
```java
// Balanced performance and strength
ComputerStrategyMinMax minmax = new ComputerStrategyMinMax(3);
ComputerStrategyComposite composite = new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(3),
    new ComputerStrategyMinMax(3),
    new SimpleThresholdCalculator(28)
);
```

### Maximum Strength (Slow)
```java
// For analysis or demonstrations
ComputerStrategyMinMax minmax = new ComputerStrategyMinMax(4);
ComputerStrategyComposite composite = new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(4),
    new ComputerStrategyMinMax(3),
    new SimpleThresholdCalculator(30)
);
```

## Common Issues and Solutions

### Issue: MinMax too slow
**Solutions**:
1. Reduce depth to 2
2. Implement move ordering
3. Reduce threshold in Composite to 24 squares

### Issue: MinMax doesn't find winning moves
**Solutions**:
1. Check terminal state evaluation
2. Verify player perspective in heuristic
3. Increase depth to 4 for testing

### Issue: Composite switches too early/late
**Solutions**:
1. Adjust threshold (27-30 range)
2. Monitor empty squares at switch point
3. Consider diffusion-based threshold

### Issue: Out of memory errors
**Solutions**:
1. Reduce depth
2. Limit game state copying
3. Clear transposition table periodically

## File Structure

```
src/main/java/de/greenoid/game/pentomino/model/
├── ComputerStrategy.java                    # Interface (existing)
├── ComputerStrategyMinMax.java             # NEW: MinMax implementation
├── ComputerStrategyComposite.java          # NEW: Composite implementation
├── ThresholdCalculator.java                # NEW: Interface
├── SimpleThresholdCalculator.java          # NEW: Simple threshold
└── DiffusionThresholdCalculator.java       # NEW: Advanced threshold (optional)

src/test/java/de/greenoid/game/pentomino/model/
├── ComputerStrategyMinMaxTest.java         # NEW: MinMax tests
└── ComputerStrategyCompositeTest.java      # NEW: Composite tests

doc/
├── minmax-composite-strategy-design.md     # Created: Full design doc
└── implementation-plan.md                   # This file
```

## Next Steps

1. Review this implementation plan
2. Approve the design approach
3. Switch to Code mode for implementation
4. Start with Phase 1: MinMax Foundation
5. Test thoroughly before moving to Phase 3: Composite

## Questions for Clarification

Before implementation, please confirm:

1. **Heuristic Choice**: Start with mobility-based heuristic (simple, fast)?
2. **Default Depth**: Use 3 as default (computer, opponent, computer)?
3. **Threshold**: Use 28 empty squares as initial threshold?
4. **Advanced Features**: Implement transposition table in Phase 4 or skip?
5. **Testing**: Focus on unit tests first, then integration tests?

Once approved, I'll switch to Code mode and begin implementation.