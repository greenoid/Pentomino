package de.greenoid.game.pentomino.ui;

import de.greenoid.game.pentomino.model.Board;
import de.greenoid.game.pentomino.model.GameState;
import de.greenoid.game.pentomino.model.PentominoPiece;
import de.greenoid.game.pentomino.model.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel that displays the 8x8 game board and handles piece placement.
 */
public class GameBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 60;
    private static final int BOARD_SIZE = Board.SIZE * SQUARE_SIZE;

    private final GameState gameState;
    private Point hoverPosition;
    private PentominoPiece selectedPiece;
    private PentominoPiece previewPiece;
    private Point previewPosition;

    public GameBoardPanel(GameState gameState) {
        this.gameState = gameState;
        this.hoverPosition = null;
        this.selectedPiece = null;
        this.previewPiece = null;
        this.previewPosition = null;

        setupPanel();
        setupMouseListener();
    }

    private void setupPanel() {
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        // Let FlatLaf handle the background color
        setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 2));
    }

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleMouseClick(e);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    handleRotation(e);
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    handleFlip(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                hoverPosition = null;
                previewPiece = null;
                previewPosition = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoverPosition(e);
            }
        });
    }

    private void handleMouseClick(MouseEvent e) {
        Point boardPosition = getBoardPosition(e.getPoint());

        if (boardPosition == null) {
            return;
        }

        PentominoGame mainWindow = (PentominoGame) SwingUtilities.getWindowAncestor(this);
        if (mainWindow == null) {
            return;
        }

        // If we have a selected piece, try to place it
        if (selectedPiece != null) {
            if (gameState.makeMove(selectedPiece, boardPosition.getY(), boardPosition.getX())) {
                mainWindow.onMoveMade();
                selectedPiece = null;
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cannot place piece there!",
                    "Invalid Move",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleRotation(MouseEvent e) {
        if (selectedPiece == null) {
            return;
        }

        // Rotate the selected piece
        PentominoPiece rotatedPiece = selectedPiece.rotate().normalize();
        
        // Update the piece in the game state
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        for (int i = 0; i < availablePieces.size(); i++) {
            if (availablePieces.get(i).equals(selectedPiece)) {
                gameState.updatePiece(i, rotatedPiece);
                selectedPiece = rotatedPiece;
                
                // Update PiecePanel
                PentominoGame mainWindow = (PentominoGame) SwingUtilities.getWindowAncestor(this);
                if (mainWindow != null) {
                    mainWindow.updatePiecePanelSelection(rotatedPiece);
                }
                break;
            }
        }
        
        repaint();
    }

    private void handleFlip(MouseEvent e) {
        if (selectedPiece == null) {
            return;
        }

        // Flip the selected piece
        PentominoPiece flippedPiece = selectedPiece.flip().normalize();
        
        // Update the piece in the game state
        List<PentominoPiece> availablePieces = gameState.getAvailablePieces();
        for (int i = 0; i < availablePieces.size(); i++) {
            if (availablePieces.get(i).equals(selectedPiece)) {
                gameState.updatePiece(i, flippedPiece);
                selectedPiece = flippedPiece;
                
                // Update PiecePanel
                PentominoGame mainWindow = (PentominoGame) SwingUtilities.getWindowAncestor(this);
                if (mainWindow != null) {
                    mainWindow.updatePiecePanelSelection(flippedPiece);
                }
                break;
            }
        }
        
        repaint();
    }

    private void updateHoverPosition(MouseEvent e) {
        Point boardPosition = getBoardPosition(e.getPoint());

        if (boardPosition != null && selectedPiece != null) {
            hoverPosition = boardPosition;
            previewPiece = selectedPiece;
            previewPosition = boardPosition;
        } else {
            hoverPosition = boardPosition;
            previewPiece = null;
            previewPosition = null;
        }

        repaint();
    }

    private Point getBoardPosition(java.awt.Point mousePoint) {
        int col = (int) mousePoint.getX() / SQUARE_SIZE;
        int row = (int) mousePoint.getY() / SQUARE_SIZE;

        if (col >= 0 && col < Board.SIZE && row >= 0 && row < Board.SIZE) {
            return new Point(col, row);
        }

        return null;
    }

    public void setSelectedPiece(PentominoPiece piece) {
        this.selectedPiece = piece;
        repaint();
    }

    public PentominoPiece getSelectedPiece() {
        return selectedPiece;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2d);
        drawGrid(g2d);
        drawPlacedPieces(g2d);
        drawPreview(g2d);

        g2d.dispose();
    }

    private void drawBoard(Graphics2D g2d) {
        // Fill background
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);
    }

    private void drawGrid(Graphics2D g2d) {
        // Use theme-aware grid color
        g2d.setColor(UIManager.getColor("Component.borderColor"));

        // Draw vertical lines
        for (int i = 0; i <= Board.SIZE; i++) {
            int x = i * SQUARE_SIZE;
            g2d.drawLine(x, 0, x, BOARD_SIZE);
        }

        // Draw horizontal lines
        for (int i = 0; i <= Board.SIZE; i++) {
            int y = i * SQUARE_SIZE;
            g2d.drawLine(0, y, BOARD_SIZE, y);
        }
    }

    private void drawPlacedPieces(Graphics2D g2d) {
        Board board = gameState.getBoard();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                PentominoPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    drawPieceSquare(g2d, piece, col, row);
                }
            }
        }
    }

    private void drawPieceSquare(Graphics2D g2d, PentominoPiece piece, int col, int row) {
        int x = col * SQUARE_SIZE + 1;
        int y = row * SQUARE_SIZE + 1;
        int size = SQUARE_SIZE - 2;

        // Draw gradient square
        drawGradientSquare(g2d, x, y, size, piece.getColor());

        // Draw border
        g2d.setColor(UIManager.getColor("Component.borderColor"));
        g2d.drawRect(x, y, size, size);

        // Draw piece letter
        g2d.setColor(getContrastColor(piece.getColor()));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        String letter = piece.getType().toString();
        int textX = x + (size - fm.stringWidth(letter)) / 2;
        int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;

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

    private void drawPreview(Graphics2D g2d) {
        if (previewPiece != null && previewPosition != null) {
            // Draw semi-transparent preview
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            for (Point point : previewPiece.getShape()) {
                int col = previewPosition.getX() + point.getX();
                int row = previewPosition.getY() + point.getY();

                if (col >= 0 && col < Board.SIZE && row >= 0 && row < Board.SIZE) {
                    int x = col * SQUARE_SIZE + 1;
                    int y = row * SQUARE_SIZE + 1;
                    int size = SQUARE_SIZE - 2;

                    // Check if this position is valid for placement
                    boolean isValid = true;
                    for (Point p : previewPiece.getShape()) {
                        int r = previewPosition.getY() + p.getY();
                        int c = previewPosition.getX() + p.getX();
                        if (!gameState.getBoard().isValidPosition(r, c) ||
                            gameState.getBoard().isOccupied(r, c)) {
                            isValid = false;
                            break;
                        }
                    }

                    if (isValid) {
                        g2d.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
                    } else {
                        g2d.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
                    }

                    g2d.fillRect(x, y, size, size);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, size, size);
                }
            }

            g2d.setComposite(originalComposite);
        }
    }

    private Color getContrastColor(Color color) {
        // Calculate luminance
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }
}