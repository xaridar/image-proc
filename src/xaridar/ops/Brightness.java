package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgsObj;
import xaridar.args.NumberParam;
import xaridar.args.ParamsList;
import xaridar.args.UnnamedArgsInfo;

import java.io.IOException;
import java.util.List;

public class Brightness implements ImageOperation {
    @Override
    public String getName() {
        return "brightness";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("brightness", new NumberParam(-100, 100), 1, "brightness modifier percentage; negative signifies darkening, while positive means brightening"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        double brightness = (double) args.get("brightness");
        PixelColor finalCol;
        if (brightness < 0) finalCol = PixelColor.black();
        else finalCol = PixelColor.white();
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int color = image.data[x][y];
                    PixelColor pixelCol = PixelColor.fromRGBA(color);

                    int r = (int) (((double) (finalCol.getRed() - pixelCol.getRed()) / 100) * Math.abs(brightness));
                    int g = (int) (((double) (finalCol.getGreen() - pixelCol.getGreen()) / 100) * Math.abs(brightness));
                    int b = (int) (((double) (finalCol.getBlue() - pixelCol.getBlue()) / 100) * Math.abs(brightness));
                    PixelColor outCol = new PixelColor(
                            pixelCol.getRed() + r,
                            pixelCol.getGreen() + g,
                            pixelCol.getBlue() + b,
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
        return "edits the brightness of all input images";
    }
}
