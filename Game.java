import javax.swing.*;

/**
 * ─────────────────────────────────────────
 *  PERSON 1 — Game.java
 *
 *  Contains two classes:
 *    1. Game        — entry point (main)
 *    2. GameLogic   — the "brain":
 *       holds grid, current piece, score,
 *       level, and drives all state updates.
 * ─────────────────────────────────────────
 */

// ═════════════════════════════════════════
//  Game — Entry Point
// ═════════════════════════════════════════
public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TETRIS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
