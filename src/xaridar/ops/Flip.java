package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;

public class Flip implements ImageOperation {
    @Override
    public String getName() {
        return "flip";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("direction", new EnumParam("h", "v", "hv", "vh"), 1, "specifies the flip direction: 'h' for horizontal, 'v' for vertical, or 'vh' or 'hv' for both"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        String dir = (String) args.get("direction");
        boolean vert = !dir.equals("h");
        boolean horiz = !dir.equals("v");
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int fromX = horiz ? image.getWidth() - x - 1 : x;
                    int fromY = vert ? image.getHeight() - y - 1 : y;
                    out[x][y] = image.data[fromX][fromY];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "flips inputs horizontally, vertically, or both";
    }
}
