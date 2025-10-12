package de.greenoid.game.pentomino.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the overall state of a Pentomino game.
 */
public class GameState {
    public enum Player {
        PLAYER_1, PLAYER_2
    }

    public enum GameStatus {
        PLAYING, PLAYER_1_WINS, PLAYER_2_WINS, DRAW
    }

    private final Board board;
    private final List<PentominoPiece> availablePieces;
    private Player currentPlayer;
    private GameStatus status;
    private Player winner;
    private final List<Move> moveHistory;

    public GameState() {
        this.board = new Board();
        this.availablePieces = new ArrayList<>(PentominoPiece.createAllPieces());
        this.currentPlayer = Player.PLAYER_1;
        this.status = GameStatus.PLAYING;
        this.moveHistory = new ArrayList<>();
    }

    /**
     * Creates a copy of another game state.
     */
    public GameState(GameState other) {
        this.board = new Board(other.board);
        this.availablePieces = new ArrayList<>(other.availablePieces);
        this.currentPlayer = other.currentPlayer;
        this.status = other.status;
        this.winner = other.winner;
        this.moveHistory = new ArrayList<>(other.moveHistory);
    }

    public Board getBoard() {
        return board;
    }

    public List<PentominoPiece> getAvailablePieces() {
        return new ArrayList<>(availablePieces);
    }

    /**
     * Updates a piece at the specified index with a transformed version.
     */
    public void updatePiece(int index, PentominoPiece newPiece) {
        if (index >= 0 && index < availablePieces.size()) {
            PentominoPiece oldPiece = availablePieces.get(index);
            // Only update if the piece type matches (same piece, just transformed)
            if (oldPiece.getType() == newPiece.getType()) {
                availablePieces.set(index, newPiece);
            }
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Player getWinner() {
        return winner;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    /**
     * Attempts to make a move by placing a piece on the board.
     */
    public boolean makeMove(PentominoPiece piece, int row, int col) {
        if (status != GameStatus.PLAYING) {
            return false;
        }

        if (!availablePieces.contains(piece)) {
            return false;
        }

        if (!board.placePiece(piece, row, col)) {
            return false;
        }

        // Remove the piece from available pieces
        availablePieces.remove(piece);

        // Record the move
        Move move = new Move(currentPlayer, piece, row, col);
        moveHistory.add(move);

        // Switch to next player
        currentPlayer = (currentPlayer == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;

        // Check for game end
        checkGameEnd();

        return true;
    }

    /**
     * Checks if the current player has any legal moves available.
     */
    public boolean hasLegalMoves() {
        for (PentominoPiece piece : availablePieces) {
            if (board.hasLegalMove(piece)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the game has ended and updates the status accordingly.
     */
    private void checkGameEnd() {
        // Check if current player has no legal moves
        if (!hasLegalMoves()) {
            // Current player cannot move, so the other player wins
            winner = (currentPlayer == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
            status = (winner == Player.PLAYER_1) ? GameStatus.PLAYER_1_WINS : GameStatus.PLAYER_2_WINS;
        } else if (availablePieces.isEmpty()) {
            // All pieces placed - it's a draw
            status = GameStatus.DRAW;
        }
    }

    /**
     * Undoes the last move, restoring the game state.
     */
    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Restore the piece to available pieces
        availablePieces.add(lastMove.getPiece());

        // Remove the piece from the board
        board.removePiece(lastMove.getPiece(), lastMove.getRow(), lastMove.getCol());

        // Switch back to the player who made the move
        currentPlayer = lastMove.getPlayer();

        // Reset game status
        status = GameStatus.PLAYING;
        winner = null;

        return true;
    }

    /**
     * Checks if the current player can make any moves and updates game status accordingly.
     */
    public void checkCurrentPlayerMoves() {
        if (status != GameStatus.PLAYING) {
            return;
        }

        if (!hasLegalMoves()) {
            // Current player cannot move, so the other player wins
            winner = (currentPlayer == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
            status = (winner == Player.PLAYER_1) ? GameStatus.PLAYER_1_WINS : GameStatus.PLAYER_2_WINS;
        }
    }

    /**
     * Makes a computer move using the specified strategy.
     *
     * @param strategy The computer strategy to use for move calculation
     * @return true if a move was made, false if no moves are possible
     */
    public boolean makeComputerMove(ComputerStrategy strategy) {
        if (status != GameStatus.PLAYING) {
            return false;
        }

        ComputerStrategy.ComputerMove move = strategy.calculateMove(this);
        if (move == null) {
            // No possible moves - computer loses
            checkCurrentPlayerMoves();
            return false;
        }

        // Make the move
        if (makeMove(move.getPiece(), move.getRow(), move.getCol())) {
            return true;
        }

        return false;
    }

    /**
     * Resets the game to initial state.
     */
    public void reset() {
        board.clear();
        availablePieces.clear();
        availablePieces.addAll(PentominoPiece.createAllPieces());
        currentPlayer = Player.PLAYER_1;
        status = GameStatus.PLAYING;
        winner = null;
        moveHistory.clear();
    }

    /**
     * Gets the number of moves made so far.
     */
    public int getMoveCount() {
        return moveHistory.size();
    }

    /**
     * Represents a single move in the game.
     */
    public static class Move {
        private final Player player;
        private final PentominoPiece piece;
        private final int row;
        private final int col;

        public Move(Player player, PentominoPiece piece, int row, int col) {
            this.player = player;
            this.piece = piece;
            this.row = row;
            this.col = col;
        }

        public Player getPlayer() {
            return player;
        }

        public PentominoPiece getPiece() {
            return piece;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public String toString() {
            return player + " placed " + piece + " at (" + row + "," + col + ")";
        }
    }
}