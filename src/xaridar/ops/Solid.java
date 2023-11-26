package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class Solid implements ImageOperation {

    @Override
    public String getName() {
        return "solid";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("size", new NumberParam(1, true), 2, "the width and height of the resulting image"),
                List.of(
                    new ParamsList.Param("instances", "i", List.of(new NumberParam(1, true)), List.of(1), "the number of images to be generated"),
                    new ParamsList.Param("rgb", "", List.of(
                            new RGBNumberParam(),
                            new RGBNumberParam(),
                            new RGBNumberParam()
                    ), true, "RGB representation of the desired border color"),
                    new ParamsList.Param("rgba", "", List.of(
                            new RGBNumberParam(),
                            new RGBNumberParam(),
                            new RGBNumberParam(),
                            new RGBNumberParam()
                    ), true, "RGBA representation of the desired border color"),
                    new ParamsList.Param("hex", "x", List.of(
                            new StringParam("#?[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")
                    ), true, "hex representation of the desired border color"),
                    new ParamsList.Param("color", "c", List.of(
                            new EnumParam(OpUtil.predefColors.keySet().toArray(String[]::new))
                    ), true, "color name of the desired border color")
                ),
                Collections.emptyMap(),
                Map.of(new String[]{"rgb", "rgba", "hex", "color"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException {
        List<Integer> dims = (List<Integer>) args.get("size");
        PixelColor col = PixelColor.white();
        if (args.exists("rgb")) {
            List<Integer> rgb = (List<Integer>) args.get("rgb");
            col = new PixelColor(rgb.get(0), rgb.get(1), rgb.get(2), 255);
        } else if (args.exists("rgba")) {
            List<Integer> rgba = (List<Integer>) args.get("rgba");
            col = new PixelColor(rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3));
        } else if (args.exists("hex")) {
            String hex = (String) args.get("hex");
            col = PixelColor.fromHex(hex);
        } else if (args.exists("color")) {
            String color = (String) args.get("color");
            col = new PixelColor(OpUtil.predefColors.get(color));
        }
        int[][] data = new int[dims.get(0)][dims.get(1)];
        for (int y = 0; y < data[0].length; y++) {
            for (int x = 0; x < data.length; x++) {
                data[x][y] = col.toRGBA();
            }
        }
        List<FileInfo> ret = new ArrayList<>();
        for (int i = 0; i < ((int) args.get("instances")); i++) {
            ret.add(new FileInfo(data, "png", null));
        }
        return ret;
    }

    @Override
    public boolean inputNeeded() {
        return false;
    }

    @Override
    public String getDescr() {
        return "generation op (does not take input); creates a solid-color image of any color and size";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.INPUT;
    }
}
