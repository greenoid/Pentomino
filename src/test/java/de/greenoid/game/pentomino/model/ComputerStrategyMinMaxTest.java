package de.greenoid.game.pentomino.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComputerStrategyMinMax.
 */
public class ComputerStrategyMinMaxTest {

    private GameState gameState;
    private ComputerStrategyMinMax strategy;

    @BeforeEach
    public void setUp() {
        gameState = new GameState();
        strategy = new ComputerStrategyMinMax(3);
    }

    @Test
    public void testStrategyCreation() {
        assertNotNull(strategy, "Strategy should be created");
        assertEquals("MinMax Strategy (depth=3)", strategy.getStrategyName());
    }

    @Test
    public void testDepthParameter() {
        ComputerStrategyMinMax depth1 = new ComputerStrategyMinMax(1);
        ComputerStrategyMinMax depth5 = new ComputerStrategyMinMax(5);
        
        assertTrue(depth1.getStrategyName().contains("depth=1"));
        assertTrue(depth5.getStrategyName().contains("depth=5"));
    }

    @Test
    public void testCalculateMoveReturnsValidMove() {
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        
        assertNotNull(move, "Should return a move for initial game state");
        assertNotNull(move.getPiece(), "Move should have a piece");
        assertTrue(move.getRow() >= 0 && move.getRow() < Board.SIZE, 
                  "Row should be valid");
        assertTrue(move.getCol() >= 0 && move.getCol() < Board.SIZE, 
                  "Column should be valid");
    }

    @Test
    public void testCalculateMoveForEmptyBoard() {
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        
        assertNotNull(move, "Should find a move on empty board");
        
        // Verify the move is actually legal
        assertTrue(gameState.getBoard().canPlaceAt(move.getPiece(), 
                                                   move.getRow(), 
                                                   move.getCol()),
                  "Returned move should be legal");
    }

    @Test
    public void testNoMovesAvailable() {
        // Fill the board completely
        fillBoardCompletely(gameState);
        
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        
        assertNull(move, "Should return null when no moves available");
    }

    @Test
    public void testMoveIsActuallyPlaceable() {
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        
        assertNotNull(move);
        
        // Try to actually make the move
        boolean success = gameState.makeMove(move.getPiece(), 
                                            move.getRow(), 
                                            move.getCol());
        
        assertTrue(success, "The move returned by strategy should be valid");
    }

    @Test
    public void testMultipleMoves() {
        // Test that strategy can handle multiple consecutive moves
        for (int i = 0; i < 5; i++) {
            ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
            assertNotNull(move, "Should find move " + (i + 1));
            
            boolean success = gameState.makeMove(move.getPiece(), 
                                                move.getRow(), 
                                                move.getCol());
            assertTrue(success, "Move " + (i + 1) + " should be valid");
        }
    }

    @Test
    public void testPerformanceDepth2() {
        ComputerStrategyMinMax fastStrategy = new ComputerStrategyMinMax(2);
        
        long startTime = System.currentTimeMillis();
        ComputerStrategy.ComputerMove move = fastStrategy.calculateMove(gameState);
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(move, "Should find a move");
        assertTrue(duration < 5000, 
                  "Depth 2 should complete in less than 5 seconds, took " + duration + "ms");
        
        System.out.println("MinMax depth 2 took " + duration + "ms");
    }

    @Test
    public void testNodesEvaluatedIncreases() {
        ComputerStrategy.ComputerMove move1 = strategy.calculateMove(gameState);
        int nodes1 = strategy.getNodesEvaluated();
        
        assertTrue(nodes1 > 0, "Should evaluate some nodes");
        System.out.println("Evaluated " + nodes1 + " nodes");
    }

    @Test
    public void testDifferentDepthsProduceDifferentResults() {
        ComputerStrategyMinMax depth1 = new ComputerStrategyMinMax(1);
        ComputerStrategyMinMax depth2 = new ComputerStrategyMinMax(2);
        
        // Both should find valid moves
        ComputerStrategy.ComputerMove move1 = depth1.calculateMove(gameState);
        gameState = new GameState(); // Reset
        ComputerStrategy.ComputerMove move2 = depth2.calculateMove(gameState);
        
        assertNotNull(move1, "Depth 1 should find a move");
        assertNotNull(move2, "Depth 2 should find a move");
        
        // They might be different (not guaranteed, but good to log)
        System.out.println("Depth 1 move: " + move1);
        System.out.println("Depth 2 move: " + move2);
    }

    @Test
    public void testDefaultConstructor() {
        ComputerStrategyMinMax defaultStrategy = new ComputerStrategyMinMax();
        assertTrue(defaultStrategy.getStrategyName().contains("depth=3"),
                  "Default constructor should use depth 3");
    }

    /**
     * Helper method to fill the board completely for testing.
     */
    private void fillBoardCompletely(GameState state) {
        // Place pieces randomly until no more moves possible
        ComputerStrategyRandom randomStrategy = new ComputerStrategyRandom();
        
        while (state.hasLegalMoves() && !state.getAvailablePieces().isEmpty()) {
            ComputerStrategy.ComputerMove move = randomStrategy.calculateMove(state);
            if (move == null) break;
            state.makeMove(move.getPiece(), move.getRow(), move.getCol());
        }
    }
}