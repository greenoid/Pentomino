package de.greenoid.game.pentomino.ui;

import de.greenoid.game.pentomino.model.GameState;
import de.greenoid.game.pentomino.model.PentominoPiece;
import de.greenoid.game.pentomino.model.ComputerStrategy;
import de.greenoid.game.pentomino.model.ComputerStrategyRandom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JButton undoButton;
    private ComputerStrategy computerStrategy;

    public PentominoGame() {
        initializeGame();
        setupUI();
        setupEventHandlers();
    }

    private void initializeGame() {
        gameState = new GameState();
        computerStrategy = new ComputerStrategyRandom();
    }


    private void setupUI() {
        setTitle("Pentomino Game - Human vs Computer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        gameBoardPanel = new GameBoardPanel(gameState);
        piecePanel = new PiecePanel(gameState);

        // Create control panel
        JPanel controlPanel = createControlPanel();

        // Create info panel
        JPanel infoPanel = createInfoPanel();

        // Add panels to frame
        add(gameBoardPanel, BorderLayout.CENTER);
        add(piecePanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);

        // Set window properties
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 700));
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        newGameButton = new JButton("New Game");
        undoButton = new JButton("Undo Move");
        JButton quitButton = new JButton("Quit Game");

        panel.add(newGameButton);
        panel.add(undoButton);
        panel.add(quitButton);

        // Add quit button event handler
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoMove();
            }
        });
    }

    private void newGame() {
        gameState.reset();
        // Human player always starts first
        gameState.checkCurrentPlayerMoves(); // Check if current player can move
        gameBoardPanel.repaint();
        piecePanel.updateAvailablePieces();
        updateStatusDisplay();
    }

    private void undoMove() {
        if (gameState.undoLastMove()) {
            gameState.checkCurrentPlayerMoves(); // Check if current player can move after undo
            gameBoardPanel.repaint();
            piecePanel.updateAvailablePieces();
            updateStatusDisplay();
        } else {
            JOptionPane.showMessageDialog(this,
                "No moves to undo!",
                "Undo",
                JOptionPane.INFORMATION_MESSAGE);
        }
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Small delay to show "Computer is thinking" message
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Make computer move
                boolean moveMade = gameState.makeComputerMove(computerStrategy);

                // Update UI after computer move
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Check if computer can make another move after this move
                        gameState.checkCurrentPlayerMoves();

                        gameBoardPanel.repaint();
                        piecePanel.updateAvailablePieces();
                        updateStatusDisplay();

                        // If no move was made, show appropriate message
                        if (!moveMade && gameState.getStatus() == GameState.GameStatus.PLAYING) {
                            statusLabel.setText("Computer cannot move!");
                        }
                    }
                });
            }
        });
    }

    public GameState getGameState() {
        return gameState;
    }

    /**
     * Updates the piece panel's selected piece reference.
     */
    public void updatePiecePanelSelection(PentominoPiece piece) {
        piecePanel.setSelectedPiece(piece);
        piecePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PentominoGame().setVisible(true);
            }
        });
    }
}