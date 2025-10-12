# Pentomino Game - Java Swing Implementation

A strategic two-player Pentomino game with AI opponents of varying difficulty levels.

## Features

- **Human vs Computer Gameplay**: Play against AI opponents
- **Multiple AI Strategies**: 
  - Random Strategy (baseline)
  - Open Space Strategy with 3 difficulty levels (Easy, Medium, Hard)
- **Interactive GUI**: Clean Swing-based interface
- **Strategic AI**: Advanced diffusion-based algorithm that maintains future placement options

## Quick Start

### Running the Game

Simply execute the jar file:
```bash
java -jar target/Pentomino-1.0-SNAPSHOT.jar
```

Or build and run with Maven:
```bash
mvn clean package
java -jar target/Pentomino-1.0-SNAPSHOT.jar
```

Or run directly with Maven:
```bash
mvn exec:java -Dexec.mainClass="de.greenoid.game.pentomino.ui.PentominoGame"
```

## Building from Source

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Build Commands
```bash
# Compile only
mvn clean compile

# Build jar file
mvn clean package

# Run without building jar
mvn exec:java -Dexec.mainClass="de.greenoid.game.pentomino.ui.PentominoGame"
```

## AI Strategy

### Open Space Strategy
The advanced AI uses an iterative diffusion algorithm to evaluate board positions:

1. **Initialization**: Board positions marked as 1 (free) or 0 (occupied)
2. **Diffusion**: Each position's value becomes the sum of its 8 neighbors
3. **Iteration**: Process repeats N times (configurable 1-5)
4. **Scoring**: Each move scored by summing position values
5. **Selection**: Highest-scoring move selected (random if tied)

### Strategy Levels
- **Random**: Baseline, completely random moves
- **Easy (1 iteration)**: Basic improvement, considers immediate neighbors
- **Medium (3 iterations)**: Balanced strategy, regional patterns ⭐ *Default*
- **Hard (5 iterations)**: Most strategic, board-wide influence

### Expected Performance
- **Win Rate vs Random**: 60-70%
- **Move Speed**: 10-20ms (instant to user)
- **Strategic Benefits**: 
  - Avoids creating isolated pockets
  - Maintains future placement options
  - Better board coverage

## Game Controls

- **AI Strategy Dropdown**: Select computer difficulty level
- **New Game**: Start a new game
- **Undo Move**: Undo the last move
- **Quit Game**: Exit the application

## Project Structure

```
Pentomino/
├── src/main/java/de/greenoid/game/pentomino/
│   ├── model/
│   │   ├── Board.java                      # Game board logic
│   │   ├── ComputerStrategy.java           # Strategy interface
│   │   ├── ComputerStrategyRandom.java     # Random AI
│   │   ├── ComputerStrategyOpenSpace.java  # Advanced AI
│   │   ├── GameState.java                  # Game state management
│   │   ├── PentominoPiece.java             # Piece definitions
│   │   └── Point.java                      # Coordinate handling
│   └── ui/
│       ├── GameBoardPanel.java             # Board visualization
│       ├── PentominoGame.java              # Main window
│       └── PiecePanel.java                 # Piece selection panel
├── doc/
│   ├── architecture.md                     # System architecture
│   ├── rules.md                            # Game rules
│   ├── openspace-strategy-plan.md          # Algorithm design
│   ├── implementation-guide.md             # Technical specifications
│   ├── strategy-testing-guide.md           # Testing instructions
│   └── README-OpenSpaceStrategy.md         # Strategy summary
├── pom.xml                                 # Maven configuration
└── README.md                               # This file
```

## Documentation

- **[Architecture](doc/architecture.md)**: System design and component structure
- **[Game Rules](doc/rules.md)**: How to play Pentomino
- **[Strategy Plan](doc/openspace-strategy-plan.md)**: Algorithm design and visualization
- **[Implementation Guide](doc/implementation-guide.md)**: Detailed code specifications
- **[Testing Guide](doc/strategy-testing-guide.md)**: How to test and compare strategies
- **[Strategy Summary](doc/README-OpenSpaceStrategy.md)**: Complete implementation summary

## Development

### Code Quality
- ✅ No compilation warnings
- ✅ Clean code with lambda expressions
- ✅ Comprehensive documentation
- ✅ Proper Maven configuration
- ✅ Executable jar with manifest

### Testing
The game has been tested with:
- Multiple strategy levels
- Various game scenarios
- Performance benchmarking
- User interface testing

## License

This project is created as an educational implementation of the Pentomino game.

## Version History

### 1.0-SNAPSHOT (Current)
- ✅ Implemented Open Space Strategy with diffusion algorithm
- ✅ Added configurable AI difficulty levels
- ✅ Created strategy selector UI
- ✅ Fixed all code warnings
- ✅ Created executable jar with proper MANIFEST.MF
- ✅ Comprehensive documentation

---

**Enjoy playing Pentomino!**