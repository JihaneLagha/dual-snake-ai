package project;

import java.awt.Point;
import java.util.*;

class AIPathfinder {
    private final int cols, rows;

    public AIPathfinder(int cols, int rows) { 
        this.cols = cols; 
        this.rows = rows; 
    }

    private List<Point> reconstruct(Node node) {
        LinkedList<Point> path = new LinkedList<>();
        while (node != null) { 
            path.addFirst(node.p); 
            node = node.parent; 
        }
        return path;
    }

    private int manhattan(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Point> neighbors(Point p) {
        List<Point> n = new ArrayList<>();
        n.add(new Point(p.x + 1, p.y));
        n.add(new Point(p.x - 1, p.y));
        n.add(new Point(p.x, p.y + 1));
        n.add(new Point(p.x, p.y - 1));
        return n;
    }

    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < cols && p.y >= 0 && p.y < rows;
    }

    private boolean isBlocked(Point p, Snake currentSnake, Snake otherSnake, Maze maze) {
        // Check if it's a maze wall
        if (maze != null && maze.isWall(p)) return true;
        
        // Allow current snake's head
        Point head = currentSnake.getHead();
        if (p.equals(head)) return false;
        
        // Allow current snake's tail (it will move away)
        List<Point> body = currentSnake.getBody();
        Point tail = body.get(body.size() - 1);
        if (p.equals(tail)) return false;
        
        // Check if occupied by either snake
        return currentSnake.occupies(p) || otherSnake.occupies(p);
    }
    
    
     //A* implementation
    public List<Point> findPathAStar(Point start, Point goal, Snake currentSnake, Snake otherSnake, Maze maze) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Point, Integer> gScore = new HashMap<>();
        Node startNode = new Node(new Point(start.x, start.y));
        startNode.g = 0; 
        startNode.f = manhattan(start, goal);
        open.add(startNode);
        gScore.put(start, 0);
        Set<Point> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (cur.p.equals(goal)) return reconstruct(cur);//reach goal
            closed.add(cur.p);
            
            for (Point nb : neighbors(cur.p)) {
                if (!inBounds(nb)) continue;
                if (isBlocked(nb, currentSnake, otherSnake, maze)) continue;
                if (closed.contains(nb)) continue;
                
                int tentativeG = cur.g + 1;//cost increases by 1
                if (tentativeG < gScore.getOrDefault(nb, Integer.MAX_VALUE)) {
                    Node next = new Node(new Point(nb.x, nb.y));
                    next.parent = cur;
                    next.g = tentativeG;
                    next.f = tentativeG + manhattan(nb, goal);
                    gScore.put(nb, tentativeG);
                    open.add(next);
                }
            }
        }
        return null;//no path
    }
    
    
    //Greedy Best-First Search implementation
    public List<Point> findPathGreedy(Point start, Point goal, Snake currentSnake, Snake otherSnake, Maze maze) {
        LinkedList<Point> path = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        Point cur = new Point(start.x, start.y);
        path.add(cur);
        visited.add(cur);
        int steps = 0;
        
        while (!cur.equals(goal) && steps < cols * rows) {
            List<Point> nb = neighbors(cur);
            nb.removeIf(p -> !inBounds(p) || isBlocked(p, currentSnake, otherSnake, maze) || visited.contains(p));
            if (nb.isEmpty()) break;
            
            nb.sort(Comparator.comparingInt(p -> manhattan(p, goal)));//choose nearest
            cur = nb.get(0);
            path.add(cur);
            visited.add(cur);
            steps++;
        }
        
        if (cur.equals(goal)) return path; 
        else return null;
    }

    public Point safeMove(Point head, Snake currentSnake, Snake otherSnake, Maze maze) {
        List<Point> possibleMoves = new ArrayList<>();
        
        for (Point nb : neighbors(head)) {
            if (!inBounds(nb)) continue;
            if (!isBlocked(nb, currentSnake, otherSnake, maze)) {
                possibleMoves.add(nb);
            }
        }
        
        if (possibleMoves.isEmpty()) {
            return null;
        }
        
        possibleMoves.sort((a, b) -> {
            int scoreA = evaluateMoveQuality(a, currentSnake, otherSnake, maze);
            int scoreB = evaluateMoveQuality(b, currentSnake, otherSnake, maze);
            return Integer.compare(scoreB, scoreA);
        });
        
        return possibleMoves.get(0);
    }
    
    private int evaluateMoveQuality(Point move, Snake currentSnake, Snake otherSnake, Maze maze) {
        int score = 0;
        
        int distFromWall = Math.min(
            Math.min(move.x, cols - move.x - 1),
            Math.min(move.y, rows - move.y - 1)
        );
        
        score += distFromWall * 10;
        
        int freeNeighbors = 0;
        for (Point nb : neighbors(move)) {
            if (inBounds(nb) && !isBlocked(nb, currentSnake, otherSnake, maze)) {
                freeNeighbors++;
            }
        }
        
        score += freeNeighbors * 20;
        
        Point otherHead = otherSnake.getHead();
        int distFromOther = manhattan(move, otherHead);
        score += distFromOther * 5;
        
        return score;
    }
}