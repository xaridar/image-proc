package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dither implements ImageOperation {
    @Override
    public String getName() {
        return "dither";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("colors-list", new StringParam("" +
                        "\\[((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9])),\\s*((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9])),\\s*((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|[0-9]|([1-9][0-9]))]"
                ), 0, true, "list of RGB colors of format [r, g, b] to quantize input to before dithering"),
                List.of(new ParamsList.Param("colors", "c", List.of(new NumberParam(1, true)), true, "number of averaged colors to use to quantize input before dithering")),
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
            System.arraycopy(image.data, 0, out, 0, image.getWidth());
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int old = out[x][y];
                    out[x][y] = PixelColor.fromRGBA(old).closestColor(palette).toRGBA();
                    double error = PixelColor.fromRGBA(old).distDir(PixelColor.fromRGBA(out[x][y]));
                    if (x < image.getWidth() - 1) out[x + 1][y] += error * 7 / 16;
                    if (x > 0 && y < image.getHeight() - 1) out[x - 1][y + 1] += error * 3 / 16;
                    if (y < image.getHeight() - 1) out[x][y + 1] += error * 5 / 16;
                    if (x < image.getWidth() - 1 && y < image.getHeight() - 1) out[x + 1][y + 1] += error / 16;
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a dithering filter to input by quantizing it and applying smoothing";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.NDN;
    }
}
