package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.List;

public class Kuwahara implements ImageOperation {
    @Override
    public String getName() {
        return "kuwahara";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("window-size", new OddNumberParam(3), 1, "the size of the window used for the filter; a smaller window better preserves details"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int windowSize = (int) args.get("window-size");
        int halfWindowSize = windowSize / 2;
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    double[] luminanceSum = new double[4];
                    double[] luminanceSqrSum = new double[4];
                    int[] pixelCount = new int[4];

                    for (int offsetY = -halfWindowSize; offsetY <= halfWindowSize; offsetY++) {
                        for (int offsetX = -halfWindowSize; offsetX <= halfWindowSize; offsetX++) {
                            int neighborX = x + offsetX;
                            int neighborY = y + offsetY;

                            if (neighborX >= 0 && neighborX < image.getWidth() && neighborY >= 0 && neighborY < image.getHeight()) {
                                int color = image.data[neighborX][neighborY];
                                double luminance = PixelColor.fromRGBA(color).luminance();
                                int quadrant;
                                if (offsetX * offsetY <= 0) {
                                    quadrant = offsetX * offsetX <= offsetY * offsetY ? 0 : 2;
                                } else {
                                    quadrant = offsetX * offsetX <= offsetY * offsetY ? 1 : 3;
                                }

                                luminanceSum[quadrant] += luminance;
                                luminanceSqrSum[quadrant] += luminance * luminance;
                                pixelCount[quadrant]++;
                            }
                        }
                    }

                    int regionSize;
                    double minStandardDeviation = Double.MAX_VALUE;
                    int selectedQuadrant = 0;

                    for (int quadrant = 0; quadrant < 4; quadrant++) {
                        regionSize = pixelCount[quadrant];
                        if (regionSize > 0) {
                            double meanLuminance = luminanceSum[quadrant] / regionSize;
                            double variance = luminanceSqrSum[quadrant] / regionSize - meanLuminance * meanLuminance;
                            double standardDeviation = Math.sqrt(variance);
                            if (standardDeviation < minStandardDeviation) {
                                minStandardDeviation = standardDeviation;
                                selectedQuadrant = quadrant;
                            }
                        }
                    }

                    int offsetX = selectedQuadrant % 2 == 0 ? 0 : halfWindowSize;
                    int offsetY = selectedQuadrant < 2 ? 0 : halfWindowSize;
                    int outputX = x - halfWindowSize + offsetX;
                    int outputY = y - halfWindowSize + offsetY;

                    if (outputX >= 0 && outputX < image.getWidth() && outputY >= 0 && outputY < image.getHeight()) {
                        int outputColor = image.data[outputX][outputY];
                        out[x][y] = outputColor;
                    }
                }
            }
            return image.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "applies a Kuwahara filter to each input, which provides smoothing and blurs edges and results in a look like a brush stroke";
    }
}
