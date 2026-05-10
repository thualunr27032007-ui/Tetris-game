import java.awt.Color;

/**
 * ─────────────────────────────────────────
 *  PERSON 1 — Constants.java
 *  All game-wide constants: grid, window,
 *  speeds, colors, and scoring.
 * ─────────────────────────────────────────
 */
public class Constants {

    // ── Grid dimensions ──────────────────
    public static final int COLS     = 10;
    public static final int ROWS     = 20;
    public static final int CELL     = 30;   // pixels per cell

    // ── Window / layout ──────────────────
    public static final int BOARD_W  = COLS * CELL;          // 300
    public static final int BOARD_H  = ROWS * CELL;          // 600
    public static final int SIDE_W   = 140;                  // side panel width
    public static final int PAD      = 12;
    public static final int TOTAL_W  = SIDE_W + PAD + BOARD_W + PAD + SIDE_W; // 604
    public static final int TOTAL_H  = BOARD_H + PAD * 2;   // 624

    // ── Drop speed per level (ms / row) ──
    // Index 0 = Level 1, index 14 = Level 15
    public static final int[] SPEEDS = {
        800, 720, 640, 560, 480,
        400, 340, 280, 220, 160,
        120,  80,  60,  50,  40
    };

    // ── Scoring: lines cleared × level ───
    // Index = number of lines cleared at once
    public static final int[] SCORE_TABLE = { 0, 100, 300, 500, 800 };

    // Lines needed to level up = level × LINES_PER_LEVEL
    public static final int LINES_PER_LEVEL = 5;

    // ── Piece color IDs (1–7, 0 = empty) ─
    public static final int EMPTY = 0;
    public static final int ID_I  = 1;
    public static final int ID_O  = 2;
    public static final int ID_T  = 3;
    public static final int ID_S  = 4;
    public static final int ID_Z  = 5;
    public static final int ID_J  = 6;
    public static final int ID_L  = 7;

    // ── Piece colors indexed by ID ────────
    // Index 0 is unused (empty); 1–7 match ID constants above
    public static final Color[] PIECE_COLORS = {
        null,                           // 0 – empty
        new Color(  0, 212, 255),       // 1 – I  cyan
        new Color(255, 215,   0),       // 2 – O  yellow
        new Color(170,  68, 255),       // 3 – T  purple
        new Color( 68, 255, 136),       // 4 – S  green
        new Color(255,  68,  68),       // 5 – Z  red
        new Color( 68, 136, 255),       // 6 – J  blue
        new Color(255, 136,  68)        // 7 – L  orange
    };

    // ── UI palette ────────────────────────
    public static final Color BG        = new Color( 26,  26,  46);
    public static final Color BOARD_BG  = new Color( 13,  13,  26);
    public static final Color SIDE_BG   = new Color( 22,  33,  62);
    public static final Color BORDER    = new Color( 15,  52,  96);
    public static final Color ACCENT    = new Color(233,  69,  96);
    public static final Color TEXT_MAIN = Color.WHITE;
    public static final Color TEXT_MUTE = new Color(136, 146, 176);
    public static final Color TEXT_KEY  = new Color(204, 214, 246);
    public static final Color GRID_LINE = new Color(255, 255, 255, 12);
}
