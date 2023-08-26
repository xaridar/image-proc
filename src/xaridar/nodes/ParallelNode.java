package xaridar.nodes;

import java.util.Arrays;
import java.util.List;

public class ParallelNode extends Node {
    public List<Node> nodes;

    public ParallelNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public ParallelNode(Node... nodes) {
        this.nodes = Arrays.asList(nodes);
    }

    @Override
    public Node copy() {
        Node n = new ParallelNode(nodes);
        if (hasChild()) n.setChild(getChild().copy());
        return n;
    }
}
