import java.awt.Color;

/**
 * ─────────────────────────────────────────
 *  PERSON 1 — Grid.java
 *  Manages the 10×20 game board.
 *
 *  grid[row][col] stores:
 *    0       → empty cell
 *    1–7     → locked piece color ID
 * ─────────────────────────────────────────
 */
public class Grid {

    // ── Internal board ───────────────────
    private int[][] grid;

    public Grid() {
        grid = new int[Constants.ROWS][Constants.COLS];
    }

    // ─────────────────────────────────────
    //  canPlace(piece, px, py)
    //  Returns true if the piece fits at
    //  position (px, py) without overlap
    //  or going out of bounds.
    // ─────────────────────────────────────
    public boolean canPlace(Piece piece, int px, int py) {
        int[][] blocks = piece.getBlocks();
        for (int[] block : blocks) {
            int c = px + block[0];   // column
            int r = py + block[1];   // row
            if (c < 0 || c >= Constants.COLS) return false;   // wall
            if (r >= Constants.ROWS)           return false;   // floor
            if (r >= 0 && grid[r][c] != Constants.EMPTY) return false; // locked cell
        }
        return true;
    }

    // ─────────────────────────────────────
    //  placePiece(piece)
    //  Permanently stamp the piece's color
    //  ID onto the grid (called on lock).
    // ─────────────────────────────────────
    public void placePiece(Piece piece) {
        int[][] blocks = piece.getBlocks();
        int colorId    = piece.getColorId();
        for (int[] block : blocks) {
            int c = piece.getX() + block[0];
            int r = piece.getY() + block[1];
            if (r >= 0 && r < Constants.ROWS && c >= 0 && c < Constants.COLS) {
                grid[r][c] = colorId;
            }
        }
    }

    // ─────────────────────────────────────
    //  clearLines()
    //  Scans bottom-up; removes any full
    //  row and shifts everything down.
    //  Returns number of lines cleared.
    // ─────────────────────────────────────
    public int clearLines() {
        int cleared = 0;
        for (int r = Constants.ROWS - 1; r >= 0; ) {
            if (isRowFull(r)) {
                removeRow(r);   // shifts rows above down
                cleared++;
                // re-check same index (rows shifted down)
            } else {
                r--;
            }
        }
        return cleared;
    }

    // ── Check if a single row is full ────
    private boolean isRowFull(int row) {
        for (int c = 0; c < Constants.COLS; c++) {
            if (grid[row][c] == Constants.EMPTY) return false;
        }
        return true;
    }

    // ── Remove one row and shift above rows down ──
    private void removeRow(int row) {
        // Shift every row above down by one
        for (int r = row; r > 0; r--) {
            grid[r] = grid[r - 1].clone();
        }
        // Top row becomes empty
        grid[0] = new int[Constants.COLS];
    }

    // ─────────────────────────────────────
    //  getCell(row, col)
    //  Returns color ID at (row, col),
    //  or 0 if empty.
    // ─────────────────────────────────────
    public int getCell(int row, int col) {
        if (row < 0 || row >= Constants.ROWS) return Constants.EMPTY;
        if (col < 0 || col >= Constants.COLS) return Constants.EMPTY;
        return grid[row][col];
    }

    // ─────────────────────────────────────
    //  getColor(row, col)
    //  Convenience: returns the Color object
    //  for the piece locked at (row, col),
    //  or null if empty.
    // ─────────────────────────────────────
    public Color getColor(int row, int col) {
        int id = getCell(row, col);
        if (id == Constants.EMPTY) return null;
        return Constants.PIECE_COLORS[id];
    }

    // ─────────────────────────────────────
    //  ghostRow(piece)
    //  Returns the lowest row the piece
    //  can reach (used for ghost drawing).
    // ─────────────────────────────────────
    public int ghostRow(Piece piece) {
        int gy = piece.getY();
        while (canPlace(piece, piece.getX(), gy + 1)) gy++;
        return gy;
    }

    // ─────────────────────────────────────
    //  reset()
    //  Clears the entire board (new game).
    // ─────────────────────────────────────
    public void reset() {
        grid = new int[Constants.ROWS][Constants.COLS];
    }
}
