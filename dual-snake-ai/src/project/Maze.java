package project;

import java.awt.*;
import java.util.*;

public class Maze {
    private final int cols, rows; // Number of columns and rows in the grid
    private final boolean[][] walls; // 2D array to mark which cells are walls
    private final Random random = new Random(); // Random generator for random walls

    public Maze(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.walls = new boolean[cols][rows]; // Initialize all cells as empty (false)
    }

    // -------------------------------
    // RANDOM WALL GENERATION
    // -------------------------------
    public int generateRandomWalls(int wallCount) {
        clearWalls(); // Remove old walls
        int placed = 0;
        int attempts = 0;

        // Try placing walls until we reach the required count
        // Increased attempt limit to ensure better success rate
        while (placed < wallCount && attempts < wallCount * 10) {
            int x = random.nextInt(cols);
            int y = random.nextInt(rows);

            // Skip edges to keep the maze playable
            if (x > 2 && x < cols - 3 && y > 2 && y < rows - 3) {
                // If cell not already a wall, place a wall
                if (!walls[x][y]) {
                    walls[x][y] = true;
                    placed++;
                }
            }
            attempts++;
        }
        
        return placed; // Return actual number of walls placed
    }

    // -------------------------------
    // CHOOSE A PRE-DEFINED WALL PATTERN
    // -------------------------------
    public void generatePattern(String pattern) {
        clearWalls(); // Start fresh

        switch (pattern.toLowerCase()) {
            case "vertical_lines" -> generateVerticalLines();
            case "horizontal_lines" -> generateHorizontalLines();
            case "grid" -> generateGridPattern();
            case "spiral" -> generateSpiral();
            case "random" -> generateRandomWalls(50);
            case "cross" -> generateCross();
            case "rooms" -> generateRooms();
            default -> generateRandomWalls(30); // Fallback pattern
        }
    }

    // -------------------------------
    // VERTICAL LINE PATTERN
    // -------------------------------
    private void generateVerticalLines() {
        for (int x = 5; x < cols; x += 7) { // Vertical lines spaced apart
            for (int y = 0; y < rows; y++) {
                if (y % 5 != 2) { // Leave some gaps
                    walls[x][y] = true;
                }
            }
        }
    }

    // -------------------------------
    // HORIZONTAL LINE PATTERN
    // -------------------------------
    private void generateHorizontalLines() {
        for (int y = 5; y < rows; y += 7) { // Horizontal lines spaced apart
            for (int x = 0; x < cols; x++) {
                if (x % 5 != 2) {
                    walls[x][y] = true;
                }
            }
        }
    }

    // -------------------------------
    // GRID PATTERN (vertical + horizontal)
    // -------------------------------
    private void generateGridPattern() {
        // Vertical sections
        for (int x = 4; x < cols; x += 6) {
            for (int y = 0; y < rows; y++) {
                if (y % 4 != 2) {
                    walls[x][y] = true;
                }
            }
        }

        // Horizontal sections
        for (int y = 4; y < rows; y += 6) {
            for (int x = 0; x < cols; x++) {
                if (x % 4 != 2) {
                    walls[x][y] = true;
                }
            }
        }
    }

    // -------------------------------
    // SPIRAL PATTERN
    // -------------------------------
    private void generateSpiral() {
        int centerX = cols / 2;
        int centerY = rows / 2;

        // Draw rings around the center
        for (int r = 3; r < Math.min(cols, rows) / 2; r += 4) {
            for (int angle = 0; angle < 360; angle += 15) {
                double rad = Math.toRadians(angle);
                int x = centerX + (int)(r * Math.cos(rad));
                int y = centerY + (int)(r * Math.sin(rad));

                if (x >= 0 && x < cols && y >= 0 && y < rows) {
                    // Create broken spiral (not full circle)
                    if (angle % 90 < 60) {
                        walls[x][y] = true;
                    }
                }
            }
        }
    }

    // -------------------------------
    // CROSS SHAPE PATTERN
    // -------------------------------
    private void generateCross() {
        int centerX = cols / 2;

        // Vertical bar
        for (int y = 5; y < rows - 5; y++) {
            if (y % 5 != 2) {
                walls[centerX][y] = true;
                walls[centerX + 1][y] = true;
            }
        }

        int centerY = rows / 2;

        // Horizontal bar
        for (int x = 5; x < cols - 5; x++) {
            if (x % 5 != 2) {
                walls[x][centerY] = true;
                walls[x][centerY + 1] = true;
            }
        }
    }

    // -------------------------------
    // ROOMS PATTERN (3 boxed rooms with openings)
    // -------------------------------
    private void generateRooms() {
        // —— Room 1: Top-left ——
        for (int x = 3; x <= 12; x++) {
            walls[x][8] = true;
            walls[x][12] = true;
        }
        for (int y = 8; y <= 12; y++) {
            if (y != 10) { // Create a door
                walls[3][y] = true;
                walls[12][y] = true;
            }
        }

        // —— Room 2: Top-right ——
        for (int x = cols - 13; x <= cols - 4; x++) {
            walls[x][8] = true;
            walls[x][12] = true;
        }
        for (int y = 8; y <= 12; y++) {
            if (y != 10) { // Create a door
                walls[cols - 13][y] = true;
                walls[cols - 4][y] = true;
            }
        }

        // —— Room 3: Bottom-center ——
        for (int x = cols / 2 - 5; x <= cols / 2 + 5; x++) {
            walls[x][rows - 12] = true;
            walls[x][rows - 8] = true;
        }
        for (int y = rows - 12; y <= rows - 8; y++) {
            if (y != rows - 10) { // Create a door
                walls[cols / 2 - 5][y] = true;
                walls[cols / 2 + 5][y] = true;
            }
        }
    }

    // -------------------------------
    // CLEAR ALL WALLS
    // -------------------------------
    public void clearWalls() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                walls[x][y] = false;
            }
        }
    }

    // -------------------------------
    // CHECK IF CELL IS WALL
    // -------------------------------
    public boolean isWall(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return false; // Out of bounds = not a wall
        }
        return walls[x][y];
    }

    public boolean isWall(Point p) {
        return isWall(p.x, p.y);
    }

    // -------------------------------
    // RENDER WALLS ON THE SCREEN
    // -------------------------------
    public void render(Graphics g, int cellSize) {
        g.setColor(new Color(80, 80, 80)); // Dark wall color

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (walls[x][y]) {
                    // Draw filled square
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                    // Draw border on top of it
                    g.setColor(new Color(100, 100, 100));
                    g.drawRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);

                    // Reset color
                    g.setColor(new Color(80, 80, 80));
                }
            }
        }
    }

    
    // -------------------------------
    // GETTERS
    // -------------------------------
    public int getCols() {
        return cols;
    }
    
    public int getRows() {
        return rows;
    }
}