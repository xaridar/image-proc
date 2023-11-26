package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Cutout implements ImageOperation {
    @Override
    public String getName() {
        return "cutout";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("black", "b", Collections.emptyList(), true, "flag for using black instead of white as the color to cut out from"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        boolean black = args.exists("black");
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if ((!black && PixelColor.fromRGBA(image.data[x][y]).equals(PixelColor.white())) || (black && PixelColor.fromRGBA(image.data[x][y]).equals(PixelColor.black()))) {
                        out[x][y] = PixelColor.transparent().toRGBA();
                    } else out[x][y] = image.data[x][y];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "cuts out non-white (or non-black if specified) pixels from each input and places them on a transparent background";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.TRANSFORM;
    }
}
