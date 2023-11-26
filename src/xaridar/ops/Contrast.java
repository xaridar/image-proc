package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;

public class Contrast implements ImageOperation {
    @Override
    public String getName() {
        return "contrast";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("intensity", new NumberParam(-100, 100), 1, "contrast modifier percentage; negative signifies decrease of contrast, while positive means increase"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        double intensity = (double) args.get("intensity");
        PixelColor finalCol = PixelColor.gray(0.5f);
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = image.data[x][y];
                    PixelColor pixelCol = PixelColor.fromRGBA(color);

                    int r = (int) (((double) (finalCol.getRed() - pixelCol.getRed()) / 100) * intensity);
                    int g = (int) (((double) (finalCol.getGreen() - pixelCol.getGreen()) / 100) * intensity);
                    int b = (int) (((double) (finalCol.getBlue() - pixelCol.getBlue()) / 100) * intensity);
                    PixelColor outCol = new PixelColor(
                            pixelCol.getRed() - r,
                            pixelCol.getGreen() - g,
                            pixelCol.getBlue() - b,
                            pixelCol.getAlpha()
                    );
                    out[x][y] = outCol.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "edits the contrast of all input images";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.REBAL;
    }
}
