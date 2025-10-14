# MinMax and Composite Strategy Design

## Overview
This document details the design and implementation plan for two advanced computer strategies:
1. **ComputerStrategyMinMax**: Uses the minimax algorithm with alpha-beta pruning for optimal endgame play
2. **ComputerStrategyComposite**: Combines OpenSpace strategy for early game with MinMax for endgame

## 1. ComputerStrategyMinMax Design

### 1.1 Core Algorithm

The MinMax strategy implements the classic minimax algorithm with alpha-beta pruning to search the game tree for optimal moves.

#### Algorithm Flow
```
function minimax(gameState, depth, alpha, beta, maximizingPlayer):
    if depth == 0 OR game is terminal:
        return heuristic_evaluation(gameState)
    
    if maximizingPlayer:
        maxEval = -infinity
        for each possible move:
            eval = minimax(apply_move, depth-1, alpha, beta, false)
            maxEval = max(maxEval, eval)
            alpha = max(alpha, eval)
            if beta <= alpha:
                break  # Beta cutoff
        return maxEval
    else:
        minEval = +infinity
        for each possible move:
            eval = minimax(apply_move, depth-1, alpha, beta, true)
            minEval = min(minEval, eval)
            beta = min(beta, eval)
            if beta <= alpha:
                break  # Alpha cutoff
        return minEval
```

### 1.2 Configuration Parameters

```java
public class ComputerStrategyMinMax implements ComputerStrategy {
    private final int maxDepth;              // Default: 3 (computer, human, computer)
    private final boolean useTranspositionTable;  // Cache for seen positions
    private final int maxThinkingTimeMs;     // Time limit per move
}
```

**Depth Parameter**:
- `depth = 1`: Computer's immediate move only
- `depth = 2`: Computer's move + opponent's response
- `depth = 3`: Computer + opponent + computer (recommended default)
- `depth = 4+`: Very slow, only for endgame with few pieces left

### 1.3 Heuristic Evaluation Function

When the maximum depth is reached without a terminal state, we need to evaluate the board position. Here are several heuristic strategies:

#### Option 1: Mobility-Based Heuristic (Recommended)
Score based on the number of legal moves available for each player:

```java
private int evaluatePosition(GameState gameState) {
    Player currentPlayer = gameState.getCurrentPlayer();
    
    // Count legal moves for current player
    int myMoves = countLegalMoves(gameState, currentPlayer);
    
    // Simulate opponent's turn and count their moves
    Player opponent = getOpponent(currentPlayer);
    int opponentMoves = countLegalMoves(gameState, opponent);
    
    // Prefer positions where we have more options than opponent
    return myMoves - opponentMoves;
}
```

**Advantages**:
- Simple and fast to calculate
- Directly related to game objective (force opponent into no-move situation)
- Works well in Pentomino where mobility is key

#### Option 2: Weighted Position Heuristic
Combines multiple factors:

```java
private int evaluatePosition(GameState gameState) {
    int score = 0;
    
    // Factor 1: Mobility advantage (weight: 100)
    score += 100 * (myMoves - opponentMoves);
    
    // Factor 2: Board control (weight: 10)
    // Prefer occupying central positions
    score += 10 * evaluateBoardControl(gameState);
    
    // Factor 3: Piece efficiency (weight: 5)
    // Prefer using pieces that create fewer isolated empty spaces
    score += 5 * evaluatePieceEfficiency(gameState);
    
    return score;
}
```

#### Option 3: OpenSpace-Enhanced Heuristic
Leverage the diffusion algorithm from ComputerStrategyOpenSpace:

```java
private int evaluatePosition(GameState gameState) {
    int[][] myOpenness = evaluateBoardOpenness(gameState.getBoard());
    
    // Simulate opponent's move and evaluate their openness
    GameState opponentState = simulateOpponentTurn(gameState);
    int[][] opponentOpenness = evaluateBoardOpenness(opponentState.getBoard());
    
    // Compare maximum openness values
    int myMaxOpenness = getMaxValue(myOpenness);
    int opponentMaxOpenness = getMaxValue(opponentOpenness);
    
    return myMaxOpenness - opponentMaxOpenness;
}
```

**Recommendation**: Start with Option 1 (Mobility-Based) for simplicity and performance, then optionally enhance with Option 3 if needed.

### 1.4 Performance Optimizations

#### Move Ordering
Order moves from most to least promising to maximize alpha-beta pruning:

