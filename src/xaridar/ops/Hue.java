package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.util.List;
import java.util.Map;

public class Hue implements ImageOperation {
    @Override
    public String getName() {
        return "hue";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                List.of(
                        new ParamsList.Param("rgb", "", List.of(
                                new RGBNumberParam(),
                                new RGBNumberParam(),
                                new RGBNumberParam()
                        ), true, "RGB representation of the desired hue filter color"),
                        new ParamsList.Param("hex", "x", List.of(
                                new StringParam("#?[0-9A-Fa-f]{6}")
                        ), true, "hex representation of the desired hue filter color"),
                        new ParamsList.Param("color", "c", List.of(
                                new EnumParam(OpUtil.predefColors.keySet().stream().filter(c -> !c.equals("transparent")).toArray(String[]::new))
                        ), true, "color name of the desired hue filter color")
                ),
                Map.of(new String[]{"rgb", "hex", "color"}, 1),
                Map.of(new String[]{"rgb", "hex", "color"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
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
                    PixelColor newCol = PixelColor.hue(finalCol, pixelCol.luminance(), pixelCol.getAlpha());
                    out[x][y] = newCol.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a hue filter to input, which converts each pixel into one of an equivalent luminosity of the provided hue";
    }
}
