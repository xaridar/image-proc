package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.ArgsObj;
import xaridar.args.EnumParam;
import xaridar.args.NumberParam;
import xaridar.args.ParamsList;
import xaridar.PixelColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Grayscale implements xaridar.ops.ImageOperation {
    @Override
    public String getName() {
        return "grayscale";
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
                    int color = image.data[x][y];
                    PixelColor col = PixelColor.fromRGBA(color);
                    PixelColor gray = PixelColor.gray(col.luminance(), col.getAlpha());
                    out[x][y] = gray.toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a grayscale filter, using pixel luminosity to determine gray value of each pixel";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EFFECTS;
    }
}
