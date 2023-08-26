package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Threshold implements ImageOperation {
    @Override
    public String getName() {
        return "threshold";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(List.of(
                new ParamsList.Param("red-min", "rmin", List.of(new RGBNumberParam()), true, "minimum red value"),
                new ParamsList.Param("red-max", "rmax", List.of(new RGBNumberParam()), true, "maximum red value"),
                new ParamsList.Param("red-range", "rrange", List.of(new RGBNumberParam(), new RGBNumberParam()), true, "red range"),
                new ParamsList.Param("green-min", "gmin", List.of(new RGBNumberParam()), true, "minimum green value"),
                new ParamsList.Param("green-max", "gmax", List.of(new RGBNumberParam()), true, "maximum green value"),
                new ParamsList.Param("green-range", "grange", List.of(new RGBNumberParam(), new RGBNumberParam()), true, "green range"),
                new ParamsList.Param("blue-min", "bmin", List.of(new RGBNumberParam()), true, "minimum blue value"),
                new ParamsList.Param("blue-max", "bmax", List.of(new RGBNumberParam()), true, "maximum blue value"),
                new ParamsList.Param("blue-range", "brange", List.of(new RGBNumberParam(), new RGBNumberParam()), true, "blue range"),
                new ParamsList.Param("alpha-min", "amin", List.of(new RGBNumberParam()), true, "minimum alpha value"),
                new ParamsList.Param("alpha-max", "amax", List.of(new RGBNumberParam()), true, "maximum alpha value"),
                new ParamsList.Param("alpha-range", "arange", List.of(new RGBNumberParam(), new RGBNumberParam()), true, "alpha range"),
                new ParamsList.Param("lum-min", "lmin", List.of(new NumberParam(0, 1)), true, "minimum luminance"),
                new ParamsList.Param("lum-max", "lmax", List.of(new NumberParam(0, 1)), true, "maximum luminance"),
                new ParamsList.Param("lum-range", "lrange", List.of(new NumberParam(0, 1), new NumberParam(0, 1)), true, "luminance range"),
                new ParamsList.Param("output", "o", List.of(new EnumParam("bw", "color", "bw-alpha", "color-alpha")), List.of("bw"), "output style; either black-on-white (bw), color-on-white (color), black-on-transparent (bw-alpha), or color-on-transparent (color-alpha)")),
                Map.of(new String[]{"red-min", "red-max", "red-range", "green-min", "green-max", "green-range", "blue-min", "blue-max", "blue-range", "alpha-min", "alpha-max", "alpha-range", "lum-min", "lum-max", "lum-alpha"}, 1),
                Map.of(
                        new String[]{"red-min", "red-range"}, 1,
                        new String[]{"red-max", "red-range"}, 1,
                        new String[]{"green-min", "green-range"}, 1,
                        new String[]{"green-max", "green-range"}, 1,
                        new String[]{"blue-min", "blue-range"}, 1,
                        new String[]{"blue-max", "blue-range"}, 1,
                        new String[]{"alpha-min", "alpha-range"}, 1,
                        new String[]{"alpha-max", "alpha-range"}, 1,
                        new String[]{"lum-min", "lum-range"}, 1,
                        new String[]{"lum-max", "lum-range"}, 1
                )
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    boolean passedThreshold = true;
                    if (args.exists("red-min")) {
                        int cond = (int) args.get("red-min");
                        if (col.getRed() < cond) passedThreshold = false;
                    }
                    if (args.exists("red-max")) {
                        int cond = (int) args.get("red-max");
                        if (col.getRed() > cond) passedThreshold = false;
                    }
                    if (args.exists("red-range")) {
                        Integer[] cond = ((List<Integer>) args.get("red-range")).toArray(Integer[]::new);
                        if (col.getRed() < cond[0] || col.getRed() > cond[1]) passedThreshold = false;
                    }
                    if (args.exists("green-min")) {
                        int cond = (int) args.get("green-min");
                        if (col.getGreen() < cond) passedThreshold = false;
                    }
                    if (args.exists("green-max")) {
                        int cond = (int) args.get("green-max");
                        if (col.getGreen() > cond) passedThreshold = false;
                    }
                    if (args.exists("green-range")) {
                        Integer[] cond = ((List<Integer>) args.get("green-range")).toArray(Integer[]::new);
                        if (col.getGreen() < cond[0] || col.getGreen() > cond[1]) passedThreshold = false;
                    }
                    if (args.exists("blue-min")) {
                        int cond = (int) args.get("blue-min");
                        if (col.getBlue() < cond) passedThreshold = false;
                    }
                    if (args.exists("blue-max")) {
                        int cond = (int) args.get("blue-max");
                        if (col.getBlue() > cond) passedThreshold = false;
                    }
                    if (args.exists("blue-range")) {
                        Integer[] cond = ((List<Integer>) args.get("blue-range")).toArray(Integer[]::new);
                        if (col.getBlue() < cond[0] || col.getBlue() > cond[1]) passedThreshold = false;
                    }
                    if (args.exists("alpha-min")) {
                        int cond = (int) args.get("alpha-min");
                        if (col.getAlpha() < cond) passedThreshold = false;
                    }
                    if (args.exists("alpha-max")) {
                        int cond = (int) args.get("alpha-max");
                        if (col.getAlpha() > cond) passedThreshold = false;
                    }
                    if (args.exists("alpha-range")) {
                        Integer[] cond = ((List<Integer>) args.get("alpha-range")).toArray(Integer[]::new);
                        if (col.getAlpha() < cond[0] || col.getAlpha() > cond[1]) passedThreshold = false;
                    }
                    if (args.exists("lum-min")) {
                        double cond = (double) args.get("lum-min");
                        if (col.luminance() < cond) passedThreshold = false;
                    }
                    if (args.exists("lum-max")) {
                        double cond = (double) args.get("lum-max");
                        if (col.luminance() > cond) passedThreshold = false;
                    }
                    if (args.exists("lum-range")) {
                        Double[] cond = ((List<Double>) args.get("lum-range")).toArray(Double[]::new);
                        if (col.luminance() < cond[0] || col.luminance() > cond[1]) passedThreshold = false;
                    }
                    String outputStyle = (String) args.get("output");
                    switch (outputStyle) {
                        case "bw":
                            if (passedThreshold) col = PixelColor.black();
                            else col = PixelColor.white();
                            break;
                        case "color":
                            if (!passedThreshold) col = PixelColor.white();
                            break;
                        case "bw-alpha":
                            if (passedThreshold) col = PixelColor.black();
                            else col = PixelColor.transparent();
                            break;
                        case "color-alpha":
                            if (!passedThreshold) col = PixelColor.transparent();
                            break;
                    }
                    out[x][y] = col.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "returns only the pixels that match the given conditions, in a variety of output styles";
    }
}
