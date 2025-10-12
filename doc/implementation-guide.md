# Open Space Strategy - Implementation Guide

## Executive Summary

We will create a new computer strategy called **ComputerStrategyOpenSpace** that uses iterative diffusion to evaluate board positions. This strategy will provide smarter gameplay than the existing random strategy by preferring moves that keep more future options open.

## Key Implementation Points

### 1. Core Algorithm: Iterative Diffusion

**Purpose**: Evaluate each board position based on surrounding "openness"

**Process**:
```
Step 1: Initialize 8x8 grid
  - unoccupied positions = 1
  - occupied positions = 0

Step 2: Repeat N times (configurable, recommend 3):
  For each position (row, col):
    newValue = sum of 8 neighbors from previous iteration
  Replace grid with new values

Step 3: Result
  - High values = positions in large open areas
  - Low values = positions near occupied spaces/edges
```

### 2. Move Selection

**Process**:
```
1. Find all possible moves (all pieces × all positions × all transformations)
2. For each move:
   - Identify 5 positions the piece would occupy
   - Sum diffusion values at those positions
   - This is the move's score
3. Select move with highest score (random if tied)
```

### 3. Class Structure

```java
package de.greenoid.game.pentomino.model;

public class ComputerStrategyOpenSpace implements ComputerStrategy {
    private final int diffusionIterations;
    private final Random random;
    
    // Main methods
    public ComputerStrategyOpenSpace(int diffusionIterations);
    public ComputerMove calculateMove(GameState gameState);
    public String getStrategyName();
    
    // Core algorithm
    private int[][] evaluateBoardOpenness(Board board);
    private int scorePossibleMove(ComputerMove move, int[][] evaluation);
    
    // Helper methods (adapted from ComputerStrategyRandom)
    private List<ComputerMove> findAllPossibleMoves(GameState gameState);
    private List<PentominoPiece> getAllTransformations(PentominoPiece piece);
}
```

### 4. Detailed Method Specifications

#### `evaluateBoardOpenness(Board board)`
```java
/**
 * Evaluates board positions using iterative diffusion.
 * 
 * @param board The current game board
 * @return 8x8 grid where each value represents position "openness"
 */
private int[][] evaluateBoardOpenness(Board board) {
    // 1. Initialize evaluation grid (1=free, 0=occupied)
    int[][] evaluation = new int[Board.SIZE][Board.SIZE];
    for (int row = 0; row < Board.SIZE; row++) {
        for (int col = 0; col < Board.SIZE; col++) {
            evaluation[row][col] = board.isOccupied(row, col) ? 0 : 1;
        }
    }
    
    // 2. Perform diffusion iterations
    for (int iteration = 0; iteration < diffusionIterations; iteration++) {
        int[][] newEvaluation = new int[Board.SIZE][Board.SIZE];
        
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                // Sum values of 8 neighbors
                int sum = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue; // Skip center
                        
                        int nr = row + dr;
                        int nc = col + dc;
                        
                        // Boundary check
                        if (nr >= 0 && nr < Board.SIZE && 
                            nc >= 0 && nc < Board.SIZE) {
                            sum += evaluation[nr][nc];
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
```

#### `scorePossibleMove(ComputerMove move, int[][] evaluation)`
```java
/**
 * Calculates score for a move by summing evaluation values
 * at all positions the piece would occupy.
 */
private int scorePossibleMove(ComputerMove move, int[][] evaluation) {
    int score = 0;
    PentominoPiece piece = move.getPiece();
    int startRow = move.getRow();
    int startCol = move.getCol();
    
    // Sum evaluation values for all 5 squares of the piece
    for (Point point : piece.getShape()) {
        int row = startRow + point.getY();
        int col = startCol + point.getX();
        score += evaluation[row][col];
    }
    
    return score;
}
```

#### `calculateMove(GameState gameState)`
```java
/**
 * Main strategy method - selects best move based on open space evaluation.
 */
@Override
public ComputerMove calculateMove(GameState gameState) {
    // 1. Evaluate board openness
    int[][] evaluation = evaluateBoardOpenness(gameState.getBoard());
    
    // 2. Find all possible moves
    List<ComputerMove> possibleMoves = findAllPossibleMoves(gameState);
    
    if (possibleMoves.isEmpty()) {
        return null;
    }
    
    // 3. Score all moves
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
    
    // 4. Random selection among best moves
    return bestMoves.get(random.nextInt(bestMoves.size()));
}
```

### 5. Integration Points

#### Current Integration (PentominoGame.java line 34):
```java
computerStrategy = new ComputerStrategyRandom();
```

#### Enhanced Integration Options:

