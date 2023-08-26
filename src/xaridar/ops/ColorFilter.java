package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ColorFilter implements ImageOperation {
    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("intensity", new NumberParam(0, 100), 1, "intensity percentage of the filter as a number from 0 to 100"),
                List.of(
                        new ParamsList.Param("rgb", "", List.of(
                                new RGBNumberParam(),
                                new RGBNumberParam(),
                                new RGBNumberParam()
                        ), true, "RGB representation of the desired filter color"),
                        new ParamsList.Param("hex", "x", List.of(
                                new StringParam("#?[0-9A-Fa-f]{6}")
                        ), true, "hex representation of the desired filter color"),
                        new ParamsList.Param("color", "c", List.of(
                                new EnumParam(OpUtil.predefColors.keySet().stream().filter(c -> !c.equals("transparent")).toArray(String[]::new))
                        ), true, "color name of the desired filter color")
                ),
                Map.of(new String[]{"rgb", "hex", "color"}, 1),
                Map.of(new String[]{"rgb", "hex", "color"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        double intensity = (double) args.get("intensity");
        PixelColor col = null;
        if (args.exists("rgb")) {
            List<Integer> rgb = (List<Integer>) args.get("rgb");
            col = new PixelColor(rgb.get(0), rgb.get(1), rgb.get(2), 255);
        } else if (args.exists("hex")) {
            String hex = (String) args.get("hex");
            col = PixelColor.fromHex(hex);
        } else if (args.exists("color")) {
            String color = (String) args.get("color");
            col = new PixelColor(OpUtil.predefColors.get(color));
        }
        PixelColor finalCol = col;
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    PixelColor pixelCol = PixelColor.fromRGBA(image.data[x][y]);
                    double lumMultiplier = pixelCol.luminance() * Math.exp(pixelCol.luminance()) + pixelCol.luminance();
                    PixelColor newCol = pixelCol
                            .withRed(pixelCol.getRed() + (int) (lumMultiplier * finalCol.getRed() * (intensity / 100)))
                            .withGreen(pixelCol.getGreen() + (int) (lumMultiplier * finalCol.getGreen() * (intensity / 100)))
                            .withBlue(pixelCol.getBlue() + (int) (lumMultiplier * finalCol.getBlue() * (intensity / 100)));
                    out[x][y] = newCol.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a filter of a specified color to each input image";
    }
}
