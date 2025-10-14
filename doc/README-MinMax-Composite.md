# MinMax and Composite Strategy User Guide

## Overview

This document provides usage instructions for the two new advanced computer strategies implemented for the Pentomino game:

1. **ComputerStrategyMinMax** - Optimal tactical play using minimax with alpha-beta pruning
2. **ComputerStrategyComposite** - Intelligent strategy that switches from fast early-game play to optimal endgame

## Quick Start

### Using MinMax Strategy

```java
// Create MinMax strategy with default depth (3)
ComputerStrategy minmax = new ComputerStrategyMinMax();

// Or specify custom depth
ComputerStrategy minmax = new ComputerStrategyMinMax(2); // Faster
ComputerStrategy minmax = new ComputerStrategyMinMax(4); // Stronger but slower

// Use in game
GameState game = new GameState();
game.makeComputerMove(minmax);
```

### Using Composite Strategy

```java
// Use default configuration (OpenSpace + MinMax)
ComputerStrategy composite = new ComputerStrategyComposite();

// Or customize with Builder
ComputerStrategy composite = new ComputerStrategyComposite.Builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(3))
    .withEndGameStrategy(new ComputerStrategyMinMax(3))
    .withSimpleThreshold(28)
    .build();

// Use in game
GameState game = new GameState();
game.makeComputerMove(composite);
```

## ComputerStrategyMinMax

### Description

Implements the minimax algorithm with alpha-beta pruning to find optimal moves by exploring the game tree. Most effective in endgame situations where the search space is manageable.

### Configuration

#### Depth Parameter

The depth parameter controls how many moves ahead the algorithm looks:

| Depth | Description | Performance | Use Case |
|-------|-------------|-------------|----------|
| 1 | Computer's move only | <1 second | Testing/debugging |
| 2 | Computer + opponent response | 1-3 seconds | Fast gameplay |
| **3** | **Computer + opponent + computer** | **3-10 seconds** | **Recommended default** |
| 4 | Two full move pairs | 10-60 seconds | Maximum strength |
| 5 | Three moves deep | >60 seconds | Analysis only |

#### Constructor Options

```java
// Default depth (3)
new ComputerStrategyMinMax()

// Custom depth (1-5)
new ComputerStrategyMinMax(3)
```

### Features

- **Alpha-Beta Pruning**: Reduces search space by 50-70%
- **Mobility Heuristic**: Evaluates positions based on available moves
- **Time Limit**: 30-second safety limit prevents infinite computation
- **Performance Monitoring**: Logs nodes evaluated and computation time

### Example Output

```
MinMax: Evaluating 45 possible moves at depth 3
MinMax: Evaluated 1247 nodes in 4832ms, best score: 12
```

### When to Use

✓ **Good for:**
- Endgame positions (few pieces remaining)
- Critical tactical positions
- Finding guaranteed wins
- Analysis and study

✗ **Not ideal for:**
- Early game (too slow, too many options)
- Positions with >10 pieces remaining
- Time-constrained scenarios

## ComputerStrategyComposite

### Description

Combines two strategies for optimal gameplay:
- **Early game**: Fast heuristic strategy (default: OpenSpace)
- **Endgame**: Thorough tactical strategy (default: MinMax)

Automatically switches between strategies based on game state.

### Configuration

#### Default Configuration

```java
new ComputerStrategyComposite()
// Uses:
//   - Early: OpenSpace (3 iterations)
//   - End: MinMax (depth 3)
//   - Threshold: 28 empty squares
```

#### Custom Configuration with Builder

```java
ComputerStrategy strategy = new ComputerStrategyComposite.Builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(3))
    .withEndGameStrategy(new ComputerStrategyMinMax(3))
    .withSimpleThreshold(28)  // Or withThreshold(new SimpleThresholdCalculator(28))
    .build();
```

#### Direct Constructor

```java
ComputerStrategy strategy = new ComputerStrategyComposite(
    new ComputerStrategyOpenSpace(3),    // Early game
    new ComputerStrategyMinMax(3),       // Endgame
    new SimpleThresholdCalculator(28)    // Threshold
);
```

