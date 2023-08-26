package xaridar.nodes;

import java.util.Arrays;
import java.util.List;

public class SplitNode extends Node {
    public List<Node> nodes;

    public SplitNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public SplitNode(Node... nodes) {
        this.nodes = Arrays.asList(nodes);
    }

    @Override
    public Node copy() {
        Node n = new SplitNode(nodes);
        if (hasChild()) n.setChild(getChild().copy());
        return n;
    }
}
