import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * ─────────────────────────────────────────
 *  GameLogic — State Machine
 * ─────────────────────────────────────────
 */
public class GameLogic {

    // ── Core state ───────────────────────
    private Grid  grid;
    private Piece currentPiece;
    private Piece heldPiece;
    private boolean canHold;

    private int score;
    private int lines;
    private int level;
    private int linesToNextLevel;

    private boolean gameOver;
    private boolean paused;
    private boolean started;

    // Next-piece queue (shows 3 ahead)
    private Deque<Piece> nextQueue;

    // 7-bag randomiser state
    private List<Integer> bag;
    private int bagIndex;
    private Random rng = new Random();

    // Drop timer
    private long lastFallTime;
    private int  dropInterval;   // ms between auto-drops

    // ── Constructor ──────────────────────
    public GameLogic() {
        grid      = new Grid();
        nextQueue = new ArrayDeque<>();
        bag       = new ArrayList<>();
        reset();
    }

    // ─────────────────────────────────────
    //  reset() — fresh game
    // ─────────────────────────────────────
    public final void reset() {
        grid.reset();
        nextQueue.clear();
        bag.clear();
        bagIndex = 0;

        heldPiece  = null;
        canHold    = true;
        score      = 0;
        lines      = 0;
        level      = 1;
        linesToNextLevel = Constants.LINES_PER_LEVEL;
        dropInterval     = Constants.SPEEDS[0];
        gameOver   = false;
        paused     = false;
        started    = true;

        fillQueue();
        spawnNext();
        lastFallTime = System.currentTimeMillis();
    }

    // ─────────────────────────────────────
    //  update() — called every frame
    //  Handles gravity (auto-fall).
    // ─────────────────────────────────────
    public void update() {
        if (!started || gameOver || paused) return;

        long now = System.currentTimeMillis();
        if (now - lastFallTime >= dropInterval) {
            lastFallTime = now;
            applyGravity();
        }
    }

    // Fall one row; lock if blocked
    private void applyGravity() {
        if (grid.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.moveDown();
        } else {
            lockAndSpawn();
        }
    }

    // ─────────────────────────────────────
    //  Player input handlers
    // ─────────────────────────────────────
    public void movePieceLeft() {
        if (!active()) return;
        if (grid.canPlace(currentPiece, currentPiece.getX() - 1, currentPiece.getY()))
            currentPiece.moveLeft();
    }

    public void movePieceRight() {
        if (!active()) return;
        if (grid.canPlace(currentPiece, currentPiece.getX() + 1, currentPiece.getY()))
            currentPiece.moveRight();
    }

    public void softDrop() {
        if (!active()) return;
        if (grid.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.moveDown();
            score++;
        } else {
            lockAndSpawn();
        }
        lastFallTime = System.currentTimeMillis();
    }

    public void hardDrop() {
        if (!active()) return;
        int ghostY = grid.ghostRow(currentPiece);
        score += (ghostY - currentPiece.getY()) * 2;
        currentPiece.setY(ghostY);
        lockAndSpawn();
        lastFallTime = System.currentTimeMillis();
    }

    public void rotatePiece() {
        if (!active()) return;
        currentPiece.rotate();
        int[] kicks = {0, -1, 1, -2, 2};
        boolean kicked = false;
        for (int k : kicks) {
            if (grid.canPlace(currentPiece, currentPiece.getX() + k, currentPiece.getY())) {
                currentPiece.setX(currentPiece.getX() + k);
                kicked = true;
                break;
            }
        }
        if (!kicked) currentPiece.rotateBack();
    }

    public void holdPiece() {
        if (!active() || !canHold) return;
        canHold = false;
        if (heldPiece == null) {
            heldPiece = new Piece(currentPiece.getColorId());
            spawnNext();
        } else {
            Piece tmp  = new Piece(heldPiece.getColorId());
            heldPiece  = new Piece(currentPiece.getColorId());
            currentPiece = tmp;
            resetPiecePosition(currentPiece);
        }
    }

    public void togglePause() {
        if (!started || gameOver) return;
        paused = !paused;
        if (!paused) lastFallTime = System.currentTimeMillis();
    }

    // ─────────────────────────────────────
    //  Lock piece and spawn next
    // ─────────────────────────────────────
    private void lockAndSpawn() {
        grid.placePiece(currentPiece);
        int cleared = grid.clearLines();
        if (cleared > 0) {
            score += Constants.SCORE_TABLE[cleared] * level;
            lines += cleared;
            linesToNextLevel -= cleared;
            if (linesToNextLevel <= 0) {
                level++;
                linesToNextLevel = level * Constants.LINES_PER_LEVEL;
                int spd = Constants.SPEEDS[Math.min(level - 1, Constants.SPEEDS.length - 1)];
                dropInterval = spd;
            }
        }
        canHold = true;
        spawnNext();
    }

    private void spawnNext() {
        currentPiece = nextQueue.removeFirst();
        fillQueue();
        resetPiecePosition(currentPiece);
        if (!grid.canPlace(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            gameOver = true;
        }
    }

    private void resetPiecePosition(Piece p) {
        p.setX((Constants.COLS - p.width()) / 2);
        p.setY(0);
    }

    // ─────────────────────────────────────
    //  7-bag piece queue
    // ─────────────────────────────────────
    private void fillQueue() {
        while (nextQueue.size() < 4) nextQueue.addLast(nextBagPiece());
    }

    private Piece nextBagPiece() {
        if (bagIndex >= bag.size()) {
            bag.clear();
            for (int id = 1; id <= 7; id++) bag.add(id);
            Collections.shuffle(bag, rng);
            bagIndex = 0;
        }
        return new Piece(bag.get(bagIndex++));
    }

    // ─────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────
    private boolean active() { return started && !gameOver && !paused; }

    // ─────────────────────────────────────
    //  Getters (used by GamePanel)
    // ─────────────────────────────────────
    public Grid    getGrid()         { return grid; }
    public Piece   getCurrentPiece() { return currentPiece; }
    public Piece   getHeldPiece()    { return heldPiece; }
    public Deque<Piece> getNextQueue() { return nextQueue; }

    public int  getScore()  { return score; }
    public int  getLines()  { return lines; }
    public int  getLevel()  { return level; }
    public int  getLinesToNextLevel() { return linesToNextLevel; }

    public boolean isGameOver() { return gameOver; }
    public boolean isPaused()   { return paused; }
    public boolean isStarted()  { return started; }
}
