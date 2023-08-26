package xaridar.nodes;

public abstract class Node {
    private Node child;

    public void setChild(Node child) {
        this.child = child;
    }

    public boolean hasChild() {
        return child != null;
    }

    public Node getChild() {
        return child;
    }

    public abstract Node copy();
}
