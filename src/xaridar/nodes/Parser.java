package xaridar.nodes;

import xaridar.args.ArgError;
import xaridar.ops.ImageOperation;
import xaridar.ops.OperationManager;
import xaridar.tokens.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    int counter = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void advance() {
        counter++;
    }

    public Node parse() throws ArgError {
        return series();
    }

    public Node series() throws ArgError {
        Node left = added();
        if (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.SERIES) {
            advance();
            Node right = series();
            left.setChild(right);
        }
        return left;
    }

    public Node added() throws ArgError {
        Node left = parallel();
        if (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.AND) {
            advance();
            Node right = added();
            Node node = left.copy();
            node.setChild(right);
            left = new ParallelNode(left, node);
        }
        return left;
    }

    public Node parallel() throws ArgError {
        Node left = split();
        if (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.PARALLEL) {
            List<Node> nodes = new ArrayList<>();
            nodes.add(left);
            while (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.PARALLEL) {
                advance();
                Node right = split();
                nodes.add(right);
            }
            left = new ParallelNode(nodes);
        }
        return left;
    }

    public Node split() throws ArgError {
        Node left = atomic();
        if (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.SPLIT) {
            List<Node> nodes = new ArrayList<>();
            nodes.add(left);
            while (counter < tokens.size() && tokens.get(counter).type == Token.TokenType.SPLIT) {
                advance();
                Node right = atomic();
                nodes.add(right);
            }
            left = new SplitNode(nodes);
        }
        return left;
    }

    public Node atomic() throws ArgError {
        Token t = tokens.get(counter);

        // Parentheses
        if (t.type == Token.TokenType.OPEN_PAREN) {
            advance();
            List<Token> contained = new ArrayList<>();
            boolean closed = false;
            Token curr = null;
            int openParens = 1;
            while (counter < tokens.size()) {
                curr = tokens.get(counter);
                advance();
                if (curr.type == Token.TokenType.OPEN_PAREN) openParens++;
                if (curr.type == Token.TokenType.CLOSE_PAREN) openParens--;
                if (openParens == 0){
                    closed = true;
                    break;
                }
                contained.add(curr);
            }
            if (!closed) throw new ArgError("Unclosed parentheses found");
            return new Parser(contained).parse();
        }

        // Operation
        ImageOperation op = OperationManager.findOp(t.val);
        if (op == null) throw new ArgError("Operation not found: " + t.val);
        advance();
        List<String> args = new ArrayList<>();
        while (counter < tokens.size()) {
            Token.TokenType tType = tokens.get(counter).type;
            if (tType == Token.TokenType.STRING) {
                args.add(tokens.get(counter).val);
                advance();
            } else break;
        }
        return new OpNode(op, args.toArray(String[]::new));
    }
}
