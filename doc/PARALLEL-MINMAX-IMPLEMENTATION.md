# Parallel MinMax Implementation

## Overview

All MinMax strategies in the Pentomino game have been successfully parallelized to utilize all available CPU cores, following the pattern from the reference `ComputerPlayer.java` implementation.

## Changes Made

### 1. ComputerStrategyMinMax.java
- **Added imports**: `java.util.concurrent.*` for parallel processing support
- **Added ExecutorService**: Creates a fixed thread pool with `Runtime.getRuntime().availableProcessors()` threads
- **Parallelized root move evaluation**: All top-level moves are now evaluated concurrently using `Callable` tasks and `Future` objects
- **Added shutdown() method**: Properly cleans up executor service resources
- **Updated strategy name**: Now includes ", parallel" suffix to indicate parallel execution

**Key Implementation Details:**
- Root-level moves are submitted as parallel tasks to the executor service
- Each task creates a copy of the game state and evaluates the move using minimax
- Results are collected via `Future.get()` calls
- Maintains alpha-beta pruning within each sequential minimax tree traversal
- Error handling ensures a fallback move is returned even if parallel execution encounters issues

### 2. ComputerStrategyMinMaxDiffusion.java
- **Same parallel implementation** as ComputerStrategyMinMax
- Uses diffusion-based heuristic for position evaluation
- Added ExecutorService and parallel root move evaluation
- Added shutdown() method for resource cleanup
- Updated strategy name to include ", parallel" suffix

### 3. ComputerStrategyMinMaxDynamic.java
- **Added shutdown() method**: Delegates to underlying strategies (early, mid, end)
- Ensures all three internal MinMax strategies properly clean up their executor services
- Updated strategy name to include ", parallel" suffix
- No changes to phase-switching logic - it continues to work seamlessly with parallel strategies

### 4. ComputerStrategyMinMaxTest.java
- **Updated test expectations**: Changed expected strategy name from "MinMax Strategy (depth=1)" to "MinMax Strategy (depth=1, parallel)"

## Performance Benefits

The parallel implementation provides significant performance improvements by:

1. **Multi-core utilization**: All CPU cores are used simultaneously during root move evaluation
2. **Faster move calculations**: Particularly beneficial with many possible moves (early/mid game)
3. **Maintained quality**: Same move quality as sequential version (uses identical minimax algorithm)
4. **Time limit safety**: Maintains 15-second thinking time limit with proper timeout handling

### Test Results
- All 26 tests pass successfully
- Parallel execution confirmed (logs show "using 16 CPU cores" on test system)
- Move calculation times remain within acceptable limits
- No regressions in move quality or correctness

## Architecture Pattern

The implementation follows the proven pattern from `ComputerPlayer.java`:

```java
// 1. Create executor service in constructor
ExecutorService executorService = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);

// 2. Submit parallel tasks for root moves
List<Future<Integer>> futures = new ArrayList<>();
for (ComputerMove move : possibleMoves) {
    Callable<Integer> task = () -> {
        GameState newState = new GameState(gameState);
        newState.makeMove(move.getPiece(), move.getRow(), move.getCol());
        return minimax(newState, depth - 1, alpha, beta, !maximizing);
    };
    futures.add(executorService.submit(task));
}

// 3. Collect results
for (int i = 0; i < futures.size(); i++) {
    int score = futures.get(i).get();
    // Update best move based on score
}

// 4. Clean up in shutdown()
executorService.shutdown();
```

## Resource Management

**Important**: Strategies with executor services should have their `shutdown()` method called when no longer needed:

```java
ComputerStrategyMinMax strategy = new ComputerStrategyMinMax(3);
// ... use strategy ...
strategy.shutdown(); // Clean up resources
```

For `ComputerStrategyMinMaxDynamic`, calling `shutdown()` will cascade to all three internal strategies.

## Backward Compatibility

- All existing code continues to work without modifications
- Strategies can be used exactly as before
- Only difference: significantly faster execution on multi-core systems
- Strategy names updated to indicate parallel execution

## Conclusion

The parallelization successfully enables all CPU cores to contribute to MinMax move calculations, dramatically improving performance while maintaining code quality, correctness, and the original algorithm's behavior.