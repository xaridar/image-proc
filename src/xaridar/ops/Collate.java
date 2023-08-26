package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Collate implements ImageOperation {
    @Override
    public String getName() {
        return "collate";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("dimensions", new StringParam("(n|\\d+)"), 2, "width and height of output in images, with 'n' possible for each dimension for automatic sizing"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException {
        List<String> dims = (List<String>) args.get("dimensions");
        int w, h;
        w = h = -1;
        if (!dims.get(0).equals("n")) {
            w = Integer.parseInt(dims.get(0));
        }
        if (!dims.get(1).equals("n")) {
            h = Integer.parseInt(dims.get(1));
        }
        if (w == -1) {
            if (h != -1) w = (int) Math.ceil((float) images.size() / h);
            else {
                int sqrt = (int) Math.ceil(Math.sqrt(images.size()));
                w = sqrt;
                h = sqrt * sqrt - images.size() < sqrt ? sqrt : sqrt - 1;
            }
        }
        if (h == -1) h = (int) Math.ceil((float) images.size() / w);
        int maxW = images.stream().map(FileInfo::getWidth).max(Integer::compare).orElse(0);
        int maxH = images.stream().map(FileInfo::getHeight).max(Integer::compare).orElse(0);
        int[][] out = new int[maxW * w][maxH * h];
        int i = 0;
        for (int fileY = 0; fileY < h; fileY++) {
            for (int fileX = 0; fileX < w; fileX++) {
                if (i < images.size()) {
                    FileInfo img = images.get(i);
                    int xDiff = (maxW - img.getWidth()) / 2;
                    int yDiff = (maxH - img.getHeight()) / 2;
                    for (int y = 0; y < maxH; y++) {
                        for (int x = 0; x < maxW; x++) {
                            if (x < xDiff || x >= img.getWidth() + xDiff || y < yDiff || y >= img.getHeight() + yDiff) out[fileX * maxW + x][fileY * maxH + y] = PixelColor.white().toRGBA();
                            else out[fileX * maxW + x][fileY * maxH + y] = img.data[x - xDiff][y - yDiff];
                        }
                    }
                }  else {
                    for (int y = 0; y < maxH; y++) {
                        for (int x = 0; x < maxW; x++) {
                            out[fileX * maxW + x][fileY * maxH + y] = PixelColor.white().toRGBA();
                        }
                    }
                }
                i++;
            }
        }
        return List.of(images.get(0).withData(out));
    }

    @Override
    public String getDescr() {
        return "combines all input images together in a grid of either automatic or specified dimensions";
    }
}
