# MinMax Proportional Strategy

## Overview

The `ComputerStrategyMinMaxProportional` is a new variant of the MinMax strategy that uses **proportional depth scaling** based on the number of moves already made in the game.

## Key Features

### 1. Proportional Depth Calculation
- **Formula**: `depth = floor(computerMoveCount / strengthFactor)`
- **Where**: `computerMoveCount = floor((totalMoves + 1) / 2)`
- **Computer move 1** (total move 1): depth = 1 (at strength 1.0)
- **Computer move 2** (total move 3): depth = 2 (at strength 1.0)
- **Computer move N** (total move 2N-1): depth = N (at strength 1.0)
- **Maximum**: capped at depth 6 to prevent excessive computation

### 2. Adjustable Strength Factor
The strength factor is a **float value** that controls the difficulty:

| Strength | Difficulty | Description | Example (Computer Move 4) |
|----------|-----------|-------------|---------------------------|
| 1.0 | Hard | Full depth, strongest play | depth = 4 |
| 1.5 | Fair | Moderate depth, balanced | depth = 2 |
| 2.0 | Easy | Half depth, easier | depth = 2 |

### 3. Technical Implementation
- Uses **MinMax algorithm** with **alpha-beta pruning**
- Implements **parallelization** across available CPU cores
- Inherits all performance optimizations from `ComputerStrategyMinMax`
- No threshold switching - smooth, continuous depth increase

## Advantages Over Dynamic Strategy

### Dynamic Strategy (Old)
- Uses fixed depth thresholds (e.g., depth 2 until 32 empty squares)
- Sudden jumps in difficulty at threshold points
- Depth based on empty squares, not move progression

### Proportional Strategy (New)
- Smooth, linear increase in search depth
- Depth directly correlates with computer's move progression
- Fine-tunable difficulty with float strength factor
- More intuitive: computer's later moves naturally require deeper analysis
- Counts only computer moves, not total moves

## Usage in Menu

The strategy is available in three preset difficulties:

1. **MinMax Proportional - Hard (Strength 1.0)**
   - Full proportional depth
   - Move N → depth N
   - Strongest opponent

2. **MinMax Proportional - Fair (Strength 1.5)**
   - Moderate depth scaling
   - Computer move 3 → depth 2
   - Balanced difficulty

3. **MinMax Proportional - Easy (Strength 2.0)**
   - Half-depth scaling
   - Computer move 4 → depth 2
   - Easier opponent

## Example Game Progression

### Strength 1.0 (Hard)
```
Total move 1 (Computer move 1) → depth 1
Total move 3 (Computer move 2) → depth 2
Total move 5 (Computer move 3) → depth 3
Total move 7 (Computer move 4) → depth 4
Total move 9 (Computer move 5) → depth 5
Total move 11+ (Computer move 6+) → depth 6 (capped)
```

### Strength 1.5 (Fair)
```
Total move 1 (Computer move 1) → depth 1
Total move 3 (Computer move 2) → depth 1
Total move 5 (Computer move 3) → depth 2
Total move 7 (Computer move 4) → depth 2
Total move 9 (Computer move 5) → depth 3
Total move 11 (Computer move 6) → depth 4
Total move 13 (Computer move 7) → depth 4
Total move 15 (Computer move 8) → depth 5
Total move 17+ (Computer move 9+) → depth 6 (capped)
```

### Strength 2.0 (Easy)
```
Total move 1-3 (Computer move 1-2) → depth 1
Total move 5-7 (Computer move 3-4) → depth 2
Total move 9-11 (Computer move 5-6) → depth 3
Total move 13-15 (Computer move 7-8) → depth 4
Total move 17-19 (Computer move 9-10) → depth 5
Total move 21+ (Computer move 11+) → depth 6 (capped)
```

## Performance Characteristics

- **Early game**: Very fast (depth 1-2)
- **Mid game**: Moderate speed (depth 3-4)
- **Late game**: Slower but thorough (depth 5-6)
- **Computation time**: Increases gradually with game progression
- **Parallelization**: Efficient use of multiple CPU cores

## Code Example

```java
// Create with specific strength
ComputerStrategy hard = new ComputerStrategyMinMaxProportional(1.0f);
ComputerStrategy fair = new ComputerStrategyMinMaxProportional(1.5f);
ComputerStrategy easy = new ComputerStrategyMinMaxProportional(2.0f);

// Or use the builder
ComputerStrategy custom = new ComputerStrategyMinMaxProportional.Builder()
    .withStrength(1.8f)
    .build();

// Or use difficulty presets
ComputerStrategy preset = new ComputerStrategyMinMaxProportional.Builder()
    .withDifficulty("fair")
    .build();
```

## Testing Recommendations

1. **Start with Fair (1.5)** for initial testing
2. **Try Hard (1.0)** for competitive play
3. **Use Easy (2.0)** for beginners or quick games
4. **Monitor console output** to see depth changes per move

## Implementation Details

- **File**: `src/main/java/de/greenoid/game/pentomino/model/ComputerStrategyMinMaxProportional.java`
- **Lines of code**: 167
- **Dependencies**: `ComputerStrategyMinMax`, `GameState`, `ComputerMove`
- **Thread safety**: Creates new MinMax instances per move (thread-safe)