package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FilterOut implements ImageOperation {
    @Override
    public String getName() {
        return "filter-out";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(List.of(
                        new ParamsList.Param("rgb", "", List.of(
                                new RGBNumberParam(),
                                new RGBNumberParam(),
                                new RGBNumberParam()
                        ), true, "RGB representation of the color to filter out"),
                        new ParamsList.Param("rgba", "", List.of(
                                new RGBNumberParam(),
                                new RGBNumberParam(),
                                new RGBNumberParam(),
                                new RGBNumberParam()
                        ), true, "RGBA representation of the color to filter out"),
                        new ParamsList.Param("hex", "x", List.of(
                                new StringParam("#?[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")
                        ), true, "hex representation of the color to filter out")
                ),
                Map.of(new String[]{"rgb", "rgba", "hex"}, 1),
                Map.of(new String[]{"rgb", "rgba", "hex"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        PixelColor col = null;
        if (args.exists("rgb")) {
            List<Integer> rgb = (List<Integer>) args.get("rgb");
            col = new PixelColor(rgb.get(0), rgb.get(1), rgb.get(2), 255);
        } else if (args.exists("rgba")) {
            List<Integer> rgba = (List<Integer>) args.get("rgba");
            col = new PixelColor(rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3));
        } else if (args.exists("hex")) {
            String hex = (String) args.get("hex");
            col = PixelColor.fromHex(hex);
        }
        PixelColor finalCol = col;
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = image.data[x][y];
                    PixelColor pixelCol = PixelColor.fromRGBA(color);
                    if (pixelCol.equals(finalCol)) out[x][y] = PixelColor.transparent().toRGBA();
                    else out[x][y] = color;
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "filters out a specific pixel color from all input";
    }
}
