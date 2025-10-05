# Pentomino Game Architecture Documentation

## Overview
This document describes the architecture, design patterns, and libraries used in the Pentomino game implementation.

## Architecture Overview

### System Architecture
The Pentomino game follows a **Model-View-Controller (MVC)** architectural pattern:

```
┌─────────────────────────────────────────────────────────┐
│                    PentominoGame (Main)                  │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │   Model     │  │   View      │  │   Controller    │  │
│  │             │  │             │  │                 │  │
│  │ • GameState │  │ • GameBoard │  │ • Mouse Events  │  │
│  │ • Board     │  │ • PiecePanel│  │ • Game Logic    │  │
│  │ • Pieces    │  │ • UI Panels │  │ • Input Handling│  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Package Structure
```
src/main/java/de/greenoid/game/pentomino/
├── model/           # Game logic and data models
│   ├── Board.java              # Game board representation
│   ├── GameState.java          # Overall game state management
│   ├── PentominoPiece.java     # Pentomino piece definitions
│   └── Point.java              # 2D coordinate representation
├── ui/              # User interface components
│   ├── GameBoardPanel.java     # Board visualization
│   ├── PentominoGame.java      # Main game window
│   └── PiecePanel.java         # Available pieces display
└── util/            # Utility classes (if needed)
```

## Design Patterns Used

### 1. Model-View-Controller (MVC)
- **Model**: `GameState`, `Board`, `PentominoPiece` - Handle game logic and data
- **View**: `GameBoardPanel`, `PiecePanel` - Handle visualization and display
- **Controller**: `PentominoGame` - Manages user input and coordinates between Model and View

### 2. Observer Pattern
- UI components observe the `GameState` for changes
- When game state changes, views automatically update
- Implemented through direct method calls and repaint() triggers

### 3. Factory Pattern
- `PentominoPiece.createAllPieces()` - Creates all 12 standard pentomino pieces
- Centralizes piece creation logic and ensures consistency

### 4. Command Pattern
- Move history in `GameState` allows undo functionality
- Each move is stored as a command that can be reversed

### 5. State Pattern
- `GameState.GameStatus` enum manages different game states
- Clean transitions between PLAYING, PLAYER_1_WINS, PLAYER_2_WINS, DRAW

## Libraries and Technologies

### Core Technologies
- **Java 8+** - Primary programming language
- **Swing** - GUI framework for desktop application
- **AWT** - Abstract Window Toolkit for graphics and events

### Build Tools
- **Maven** - Project management and build automation
  - `pom.xml` - Project configuration and dependencies
  - Handles compilation, packaging, and dependency management

### Key Libraries Used

#### Swing Components
```java
javax.swing.*
├── JFrame          // Main application window
├── JPanel          // Container for UI components
├── JButton         // Interactive buttons (New Game, Undo, Quit)
├── JLabel          // Status and player information display
└── JOptionPane     // Dialog boxes for user interaction
```

#### AWT Graphics
```java
java.awt.*
├── Graphics2D      // 2D graphics rendering
├── Color           // Color definitions for pieces
├── Font            // Text rendering
├── BasicStroke     // Shape outlines and borders
└── AlphaComposite  // Transparency effects for previews
```

#### Event Handling
```java
java.awt.event.*
├── MouseAdapter    // Mouse input handling
├── MouseEvent      // Mouse click and movement events
└── ActionListener  // Button click events
```

## Component Architecture

### Model Layer

#### GameState Class
- **Responsibility**: Manages overall game state and rules
- **Key Features**:
  - Player turn management
  - Move validation and execution
  - Game end detection
  - Move history for undo functionality
- **Collaborators**: Board, PentominoPiece

#### Board Class
- **Responsibility**: Represents the 8x8 game board
- **Key Features**:
  - Piece placement validation
  - Occupancy tracking
  - Legal move detection
  - Board state management
- **Data Structures**:
  - `PentominoPiece[8][8]` - Piece positions
  - `boolean[8][8]` - Occupancy matrix

#### PentominoPiece Class
- **Responsibility**: Defines pentomino piece properties and transformations
- **Key Features**:
  - Shape representation using Point arrays
  - Rotation and flip transformations
  - Color coding for visual distinction
  - Normalization for consistent positioning
- **Transformations**:
  - `rotate()` - 90° clockwise rotation
  - `flip()` - Vertical mirror flip
  - `normalize()` - Position standardization

### View Layer

#### GameBoardPanel Class
- **Responsibility**: Renders the game board and handles board interactions
- **Key Features**:
  - Grid drawing and piece visualization
  - Mouse interaction handling
  - Preview rendering for piece placement
  - Real-time board state updates
- **Rendering**:
  - 50px square grid
  - Semi-transparent preview overlays
  - Color-coded piece representation

#### PiecePanel Class
- **Responsibility**: Displays available pieces and handles piece selection
- **Key Features**:
  - Available pieces grid layout
  - Piece selection with visual feedback
  - Rotation and flip interactions
  - Real-time piece inventory updates
- **Layout**: 3-column grid with 15px spacing

#### PentominoGame Class
- **Responsibility**: Main application window and coordination
- **Key Features**:
  - Window layout management
  - Menu and control panel creation
  - Event handling coordination
  - Game state synchronization

### Controller Layer

#### Input Handling
- **Mouse Events**:
  - Left click: Piece selection and placement
  - Right click: Piece rotation
  - Middle click: Piece flipping
- **Button Events**:
  - New Game: Reset game state
  - Undo Move: Reverse last move
  - Quit Game: Exit application

## Data Flow

### Game Initialization
1. `PentominoGame.main()` creates main window
2. `GameState` initializes with all 12 pentomino pieces
3. UI components are created and linked to game state
4. Initial board state is rendered

### Move Execution
1. User selects piece (left click on PiecePanel)
2. User positions piece on board (mouse movement shows preview)
3. User places piece (left click on GameBoardPanel)
4. `GameState.makeMove()` validates and executes move
5. `GameState.checkCurrentPlayerMoves()` verifies game end conditions
6. UI updates to reflect new state

### Piece Transformation
1. User right-clicks piece (rotation) or middle-clicks (flip)
2. `PentominoPiece.rotate()` or `flip()` creates new piece instance
3. `GameState.updatePiece()` replaces piece in available pieces
4. UI refreshes to show transformed piece

## Performance Considerations

### Efficiency Optimizations
- **Immutable Piece Objects**: Pieces are immutable, reducing state corruption risk
- **Copy-on-Write**: `getAvailablePieces()` returns defensive copies
- **Efficient Rendering**: Graphics2D with anti-aliasing for smooth visuals
- **Event Coalescing**: Mouse motion events are handled efficiently

### Memory Management
- **Garbage Collection Friendly**: No circular references between components
- **Resource Cleanup**: Proper disposal of graphics contexts
- **Minimal Object Creation**: Reuse of UI components and graphics objects

## Extensibility

### Easy Extension Points
- **New Piece Types**: Add to `PentominoPiece.PieceType` enum
- **Different Board Sizes**: Modify `Board.SIZE` constant
- **Additional Players**: Extend player management in `GameState`
- **New Game Modes**: Add game mode enumeration and logic

### Configuration
- **Maven Dependencies**: Easily add new libraries via `pom.xml`
- **Resource Management**: Images and assets in `src/main/resources/`
- **Build Configuration**: Customizable build process and packaging

## Testing Strategy

### Potential Test Areas
- **Unit Tests**: Individual model classes (Board, PentominoPiece)
- **Integration Tests**: Game state transitions and move validation
- **UI Tests**: Component rendering and event handling
- **End-to-End Tests**: Complete game scenarios

### Testing Tools
- **JUnit**: Unit testing framework
- **Maven Surefire**: Test execution
- **Swing Testing**: UI component testing utilities

## Deployment

### Build Process
```bash
mvn clean compile    # Compile source code
mvn exec:java        # Run application
mvn package          # Create JAR file
```

### Distribution
- **JAR Packaging**: Self-contained executable JAR
- **Dependencies**: All dependencies bundled via Maven Shade plugin
- **Cross-Platform**: Runs on any Java 8+ compatible platform

## Conclusion

This architecture provides a clean, maintainable, and extensible foundation for the Pentomino game. The MVC pattern ensures separation of concerns, while the use of established design patterns provides consistency and reliability. The choice of Swing for the UI and Maven for build management ensures the application is both functional and easy to develop and deploy.