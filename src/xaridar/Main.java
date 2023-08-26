package xaridar;

import xaridar.args.ArgError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            // GUI
            System.out.println("GUI not supported yet :(");
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
