package project;

import java.awt.Point;
import java.util.Objects;

class Node {
    public final Point p;
    public Node parent;
    public int g;
    public int f;
    
    public Node(Point p) { 
        this.p = p; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return Objects.equals(p, n.p);
    }
    
    @Override
    public int hashCode() { 
        return Objects.hash(p); 
    }
}