```java
private List<ComputerMove> orderMoves(List<ComputerMove> moves, int[][] evaluation) {
    // Sort moves by quick heuristic (e.g., openness score)
    moves.sort((m1, m2) -> scoreMove(m2, evaluation) - scoreMove(m1, evaluation));
    return moves;
}
```

#### Transposition Table
Cache previously evaluated positions:

```java
private Map<Long, Integer> transpositionTable = new HashMap<>();

private long hashGameState(GameState state) {
    // Use Zobrist hashing for efficient position caching
    return calculateZobristHash(state);
}
```

#### Iterative Deepening
Search progressively deeper while respecting time limits:

```java
private ComputerMove calculateMoveWithTimeLimit() {
    ComputerMove bestMove = null;
    for (int depth = 1; depth <= maxDepth; depth++) {
        if (isTimeExpired()) break;
        bestMove = searchAtDepth(depth);
    }
    return bestMove;
}
```

### 1.5 Terminal State Handling

```java
private boolean isTerminal(GameState gameState) {
    // Game is terminal if:
    // 1. Current player has no legal moves (they lose)
    // 2. All pieces are placed (draw)
    return !gameState.hasLegalMoves() || 
           gameState.getAvailablePieces().isEmpty();
}

private int getTerminalScore(GameState gameState) {
    if (!gameState.hasLegalMoves()) {
        // Current player loses = very negative score
        return Integer.MIN_VALUE + 1;
    }
    // Draw = neutral score
    return 0;
}
```

## 2. ComputerStrategyComposite Design

### 2.1 Strategy Architecture

```java
public class ComputerStrategyComposite implements ComputerStrategy {
    private final ComputerStrategy earlyGameStrategy;  // OpenSpace
    private final ComputerStrategy endGameStrategy;    // MinMax
    private final ThresholdCalculator thresholdCalculator;
    
    private boolean hasSwitch = false;
    private int switchedAtMove = -1;
}
```

### 2.2 Threshold Strategies

#### Strategy 1: Simple Empty Space Count (Recommended for Initial Implementation)

```java
public class SimpleThresholdCalculator implements ThresholdCalculator {
    private final int thresholdSquares;  // Default: 27-30
    
    @Override
    public boolean shouldSwitchStrategy(GameState gameState) {
        int emptySquares = Board.SIZE * Board.SIZE - 
                          gameState.getBoard().getOccupiedSquareCount();
        return emptySquares <= thresholdSquares;
    }
}
```

**Rationale**:
- 8x8 board = 64 squares total
- Each piece occupies ~5 squares
- 3 pieces remaining ≈ 15 squares
- Add buffer for spacing ≈ 27-30 squares threshold
- Switches roughly after 50% board coverage

**Tuning Guidelines**:
- Start with threshold = 28 squares
- If MinMax is too slow, increase to 24 squares
- If MinMax doesn't help, increase to 32 squares
- Monitor computation time and adjust

#### Strategy 2: Diffusion-Based Threshold (Advanced)

```java
public class DiffusionThresholdCalculator implements ThresholdCalculator {
    private final int maxOpennessThreshold;  // Default: 50
    private final int diffusionIterations;
    
    @Override
    public boolean shouldSwitchStrategy(GameState gameState) {
        int[][] evaluation = evaluateBoardOpenness(gameState.getBoard());
        int maxOpenness = getMaxValue(evaluation);
        return maxOpenness <= maxOpennessThreshold;
    }
}
```

**Rationale**:
- High openness = many placement options = use OpenSpace strategy
- Low openness = constrained board = use MinMax for optimal play
- More adaptive to actual game state vs. simple square count

**Advantages**:
- Context-aware switching
- Considers board topology, not just fill percentage
- Better handles irregular game progressions

**Disadvantages**:
- More complex to tune
- Slight performance overhead from diffusion calculation

### 2.3 Strategy Switching Logic

```java
@Override
public ComputerMove calculateMove(GameState gameState) {
    // Check if we should switch strategies
    if (!hasSwitched && thresholdCalculator.shouldSwitchStrategy(gameState)) {
        hasSwitched = true;
        switchedAtMove = gameState.getMoveCount();
        System.out.println("Switching to endgame strategy at move " + switchedAtMove);
    }
    
    // Use appropriate strategy
    ComputerStrategy activeStrategy = hasSwitched ? 
        endGameStrategy : earlyGameStrategy;
    
    return activeStrategy.calculateMove(gameState);
}
```

### 2.4 Configuration Options

