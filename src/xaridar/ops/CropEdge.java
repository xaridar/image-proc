package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;

public class CropEdge implements ImageOperation {
    @Override
    public String getName() {
        return "crop-edge";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("thickness", new NumberParam(0, true), 1, "number of pixels on each border to crop"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int thickness = (int) args.get("thickness");
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth() - thickness * 2][image.getHeight() - thickness * 2];
            for (int y = 0; y < image.getHeight() - thickness * 2; y++) {
                for (int x = 0; x < image.getWidth() - thickness * 2; x++) {
                    out[x][y] = image.data[x + thickness][y + thickness];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "crops all edges of input by the same number of pixels";
    }
}
