package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Quantize implements ImageOperation {
    @Override
    public String getName() {
        return "quantize";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("colors-list", new StringParam("" +
                        "\\[((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9])),\\s*((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9])),\\s*((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9]))]"
                ), 0, true, "list of RGB colors of format [r, g, b] to quantize input to"),
                List.of(new ParamsList.Param("colors", "c", List.of(new NumberParam(1, true)), true, "number of averaged colors to use to quantize input")),
                Map.of(new String[]{"colors-list", "colors"}, 1),
                Map.of(new String[]{"colors-list", "colors"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        PixelColor[] cols = null;
        if (args.exists("colors-list")) {
            List<String> colors = (List<String>) args.get("colors-list");
            Pattern p = Pattern.compile("\\d+");
            cols = colors.stream().map(c -> {
                Matcher m = p.matcher(c);
                int[] rgb = new int[3];
                for (int i = 0; i < 3; i++) {
                    m.find();
                    rgb[i] = Integer.parseInt(m.group());
                }
                return new PixelColor(rgb, 255);
            }).toArray(PixelColor[]::new);
        }
        PixelColor[] finalCols = cols;
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            PixelColor[] palette;
            if (args.exists("colors")) palette = OpUtil.quantize(image.data, (int) args.get("colors"));
            else palette = finalCols;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    PixelColor old = PixelColor.fromRGBA(image.data[x][y]);
                    out[x][y] = old.closestColor(palette).withAlpha(old.getAlpha()).toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "returns each input quantized into either the average c colors or the specific colors chosen by replacing each pixel with the closest from the possibilities";
    }
}
