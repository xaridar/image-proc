package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Average implements ImageOperation {
    @Override
    public String getName() {
        return "average";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("with-alpha", "a", List.of(), true, "flag to specify whether the alpha channel should be averaged in the resulting output"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, InterruptedException {
        boolean withAlpha = args.exists("with-alpha");
        int[][][] out = new int[images.get(0).getWidth()][images.get(0).getHeight()][4];
        AtomicBoolean sameSize = new AtomicBoolean(true);
        OpUtil.multiprocess(images, image -> {
            if (image.getWidth() != out.length || image.getHeight() != out[0].length){
                sameSize.set(false);
                return null;
            }
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    out[x][y][0] += col.getRed();
                    out[x][y][1] += col.getGreen();
                    out[x][y][2] += col.getBlue();
                    out[x][y][3] += col.getAlpha();
                }
            }
            return null;
        });
        int[][] rgba = new int[images.get(0).getWidth()][images.get(0).getHeight()];
        for (int x = 0; x < out.length; x++) {
            for (int y = 0; y < out[x].length; y++) {
                PixelColor col = new PixelColor(Arrays.stream(out[x][y]).map(i -> i / images.size()).toArray());
                if (!withAlpha) col = col.withAlpha(255);
                rgba[x][y] = col.toRGBA();
            }
        }
        if (!sameSize.get()) throw new ArgError("Cannot average images of different dimensions");
        return List.of(images.get(0).withData(rgba));
    }

    @Override
    public String getDescr() {
        return "averages all input images by pixel and returns a single image containing the result";
    }
}
