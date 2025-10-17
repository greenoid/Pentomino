package de.greenoid.game.pentomino.ui;

import de.greenoid.game.pentomino.model.GameState;
import de.greenoid.game.pentomino.model.PentominoPiece;
import de.greenoid.game.pentomino.model.ComputerStrategy;
import de.greenoid.game.pentomino.model.ComputerStrategyRandom;
import de.greenoid.game.pentomino.model.ComputerStrategyOpenSpace;
import de.greenoid.game.pentomino.model.ComputerStrategyMinMaxProportional;

import javax.swing.*;
import java.awt.*;

/**
 * Main game window for the Pentomino game.
 */
public class PentominoGame extends JFrame {
    private GameState gameState;
    private GameBoardPanel gameBoardPanel;
    private PiecePanel piecePanel;
    private JLabel statusLabel;
    private JLabel currentPlayerLabel;
    private JButton newGameButton;
    private JComboBox<String> strategySelector;
    private JCheckBox darkModeToggle;
    private ComputerStrategy computerStrategy;
    private ComputerStrategy player1Strategy; // For computer-only mode
    private boolean isDarkMode = true;
    private boolean computerOnlyMode = false;
    private boolean isInitializing = true; // Flag to prevent strategy recreation during setup

    public PentominoGame() {
        this(false);
    }

    public PentominoGame(boolean computerOnlyMode) {
        this.computerOnlyMode = computerOnlyMode;
        initializeGame();
        setupUI();
        setupEventHandlers();
        isInitializing = false; // Initialization complete
        
        if (computerOnlyMode) {
            // Start the computer vs computer game
            SwingUtilities.invokeLater(() -> playComputerOnlyGame());
        }
    }

    private void initializeGame() {
        gameState = new GameState();
        // Default to MinMax Proportional Hard (best performance with smooth progression)
        computerStrategy = new ComputerStrategyMinMaxProportional(1.0f);
        
        if (computerOnlyMode) {
            // In computer-only mode, player 1 also uses a strategy
            player1Strategy = new ComputerStrategyMinMaxProportional(1.0f);
        }
    }


    private void setupUI() {
        String title = computerOnlyMode ?
            "Pentomino Game - Computer vs Computer" :
            "Pentomino Game - Human vs Computer";
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        gameBoardPanel = new GameBoardPanel(gameState);
        piecePanel = new PiecePanel(gameState);
        
        // Set dark mode as default
        gameBoardPanel.setDarkMode(true);
        piecePanel.setDarkMode(true);

        // Create control panel
        JPanel controlPanel = createControlPanel();

        // Create info panel
        JPanel infoPanel = createInfoPanel();

        // Add panels to frame
        add(gameBoardPanel, BorderLayout.CENTER);
        add(piecePanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
        
        // Apply dark mode theme
        updateTheme();

        // Set window properties
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 650));
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        newGameButton = new JButton("New Game");
        JButton quitButton = new JButton("Quit Game");
        
        // Add dark mode toggle (selected by default)
        darkModeToggle = new JCheckBox("Dark Mode", true);
        darkModeToggle.addActionListener(e -> toggleDarkMode());
        panel.add(darkModeToggle);

        // Add strategy selector
        panel.add(new JLabel("AI Strategy:"));
        strategySelector = new JComboBox<>(new String[]{
            "MinMax Proportional - Hard (Strength 1.0)",
            "MinMax Proportional - Fair (Strength 1.5)",
            "MinMax Proportional - Easy (Strength 2.0)",
            "Open Space (Medium - 3 iterations)",
            "Random Strategy"
        });
        // Don't set selected index here - it will be set in setupEventHandlers
        // to avoid triggering updateStrategy() during initialization
        panel.add(strategySelector);
        
        // Disable strategy selector in computer-only mode
        if (computerOnlyMode) {
            strategySelector.setEnabled(false);
        }

        panel.add(newGameButton);
        panel.add(quitButton);

