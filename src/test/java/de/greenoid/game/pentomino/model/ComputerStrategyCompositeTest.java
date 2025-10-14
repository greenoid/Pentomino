package de.greenoid.game.pentomino.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComputerStrategyComposite.
 */
public class ComputerStrategyCompositeTest {

    private GameState gameState;
    private ComputerStrategyComposite strategy;

    @BeforeEach
    public void setUp() {
        gameState = new GameState();
        strategy = new ComputerStrategyComposite();
    }

    @Test
    public void testStrategyCreation() {
        assertNotNull(strategy, "Strategy should be created");
        assertTrue(strategy.getStrategyName().contains("Composite Strategy"));
    }

    @Test
    public void testDefaultConstructor() {
        ComputerStrategyComposite defaultStrategy = new ComputerStrategyComposite();
        
        assertFalse(defaultStrategy.hasSwitched(), 
                   "Should start in early game mode");
        assertEquals(-1, defaultStrategy.getSwitchMove(), 
                    "Switch move should be -1 initially");
    }

    @Test
    public void testInitiallyInEarlyGameMode() {
        assertFalse(strategy.hasSwitched(), "Should start in early game mode");
        assertTrue(strategy.getStrategyName().contains("EARLY"));
    }

    @Test
    public void testCalculateMoveReturnsValidMove() {
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        
        assertNotNull(move, "Should return a move for initial game state");
        assertNotNull(move.getPiece(), "Move should have a piece");
    }

    @Test
    public void testStrategySwitchingOccurs() {
        // Play moves until board fills up and strategy switches
        int maxMoves = 30; // Enough to trigger switch
        boolean switched = false;
        
        for (int i = 0; i < maxMoves && gameState.getStatus() == GameState.GameStatus.PLAYING; i++) {
            ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
            if (move == null) break;
            
            gameState.makeMove(move.getPiece(), move.getRow(), move.getCol());
            
            if (strategy.hasSwitched()) {
                switched = true;
                System.out.println("Strategy switched at move " + strategy.getSwitchMove());
                break;
            }
        }
        
        assertTrue(switched, "Strategy should have switched to endgame by move 30");
    }

    @Test
    public void testSwitchOnlyHappensOnce() {
        // Fill board to trigger switch
        fillBoardPartially(gameState, 35); // 35 squares filled, 29 empty
        
        assertFalse(strategy.hasSwitched());
        
        // Make one move - should trigger switch
        strategy.calculateMove(gameState);
        gameState.makeComputerMove(strategy);
        
        boolean switchedAfterFirst = strategy.hasSwitched();
        int firstSwitchMove = strategy.getSwitchMove();
        
        if (switchedAfterFirst) {
            // Make more moves - switch should not change
            for (int i = 0; i < 3 && gameState.hasLegalMoves(); i++) {
                gameState.makeComputerMove(strategy);
            }
            
            assertEquals(firstSwitchMove, strategy.getSwitchMove(),
                        "Switch move should not change after first switch");
        }
    }

    @Test
    public void testGetters() {
        assertNotNull(strategy.getEarlyGameStrategy(), 
                     "Should have early game strategy");
        assertNotNull(strategy.getEndGameStrategy(), 
                     "Should have endgame strategy");
        assertNotNull(strategy.getThresholdCalculator(), 
                     "Should have threshold calculator");
    }

    @Test
    public void testBuilderPattern() {
        ComputerStrategyComposite customStrategy = new ComputerStrategyComposite.Builder()
            .withEarlyGameStrategy(new ComputerStrategyRandom())
            .withEndGameStrategy(new ComputerStrategyMinMax(2))
            .withSimpleThreshold(24)
            .build();
        
        assertNotNull(customStrategy);
        assertNotNull(customStrategy.getEarlyGameStrategy());
        assertNotNull(customStrategy.getEndGameStrategy());
    }

    @Test
    public void testBuilderWithDefaults() {
        ComputerStrategyComposite strategy = new ComputerStrategyComposite.Builder()
            .build();
        
        assertNotNull(strategy);
        assertNotNull(strategy.getEarlyGameStrategy());
        assertNotNull(strategy.getEndGameStrategy());
        assertNotNull(strategy.getThresholdCalculator());
    }

    @Test
    public void testBuilderWithSimpleThreshold() {
        ComputerStrategyComposite strategy = new ComputerStrategyComposite.Builder()
            .withSimpleThreshold(30)
            .build();
        
        ThresholdCalculator calc = strategy.getThresholdCalculator();
        assertTrue(calc instanceof SimpleThresholdCalculator);
    }

    @Test
    public void testBuilderWithDiffusionThreshold() {
        ComputerStrategyComposite strategy = new ComputerStrategyComposite.Builder()
            .withDiffusionThreshold(50, 3)
            .build();
        
        ThresholdCalculator calc = strategy.getThresholdCalculator();
        assertTrue(calc instanceof DiffusionThresholdCalculator);
    }

    @Test
    public void testResetSwitchState() {
        // Trigger a switch
        fillBoardPartially(gameState, 36);
        strategy.calculateMove(gameState);
        gameState.makeComputerMove(strategy);
        
        if (strategy.hasSwitched()) {
            strategy.resetSwitchState();
            
            assertFalse(strategy.hasSwitched(), 
                       "Switch state should be reset");
            assertEquals(-1, strategy.getSwitchMove(), 
                        "Switch move should be reset");
        }
    }

    @Test
    public void testNullStrategiesThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComputerStrategyComposite(null, 
                                         new ComputerStrategyMinMax(), 
                                         new SimpleThresholdCalculator());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new ComputerStrategyComposite(new ComputerStrategyRandom(), 
                                         null, 
                                         new SimpleThresholdCalculator());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new ComputerStrategyComposite(new ComputerStrategyRandom(), 
                                         new ComputerStrategyMinMax(), 
                                         null);
        });
    }

    @Test
    public void testStrategyNameChangesAfterSwitch() {
        String initialName = strategy.getStrategyName();
        assertTrue(initialName.contains("EARLY"), 
                  "Initial name should indicate early game");
        
        // Fill board to trigger switch
        fillBoardPartially(gameState, 36);
        strategy.calculateMove(gameState);
        gameState.makeComputerMove(strategy);
        
        if (strategy.hasSwitched()) {
            String newName = strategy.getStrategyName();
            assertTrue(newName.contains("ENDGAME"), 
                      "Name after switch should indicate endgame");
        }
    }

    @Test
    public void testPerformanceEarlyGame() {
        // Early game should be fast
        long startTime = System.currentTimeMillis();
        ComputerStrategy.ComputerMove move = strategy.calculateMove(gameState);
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(move);
        assertTrue(duration < 2000, 
                  "Early game move should be fast (<2s), took " + duration + "ms");
        
        System.out.println("Composite early game move took " + duration + "ms");
    }

    /**
     * Helper method to fill the board partially.
     */
    private void fillBoardPartially(GameState state, int targetSquares) {
        ComputerStrategyRandom randomStrategy = new ComputerStrategyRandom();
        
        while (state.getBoard().getOccupiedSquareCount() < targetSquares && 
               state.hasLegalMoves()) {
            ComputerStrategy.ComputerMove move = randomStrategy.calculateMove(state);
            if (move == null) break;
            state.makeMove(move.getPiece(), move.getRow(), move.getCol());
        }
    }
}