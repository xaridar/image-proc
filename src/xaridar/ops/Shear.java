package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;

public class Shear implements ImageOperation {
    @Override
    public String getName() {
        return "shear";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("factors", new NumberParam(-0.5, 0.5), 2, "x and y factors to shear inputs by"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        List<Double> factors = (List<Double>) args.get("factors");
        double xFact = Math.abs(factors.get(0));
        double yFact = Math.abs(factors.get(1));
        return OpUtil.multiprocess(images, image -> {
            int xDist = (int) ((image.getWidth() + image.getHeight() * xFact) / (1 - factors.get(0) * factors.get(1)));
            int yDist = (int) ((image.getHeight() + image.getWidth() * yFact) / (1 - factors.get(0) * factors.get(1)));
            int[][] out = new int[xDist][yDist];
            for (int y = 0; y < out[0].length; y++) {
                for (int x = 0; x < out.length; x++) {
                    int usedX = x;
                    if (factors.get(0) != xFact) usedX -= (image.getHeight()) * xFact / (1 - factors.get(0) * factors.get(1));
                    int usedY = y;
                    if (factors.get(1) != yFact) usedY -= (image.getWidth()) * yFact / (1 - factors.get(0) * factors.get(1));
                    int newX = (int) (usedX - factors.get(0) * usedY);
                    int newY = (int) (usedY - factors.get(1) * usedX);
//                    newY -= image.getWidth() * yFact - 1;
                    if (newX >= 0 && newX < image.getWidth() && newY >= 0 && newY < image.getHeight()) out[x][y] = image.data[newX][newY];
                    else out[x][y] = PixelColor.transparent().toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "shears inputs by a given set of factors and reshapes frames to match";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.TRANSFORM;
    }
}
