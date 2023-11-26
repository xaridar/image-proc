package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HistogramEqualization implements ImageOperation {
    @Override
    public String getName() {
        return "histeq";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList();
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            List<Integer> lums = Arrays.stream(image.data)
                    .flatMap(row -> Arrays.stream(row)
                            .boxed().map(x -> (int) (PixelColor.fromRGBA(x).luminance() * 256))
                    ).collect(Collectors.toList());
            int[] hist = IntStream.rangeClosed(0, 256).map(x -> (int) lums.stream().filter(i -> i == x).count()).toArray();
            int[] histEq = IntStream.rangeClosed(0, 256)
                    .map(n -> (int) ((255d / (image.getWidth() * image.getHeight())) *
                                                Arrays.stream(hist)
                                                        .limit(n + 1)
                                                        .reduce(0, Integer::sum)))
                    .toArray();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    PixelColor col = PixelColor.fromRGBA(image.data[x][y]);
                    int lum = (int) (col.luminance() * 256);
                    int eq = histEq[lum];
                    out[x][y] = PixelColor.hue(col, eq / 256d, col.getAlpha()).toRGBA();
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies histogram equalization to each input, which spreads out luminosity evenly to improve contrast";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.REBAL;
    }
}
