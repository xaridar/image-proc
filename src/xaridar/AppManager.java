package xaridar;

import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.ops.ImageOperation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppManager {

    public static List<FileInfo> parseOp(ImageOperation operation, List<FileInfo> imgs, String[] args) throws ArgError, IOException, InterruptedException {
        if (!operation.inputNeeded()) {
            if (imgs.size() > 0) throw new ArgError(operation.getName() + " does not use file inputs, so it should be placed at the beginning of the operation chain");
            return AppManager.parseOpNoInput(operation, args);
        }
        if (imgs.size() == 0)
            throw new ArgError("To use the " + operation.getName() + " operation, you must pass an image to it");
        ArgsObj argsObj = operation.getParams().parse(args, operation.getName());

        List<FileInfo> outData = operation.process(imgs, argsObj);
        outData.forEach(op -> op.addOp(operation.getName()));

        return outData;
    }

    public static List<FileInfo> parseOpNoInput(ImageOperation operation, String[] args) throws ArgError, IOException, InterruptedException {
        ArgsObj argsObj = operation.getParams().parse(args, operation.getName());
        List<FileInfo> outData = operation.process(Collections.emptyList(), argsObj);

        return outData;
    }
}
