package project;

import java.awt.Point;

public class Food {

    // Grid dimensions
    private final int cols, rows;

    // Current position of the food
    private Point pos;

    // Flag: true if food currently exists on the board
    private boolean placed = false;

    
    // Constructor: stores grid size and starts with no food placed
    public Food(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.pos = null;
        this.placed = false;
    }

    
    /*
     * Places food at (x, y) if:
     *   - within grid
     *   - not on a snake body
     *   - not on a maze wall
     * Returns true if placement was successful.
     */
    public boolean place(int x, int y, Snake snake1, Snake snake2, Maze maze) {

        // Check boundaries
        if (x < 0 || x >= cols || y < 0 || y >= rows) return false;

        Point p = new Point(x, y);

        // Food cannot be placed on either snake
        if (snake1.occupies(p) || snake2.occupies(p)) return false;

        // Food cannot be placed on a maze wall
        if (maze != null && maze.isWall(p)) return false;

        // Valid location → place food
        pos = p;
        placed = true;
        return true;
    }
   
    // Remove food from the board
    public void clear() {
        pos = null;
        placed = false;
    }

    
    // Check if food currently exists
    public boolean isPlaced() {
        return placed;
    }

    
    // Get current food position
    public Point getPosition() {
        return pos;
    }
}