```java
public ComputerStrategyComposite(Builder builder) {
    this.earlyGameStrategy = builder.earlyGameStrategy != null ?
        builder.earlyGameStrategy : new ComputerStrategyOpenSpace(3);
    
    this.endGameStrategy = builder.endGameStrategy != null ?
        builder.endGameStrategy : new ComputerStrategyMinMax(3);
    
    this.thresholdCalculator = builder.thresholdCalculator != null ?
        builder.thresholdCalculator : new SimpleThresholdCalculator(28);
}

// Builder pattern for configuration
public static class Builder {
    private ComputerStrategy earlyGameStrategy;
    private ComputerStrategy endGameStrategy;
    private ThresholdCalculator thresholdCalculator;
    
    public Builder withEarlyGameStrategy(ComputerStrategy strategy) {
        this.earlyGameStrategy = strategy;
        return this;
    }
    // ... other builder methods
}
```

## 3. Implementation Architecture

### 3.1 Class Diagram

```
┌─────────────────────────────────────────┐
│       ComputerStrategy Interface         │
├─────────────────────────────────────────┤
│ + calculateMove(GameState): ComputerMove│
│ + getStrategyName(): String             │
└─────────────────────────────────────────┘
                    △
                    │
         ┌──────────┴──────────┐
         │                     │
┌────────┴────────────┐  ┌────┴────────────────────┐
│ ComputerStrategyMinMax│  │ComputerStrategyComposite│
├─────────────────────┤  ├─────────────────────────┤
│ - maxDepth: int     │  │ - earlyStrategy         │
│ - heuristic         │  │ - endStrategy           │
├─────────────────────┤  │ - threshold             │
│ + minimax()         │  │ - hasSwitched: boolean  │
│ + alphaBeta()       │  ├─────────────────────────┤
│ + evaluate()        │  │ + calculateMove()       │
└─────────────────────┘  │ + shouldSwitch()        │
                         └─────────────────────────┘
                                    │
                                    │ uses
                         ┌──────────┴──────────────┐
                         │                         │
              ┌──────────┴─────────┐    ┌────────┴──────────┐
              │ ThresholdCalculator│    │  ComputerStrategy  │
              │    (Interface)     │    │   (Early/End)      │
              └────────────────────┘    └────────────────────┘
                         △
                         │
            ┌────────────┴────────────┐
            │                         │
┌───────────┴────────────┐ ┌─────────┴──────────────┐
│SimpleThresholdCalculator│ │DiffusionThresholdCalc  │
└─────────────────────────┘ └────────────────────────┘
```

### 3.2 Key Classes and Responsibilities

**ComputerStrategyMinMax**:
- Implements minimax algorithm with alpha-beta pruning
- Manages search depth and time limits
- Evaluates non-terminal positions using heuristic
- Handles move generation and ordering

**ComputerStrategyComposite**:
- Delegates to early-game and end-game strategies
- Monitors game state to determine switch point
- Configurable threshold calculation
- Reports strategy transitions

**ThresholdCalculator Interface**:
- `boolean shouldSwitchStrategy(GameState gameState)`
- Allows pluggable threshold strategies

**SimpleThresholdCalculator**:
- Counts empty squares
- Compares against fixed threshold

**DiffusionThresholdCalculator**:
- Reuses diffusion algorithm from OpenSpace
- Monitors maximum openness score

## 4. Testing Strategy

### 4.1 Unit Tests

**MinMax Algorithm Tests**:
```java
@Test
public void testMinMaxFindsWinningMove() {
    // Setup game state where computer can win in 1 move
    GameState state = createNearWinState();
    ComputerStrategyMinMax strategy = new ComputerStrategyMinMax(3);
    
    ComputerMove move = strategy.calculateMove(state);
    
    // Verify move leads to win
    state.makeMove(move.getPiece(), move.getRow(), move.getCol());
    assertTrue(state.getStatus() == GameStatus.PLAYER_2_WINS);
}

@Test
public void testAlphaBetaPruning() {
    // Verify pruning reduces nodes evaluated
    ComputerStrategyMinMax strategy = new ComputerStrategyMinMax(3);
    GameState state = new GameState();
    
    int nodesEvaluated = strategy.calculateMoveAndCountNodes(state);
    
    assertTrue(nodesEvaluated < maxPossibleNodes);
}
```

**Composite Strategy Tests**:
```java
@Test
public void testStrategySwitch() {
    ComputerStrategyComposite strategy = new ComputerStrategyComposite
        .builder()
        .withThreshold(new SimpleThresholdCalculator(30))
        .build();
    
    GameState state = createStateWithEmptySquares(35);
    assertFalse(strategy.hasSwitched());
    
    state = createStateWithEmptySquares(25);
    strategy.calculateMove(state);
    assertTrue(strategy.hasSwitched());
}
```

