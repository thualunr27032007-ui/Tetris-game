
public class Collision {
    public static boolean canMove(Tetris.Piece piece, int newX, int newY, int[][] grid, int gridWidth, int gridHeight) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.getPieceX(i);
            int y = newY + piece.getPieceY(i);

            if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
                return false;
            }

            if (grid[y][x] != 0) {
                return false;
            }
        }

        return true;
    }
}