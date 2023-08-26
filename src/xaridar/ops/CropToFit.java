package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.EnumParam;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.List;

public class CropToFit implements ImageOperation {
    @Override
    public String getName() {
        return "crop-to-fit";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("color", "c", List.of(new EnumParam("b", "w", "t")), List.of("t"), "specifies the color to crop out: 'b'lack, 'w'hite, or 't'ransparent"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        String color = (String) args.get("color");
        PixelColor col;
        if (color.equals("w")) {
            col = PixelColor.white();
        } else if (color.equals("b")) {
            col = PixelColor.black();
        } else {
            col = PixelColor.transparent();
        }
        return OpUtil.multiprocess(images, image -> {
            int minX = image.getWidth() - 1, maxX = 0, minY = image.getHeight() - 1, maxY = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    PixelColor currCol = PixelColor.fromRGBA(image.data[x][y]);
                    if (!currCol.equals(col)) {
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            int w = 1 + (maxX - minX);
            int h = 1 + (maxY - minY);
            int[][] out = new int[w][h];
            for (int x = 0; x < w; x++) {
                System.arraycopy(image.data[x + minX], minY, out[x], 0, h);
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "crops inputs to remove surrounding empty black, white, or transparent (as specified) areas around the edges";
    }
}
