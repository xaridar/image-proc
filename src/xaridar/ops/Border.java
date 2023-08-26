package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Border implements ImageOperation {
    @Override
    public String getName() {
        return "border";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("thickness", new NumberParam(0, true), 1, "border thickness in pixels"),
                List.of(
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
                        ), true, "color name of the desired border color"),
                        new ParamsList.Param("location", "l", List.of(
                                new EnumParam("inside", "outside")
                        ), List.of("outside"), "border location (inside/outside)")
                ),
                Map.of(new String[]{"rgb", "rgba", "hex", "color"}, 1),
                Map.of(new String[]{"rgb", "rgba", "hex", "color"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int thickness = (int) args.get("thickness");
        String location = (String) args.get("location");
        PixelColor col = PixelColor.transparent();
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
        PixelColor finalCol = col;
        return OpUtil.multiprocess(images, image -> {
            int width = image.getWidth();
            int height = image.getHeight();
            if (location.equals("outside")) {
                width += thickness * 2;
                height += thickness * 2;
            }
            int[][] out = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x < thickness || y < thickness || width - 1 - x < thickness || height - 1 - y < thickness) out[x][y] = finalCol.toRGBA();
                    else out[x][y] = image.data[x - (width == image.getWidth() ? 0 : thickness)][y - (height == image.getHeight() ? 0 : thickness)];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "adds a solid color border to every input image as specified";
    }
}
