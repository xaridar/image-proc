package xaridar.nodes;

import xaridar.ops.ImageOperation;

import java.util.List;

public class OpNode extends Node {
    public ImageOperation operation;
    public String [] args;

    public OpNode(ImageOperation operation, String [] args) {
        this.operation = operation;
        this.args = args;
    }

    @Override
    public Node copy() {
        Node n = new OpNode(operation, args);
        if (hasChild()) n.setChild(getChild().copy());
        return n;
    }
}
