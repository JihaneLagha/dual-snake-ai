package project;

import java.awt.Point;
import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

class Snake {

    // The snake body is stored as a list of Points (head = first element)
    private final Deque<Point> body = new LinkedList<>();

    // Current movement direction (dx, dy)
    private int dirX = 1, dirY = 0;   // Start moving right

    private boolean grow = false;     // If true → snake will grow on next move
    private Color color;              // Snake color
    private int cols, rows;           // Grid dimensions for wrap-around logic
    

    // Constructor: initialize snake with starting position and color
    public Snake(int startX, int startY, Color color) {
        this.color = color;
        reset(startX, startY);
    }

    // Set grid dimensions (needed for wrapping at edges)
    public void setGridSize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    // Reset snake to starting size (3 blocks)
    public void reset(int startX, int startY) {
        body.clear();

        // Head position
        body.addFirst(new Point(startX, startY));
        // Body trailing behind the head
        body.addLast(new Point(startX - 1, startY));
        body.addLast(new Point(startX - 2, startY));

        // Reset movement and growth
        dirX = 1; 
        dirY = 0; 
        grow = false;
    }

    // Returns snake head (front of deque)
    public Point getHead() {
        return body.peekFirst();
    }

    // Returns an unmodifiable copy of the snake body
    public List<Point> getBody() {
        return List.copyOf(body);
    }

    public Color getColor() {
        return color;
    }

    // Update movement direction (prevents reversing 180 degrees)
    public void setDirection(int dx, int dy) {
        // Can't reverse direction directly
        if (dx == -dirX && dy == -dirY) return;

        // Ignore zero movement
        if (dx == 0 && dy == 0) return;

        dirX = dx;
        dirY = dy;
    }

    /*
     * Move the snake:
     *  - returns true when food is eaten
     *  - returns false for normal move
     */
    public boolean move(Point foodPos, Maze maze) {
        Point head = getHead();

        // Calculate next position
        int nextX = head.x + dirX;
        int nextY = head.y + dirY;

        if (nextX < 0) nextX = cols - 1;
        if (nextX >= cols) nextX = 0;
        if (nextY < 0) nextY = rows - 1;
        if (nextY >= rows) nextY = 0;

        Point next = new Point(nextX, nextY);

        // Check collision with maze walls
        if (maze != null && maze.isWall(next)) {
            // Snake can't enter the wall → movement blocked
            return false;
        }

        // Add new head
        body.addFirst(next);

        // If snake reaches food → grow
        if (foodPos != null && next.equals(foodPos)) {
            grow = true;
            return true;  // return true (food eaten)
        }

        // If not growing → remove tail
        if (!grow) {
            body.removeLast();
        } else {
            grow = false; // Reset growth flag
        }

        return false;
    }

    // Check if snake occupies a given point (used for collision / food placement)
    public boolean occupies(Point p) {
        for (Point b : body) {
            if (b.equals(p)) return true;
        }
        return false;
    }

   
}
