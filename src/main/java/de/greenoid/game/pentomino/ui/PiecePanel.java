package de.greenoid.game.pentomino.ui;

import de.greenoid.game.pentomino.model.GameState;
import de.greenoid.game.pentomino.model.PentominoPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel that displays the available pentomino pieces for selection.
 */
public class PiecePanel extends JPanel {
    private static final int PIECE_DISPLAY_SIZE = 80;

    private GameState gameState;
    private PentominoPiece selectedPiece;

    public PiecePanel(GameState gameState) {
        this.gameState = gameState;
        this.selectedPiece = null;

        setupPanel();
        setupMouseListener();
    }

    private void setupPanel() {
        setPreferredSize(new Dimension(350, 700));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createTitledBorder("Available Pieces"));
    }

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handlePieceSelection(e);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    handlePieceRotation(e);
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    handlePieceFlip(e);
                }
            }
        });
    }

    private void handlePieceSelection(MouseEvent e) {
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();

        if (availablePieces.isEmpty()) {
            return;
        }

        // Calculate which piece was clicked
        int piecesPerRow = 3;
        int pieceIndex = (e.getY() / (PIECE_DISPLAY_SIZE + 15)) * piecesPerRow + (e.getX() / (PIECE_DISPLAY_SIZE + 15));

        if (pieceIndex >= 0 && pieceIndex < availablePieces.size()) {
            PentominoPiece clickedPiece = availablePieces.get(pieceIndex);

            // If this piece is already selected, deselect it
            if (clickedPiece.equals(selectedPiece)) {
                selectedPiece = null;
            } else {
                selectedPiece = clickedPiece;
            }

            // Update the game board panel
            SwingUtilities.getAncestorOfClass(PentominoGame.class, this);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof PentominoGame) {
                GameBoardPanel gameBoardPanel = findGameBoardPanel(frame);
                if (gameBoardPanel != null) {
                    gameBoardPanel.setSelectedPiece(selectedPiece);
                }
            }

            repaint();
        }
    }

    private void handlePieceRotation(MouseEvent e) {
        // Calculate which piece was clicked
        int piecesPerRow = 3;
        int pieceIndex = (e.getY() / (PIECE_DISPLAY_SIZE + 15)) * piecesPerRow + (e.getX() / (PIECE_DISPLAY_SIZE + 15));

        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        if (pieceIndex >= 0 && pieceIndex < availablePieces.size()) {
            PentominoPiece clickedPiece = availablePieces.get(pieceIndex);

            // Create rotated and normalized version of the clicked piece
            PentominoPiece rotatedPiece = clickedPiece.rotate().normalize();

            // Update the piece in the game state
            gameState.updatePiece(pieceIndex, rotatedPiece);

            // If this piece is selected, update the selected piece reference
            if (clickedPiece.equals(selectedPiece)) {
                selectedPiece = rotatedPiece;

                // Update the game board panel with the rotated piece
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof PentominoGame) {
                    GameBoardPanel gameBoardPanel = findGameBoardPanel(frame);
                    if (gameBoardPanel != null) {
                        gameBoardPanel.setSelectedPiece(selectedPiece);
                    }
                }
            }

            repaint();
        }
    }

    private void handlePieceFlip(MouseEvent e) {
        // Calculate which piece was clicked
        int piecesPerRow = 3;
        int pieceIndex = (e.getY() / (PIECE_DISPLAY_SIZE + 15)) * piecesPerRow + (e.getX() / (PIECE_DISPLAY_SIZE + 15));

        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        if (pieceIndex >= 0 && pieceIndex < availablePieces.size()) {
            PentominoPiece clickedPiece = availablePieces.get(pieceIndex);

            // Create flipped and normalized version of the clicked piece
            PentominoPiece flippedPiece = clickedPiece.flip().normalize();

            // Update the piece in the game state
            gameState.updatePiece(pieceIndex, flippedPiece);

            // If this piece is selected, update the selected piece reference
            if (clickedPiece.equals(selectedPiece)) {
                selectedPiece = flippedPiece;

                // Update the game board panel with the flipped piece
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof PentominoGame) {
                    GameBoardPanel gameBoardPanel = findGameBoardPanel(frame);
                    if (gameBoardPanel != null) {
                        gameBoardPanel.setSelectedPiece(selectedPiece);
                    }
                }
            }

            repaint();
        }
    }

    private GameBoardPanel findGameBoardPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof GameBoardPanel) {
                return (GameBoardPanel) component;
            } else if (component instanceof Container) {
                GameBoardPanel panel = findGameBoardPanel((Container) component);
                if (panel != null) {
                    return panel;
                }
            }
        }
        return null;
    }

    public void setSelectedPiece(PentominoPiece piece) {
        this.selectedPiece = piece;
    }

    public void updateAvailablePieces() {
        // Clear selection if the selected piece is no longer available
        if (selectedPiece != null) {
            List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
            if (!availablePieces.contains(selectedPiece)) {
                selectedPiece = null;

                // Update game board panel
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame instanceof PentominoGame) {
                    GameBoardPanel gameBoardPanel = findGameBoardPanel(frame);
                    if (gameBoardPanel != null) {
                        gameBoardPanel.setSelectedPiece(null);
                    }
                }
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawAvailablePieces(g2d);

        g2d.dispose();
    }

    private void drawAvailablePieces(Graphics2D g2d) {
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();

        if (availablePieces.isEmpty()) {
            drawNoPiecesMessage(g2d);
            return;
        }

        int piecesPerRow = 3;
        int pieceSpacing = 15;
        int startX = 25;
        int startY = 25;

        for (int i = 0; i < availablePieces.size(); i++) {
            PentominoPiece piece = availablePieces.get(i);

            int row = i / piecesPerRow;
            int col = i % piecesPerRow;

            int x = startX + col * (PIECE_DISPLAY_SIZE + pieceSpacing);
            int y = startY + row * (PIECE_DISPLAY_SIZE + pieceSpacing);

            drawPiece(g2d, piece, x, y, isPieceSelected(piece));
        }
    }

    private void drawPiece(Graphics2D g2d, PentominoPiece piece, int x, int y, boolean isSelected) {
        // Draw selection outline with appropriate color
        g2d.setColor(isSelected ? Color.RED : Color.BLACK);
        g2d.setStroke(new BasicStroke(isSelected ? 3 : 2));
        g2d.drawRect(x - 5, y - 5, PIECE_DISPLAY_SIZE + 10, PIECE_DISPLAY_SIZE + 10);
        g2d.setStroke(new BasicStroke(1)); // Reset stroke

        // Draw piece squares
        for (de.greenoid.game.pentomino.model.Point point : piece.getShape()) {
            int squareX = x + point.getX() * (PIECE_DISPLAY_SIZE / 5);
            int squareY = y + point.getY() * (PIECE_DISPLAY_SIZE / 5);
            int squareSize = PIECE_DISPLAY_SIZE / 5;

            // Fill with piece color
            g2d.setColor(piece.getColor());
            g2d.fillRect(squareX, squareY, squareSize, squareSize);

            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(squareX, squareY, squareSize, squareSize);
        }

    }

    private void drawNoPiecesMessage(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        Font font = new Font(Font.SANS_SERIF, Font.ITALIC, 14);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        String message = "No pieces available";
        int textX = (getWidth() - fm.stringWidth(message)) / 2;
        int textY = getHeight() / 2 + fm.getAscent() / 2;

        g2d.drawString(message, textX, textY);
    }

    private boolean isPieceSelected(PentominoPiece piece) {
        return piece != null && piece.equals(selectedPiece);
    }

    private int getPieceIndex(PentominoPiece piece) {
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        return availablePieces.indexOf(piece);
    }
}