### 4.2 Performance Tests

```java
@Test
public void testMinMaxPerformance() {
    ComputerStrategyMinMax strategy = new ComputerStrategyMinMax(3);
    GameState state = createMidGameState();
    
    long startTime = System.currentTimeMillis();
    ComputerMove move = strategy.calculateMove(state);
    long duration = System.currentTimeMillis() - startTime;
    
    assertTrue(duration < 10000, "MinMax should complete in under 10 seconds");
}
```

### 4.3 Integration Tests

- Full game simulations with different strategy combinations
- MinMax vs Random strategy
- Composite vs OpenSpace strategy
- Performance profiling under various board states

## 5. Performance Considerations

### 5.1 Computational Complexity

**MinMax without pruning**: O(b^d)
- b = branching factor (average ~50-200 moves)
- d = depth (3 levels recommended)

**MinMax with alpha-beta pruning**: O(b^(d/2)) in best case
- Effective depth doubling with good move ordering

**Memory usage**:
- GameState copy per node: ~2KB
- Stack depth: ~3 levels
- Transposition table: ~10MB (optional)

### 5.2 Optimization Priorities

1. **Move Ordering** (Highest Impact)
   - Sort by simple heuristic before full evaluation
   - Reduces pruned branches by 50-70%

2. **Transposition Table** (Medium Impact)
   - Cache repeated positions
   - Significant in symmetric positions

3. **Iterative Deepening** (Low Impact)
   - Better for time-constrained scenarios
   - Minimal benefit if depth is fixed

## 6. Configuration Recommendations

### 6.1 Default Configurations

**For Balanced Play**:
```java
ComputerStrategyComposite strategy = new ComputerStrategyComposite.builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(3))
    .withEndGameStrategy(new ComputerStrategyMinMax(3))
    .withThreshold(new SimpleThresholdCalculator(28))
    .build();
```

**For Faster Play**:
```java
ComputerStrategyComposite strategy = new ComputerStrategyComposite.builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(2))
    .withEndGameStrategy(new ComputerStrategyMinMax(2))
    .withThreshold(new SimpleThresholdCalculator(24))
    .build();
```

**For Maximum Strength**:
```java
ComputerStrategyComposite strategy = new ComputerStrategyComposite.builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(4))
    .withEndGameStrategy(new ComputerStrategyMinMax(4))
    .withThreshold(new DiffusionThresholdCalculator(40, 3))
    .build();
```

## 7. Implementation Phases

### Phase 1: MinMax Core
1. Implement basic minimax without pruning
2. Add terminal state detection
3. Implement simple mobility heuristic
4. Test with depth=1 and depth=2

### Phase 2: Alpha-Beta Pruning
1. Add alpha-beta cutoffs
2. Implement move ordering
3. Performance testing
4. Optimize heuristic

### Phase 3: Composite Strategy
1. Implement simple threshold calculator
2. Add strategy switching logic
3. Test threshold tuning
4. Add logging/debugging

### Phase 4: Advanced Features
1. Implement diffusion-based threshold
2. Add transposition table (optional)
3. Implement iterative deepening (optional)
4. Final performance optimization

## 8. Expected Behavior

### 8.1 MinMax Strategy
- **Early game**: May be slower than OpenSpace (~3-8 seconds per move)
- **Mid game**: Should find good tactical moves
- **End game**: Should play optimally and never miss wins
- **Performance**: <10 seconds per move at depth=3

### 8.2 Composite Strategy
- **Move 1-15**: Uses OpenSpace (fast, <1 second)
- **Move 16-20**: Monitors threshold, may switch
- **Move 21+**: Uses MinMax (slower, 3-10 seconds)
- **Switch point**: Typically around move 18-22 on 8x8 board

## 9. Future Enhancements

1. **Machine Learning Heuristic**: Train neural network for position evaluation
2. **Opening Book**: Pre-computed strong opening moves
3. **Endgame Database**: Perfect play for last 2-3 pieces
4. **Multi-threading**: Parallel search of move branches
5. **Adaptive Depth**: Increase depth as pieces decrease

## Conclusion

This design provides a robust foundation for implementing both the MinMax and Composite strategies. The modular architecture allows for easy testing, tuning, and future enhancements. Start with the simple configurations and optimize based on performance testing and gameplay evaluation.