### Threshold Calculators

#### SimpleThresholdCalculator

Switches based on number of empty squares remaining:

```java
// Switch when 28 or fewer squares are empty (default)
new SimpleThresholdCalculator(28)

// Earlier switch (more endgame time)
new SimpleThresholdCalculator(32)

// Later switch (less endgame computation)
new SimpleThresholdCalculator(24)
```

**Recommended values for 8x8 board:**
- 24-26: Earlier switch, more thorough endgame
- 28: Balanced (default, switches around move 18-22)
- 30-32: Later switch, faster overall

#### DiffusionThresholdCalculator

Switches based on board openness (more sophisticated):

```java
// Switch when max openness falls to 50 or below
new DiffusionThresholdCalculator(50, 3)

// More sensitive to fragmentation
new DiffusionThresholdCalculator(60, 3)

// Less sensitive
new DiffusionThresholdCalculator(40, 3)
```

**Parameters:**
- First: Maximum openness threshold (40-60 recommended)
- Second: Diffusion iterations (3 recommended)

### Example Output

When switching occurs:

```
========================================
COMPOSITE STRATEGY: Switching to endgame!
  Move number: 19
  Empty squares: 27
  Pieces remaining: 7
  From: Open Space Strategy (3 iterations)
  To: MinMax Strategy (depth=3)
  Threshold: Simple threshold (switch at 28 empty squares)
========================================
```

### When to Use

✓ **Best for:**
- Complete games (start to finish)
- Balanced performance and strength
- General gameplay
- Tournament or competitive play

✓ **Advantages:**
- Fast early moves (<1 second)
- Optimal endgame play (3-10 seconds)
- Adaptive to game state
- Best overall performance

## Configuration Presets

### Fast Configuration (Development/Testing)

```java
// MinMax with depth 2
ComputerStrategy minmax = new ComputerStrategyMinMax(2);

// Composite with early threshold
ComputerStrategy composite = new ComputerStrategyComposite.Builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(2))
    .withEndGameStrategy(new ComputerStrategyMinMax(2))
    .withSimpleThreshold(24)
    .build();
```

**Characteristics:**
- Fast computation (~1-3 seconds per move)
- Moderate strength
- Good for testing and debugging

### Balanced Configuration (Recommended)

```java
// MinMax with depth 3
ComputerStrategy minmax = new ComputerStrategyMinMax(3);

// Composite with default settings
ComputerStrategy composite = new ComputerStrategyComposite();
```

**Characteristics:**
- Good performance (3-10 seconds per move in endgame)
- Strong tactical play
- Recommended for regular gameplay

### Maximum Strength Configuration

```java
// MinMax with depth 4
ComputerStrategy minmax = new ComputerStrategyMinMax(4);

// Composite with late threshold
ComputerStrategy composite = new ComputerStrategyComposite.Builder()
    .withEarlyGameStrategy(new ComputerStrategyOpenSpace(4))
    .withEndGameStrategy(new ComputerStrategyMinMax(4))
    .withDiffusionThreshold(60, 3)
    .build();
```

**Characteristics:**
- Slow computation (10-60 seconds per move)
- Maximum strength
- Best for analysis or demonstrations

## Integration with Game

### In PentominoGame UI

To integrate these strategies into the main game UI, modify the computer opponent selection:

```java
// In PentominoGame class
private ComputerStrategy computerStrategy;

// Initialize with desired strategy
this.computerStrategy = new ComputerStrategyComposite(); // or new ComputerStrategyMinMax(3)

// Use when computer makes a move
if (gameState.getCurrentPlayer() == GameState.Player.PLAYER_2) {
    gameState.makeComputerMove(computerStrategy);
    gameBoardPanel.repaint();
}
```

### Strategy Selection Menu

Add a menu to let users choose strategies:

