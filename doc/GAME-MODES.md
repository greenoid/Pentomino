# Pentomino Game Modes

## Overview

The Pentomino game now supports two game modes and features the advanced Composite strategy as the default AI.

## Game Modes

### 1. Human vs Computer (Default)

This is the standard mode where a human player competes against the computer AI.

**How to Run**:
```bash
# Using Maven
mvn exec:java

# Or run the JAR
java -jar target/Pentomino-1.0-SNAPSHOT.jar
```

**Features**:
- Human player (Player 1) always moves first
- Computer AI (Player 2) responds automatically
- Strategy can be changed during gameplay via dropdown menu
- Full UI interaction with piece selection and placement

### 2. Computer vs Computer Mode

An automated mode where two computer AIs play against each other. The game runs automatically and exits when complete.

**How to Run**:
```bash
# Using Maven
mvn exec:java -Dexec.args="-computeronly"

# Or run the JAR
java -jar target/Pentomino-1.0-SNAPSHOT.jar -computeronly
```

**Features**:
- Both players are controlled by AI
- Player 1: ComputerStrategyComposite
- Player 2: ComputerStrategyComposite
- Moves are made automatically with 1-second delay for visibility
- Game exits automatically 3 seconds after completion
- Strategy selector is disabled
- Useful for testing, analysis, and demonstrations

## Default AI Strategy

### ComputerStrategyComposite (NEW DEFAULT)

The game now defaults to the **Composite Strategy**, which provides the best overall performance:

- **Early Game (Moves 1-20)**: Uses OpenSpace strategy (<1ms per move)
- **Endgame (Move 21+)**: Switches to MinMax strategy (3-10s per move)
- **Automatic Switching**: Based on board state threshold (28 empty squares)

**Why Composite is Default**:
- ⚡ Fast early game moves
- 🎯 Optimal endgame tactics
- 🏆 Best win rate against other strategies
- 🔄 Adaptive gameplay

## Available AI Strategies

The strategy selector offers these options (in order):

1. **Composite Strategy (Recommended)** - Default, best overall
2. **MinMax Only (Depth 2 - Fast)** - 1-3 seconds per move
3. **MinMax Only (Depth 3 - Strong)** - 3-10 seconds per move
4. **Open Space (Easy - 1 iteration)** - Weakest OpenSpace
5. **Open Space (Medium - 3 iterations)** - Balanced OpenSpace
6. **Open Space (Hard - 5 iterations)** - Strongest OpenSpace
7. **Random Strategy** - Random valid moves only

### Strategy Comparison

| Strategy | Speed | Strength | Best For |
|----------|-------|----------|----------|
| Composite | ⚡⚡ | ⭐⭐⭐⭐⭐ | **General play** |
| MinMax (D3) | ⚡ | ⭐⭐⭐⭐ | Endgame focus |
| MinMax (D2) | ⚡⚡ | ⭐⭐⭐ | Fast games |
| OpenSpace (Hard) | ⚡⚡⚡ | ⭐⭐⭐ | Quick tactical |
| OpenSpace (Medium) | ⚡⚡⚡ | ⭐⭐ | Beginner-friendly |
| OpenSpace (Easy) | ⚡⚡⚡ | ⭐ | Learning |
| Random | ⚡⚡⚡ | ⭐ | Testing |

## Computer-Only Mode Details

### Purpose

Computer-only mode is designed for:
- **Strategy Testing**: Compare different AI strategies
- **Performance Analysis**: Benchmark strategy performance
- **Demonstrations**: Show AI capabilities
- **Research**: Analyze gameplay patterns

### Game Flow

1. **Initialization**:
   - Both players use ComputerStrategyComposite
   - Game board and UI are created
   - Strategy selector is disabled

2. **Gameplay**:
   - Game starts automatically
   - Player 1 (Computer 1) moves first
   - 1-second delay between moves for visualization
   - Board updates after each move
   - Status shows which computer is thinking

