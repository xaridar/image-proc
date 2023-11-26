package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.NumberParam;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SaltAndPepper implements ImageOperation {
    @Override
    public String getName() {
        return "salt-and-pepper";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new ParamsList.Param("density", "d", List.of(new NumberParam(0, 100, true)), "the approximate percentage of pixels to be obfuscated into black or white"),
                new ParamsList.Param("seed", "s", List.of(new NumberParam(0, true)), true, "optional seed to guarantee consistency between calls")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int density = (int) args.get("density");
        Random rand;
        if (args.exists("random-seed")) {
            rand = new Random((int) args.get("seed"));
        } else rand = new Random();
        double densityP = density / 100d;
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    double r = rand.nextDouble();
                    if (r < densityP / 2) out[x][y] = PixelColor.black().toRGBA();
                    else if (r < densityP) out[x][y] = PixelColor.white().toRGBA();
                    else out[x][y] = image.data[x][y];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies salt-and-pepper noise to inputs, which randomly converts some percentage of the pixels to either black or white";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.NDN;
    }
}
