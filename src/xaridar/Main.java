package xaridar;

import xaridar.args.ArgError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            GUI gui = new GUI();
        } else {
            CLI cli = new CLI();
            try {
                cli.parse(args);
            } catch (ArgError | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
