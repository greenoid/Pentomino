package de.greenoid.game.pentomino.ui;

import de.greenoid.game.pentomino.model.GameState;
import de.greenoid.game.pentomino.model.PentominoPiece;
import de.greenoid.game.pentomino.model.ComputerStrategy;
import de.greenoid.game.pentomino.model.ComputerStrategyRandom;
import de.greenoid.game.pentomino.model.ComputerStrategyOpenSpace;

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
    private boolean isDarkMode = true;

    public PentominoGame() {
        initializeGame();
        setupUI();
        setupEventHandlers();
    }

    private void initializeGame() {
        gameState = new GameState();
        // Default to Open Space Strategy with 3 iterations (balanced difficulty)
        computerStrategy = new ComputerStrategyOpenSpace(3);
    }


    private void setupUI() {
        setTitle("Pentomino Game - Human vs Computer");
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
            "Random Strategy",
            "Open Space (Easy - 1 iteration)",
            "Open Space (Medium - 3 iterations)",
            "Open Space (Hard - 5 iterations)"
        });
        strategySelector.setSelectedIndex(2); // Default to Medium
        panel.add(strategySelector);

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
     */
    private void updateStrategy() {
        String selected = (String) strategySelector.getSelectedItem();
        
        if (selected != null) {
            if (selected.startsWith("Random")) {
                computerStrategy = new ComputerStrategyRandom();
            } else if (selected.contains("Easy")) {
                computerStrategy = new ComputerStrategyOpenSpace(1);
            } else if (selected.contains("Medium")) {
                computerStrategy = new ComputerStrategyOpenSpace(3);
            } else if (selected.contains("Hard")) {
                computerStrategy = new ComputerStrategyOpenSpace(5);
            }
            
            // Update status to show the new strategy
            statusLabel.setText("Strategy changed to: " + computerStrategy.getStrategyName());
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
                if (gameState.getCurrentPlayer() == GameState.Player.PLAYER_1) {
                    currentPlayerLabel.setText("Your turn");
                    statusLabel.setText("Game in progress");
                } else {
                    currentPlayerLabel.setText("Computer's turn");
                    statusLabel.setText("Computer is thinking...");
                }
                break;
            case PLAYER_1_WINS:
                currentPlayerLabel.setText("Game Over");
                statusLabel.setText("You win!");
                break;
            case PLAYER_2_WINS:
                currentPlayerLabel.setText("Game Over");
                statusLabel.setText("Computer wins!");
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
        // Check if the human player (who just moved) has any legal moves left
        gameState.checkCurrentPlayerMoves();

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PentominoGame().setVisible(true));
    }
}