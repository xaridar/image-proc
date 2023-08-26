package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChannelFilter implements ImageOperation {

    @Override
    public String getName() {
        return "channel-filter";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("color", new EnumParam("r", "g", "b"), 1, 3, "choice of 'r', 'g', and/or 'b' channels to filter images by"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        List<String> channels = (List<String>) args.get("color");
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    int r = 0, g = 0, b = 0;
                    if (channels.contains("r")) {
                        r = col.getRed();
                    }
                    if (channels.contains("g")) {
                        g = col.getGreen();
                    }
                    if (channels.contains("b")) {
                        b = col.getBlue();
                    }
                    out[x][y] = new PixelColor(r,g,b,col.getAlpha()).toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "returns each input image with only one or more color channels (r, g, b) included";
    }
}