**Option A: Simple Toggle (Quick Implementation)**
```java
// At class level
private boolean useAdvancedStrategy = true;

// In initializeGame()
if (useAdvancedStrategy) {
    computerStrategy = new ComputerStrategyOpenSpace(3); // 3 iterations
} else {
    computerStrategy = new ComputerStrategyRandom();
}
```

**Option B: User-Selectable (Better UX)**
```java
// Add to createControlPanel()
JComboBox<String> strategySelector = new JComboBox<>(new String[]{
    "Random Strategy",
    "Open Space Strategy (Easy - 1 iteration)",
    "Open Space Strategy (Medium - 3 iterations)",
    "Open Space Strategy (Hard - 5 iterations)"
});

strategySelector.addActionListener(e -> {
    String selected = (String) strategySelector.getSelectedItem();
    if (selected.startsWith("Random")) {
        computerStrategy = new ComputerStrategyRandom();
    } else if (selected.contains("Easy")) {
        computerStrategy = new ComputerStrategyOpenSpace(1);
    } else if (selected.contains("Medium")) {
        computerStrategy = new ComputerStrategyOpenSpace(3);
    } else {
        computerStrategy = new ComputerStrategyOpenSpace(5);
    }
});
```

### 6. Testing Approach

#### Unit Tests
```java
@Test
public void testDiffusionBasic() {
    // Empty board should have uniform high values after iterations
    Board emptyBoard = new Board();
    ComputerStrategyOpenSpace strategy = new ComputerStrategyOpenSpace(3);
    int[][] evaluation = strategy.evaluateBoardOpenness(emptyBoard);
    
    // Check that center positions have higher values than edges
    assertTrue(evaluation[4][4] > evaluation[0][0]);
}

@Test
public void testMoveScoringPrefersCentralPositions() {
    // On empty board, center moves should score higher
    GameState gameState = new GameState();
    ComputerStrategyOpenSpace strategy = new ComputerStrategyOpenSpace(3);
    
    // Compare move at center vs edge
    // Assert center scores higher
}
```

#### Integration Test
```java
@Test
public void testStrategySelectsValidMove() {
    GameState gameState = new GameState();
    ComputerStrategyOpenSpace strategy = new ComputerStrategyOpenSpace(3);
    
    ComputerMove move = strategy.calculateMove(gameState);
    assertNotNull(move);
    assertTrue(gameState.getBoard().canPlaceAt(
        move.getPiece(), move.getRow(), move.getCol()));
}
```

#### Performance Comparison
- Play 100 games: Random vs OpenSpace
- Track win rates, average moves per game
- Measure computation time per move

### 7. Expected Performance

**Computation Time**:
- Board evaluation: O(SIZE² × iterations) = O(64 × 3) = ~192 operations
- Move scoring: O(moves × 5) = O(~500 × 5) = ~2,500 operations
- Total: < 10ms per move (acceptable for real-time play)

**Strategic Improvement**:
- Fewer early game mistakes (better initial placement)
- Avoids creating isolated pockets
- Higher average piece count before blocking
- Expected win rate vs Random: 60-70%

### 8. Configuration Recommendations

| Iterations | Description | Use Case |
|-----------|-------------|----------|
| 1 | Immediate neighbors only | Fast, basic improvement over random |
| 2 | Local area | Good balance of speed and strategy |
| 3 | Regional influence | **Recommended default** |
| 4-5 | Board-wide patterns | Advanced AI, slower computation |

### 9. Future Enhancements

1. **Adaptive Depth**: Increase iterations as board fills up
2. **Weighted Diffusion**: Different weights for orthogonal vs diagonal
3. **Piece-Aware Scoring**: Consider piece shape compatibility
4. **Opponent Modeling**: Track opponent's typical moves
5. **Minimax Search**: Look ahead 2-3 moves
6. **Opening Book**: Pre-computed strong opening moves

### 10. File Locations

**New File**:
- `src/main/java/de/greenoid/game/pentomino/model/ComputerStrategyOpenSpace.java`

**Modified Files** (for UI integration):
- `src/main/java/de/greenoid/game/pentomino/ui/PentominoGame.java` (add strategy selector)

**Documentation**:
- `doc/openspace-strategy-plan.md` (algorithm design)
- `doc/implementation-guide.md` (this file)

## Ready for Implementation

The architecture is complete and ready for coding. The implementation should proceed in this order:

1. Create ComputerStrategyOpenSpace class skeleton
2. Implement evaluateBoardOpenness() method
3. Implement scorePossibleMove() method
4. Implement calculateMove() with move selection
5. Add helper methods from Random strategy
6. Test with simple scenarios
7. Integrate into PentominoGame UI
8. Performance testing and comparison

**Estimated Implementation Time**: 2-3 hours for core functionality + testing