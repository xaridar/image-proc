package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.util.Collections;
import java.util.List;

public class Rotate implements ImageOperation {
    @Override
    public String getName() {
        return "rotate";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("rotation", new NumberParam(), 1, "rotation (in degrees) of returned image(s)"),
                new ParamsList.Param("crop", "c", Collections.emptyList(), true, "flag to crop rotated image to fit in frame of original image; default behavior is adjusting frame dimensions to fit")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        double rot = (double) args.get("rotation");
        double theta = rot * Math.PI / 180;
        boolean crop = args.exists("crop");
        return OpUtil.multiprocess(images, image -> {
            int newWidth = (int) Math.ceil(image.getWidth() * Math.abs(Math.cos(theta)) + image.getHeight() * Math.abs(Math.sin(theta)));
            int newHeight = (int) Math.ceil(image.getWidth() * Math.abs(Math.sin(theta)) + image.getHeight() * Math.abs(Math.cos(theta)));
            int[][] out = new int[crop ? image.getWidth() : newWidth][crop ? image.getHeight() : newHeight];
            double[] center = new double[]{ image.getWidth() / 2f - 0.5f, image.getHeight() / 2f - 0.5f };
            double[] newCenter = crop ? center : new double[]{ newWidth / 2f - 0.5f, newHeight / 2f - 0.5f };
            for (int y = 0; y < out[0].length; y++) {
                for (int x = 0; x < out.length; x++) {
                    out[x][y] = PixelColor.transparent().toRGBA();
                    double distFromCenterX = x - newCenter[0];
                    double distFromCenterY = y - newCenter[1];
                    double xComp = distFromCenterX * Math.cos(-theta) - distFromCenterY * Math.sin(-theta);
                    double yComp = distFromCenterX * Math.sin(-theta) + distFromCenterY * Math.cos(-theta);
                    int xPixel = (int) Math.round(center[0] + xComp);
                    if (OpUtil.clamp(xPixel, 0, image.getWidth() - 1) != xPixel) continue;
                    int yPixel = (int) Math.round(center[1] + yComp);
                    if (OpUtil.clamp(yPixel, 0, image.getHeight() - 1) != yPixel) continue;
                    out[x][y] = image.data[xPixel][yPixel];
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "rotates inputs by a given degree measure and reshapes frames to match, if desired";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.TRANSFORM;
    }
}
