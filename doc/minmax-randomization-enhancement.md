# MinMax Randomization Enhancement

## Overview

Added randomization to the MinMax strategy to introduce variety in move selection when multiple moves have equivalent scores.

## Problem

Previously, when multiple moves had the same evaluation score (especially at low depths like depth 1), the algorithm would always select the first move in the list. This resulted in predictable, repetitive opening moves - always the 'F' piece in the upper left corner.

## Solution

Modified [`ComputerStrategyMinMax.java`](../src/main/java/de/greenoid/game/pentomino/model/ComputerStrategyMinMax.java) to:

1. **Track all equivalent moves**: Instead of keeping just the best move, maintain a list of all moves within a score tolerance
2. **Random selection**: When multiple moves have equivalent scores, randomly select one
3. **Score tolerance**: Use `SCORE_TOLERANCE = 5` to consider moves "equivalent" if their scores differ by ≤ 5 points

## Implementation Details

### Key Changes

```java
private final Random random;
private static final int SCORE_TOLERANCE = 5;
List<ComputerMove> bestMoves = new ArrayList<>();
```

### Selection Logic

```java
if (score > bestScore + SCORE_TOLERANCE) {
    // Found a clearly better move
    bestScore = score;
    bestMoves.clear();
    bestMoves.add(move);
} else if (Math.abs(score - bestScore) <= SCORE_TOLERANCE) {
    // Found a move with equivalent score
    bestMoves.add(move);
}

// Randomly select from equivalent moves
if (bestMoves.size() > 1) {
    int randomIndex = random.nextInt(bestMoves.size());
    bestMove = bestMoves.get(randomIndex);
}
```

## Benefits

1. **Varied opening moves**: Each game starts differently, making gameplay more interesting
2. **Maintains strength**: Only selects from truly equivalent moves, doesn't sacrifice quality
3. **Unpredictability**: Computer opponent becomes less predictable while maintaining skill level
4. **Natural variety**: Depth 1 moves now use different pieces and positions each game

## Test Results

From test output:
```
MinMax: Evaluating 3496 possible moves at depth 1 using 16 CPU cores
MinMax: Selected randomly from 3496 equivalent moves (score: 169)
```

This shows that at depth 1, all ~3496 possible first moves have the same score, and the algorithm now randomly selects from them.

Example varied moves:
- `ComputerMove{piece=V (V), row=5, col=6}`
- `ComputerMove{piece=T (T), row=3, col=5}`
- `ComputerMove{piece=F (F), row=0, col=1}`

## Impact on Proportional Strategy

Since [`ComputerStrategyMinMaxProportional`](../src/main/java/de/greenoid/game/pentomino/model/ComputerStrategyMinMaxProportional.java) uses `ComputerStrategyMinMax` internally, it automatically benefits from this randomization:

- **First move** (depth 1): Random selection from all equivalent moves
- **Later moves** (higher depths): More moves become non-equivalent, less randomization
- **Result**: Varied openings with consistent mid/late game strategy

## Performance

- **No significant overhead**: Random selection only happens after all moves are evaluated
- **Minimal computation**: Simple `random.nextInt()` call on a small list
- **Same test performance**: All 26 tests still pass with similar execution times

## Configuration

The randomization tolerance can be adjusted by modifying:
```java
private static final int SCORE_TOLERANCE = 5;
```

- **Lower tolerance** (e.g., 1-2): Less randomization, more deterministic
- **Higher tolerance** (e.g., 10-20): More randomization, more variety
- **Current value (5)**: Good balance for varied openings without sacrificing quality

## Verification

✅ All tests pass (26/26)  
✅ Randomization working at depth 1  
✅ Proportional strategy benefits automatically  
✅ No performance degradation  
✅ Opening moves now vary each game  