package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Invert implements ImageOperation {
    @Override
    public String getName() {
        return "invert";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList();
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    int r = col.getRed();
                    int g = col.getGreen();
                    int b = col.getBlue();
                    int a = col.getAlpha();
                    out[x][y] = new PixelColor(255 - r, 255 - g, 255 - b, a).toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "inverts the color of input images";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EFFECTS;
    }
}
