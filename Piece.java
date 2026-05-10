/**
 * ─────────────────────────────────────────
 *  PERSON 2 — Piece.java
 *
 *  All 7 Tetris pieces (I, O, T, S, Z, J, L)
 *  Each piece stores:
 *    - colorId  → links to Constants.PIECE_COLORS
 *    - matrix   → current rotation state (2D int[][])
 *    - x, y     → position on the grid
 *
 *  Methods:
 *    getBlocks()   → returns array of [col, row] offsets
 *    moveLeft/Right/Down/Up()
 *    rotate()      → 90° clockwise
 *    rotateBack()  → undo rotation (for wall-kick fail)
 * ─────────────────────────────────────────
 */
public class Piece {

    // ── Piece data ───────────────────────
    private int   colorId;
    private int[][] matrix;   // current rotation
    private int   x, y;      // grid position

    // ─────────────────────────────────────
    //  Static shape definitions
    //  Each piece is defined as a 2D int[][]
    //  1 = filled cell, 0 = empty
    // ─────────────────────────────────────
    private static final int[][] SHAPE_I = {
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 0, 0, 0},
        {0, 0, 0, 0}
    };
    private static final int[][] SHAPE_O = {
        {1, 1},
        {1, 1}
    };
    private static final int[][] SHAPE_T = {
        {0, 1, 0},
        {1, 1, 1},
        {0, 0, 0}
    };
    private static final int[][] SHAPE_S = {
        {0, 1, 1},
        {1, 1, 0},
        {0, 0, 0}
    };
    private static final int[][] SHAPE_Z = {
        {1, 1, 0},
        {0, 1, 1},
        {0, 0, 0}
    };
    private static final int[][] SHAPE_J = {
        {1, 0, 0},
        {1, 1, 1},
        {0, 0, 0}
    };
    private static final int[][] SHAPE_L = {
        {0, 0, 1},
        {1, 1, 1},
        {0, 0, 0}
    };

    // Map colorId → shape template
    private static int[][] shapeFor(int id) {
        return switch (id) {
            case Constants.ID_I -> copyMatrix(SHAPE_I);
            case Constants.ID_O -> copyMatrix(SHAPE_O);
            case Constants.ID_T -> copyMatrix(SHAPE_T);
            case Constants.ID_S -> copyMatrix(SHAPE_S);
            case Constants.ID_Z -> copyMatrix(SHAPE_Z);
            case Constants.ID_J -> copyMatrix(SHAPE_J);
            case Constants.ID_L -> copyMatrix(SHAPE_L);
            default -> throw new IllegalArgumentException("Unknown piece id: " + id);
        };
    }

    // ─────────────────────────────────────
    //  Constructor (by colorId 1–7)
    // ─────────────────────────────────────
    public Piece(int colorId) {
        this.colorId = colorId;
        this.matrix  = shapeFor(colorId);
        this.x = 0;
        this.y = 0;
    }

    // ─────────────────────────────────────
    //  getBlocks()
    //  Returns every filled cell as a
    //  [col-offset, row-offset] pair,
    //  relative to (x, y).
    //  GameLogic/Grid use this to check
    //  collision and draw the piece.
    // ─────────────────────────────────────
    public int[][] getBlocks() {
        java.util.List<int[]> blocks = new java.util.ArrayList<>();
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                if (matrix[r][c] != 0) {
                    blocks.add(new int[]{c, r});  // [col-offset, row-offset]
                }
            }
        }
        return blocks.toArray(new int[0][]);
    }

    // ─────────────────────────────────────
    //  Movement — shift position by 1
    // ─────────────────────────────────────
    public void moveLeft()  { x--; }
    public void moveRight() { x++; }
    public void moveDown()  { y++; }
    public void moveUp()    { y--; }   // used by some wall-kick strategies

    // ─────────────────────────────────────
    //  rotate() — 90° clockwise in-place
    //
    //  Algorithm: transpose then reverse rows
    //    1. Transpose: matrix[i][j] ↔ matrix[j][i]
    //    2. Reverse each row (horizontal flip)
    // ─────────────────────────────────────
    public void rotate() {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = matrix[r][c];
            }
        }
        matrix = rotated;
    }

    // ─────────────────────────────────────
    //  rotateBack() — 90° counter-clockwise
    //  Called when rotation causes a
    //  collision that cannot be wall-kicked.
    // ─────────────────────────────────────
    public void rotateBack() {
        // Three clockwise rotations = one counter-clockwise
        rotate(); rotate(); rotate();
    }

    // ─────────────────────────────────────
    //  Dimension helpers (used by GameLogic)
    // ─────────────────────────────────────
    public int width()  { return matrix[0].length; }
    public int height() { return matrix.length; }

    // ─────────────────────────────────────
    //  Getters / Setters
    // ─────────────────────────────────────
    public int getColorId() { return colorId; }
    public int getX()       { return x; }
    public int getY()       { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public java.awt.Color getColor() {
        return Constants.PIECE_COLORS[colorId];
    }

    // ─────────────────────────────────────
    //  Matrix copy helper (static utility)
    // ─────────────────────────────────────
    private static int[][] copyMatrix(int[][] m) {
        int[][] copy = new int[m.length][];
        for (int i = 0; i < m.length; i++) copy[i] = m[i].clone();
        return copy;
    }
}
