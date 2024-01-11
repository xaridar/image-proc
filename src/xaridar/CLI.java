package xaridar;

import xaridar.args.ArgError;
import xaridar.args.ParamsList;
import xaridar.nodes.*;
import xaridar.ops.ImageOperation;
import xaridar.ops.OperationManager;
import xaridar.tokens.Token;
import xaridar.tokens.Tokenizer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CLI {
    public void parse(String[] args) throws ArgError, IOException, InterruptedException {
        if (args[0].equals("help")) {
            // return help
            getHelp(args);
            return;
        }
        String argsStr = String.join(" ", args);
        System.out.println(argsStr);
        String[] ops = argsStr.split(">");
        if (ops.length == 0) throw new ArgError("At least an input operation and output, separated by '>', are required as arguments");
        List<Token> tokens = new Tokenizer(argsStr).tokenize();
        Node node = new Parser(tokens).parse();

        long start = System.currentTimeMillis();
        execNode(node, new ArrayList<>());
        System.out.println("Total: " + (System.currentTimeMillis() - start) + " ms");
    }

    public void getHelp(String[] args) {
        if (args.length == 1) {
            // operations list
            System.out.println("The operations available are:\n");
            for (ImageOperation op : OperationManager.alphaOps()) {
                System.out.printf("%s - %s%n", op.getName(), op.getDescr());
            }
            System.out.println("\nPipeline functions:");
            System.out.println("$ - Separates operations to be processed on all inputs in sequence.");
            System.out.println(
                    "; - Separates operations to be processed in parallel on the same input, with each parallel chain producing separate outputs."
            );
            System.out.println(
                    "~ - Returns both the output of the previous operation and that same output after the following operation has been applied.");
            System.out.println(
                    "+ - Used to separate n operations, where some integer multiple m of n inputs are provided; m inputs will be passed to each operation to be output.");
            System.out.println("() - Parentheses can be used to specify precedence and operation grouping/nesting.");
        } else if (args.length == 2) {
            // operation specified
            String op = args[1];
            if (op.equals("help")) System.out.println("help: prints information on operations\n" +
                    "'help' returns a list of operations, while 'help <operation>' returns information about a specified operation.");
            else {
                ImageOperation iOp = OperationManager.findOp(op);
                if (iOp == null) System.out.println("Operation not found: '" + op + "'.");
                else {
                    System.out.printf("%s: %s%n", iOp.getName(), iOp.getDescr());
                    System.out.printf("Category: %s%n", iOp.getCat().title);
                    ParamsList params = iOp.getParams();
                    System.out.println("\nParameters - ");
                    if (params.hasUnnamed()) {
                        System.out.printf("%s - %s", params.unnamedArgsInfo.name, params.unnamedArgsInfo.description);
                        if (params.unnamedArgsInfo.required == params.unnamedArgsInfo.max) System.out.printf("(%d values required)", params.unnamedArgsInfo.required);
                        else if (params.unnamedArgsInfo.max == -1) System.out.printf("(%d or more values required)", params.unnamedArgsInfo.required);
                        else System.out.printf("(between %d and %d values required)", params.unnamedArgsInfo.required, params.unnamedArgsInfo.max);
                    }
                    for (ParamsList.Param p : params.params) {
                        String name = String.format("--%s", p.fullName);
                        if (!p.shortName.equals("")) {
                            name += String.format(" / -%s", p.shortName);
                        }
                        if (p.optional) name += " (optional)";
                        else if (p.defaultVals.size() > 0) name += String.format(" (default=%s)", p.defaultVals.stream().map(String::valueOf).collect(Collectors.joining(" ")));
                        System.out.printf("\t%s: %s%n", name, p.description);
                    }
                    for (Map.Entry<String[], Integer> e : params.reqSets.entrySet()) {
                        System.out.printf("Requires %d or more of: %s%n", e.getValue(), String.join(", ", e.getKey()));
                    }
                    for (Map.Entry<String[], Integer> e : params.conflictingSets.entrySet()) {
                        System.out.printf("Requires no more than %d of: %s%n", e.getValue(), String.join(", ", e.getKey()));
                    }
                    System.out.println();
                }
            }
        } else {
            System.out.println("'help' can only be called with 0 or 1 argument(s). Use 'help help' to learn more.");
        }
    }

    public List<FileInfo> execNode(Node node, List<FileInfo> lastLayer) throws ArgError, IOException, InterruptedException {
        List<FileInfo> thisLayer = new ArrayList<>();
        if (node instanceof ParallelNode) {
            List<Thread> threads = new ArrayList<>();
            List<Node> nodes = ((ParallelNode) node).nodes;
            List<List<FileInfo>> out = new ArrayList<>();
            int nodesSize = nodes.size();
            for (int i = 0; i < nodesSize; i++) out.add(null);
            for (int i = 0; i < nodesSize; i++) {
                Node n = nodes.get(i);
                int finalI = i;
                Thread t = new Thread(() -> {
                    try {
                        List<FileInfo> files = execNode(n, lastLayer);
                        out.set(finalI, files);
                    } catch (ArgError | IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                threads.add(t);
            }
            for (Thread t : threads) {
                t.join();
            }
            thisLayer = out.stream().flatMap(Collection::stream).collect(Collectors.toList());
        } else if (node instanceof SplitNode) {
            List<Thread> threads = new ArrayList<>();
            List<Node> nodes = ((SplitNode) node).nodes;
            List<List<FileInfo>> out = new ArrayList<>();
            int nodesSize = nodes.size();
            if (lastLayer.size() % nodesSize != 0) throw new ArgError("Split operations must be provided in a multiple of the number of inputs.");
            int iPerOp = lastLayer.size() / nodesSize;
            for (int i = 0; i < nodesSize; i++) out.add(null);
            for (int i = 0; i < nodesSize; i++) {
                Node n = nodes.get(i);
                int finalI = i;
                Thread t = new Thread(() -> {
                    try {
                        List<FileInfo> files = execNode(n, lastLayer.subList(finalI * iPerOp, finalI * iPerOp + iPerOp));
                        out.set(finalI, files);
                    } catch (ArgError | IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                threads.add(t);
            }
            for (Thread t : threads) {
                t.join();
            }
            thisLayer = out.stream().flatMap(Collection::stream).collect(Collectors.toList());
        } else if (node instanceof OpNode) {
            ImageOperation imageOp = ((OpNode) node).operation;
            thisLayer = AppManager.parseOp(imageOp, lastLayer, ((OpNode) node).args);
        }
        if (node.hasChild()) {
            thisLayer = execNode(node.getChild(), thisLayer);
        }
        return thisLayer;
    }
}
