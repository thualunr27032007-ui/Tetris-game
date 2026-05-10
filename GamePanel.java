import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ─────────────────────────────────────────
 *  PERSON 3 — GamePanel.java
 *
 *  Responsibilities:
 *    • Extends JPanel (Swing drawing surface)
 *    • paintComponent() — draws everything
 *    • 60 FPS game loop via javax.swing.Timer
 *    • KeyListener — maps keys → GameLogic
 *
 *  What it draws:
 *    LEFT  panel  : Score, Lines, Level bar, Hold
 *    CENTER board : Grid, ghost piece, current piece
 *    RIGHT panel  : Next queue (3 ahead), Controls
 *    OVERLAY      : Start / Pause / Game-Over screen
 * ─────────────────────────────────────────
 */
public final class GamePanel extends JPanel implements KeyListener {

    // ── Fonts ────────────────────────────
    private static final Font FONT_BIG   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_MED   = new Font("SansSerif", Font.BOLD,  14);
    private static final Font FONT_SM    = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD,  10);
    private static final Font FONT_OVER  = new Font("SansSerif", Font.BOLD,  42);

    // ── Game logic reference ─────────────
    private static final long serialVersionUID = 1L;
    private transient final GameLogic logic;

    // ── 60 FPS Swing timer ───────────────
    private Timer gameLoop;

    // ── Constructor ──────────────────────
    public GamePanel() {
        logic = new GameLogic();
        super.setPreferredSize(new Dimension(Constants.TOTAL_W, Constants.TOTAL_H));
        super.setBackground(Constants.BG);
        super.setFocusable(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (gameLoop == null) {
            addKeyListener(this);
            gameLoop = new Timer(1000 / 60, e -> {
                logic.update();
                repaint();
            });
            gameLoop.start();
        }
    }

    // ═════════════════════════════════════
    //  paintComponent — master draw method
    // ═════════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Board top-left pixel position
        int bx = Constants.SIDE_W + Constants.PAD;
        int by = Constants.PAD;

        drawBackground(g2);
        drawLeftPanel(g2,  Constants.PAD, by);
        drawBoard(g2, bx, by);
        drawRightPanel(g2, bx + Constants.BOARD_W + Constants.PAD, by);

        // Overlay when not actively playing
        if (!logic.isStarted() || logic.isPaused() || logic.isGameOver()) {
            drawOverlay(g2);
        }
    }

    // ─────────────────────────────────────
    //  Background
    // ─────────────────────────────────────
    private void drawBackground(Graphics2D g2) {
        g2.setColor(Constants.BG);
        g2.fillRect(0, 0, Constants.TOTAL_W, Constants.TOTAL_H);
    }

    // ─────────────────────────────────────
    //  LEFT PANEL: Score / Lines / Level / Hold
    // ─────────────────────────────────────
    private void drawLeftPanel(Graphics2D g2, int x, int y) {
        drawStatBox(g2, x, y,        "SCORE", String.format("%,d", logic.getScore()));
        drawStatBox(g2, x, y + 90,   "LINES", String.valueOf(logic.getLines()));
        drawStatBox(g2, x, y + 180,  "LEVEL", String.valueOf(logic.getLevel()));
        drawLevelBar(g2, x + 8, y + 250, Constants.SIDE_W - 16);
        drawHoldBox(g2, x, y + 290);
    }

    // Stat box: label + large value
    private void drawStatBox(Graphics2D g2, int x, int y, String label, String value) {
        int w = Constants.SIDE_W, h = 80;
        fillRoundBox(g2, x, y, w, h);
        g2.setFont(FONT_LABEL);
        g2.setColor(Constants.TEXT_MUTE);
        drawCentered(g2, label, x, y + 20, w);
        g2.setFont(FONT_BIG);
        g2.setColor(Constants.ACCENT);
        drawCentered(g2, value, x, y + 55, w);
    }

    // Level progress bar
    private void drawLevelBar(Graphics2D g2, int x, int y, int w) {
        int total = logic.getLevel() * Constants.LINES_PER_LEVEL;
        int done  = total - logic.getLinesToNextLevel();
        float pct = Math.max(0f, Math.min(1f, (float) done / total));
        g2.setColor(Constants.BOARD_BG);
        g2.fillRoundRect(x, y, w, 7, 7, 7);
        g2.setColor(Constants.ACCENT);
        g2.fillRoundRect(x, y, (int)(w * pct), 7, 7, 7);
    }

    // Hold box
    private void drawHoldBox(Graphics2D g2, int x, int y) {
        fillRoundBox(g2, x, y, Constants.SIDE_W, 110);
        g2.setFont(FONT_LABEL);
        g2.setColor(Constants.TEXT_MUTE);
        drawCentered(g2, "HOLD", x, y + 18, Constants.SIDE_W);
        Piece held = logic.getHeldPiece();
        if (held != null) {
            drawMiniPiece(g2, held, x + Constants.SIDE_W / 2, y + 70, 20);
        }
    }

    // ─────────────────────────────────────
    //  CENTER BOARD
    // ─────────────────────────────────────
    private void drawBoard(Graphics2D g2, int bx, int by) {
        // Board background
        g2.setColor(Constants.BOARD_BG);
        g2.fillRect(bx, by, Constants.BOARD_W, Constants.BOARD_H);

        // Subtle grid lines
        g2.setColor(Constants.GRID_LINE);
        for (int c = 1; c < Constants.COLS; c++)
            g2.drawLine(bx + c * Constants.CELL, by, bx + c * Constants.CELL, by + Constants.BOARD_H);
        for (int r = 1; r < Constants.ROWS; r++)
            g2.drawLine(bx, by + r * Constants.CELL, bx + Constants.BOARD_W, by + r * Constants.CELL);

        // Draw placed (locked) cells from grid
        Grid grid = logic.getGrid();
        for (int r = 0; r < Constants.ROWS; r++) {
            for (int c = 0; c < Constants.COLS; c++) {
                Color col = grid.getColor(r, c);
                if (col != null) {
                    drawCell(g2, bx + c * Constants.CELL, by + r * Constants.CELL, col);
                }
            }
        }

        // Ghost piece (shows landing position)
        Piece current = logic.getCurrentPiece();
        if (current != null) {
            int ghostY = grid.ghostRow(current);
            for (int[] block : current.getBlocks()) {
                int px = bx + (current.getX() + block[0]) * Constants.CELL;
                int py = by + (ghostY          + block[1]) * Constants.CELL;
                if (ghostY + block[1] >= 0) {
                    drawGhostCell(g2, px, py, current.getColor());
                }
            }

            // Active (falling) piece
            for (int[] block : current.getBlocks()) {
                int px = bx + (current.getX() + block[0]) * Constants.CELL;
                int py = by + (current.getY() + block[1]) * Constants.CELL;
                if (current.getY() + block[1] >= 0) {
                    drawCell(g2, px, py, current.getColor());
                }
            }
        }

        // Board border
        g2.setColor(Constants.BORDER);
        g2.drawRect(bx, by, Constants.BOARD_W, Constants.BOARD_H);
    }

    // ─────────────────────────────────────
    //  RIGHT PANEL: Next queue + Controls
    // ─────────────────────────────────────
    private void drawRightPanel(Graphics2D g2, int x, int y) {
        // Next pieces box
        int nextH = 90 * 3 + 12;
        fillRoundBox(g2, x, y, Constants.SIDE_W, nextH);
        g2.setFont(FONT_LABEL);
        g2.setColor(Constants.TEXT_MUTE);
        drawCentered(g2, "NEXT", x, y + 18, Constants.SIDE_W);

        int ni = 0;
        for (Piece p : logic.getNextQueue()) {
            if (ni >= 3) break;
            drawMiniPiece(g2, p, x + Constants.SIDE_W / 2, y + 48 + ni * 90, 20);
            ni++;
        }

        // Controls reference
        int cy = y + nextH + 16;
        fillRoundBox(g2, x, cy, Constants.SIDE_W, 190);
        g2.setFont(FONT_LABEL);
        g2.setColor(Constants.TEXT_MUTE);
        drawCentered(g2, "CONTROLS", x, cy + 18, Constants.SIDE_W);

        String[][] ctrl = {
            {"← →",   "Move"},
            {"↑",     "Rotate"},
            {"↓",     "Soft drop"},
            {"Space", "Hard drop"},
            {"C",     "Hold"},
            {"P",     "Pause"},
            {"Enter", "Start/Restart"}
        };
        int ky = cy + 34;
        for (String[] kv : ctrl) {
            g2.setFont(FONT_SM);
            g2.setColor(Constants.TEXT_KEY);
            g2.drawString(kv[0], x + 10, ky);
            g2.setColor(Constants.TEXT_MUTE);
            g2.drawString(kv[1], x + 55, ky);
            ky += 22;
        }
    }

    // ─────────────────────────────────────
    //  OVERLAY: start / pause / game-over
    // ─────────────────────────────────────
    private void drawOverlay(Graphics2D g2) {
        // Dim the board behind
        g2.setColor(new Color(10, 10, 26, 210));
        g2.fillRect(0, 0, Constants.TOTAL_W, Constants.TOTAL_H);

        int midW = Constants.TOTAL_W;
        int midY = Constants.TOTAL_H / 2;

        if (logic.isGameOver()) {
            g2.setFont(FONT_OVER);
            g2.setColor(Constants.ACCENT);
            drawCentered(g2, "GAME OVER", 0, midY - 30, midW);

            g2.setFont(FONT_MED);
            g2.setColor(Constants.TEXT_MUTE);
            String stats = "Score: " + logic.getScore()
                         + "   Level: " + logic.getLevel()
                         + "   Lines: " + logic.getLines();
            drawCentered(g2, stats, 0, midY + 10, midW);

        } else if (logic.isPaused()) {
            g2.setFont(FONT_OVER);
            g2.setColor(Constants.ACCENT);
            drawCentered(g2, "PAUSED", 0, midY - 20, midW);

        } else {
            // Not started yet
            g2.setFont(FONT_OVER);
            g2.setColor(Constants.ACCENT);
            drawCentered(g2, "TETRIS", 0, midY - 40, midW);
            g2.setFont(FONT_MED);
            g2.setColor(Constants.TEXT_MUTE);
            drawCentered(g2, "Press ENTER to start", 0, midY, midW);
        }

        g2.setFont(FONT_MED);
        g2.setColor(Constants.TEXT_KEY);
        drawCentered(g2, "[ ENTER ] Start / Restart", 0, midY + 46, midW);
    }

    // ─────────────────────────────────────
    //  Cell renderers
    // ─────────────────────────────────────

    // Solid filled cell with highlight + shadow sheen
    private void drawCell(Graphics2D g2, int px, int py, Color col) {
        int s = Constants.CELL;
        g2.setColor(col);
        g2.fillRect(px + 1, py + 1, s - 2, s - 2);
        // Top highlight
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRect(px + 1, py + 1, s - 2, 5);
        // Bottom shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRect(px + 1, py + s - 6, s - 2, 5);
        // Inner border
        g2.setColor(new Color(0, 0, 0, 40));
        g2.drawRect(px + 1, py + 1, s - 3, s - 3);
    }

    // Transparent ghost cell (outline only)
    private void drawGhostCell(Graphics2D g2, int px, int py, Color col) {
        int s = Constants.CELL;
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 55));
        g2.fillRect(px + 2, py + 2, s - 4, s - 4);
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 110));
        g2.drawRect(px + 2, py + 2, s - 4, s - 4);
    }

    // Small piece for hold / next panels
    private void drawMiniPiece(Graphics2D g2, Piece p, int cx, int cy, int cellSize) {
        int[][] blocks = p.getBlocks();
        // Find bounding box to center the mini-piece
        int maxC = 0, maxR = 0;
        for (int[] b : blocks) { maxC = Math.max(maxC, b[0]); maxR = Math.max(maxR, b[1]); }
        int ox = cx - ((maxC + 1) * cellSize) / 2;
        int oy = cy - ((maxR + 1) * cellSize) / 2;
        for (int[] b : blocks) {
            int px = ox + b[0] * cellSize;
            int py = oy + b[1] * cellSize;
            g2.setColor(p.getColor());
            g2.fillRect(px + 1, py + 1, cellSize - 2, cellSize - 2);
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRect(px + 1, py + 1, cellSize - 2, 3);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRect(px + 1, py + 1, cellSize - 3, cellSize - 3);
        }
    }

    // ─────────────────────────────────────
    //  Panel box helper
    // ─────────────────────────────────────
    private void fillRoundBox(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Constants.SIDE_BG);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
        g2.setColor(Constants.BORDER);
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 12, 12));
    }

    // Horizontal center-align text
    private void drawCentered(Graphics2D g2, String s, int x, int y, int w) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, x + (w - fm.stringWidth(s)) / 2, y);
    }

    // ═════════════════════════════════════
    //  KeyListener — keyboard input
    // ═════════════════════════════════════
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER  -> logic.reset();           // start / restart
            case KeyEvent.VK_LEFT   -> logic.movePieceLeft();
            case KeyEvent.VK_RIGHT  -> logic.movePieceRight();
            case KeyEvent.VK_DOWN   -> logic.softDrop();
            case KeyEvent.VK_UP     -> logic.rotatePiece();
            case KeyEvent.VK_SPACE  -> logic.hardDrop();
            case KeyEvent.VK_C      -> logic.holdPiece();
            case KeyEvent.VK_P      -> logic.togglePause();
        }
        repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