        // Add quit button event handler
        quitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                PentominoGame.this,
                "Are you sure you want to quit?",
                "Quit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        statusLabel = new JLabel("Welcome to Pentomino!");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        currentPlayerLabel = new JLabel("Your turn");
        currentPlayerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        currentPlayerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentPlayerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(currentPlayerLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventHandlers() {
        newGameButton.addActionListener(e -> newGame());
        strategySelector.addActionListener(e -> updateStrategy());
        // Set initial selection - updateStrategy() will check isInitializing flag
        strategySelector.setSelectedIndex(0);
    }
    
    /**
     * Toggle between light and dark mode.
     */
    private void toggleDarkMode() {
        isDarkMode = darkModeToggle.isSelected();
        gameBoardPanel.setDarkMode(isDarkMode);
        piecePanel.setDarkMode(isDarkMode);
        updateTheme();
        gameBoardPanel.repaint();
        piecePanel.repaint();
    }
    
    /**
     * Update the theme colors for dark mode.
     */
    private void updateTheme() {
        if (isDarkMode) {
            getContentPane().setBackground(new Color(30, 30, 30));
        } else {
            getContentPane().setBackground(Color.WHITE);
        }
    }

    /**
     * Updates the computer strategy based on the selected option.
     * Only creates a new instance if the strategy type is actually different.
     */
    private void updateStrategy() {
        // Don't update during initialization to preserve the initial Composite instance
        if (isInitializing) {
            return;
        }
        
        String selected = (String) strategySelector.getSelectedItem();
        
        if (selected != null) {
            // Only create new strategy if it's different from current type
            boolean needsNewStrategy = true;
            
            if (selected.contains("Proportional") &&
                computerStrategy instanceof ComputerStrategyMinMaxProportional) {
                // Check if it's the same strength
                ComputerStrategyMinMaxProportional current = (ComputerStrategyMinMaxProportional) computerStrategy;
                float newStrength = 1.0f;
                if (selected.contains("Fair")) newStrength = 1.5f;
                else if (selected.contains("Easy")) newStrength = 2.0f;
                
                if (Math.abs(current.getStrengthFactor() - newStrength) < 0.01f) {
                    needsNewStrategy = false; // Keep same instance
                }
            }
            
            if (needsNewStrategy) {
                if (selected.contains("Proportional - Hard")) {
                    computerStrategy = new ComputerStrategyMinMaxProportional(1.0f);
                } else if (selected.contains("Proportional - Fair")) {
                    computerStrategy = new ComputerStrategyMinMaxProportional(1.5f);
                } else if (selected.contains("Proportional - Easy")) {
                    computerStrategy = new ComputerStrategyMinMaxProportional(2.0f);
                } else if (selected.contains("Open Space")) {
                    computerStrategy = new ComputerStrategyOpenSpace(3);
                } else if (selected.startsWith("Random")) {
                    computerStrategy = new ComputerStrategyRandom();
                }
                
                // Update status to show the new strategy
                statusLabel.setText("Strategy changed to: " + computerStrategy.getStrategyName());
            }
        }
    }

    private void newGame() {
        gameState.reset();
        // Human player always starts first
        gameState.checkCurrentPlayerMoves(); // Check if current player can move
        gameBoardPanel.repaint();
        piecePanel.updateAvailablePieces();
        updateStatusDisplay();
    }

    private void updateStatusDisplay() {
        switch (gameState.getStatus()) {
            case PLAYING:
                if (computerOnlyMode) {
                    if (gameState.getCurrentPlayer() == GameState.Player.PLAYER_1) {
                        currentPlayerLabel.setText("Computer 1's turn");
                        statusLabel.setText("Computer 1 is thinking...");
                    } else {
                        currentPlayerLabel.setText("Computer 2's turn");
                        statusLabel.setText("Computer 2 is thinking...");
                    }
                } else {
                    if (gameState.getCurrentPlayer() == GameState.Player.PLAYER_1) {
                        currentPlayerLabel.setText("Your turn");
                        statusLabel.setText("Game in progress");
                    } else {
                        currentPlayerLabel.setText("Computer's turn");
                        statusLabel.setText("Computer is thinking...");
                    }
                }
                break;
            case PLAYER_1_WINS:
                currentPlayerLabel.setText("Game Over");
                statusLabel.setText(computerOnlyMode ? "Computer 1 wins!" : "You win!");
                break;
            case PLAYER_2_WINS:
                currentPlayerLabel.setText("Game Over");
                statusLabel.setText("Computer" + (computerOnlyMode ? " 2" : "") + " wins!");
                break;
            case DRAW:
                currentPlayerLabel.setText("Game Over");
                statusLabel.setText("It's a draw!");
                break;
        }

        // Update current player color
        if (gameState.getStatus() == GameState.GameStatus.PLAYING) {
            Color playerColor = (gameState.getCurrentPlayer() == GameState.Player.PLAYER_1) ?
                Color.BLUE : Color.RED;
            currentPlayerLabel.setForeground(playerColor);
        } else {
            currentPlayerLabel.setForeground(Color.BLACK);
        }
    }

    /**
     * Updates the display after a move is made.
     */
    public void onMoveMade() {
        // Check if the current player has any legal moves left
        gameState.checkCurrentPlayerMoves();

        if (computerOnlyMode) {
            // In computer-only mode, we don't need to do anything here
            // The playComputerOnlyGame() method handles everything
            return;
        }

        // If game is still playing and it's now computer's turn, make computer move
        if (gameState.getStatus() == GameState.GameStatus.PLAYING &&
            gameState.getCurrentPlayer() == GameState.Player.PLAYER_2) {
            makeComputerMove();
        } else {
            // Human player's turn - update UI normally
            gameBoardPanel.repaint();
            piecePanel.updateAvailablePieces();
            updateStatusDisplay();
        }
    }

    /**
     * Makes a computer move after a delay for better user experience.
     */
    private void makeComputerMove() {
        // Update status to show computer is thinking
        statusLabel.setText("Computer is thinking...");
        currentPlayerLabel.setText("Computer's turn");

        // Use SwingUtilities.invokeLater to make computer move after UI updates
        SwingUtilities.invokeLater(() -> {
            try {
                // Small delay to show "Computer is thinking" message
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Make computer move
            boolean moveMade = gameState.makeComputerMove(computerStrategy);

            // Update UI after computer move
            SwingUtilities.invokeLater(() -> {
                // Check if computer can make another move after this move
                gameState.checkCurrentPlayerMoves();

                gameBoardPanel.repaint();
                piecePanel.updateAvailablePieces();
                updateStatusDisplay();

                // If no move was made, show appropriate message
                if (!moveMade && gameState.getStatus() == GameState.GameStatus.PLAYING) {
                    statusLabel.setText("Computer cannot move!");
                }
            });
        });
    }

    /**
     * Updates the piece panel's selected piece reference.
     */
    public void updatePiecePanelSelection(PentominoPiece piece) {
        piecePanel.setSelectedPiece(piece);
        piecePanel.repaint();
    }

    /**
     * Plays a computer vs computer game automatically.
     * Continues until the game ends.
     */
    private void playComputerOnlyGame() {
        new Thread(() -> {
            while (gameState.getStatus() == GameState.GameStatus.PLAYING) {
                try {
                    // Small delay between moves for visibility
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Determine which strategy to use based on current player
                ComputerStrategy currentStrategy =
                    (gameState.getCurrentPlayer() == GameState.Player.PLAYER_1) ?
                    player1Strategy : computerStrategy;

                // Make the move
                final boolean moveMade = gameState.makeComputerMove(currentStrategy);

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    gameState.checkCurrentPlayerMoves();
                    gameBoardPanel.repaint();
                    piecePanel.updateAvailablePieces();
                    updateStatusDisplay();

                    if (!moveMade && gameState.getStatus() == GameState.GameStatus.PLAYING) {
                        statusLabel.setText("Computer cannot move!");
                    }
                });
            }

            // Game ended - show final message
            SwingUtilities.invokeLater(() -> {
                gameBoardPanel.repaint();
                piecePanel.updateAvailablePieces();
                updateStatusDisplay();
                
                System.out.println("Game ended: " + gameState.getStatus());
                System.out.println("Winner: " + (gameState.getWinner() != null ?
                    gameState.getWinner() : "Draw"));
                
                // Exit after 3 seconds
                try {
                    Thread.sleep(3000);
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.exit(0);
                }
            });
        }).start();
    }

    public static void main(String[] args) {
        // Check for -computeronly flag
        boolean computerOnlyMode = false;
        for (String arg : args) {
            if ("-computeronly".equalsIgnoreCase(arg)) {
                computerOnlyMode = true;
                break;
            }
        }

        final boolean finalMode = computerOnlyMode;
        SwingUtilities.invokeLater(() -> {
            PentominoGame game = new PentominoGame(finalMode);
            game.setVisible(true);
        });
    }
}