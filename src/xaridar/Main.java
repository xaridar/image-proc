package xaridar;

import xaridar.args.ArgError;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            // GUI gui = new GUI();
            System.out.println("Welcome to ImageProc!");
            System.out.println(
                    "\nThis is a tool that can be loaded with any number of image processing functions to be applied in a pipeline to any number of input images.");
            System.out.println("An input sequence of operations and parameters can be specified as arguments when executing this program.");
            System.out.println(
                    "To see the available operations and pipeline functions, use the 'help' operation by providing this as a single argument.");
            System.out.println(
                    "\nThe first operation of any sequence must be one in the 'Image Generation' category, which includes both file loading and specification of internal image generation.");
            System.out.println(
                    "After images have been input, they can be put through any number of operations to edit or merge them, with any complexity of specified 'tree' using pipeline functions. These functions can cause operations to cause in sequence or parallel.");
            System.out.println("To output images, use any 'Image Output' operations. Both inputting and outputting images from files allow wildcards and template names for outputting multiple images.");
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
