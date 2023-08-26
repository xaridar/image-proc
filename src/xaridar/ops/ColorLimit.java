package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ColorLimit implements ImageOperation {
    @Override
    public String getName() {
        return "color-limit";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(List.of(
                    new ParamsList.Param("red-min", "rmin", List.of(new RGBNumberParam()), true, "minimum value for the red channel in the output images"),
                    new ParamsList.Param("red-max", "rmax", List.of(new RGBNumberParam()), true, "maximum value for the red channel in the output images"),
                    new ParamsList.Param("green-min", "gmin", List.of(new RGBNumberParam()), true, "minimum value for the green channel in the output images"),
                    new ParamsList.Param("green-max", "gmax", List.of(new RGBNumberParam()), true, "maximum value for the green channel in the output images"),
                    new ParamsList.Param("blue-min", "bmin", List.of(new RGBNumberParam()), true, "minimum value for the blue channel in the output images"),
                    new ParamsList.Param("blue-max", "bmax", List.of(new RGBNumberParam()), true, "maximum value for the blue channel in the output images"),
                    new ParamsList.Param("alpha-min", "amin", List.of(new RGBNumberParam()), true, "minimum value for the alpha channel in the output images"),
                    new ParamsList.Param("alpha-max", "amax", List.of(new RGBNumberParam()), true, "maximum value for the alpha channel in the output images"),
                    new ParamsList.Param("lum-min", "lmin", List.of(new NumberParam(0, 1)), true, "minimum luminance values in the output images"),
                    new ParamsList.Param("lum-max", "lmax", List.of(new NumberParam(0, 1)), true, "maximum luminance values in the output images")
                ),
                Map.of(new String[]{"red-min", "red-max", "green-min", "green-max", "blue-min", "blue-max", "alpha-min", "alpha-max", "lum-min", "lum-max"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    if (args.exists("red-min")) {
                        int cond = (int) args.get("red-min");
                        if (col.getRed() < cond) col = col.withRed(cond);
                    }
                    if (args.exists("red-max")) {
                        int cond = (int) args.get("red-max");
                        if (col.getRed() > cond) col = col.withRed(cond);
                    }
                    if (args.exists("green-min")) {
                        int cond = (int) args.get("green-min");
                        if (col.getGreen() < cond) col = col.withGreen(cond);
                    }
                    if (args.exists("green-max")) {
                        int cond = (int) args.get("green-max");
                        if (col.getGreen() > cond) col = col.withGreen(cond);
                    }
                    if (args.exists("blue-min")) {
                        int cond = (int) args.get("blue-min");
                        if (col.getBlue() < cond) col = col.withBlue(cond);
                    }
                    if (args.exists("blue-max")) {
                        int cond = (int) args.get("blue-max");
                        if (col.getBlue() > cond) col = col.withBlue(cond);
                    }
                    if (args.exists("alpha-min")) {
                        int cond = (int) args.get("alpha-min");
                        if (col.getAlpha() < cond) col = col.withAlpha(cond);
                    }
                    if (args.exists("alpha-max")) {
                        int cond = (int) args.get("alpha-max");
                        if (col.getAlpha() > cond) col = col.withAlpha(cond);
                    }
                    if (args.exists("lum-min")) {
                        double cond = (double) args.get("lum-min");
                        if (col.luminance() < cond) {
                            double percentage = cond / col.luminance();
                            col = PixelColor.hue(col, percentage, col.getAlpha());
                        }
                    }
                    if (args.exists("lum-max")) {
                        double cond = (double) args.get("lum-max");
                        if (col.luminance() > cond) {
                            double percentage = cond / col.luminance();
                            col = PixelColor.hue(col, percentage, col.getAlpha());
                        }
                    }
                    out[x][y] = col.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "limits the channels and/or luminance of all pixels in input images to fit within any specified ranges";
    }
}
