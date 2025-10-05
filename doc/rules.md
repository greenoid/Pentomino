# Pentomino Game Rules

## Overview

Pentomino is a geometric puzzle game that uses pieces made of five identical squares, each connected edge-to-edge. The game challenges players to place all pieces onto a game board without overlaps or gaps.

## Game Components

### The Pieces (Pentominoes)

A pentomino is a polyomino composed of 5 congruent squares connected orthogonally (sharing edges). There are exactly **12 distinct free pentominoes**, each named with a single letter that resembles its shape:

| Name | Description | Visual Shape |
|------|-------------|--------------|
| **F** | Like the letter F | 🟦🟦🟦<br>🟦⬜⬜<br>⬜🟦⬜ |
| **I** | Straight line | 🟦🟦🟦🟦🟦 |
| **L** | Like the letter L | 🟦🟦🟦🟦<br>🟦⬜⬜⬜ |
| **N** | Like a Z but skewed | 🟦🟦🟦🟦<br>⬜⬜🟦🟦 |
| **P** | Like the letter P | 🟦🟦🟦<br>🟦🟦⬜ |
| **T** | Like the letter T | 🟦🟦🟦<br>⬜🟦⬜ |
| **U** | Like the letter U | 🟦⬜🟦<br>🟦🟦🟦 |
| **V** | Like a V shape | 🟦🟦🟦<br>🟦⬜⬜<br>🟦⬜⬜ |
| **W** | Like the letter W | 🟦🟦🟦<br>⬜🟦⬜<br>🟦⬜⬜ |
| **X** | Like the letter X | ⬜🟦⬜<br>🟦🟦🟦<br>⬜🟦⬜ |
| **Y** | Like the letter Y | ⬜🟦⬜<br>🟦🟦🟦<br>⬜🟦⬜ |
| **Z** | Like the letter Z | 🟦🟦⬜<br>⬜🟦🟦<br>⬜🟦⬜ |

Each pentomino has a distinct color in this implementation:
- **F**: Red
- **I**: Dark Blue
- **L**: Light Brown
- **N**: Dark Brown
- **P**: Dark Gray
- **T**: Light Gray
- **U**: Dark Brown
- **V**: Orange
- **W**: Black
- **X**: Light Green
- **Y**: Dark Green
- **Z**: Yellow

### The Game Board

The standard Pentomino game uses an **8×8 square board** (64 squares total). This provides exactly enough space for all 12 pentominoes since 12 × 5 = 60 squares, leaving 4 empty squares.

## Objective

The primary objective is to **place all 12 pentomino pieces** onto the 8×8 game board without:
- Overlapping pieces
- Going outside the board boundaries
- Leaving any piece unplaced

## How to Play

### Setup
1. All 12 pentomino pieces are available to both players
2. The 8×8 board starts empty
3. Players take turns placing one piece per turn

### Taking Turns
1. **Player 1** starts the game
2. On your turn, select any available pentomino piece
3. **Rotate** the piece by right-clicking (90° clockwise)
4. **Flip** the piece by middle-clicking (vertical mirror)
5. **Place** the piece on the board by left-clicking on the desired position
6. The game validates that the placement is legal (no overlaps, within bounds)
7. If placement is invalid, you must choose a different position or piece

### Legal Placement Rules

A piece placement is legal if:
- All squares of the piece are within the 8×8 board boundaries
- None of the piece's squares overlap with already placed pieces
- The piece lies flat on the board (no stacking)

### Piece Transformations

Each pentomino piece can be:
- **Rotated** in 4 orientations (0°, 90°, 180°, 270°)
- **Flipped** vertically (mirror image)
- This gives 8 possible transformations per piece (4 rotations × 2 flips)

Note: Some pieces (like the X pentomino) look the same when flipped, so they have fewer distinct transformations.

## Winning the Game

### Victory Conditions
A player wins when:
- All 12 pentominoes have been successfully placed on the board, OR
- The opposing player cannot make a legal move (no remaining pieces can fit)

### Game End Scenarios

1. **Successful Completion**: All pieces placed - game ends in a draw (both players "win")
2. **Player Cannot Move**: If a player has no legal moves available with any remaining pieces in any orientation, the other player wins
3. **Board Full**: If the board becomes completely filled before all pieces are placed, the player who placed the last piece wins

## Strategy Tips

### Basic Strategy
1. **Plan ahead**: Consider how each piece placement affects future moves
2. **Save space**: Leave room for larger or more awkward pieces
3. **Use corners and edges**: These can help anchor pieces
4. **Think about the empty spaces**: The remaining 4 squares must be usable

### Advanced Techniques
1. **Piece rotation and flipping**: Always consider all 8 possible transformations
2. **Sacrifice placements**: Sometimes placing a piece in a "suboptimal" position opens up better opportunities later
3. **Pattern recognition**: Learn common pentomino arrangements and configurations

## Scoring (Optional)

While the basic game doesn't require scoring, players can track:
- **Completion time**: How quickly they solve the puzzle
- **Number of moves**: Fewer moves might indicate better efficiency
- **Pieces placed per minute**: Measure placement speed

## Variants

### Common Variations
1. **Single Player**: Solve as a puzzle without time pressure
2. **Against Time**: Race against the clock
3. **Different Board Sizes**: 6×10, 5×12, or other rectangles
4. **Themed Challenges**: Place pieces to form specific patterns
5. **Handicap**: One player uses only certain pieces

### Tournament Play
In competitive settings:
- Players alternate who goes first
- Time limits per move or total game
- Points awarded for successful completions
- Bonus points for faster solutions

## Mathematical Background

### Combinatorics
- **12 distinct free pentominoes**
- **8 possible orientations** per piece (4 rotations × 2 flips)
- **63 possible positions** per orientation on 8×8 board
- **Total possibilities**: 12 × 8 × 63 = 60,480 (though many are illegal)

### Board Coverage
- **60 squares covered** by pentominoes
- **4 squares remain empty** on 8×8 board
- **Perfect tiling** requires careful space management

## History

Pentominoes were invented by American mathematician **Solomon W. Golomb** in 1953. The name combines "pent" (five) with "domino" (two). Golomb proved that there are exactly 12 distinct pentominoes and analyzed their properties extensively.

The game has since become popular worldwide, with applications in:
- Recreational mathematics
- Computer science (tiling algorithms)
- Education (spatial reasoning)
- Art and design

## References

- Golomb, Solomon W. "Polyominoes: Puzzles, Patterns, Problems, and Packings" (Princeton University Press, 1994)
- Wikipedia: [Pentomino](https://en.wikipedia.org/wiki/Pentomino)
- The Pentomino Dictionary (maintained by the mathematics community)

---

*This implementation follows the standard Pentomino rules with two-player competitive gameplay, where players take turns placing pieces and the first player unable to move loses.*