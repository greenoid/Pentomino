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
    private static final int PIECE_DISPLAY_SIZE = 70;
    private static final int CELL_SIZE = 12;

    private GameState gameState;
    private PentominoPiece selectedPiece;
    private boolean isDarkMode = false;

    public PiecePanel(GameState gameState) {
        this.gameState = gameState;
        this.selectedPiece = null;

        setupPanel();
        setupMouseListener();
    }

    private void setupPanel() {
        setPreferredSize(new Dimension(280, 650));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createTitledBorder("Available Pieces"));
    }
    
    /**
     * Set dark mode for the piece panel.
     */
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        if (darkMode) {
            setBackground(new Color(30, 30, 30));
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                "Available Pieces",
                0, 0, null, Color.LIGHT_GRAY));
        } else {
            setBackground(new Color(250, 250, 250));
            setBorder(BorderFactory.createTitledBorder("Available Pieces"));
        }
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
        int pieceSpacing = 10;
        int startX = 15;
        int startY = 20;

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
        // Calculate bounding box for the piece
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        
        for (de.greenoid.game.pentomino.model.Point point : piece.getShape()) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }
        
        int pieceWidth = (maxX - minX + 1) * CELL_SIZE;
        int pieceHeight = (maxY - minY + 1) * CELL_SIZE;
        
        // Center the piece in the display box
        int offsetX = (PIECE_DISPLAY_SIZE - pieceWidth) / 2;
        int offsetY = (PIECE_DISPLAY_SIZE - pieceHeight) / 2;
        
        // Draw selection outline with appropriate color
        Color outlineColor = isSelected ? new Color(255, 60, 60) : (isDarkMode ? new Color(80, 80, 80) : Color.GRAY);
        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(isSelected ? 3 : 1));
        g2d.drawRect(x - 3, y - 3, PIECE_DISPLAY_SIZE + 6, PIECE_DISPLAY_SIZE + 6);
        g2d.setStroke(new BasicStroke(1)); // Reset stroke

        // Draw piece squares with gradient
        for (de.greenoid.game.pentomino.model.Point point : piece.getShape()) {
            int squareX = x + offsetX + (point.getX() - minX) * CELL_SIZE;
            int squareY = y + offsetY + (point.getY() - minY) * CELL_SIZE;

            // Draw gradient square
            drawGradientSquare(g2d, squareX, squareY, CELL_SIZE, piece.getColor());

            // Draw border
            g2d.setColor(isDarkMode ? new Color(60, 60, 60) : Color.BLACK);
            g2d.drawRect(squareX, squareY, CELL_SIZE, CELL_SIZE);
        }
        
        // Draw piece letter at center bottom of bounding box
        g2d.setColor(isDarkMode ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        String letter = piece.getType().toString();
        int textX = x + (PIECE_DISPLAY_SIZE - fm.stringWidth(letter)) / 2;
        int textY = y + PIECE_DISPLAY_SIZE + 2 + fm.getAscent();

        g2d.drawString(letter, textX, textY);
    }
    
    /**
     * Draw a square with brightness gradient (brighter in center, darker at edges).
     */
    private void drawGradientSquare(Graphics2D g2d, int x, int y, int size, Color baseColor) {
        // Create a radial gradient from center
        float centerX = x + size / 2f;
        float centerY = y + size / 2f;
        float radius = size * 0.7f;
        
        // Create brighter center color
        Color brighterColor = new Color(
            Math.min(255, (int)(baseColor.getRed() * 1.4)),
            Math.min(255, (int)(baseColor.getGreen() * 1.4)),
            Math.min(255, (int)(baseColor.getBlue() * 1.4))
        );
        
        // Create darker edge color
        Color darkerColor = new Color(
            (int)(baseColor.getRed() * 0.6),
            (int)(baseColor.getGreen() * 0.6),
            (int)(baseColor.getBlue() * 0.6)
        );
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            centerX, centerY, radius,
            new float[]{0.0f, 1.0f},
            new Color[]{brighterColor, darkerColor}
        );
        
        g2d.setPaint(gradient);
        g2d.fillRect(x, y, size, size);
    }

    private void drawNoPiecesMessage(Graphics2D g2d) {
        g2d.setColor(isDarkMode ? Color.LIGHT_GRAY : Color.GRAY);
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