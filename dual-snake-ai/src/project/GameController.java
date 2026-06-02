package project;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameController {
    private final GameBoard board;
    private final Snake snake1;
    private final Snake snake2;
    private final Food food;
    private final AIPathfinder ai;
    private final Maze maze;
    private Timer timer;
    private boolean running = false;
    private boolean paused = false;
    private int score1 = 0;
    private int score2 = 0;
    private int speed = 5;
    private boolean snake1Dead = false;
    private boolean snake2Dead = false;
    private boolean showPaths = true;
    private List<Point> path1 = null;
    private List<Point> path2 = null;

    public GameController(GameBoard board) {
        this.board = board;
        this.snake1 = new Snake(board.cols / 3, board.rows / 2, Color.GREEN);
        this.snake2 = new Snake(2 * board.cols / 3, board.rows / 2, Color.BLUE);
        this.snake1.setGridSize(board.cols, board.rows);
        this.snake2.setGridSize(board.cols, board.rows);
        this.food = new Food(board.cols, board.rows);
        this.ai = new AIPathfinder(board.cols, board.rows);
        this.maze = new Maze(board.cols, board.rows);
    }

    public void start() {
        running = true;
        paused = false;
        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        int delay = getDelayMillisFromSpeed(speed);
        timer = new Timer(delay, e -> update());
        timer.start();
    }

    public void restart() {
        if (timer != null) timer.stop();
        snake1.reset(board.cols / 3, board.rows / 2);
        snake2.reset(2 * board.cols / 3, board.rows / 2);
        food.clear();
        score1 = 0;
        score2 = 0;
        snake1Dead = false;
        snake2Dead = false;
        running = true;
        paused = false;
        path1 = null;
        path2 = null;
        startTimer();
        board.repaint();
    }

    public boolean togglePause() {
        paused = !paused;
        return paused;
    }

    private int getDelayMillisFromSpeed(int speed) {
        if (speed <= 8) {
            return 1000 / speed;
        } else {
            return Math.max(125 - (speed - 8) * 10, 80);
        }
    }

    public void setSpeed(int newSpeed) {
        if (newSpeed < 1) newSpeed = 1;
        if (newSpeed > 12) newSpeed = 12;
        this.speed = newSpeed;
        if (timer != null) {
            timer.setDelay(getDelayMillisFromSpeed(speed));
        }
    }

    public void placeFood(int x, int y) {
        food.place(x, y, snake1, snake2, maze);
        board.repaint();
    }
    
    public void changeMazePattern(String pattern) {
        maze.generatePattern(pattern);
        board.repaint();
    }
    
    public void clearMaze() {
        maze.clearWalls();
        board.repaint();
    }

    private void update() {
        if (!running || paused) return;

        if (food.isPlaced()) {
            if (!snake1Dead) {
                Point head1 = snake1.getHead();
                path1 = ai.findPathAStar(head1, food.getPosition(), snake1, snake2, maze);
                if (path1 != null && path1.size() > 1) {
                    Point next1 = path1.get(1);
                    int dx1 = next1.x - head1.x;
                    int dy1 = next1.y - head1.y;
                    snake1.setDirection(dx1, dy1);
                } else {
                    Point fallback1 = ai.safeMove(head1, snake1, snake2, maze);
                    if (fallback1 != null) {
                        snake1.setDirection(fallback1.x - head1.x, fallback1.y - head1.y);
                    }
                    path1 = null;
                }
            }

            if (!snake2Dead) {
                Point head2 = snake2.getHead();
                path2 = ai.findPathGreedy(head2, food.getPosition(), snake1, snake2, maze);
                if (path2 != null && path2.size() > 1) {
                    Point next2 = path2.get(1);
                    int dx2 = next2.x - head2.x;
                    int dy2 = next2.y - head2.y;
                    snake2.setDirection(dx2, dy2);
                } else {
                    Point fallback2 = ai.safeMove(head2, snake1, snake2, maze);
                    if (fallback2 != null) {
                        snake2.setDirection(fallback2.x - head2.x, fallback2.y - head2.y);
                    }
                    path2 = null;
                }
            }
        }

        boolean ate1 = false;
        boolean ate2 = false;
        
        if (!snake1Dead) {
            ate1 = snake1.move(food.getPosition(), maze);
        }
        if (!snake2Dead) {
            ate2 = snake2.move(food.getPosition(), maze);
        }

        if (ate1) {
            score1++;
            food.clear();
            path1 = null;
        }
        if (ate2) {
            score2++;
            food.clear();
            path2 = null;
        }

        checkCollisions();

        if (snake1Dead && snake2Dead) {
            running = false;
            if (timer != null) timer.stop();
            String winner = score1 > score2 ? "Green Snake (A*) wins!" : 
                           score2 > score1 ? "Blue Snake (Greedy) wins!" : "It's a tie!";
            JOptionPane.showMessageDialog(board, 
                "Game Over!\n" + winner + 
                "\nGreen (A*) Score: " + score1 + 
                "\nBlue (Greedy) Score: " + score2);
        }

        board.repaint();
    }

    private void checkCollisions() {
        Point head1 = null;
        Point head2 = null;
        
        if (!snake1Dead) head1 = snake1.getHead();
        if (!snake2Dead) head2 = snake2.getHead();
        
        if (!snake1Dead && !snake2Dead && head1.equals(head2)) {
            if (score1 > score2) {
                snake2Dead = true;
            } else if (score2 > score1) {
                snake1Dead = true;
            } else {
                snake1Dead = true;
                snake2Dead = true;
            }
            return;
        }
        
        if (!snake1Dead && !snake2Dead) {
            for (Point bodyPart : snake2.getBody()) {
                if (head1.equals(bodyPart)) {
                    if (score1 > score2) {
                        snake2Dead = true;
                    } else {
                        snake1Dead = true;
                    }
                    return;
                }
            }
        }
        
        if (!snake1Dead && !snake2Dead) {
            for (Point bodyPart : snake1.getBody()) {
                if (head2.equals(bodyPart)) {
                    if (score2 > score1) {
                        snake1Dead = true;
                    } else {
                        snake2Dead = true;
                    }
                    return;
                }
            }
        }
        
        if (!snake1Dead) {
            int i = 0;
            for (Point bodyPart : snake1.getBody()) {
                if (i > 0 && head1.equals(bodyPart)) {
                    snake1Dead = true;
                    return;
                }
                i++;
            }
        }

        if (!snake2Dead) {
            int i = 0;
            for (Point bodyPart : snake2.getBody()) {
                if (i > 0 && head2.equals(bodyPart)) {
                    snake2Dead = true;
                    return;
                }
                i++;
            }
        }
    }

    public void render(Graphics g) {
        // Draw grid
        for (int x = 0; x < board.cols; x++) {
            for (int y = 0; y < board.rows; y++) {
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x * board.cellSize, y * board.cellSize, board.cellSize, board.cellSize);
            }
        }

        // Draw maze walls
        if (maze != null) {
            maze.render(g, board.cellSize);
        }

        // Draw pathfinding visualization
        if (showPaths) {
            if (path1 != null && !snake1Dead) {
                g.setColor(new Color(0, 255, 0, 50));
                for (int i = 1; i < path1.size(); i++) {
                    Point p = path1.get(i);
                    g.fillRect(p.x * board.cellSize + 4, p.y * board.cellSize + 4,
                              board.cellSize - 8, board.cellSize - 8);
                }
                g.setColor(Color.GREEN);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("A* Path: " + (path1.size() - 1), 10, 15);
            }
            
            if (path2 != null && !snake2Dead) {
                g.setColor(new Color(0, 0, 255, 50));
                for (int i = 1; i < path2.size(); i++) {
                    Point p = path2.get(i);
                    g.fillRect(p.x * board.cellSize + 4, p.y * board.cellSize + 4,
                              board.cellSize - 8, board.cellSize - 8);
                }
                g.setColor(Color.BLUE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("Greedy Path: " + (path2.size() - 1), 150, 15);
            }
        }

        // Draw food
        if (food.isPlaced()) {
            Point f = food.getPosition();
            g.setColor(Color.RED);
            g.fillOval(f.x * board.cellSize + 2, f.y * board.cellSize + 2,
                       board.cellSize - 4, board.cellSize - 4);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            String msg = "Click on grid to place food";
            g.drawString(msg, board.cols * board.cellSize / 2 - 80, board.rows * board.cellSize / 2);
        }

        // Draw snakes
        if (!snake1Dead) {
            drawSnake(g, snake1);
        }
        if (!snake2Dead) {
            drawSnake(g, snake2);
        }

        // Bottom info panel
        g.setColor(Color.WHITE);
        g.fillRect(0, board.rows * board.cellSize, board.cols * board.cellSize, 100);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        
        int baseY = board.rows * board.cellSize;
        
        String status1 = snake1Dead ? " (DEAD)" : "";
        String status2 = snake2Dead ? " (DEAD)" : "";
        
        g.drawString("Green Snake (A*): " + score1 + status1, 10, baseY + 20);
        g.drawString("Blue Snake (Greedy): " + score2 + status2, 10, baseY + 35);
        g.drawString("Speed: " + speed, 10, baseY + 50);

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("PAUSED", 10, baseY + 70);
        }
        
        if ((snake1Dead || snake2Dead) && !(snake1Dead && snake2Dead)) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.RED);
            String survivor = snake1Dead ? "Blue Snake Survives!" : "Green Snake Survives!";
            g.drawString(survivor, 250, baseY + 50);
        }
    }

    private void drawSnake(Graphics g, Snake snake) {
        int i = 0;
        for (Point p : snake.getBody()) {
            if (i == 0) {
                g.setColor(snake.getColor().brighter());
            } else {
                g.setColor(snake.getColor());
            }
            g.fillRect(p.x * board.cellSize + 1, p.y * board.cellSize + 1,
                       board.cellSize - 2, board.cellSize - 2);
            i++;
        }
    }
}