3. **Completion**:
   - Game continues until win/loss/draw
   - Final status displayed
   - Result printed to console
   - Application exits after 3 seconds

### Console Output

The game logs key events to the console:

```
Game ended: PLAYER_2_WINS
Winner: PLAYER_2
```

Or for a draw:
```
Game ended: DRAW
Winner: Draw
```

### Example Session

```bash
$ java -jar Pentomino.jar -computeronly

# Window opens showing "Computer vs Computer"
# Game plays automatically
# Composite: Using early game strategy (Open Space Strategy (3 iterations))
# ... moves continue ...
# ========================================
# COMPOSITE STRATEGY: Switching to endgame!
#   Move number: 19
#   Empty squares: 27
#   Pieces remaining: 7
# ...
# Game ended: PLAYER_2_WINS
# Winner: PLAYER_2
# [Application exits]
```

## Performance Expectations

### Human vs Computer Mode

| Phase | Response Time | Experience |
|-------|---------------|------------|
| Early Game | <1s | Instant moves |
| Mid Game | 1-2s | Quick response |
| Endgame | 3-10s | Thoughtful play |
| Strategy Change | Instant | Seamless |

### Computer-Only Mode

| Phase | Move Time | Total Game |
|-------|-----------|------------|
| Moves 1-20 | <1s each | ~30-40s |
| Switch | Instant | - |
| Moves 21+ | 3-10s each | ~60-120s |
| **Total** | - | **~2-3 minutes** |

## Command Line Options

```bash
# Show help (if implemented)
java -jar Pentomino.jar -help

# Computer-only mode
java -jar Pentomino.jar -computeronly

# Regular mode (default)
java -jar Pentomino.jar
```

## UI Differences by Mode

### Human vs Computer Mode
- ✓ Strategy selector enabled
- ✓ Piece panel interactive
- ✓ Board accepts mouse clicks
- ✓ New Game button active
- ✓ Title: "Human vs Computer"

### Computer-Only Mode
- ✗ Strategy selector disabled (grayed out)
- ✓ Piece panel visible (display only)
- ✗ Board non-interactive
- ✓ New Game button active (but ineffective)
- ✓ Title: "Computer vs Computer"

## Tips and Tricks

### For Human Players
1. Start with Composite strategy (default)
2. Watch for strategy switch message
3. Try different strategies to learn
4. Use MinMax for challenging games

### For Testing/Analysis
1. Use `-computeronly` for automated testing
2. Compare strategies by running multiple games
3. Monitor console for switch points
4. Time different strategy combinations

### For Development
1. Computer-only mode great for debugging
2. Watch for performance bottlenecks
3. Verify strategy switching works correctly
4. Test game end conditions

## Troubleshooting

### Game Runs Too Slowly
- Switch to MinMax Depth 2
- Use OpenSpace strategies
- Avoid MinMax Depth 3 in early game

### Computer Takes Too Long
- MinMax at depth 3 can take up to 10 seconds
- This is expected behavior for strong play
- Switch to faster strategy if needed

### Computer-Only Mode Doesn't Exit
- Check console for errors
- Ensure game reached terminal state
- May need to manually close if exception occurs

### Strategy Not Changing
- Ensure you selected from dropdown
- Check status message for confirmation
- Strategy applies to next computer move

## Future Enhancements

Potential additions for computer-only mode:
- [ ] Specify different strategies for each player
- [ ] Run multiple games automatically
- [ ] Generate statistics and reports
- [ ] Save game logs for analysis
- [ ] Headless mode (no UI) for faster runs
- [ ] Batch testing mode

## References

- **User Guide**: [`README-MinMax-Composite.md`](README-MinMax-Composite.md)
- **Technical Design**: [`minmax-composite-strategy-design.md`](minmax-composite-strategy-design.md)
- **Test Results**: [`TEST-RESULTS.md`](TEST-RESULTS.md)
- **Main README**: [`README-STRATEGIES.md`](../README-STRATEGIES.md)