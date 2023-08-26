package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Vignette implements ImageOperation {
    @Override
    public String getName() {
        return "vignette";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("strength", new NumberParam(1, 100, true), 1, "vignette filter magnitude as a number 1-100"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int s = (int) args.get("strength");
        if (s == 0) return images.stream().map(FileInfo::copy).collect(Collectors.toList());
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            int[] center = new int[]{ image.getWidth() / 2, image.getHeight() / 2 };
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int dx = center[0] - x;
                    int dy = center[1] - y;
                    double factor = 1 - (Math.sqrt(dx*dx + dy*dy) / ((101 - s) * Math.sqrt(center[0]*center[0] + center[1]* center[1])));
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    double r = col.getRed() * factor;
                    double g = col.getGreen() * factor;
                    double b = col.getBlue() * factor;
                    out[x][y] = new PixelColor(new double[]{r, g, b}, col.getAlpha()).toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a vignette filter to each input, which darkens pixels based on their proximity to the edge";
    }
}