```java
JMenu strategyMenu = new JMenu("Computer Strategy");

JMenuItem randomItem = new JMenuItem("Random");
randomItem.addActionListener(e -> computerStrategy = new ComputerStrategyRandom());

JMenuItem openSpaceItem = new JMenuItem("Open Space");
openSpaceItem.addActionListener(e -> computerStrategy = new ComputerStrategyOpenSpace(3));

JMenuItem minMaxItem = new JMenuItem("MinMax (Depth 3)");
minMaxItem.addActionListener(e -> computerStrategy = new ComputerStrategyMinMax(3));

JMenuItem compositeItem = new JMenuItem("Composite (Default)");
compositeItem.addActionListener(e -> computerStrategy = new ComputerStrategyComposite());

strategyMenu.add(randomItem);
strategyMenu.add(openSpaceItem);
strategyMenu.add(minMaxItem);
strategyMenu.add(compositeItem);

menuBar.add(strategyMenu);
```

## Performance Expectations

### Typical Move Times (8x8 board)

| Strategy | Early Game | Mid Game | End Game |
|----------|-----------|----------|----------|
| Random | <0.1s | <0.1s | <0.1s |
| OpenSpace | 0.5-1s | 0.5-1s | 0.5-1s |
| MinMax (depth 2) | 1-2s | 1-3s | 0.5-2s |
| MinMax (depth 3) | 3-8s | 4-10s | 2-6s |
| MinMax (depth 4) | 10-30s | 15-60s | 5-20s |
| Composite | 0.5-1s | 0.5-1s | 4-10s |

### Memory Usage

- Random: ~5 MB
- OpenSpace: ~10 MB
- MinMax (depth 3): ~50-80 MB
- Composite: ~10-80 MB (depends on phase)

## Troubleshooting

### MinMax is too slow

**Solutions:**
1. Reduce depth: `new ComputerStrategyMinMax(2)`
2. Use Composite with earlier threshold
3. Increase time limit if needed

### MinMax doesn't find obvious wins

**Check:**
1. Depth might be too low - try depth 4
2. Verify heuristic evaluation is working
3. Ensure game state copying works correctly

### Composite switches too early/late

**Adjust threshold:**
```java
// Switch earlier (more endgame time)
.withSimpleThreshold(30)

// Switch later (less total endgame time)
.withSimpleThreshold(24)
```

### Out of memory errors

**Solutions:**
1. Reduce MinMax depth
2. Increase JVM heap size: `java -Xmx512m`
3. Use SimpleThresholdCalculator instead of Diffusion

## Testing and Validation

### Quick Test

```java
public static void main(String[] args) {
    GameState game = new GameState();
    ComputerStrategy strategy = new ComputerStrategyComposite();
    
    // Play several moves
    for (int i = 0; i < 10; i++) {
        ComputerStrategy.ComputerMove move = strategy.calculateMove(game);
        if (move == null) break;
        
        game.makeMove(move.getPiece(), move.getRow(), move.getCol());
        System.out.println("Move " + (i+1) + ": " + move);
    }
}
```

### Strategy Comparison

Compare different strategies by playing them against each other:

```java
GameState game = new GameState();
ComputerStrategy player1 = new ComputerStrategyOpenSpace(3);
ComputerStrategy player2 = new ComputerStrategyComposite();

while (game.getStatus() == GameState.GameStatus.PLAYING) {
    ComputerStrategy current = (game.getCurrentPlayer() == GameState.Player.PLAYER_1) 
        ? player1 : player2;
    
    if (!game.makeComputerMove(current)) {
        break;
    }
}

System.out.println("Winner: " + game.getWinner());
```

## Further Reading

- **Design Document**: [`doc/minmax-composite-strategy-design.md`](minmax-composite-strategy-design.md) - Complete technical design
- **Implementation Plan**: [`doc/implementation-plan.md`](implementation-plan.md) - Development roadmap
- **Summary**: [`doc/strategy-implementation-summary.md`](strategy-implementation-summary.md) - Executive overview
- **OpenSpace Strategy**: [`doc/README-OpenSpaceStrategy.md`](README-OpenSpaceStrategy.md) - Early game strategy details

## Support

For questions or issues:
1. Check the design documents for technical details
2. Review the implementation plan for troubleshooting
3. Examine the strategy testing guide
4. Review the source code documentation (JavaDoc)