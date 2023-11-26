package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NonLocalMeans implements ImageOperation {
    @Override
    public String getName() {
        return "non-local-means";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new ParamsList.Param("kernel-size", "k", List.of(new OddNumberParam(3)), "the side length of the kernel to use; higher value = more denoising (but even slower)"),
                new ParamsList.Param("sigma", "s", List.of(new NumberParam(0)), List.of(0.5), "the sigma value to use")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int k = (int) args.get("kernel-size");
        double s = (double) args.get("sigma");
        return OpUtil.multiprocess(images, image -> {
            int[][] out = new int[image.getWidth()][image.getHeight()];
            Thread[] threads = new Thread[image.getWidth()];
            for (int x = 0; x < image.getWidth(); x++) {
                int finalX = x;
                Thread t = new Thread(() -> {
                    for (int y = 0; y < image.getHeight(); y++) {
                        double[] weights = new double[4];
                        double[] weighted = new double[4];
                        for (int localY = 0; localY < image.getHeight(); localY++) {
                            for (int localX = 0; localX < image.getWidth(); localX++) {
                                double[] w = gaussianWeight(new int[]{finalX, y}, new int[]{localX, localY}, image.data, k, s);
                                for (int i = 0; i < 4; i++) {
                                    weights[i] += w[i];
                                    weighted[i] += w[i] * PixelColor.fromRGBA(image.data[localX][localY]).asArr()[i];
                                }
                            }
                        }
                        int[] arrOut = new int[4];
                        for (int i = 0; i < 4; i++) {
                            arrOut[i] = (int) (weighted[i] / weights[i]);
                        }
                        out[finalX][y] = new PixelColor(arrOut).toRGBA();
                    }
                });
                threads[x] = t;
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }
            return image.withData(out);
        });
    }

    private double[] gaussianWeight(int[] p, int[] q, int[][] data, int k, double s) {
        double[][] kernel = new double[k][k];
        double kernelSum = 0.0;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int xDist = i - k / 2;
                int yDist = j - k / 2;
                kernel[i][j] = Math.exp(-(xDist * xDist + yDist * yDist) / (2 * s * s));
                kernelSum += kernel[i][j];
            }
        }
// Normalize the kernel
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                kernel[i][j] /= kernelSum;
            }
        }
        PixelColor qColor = OpUtil.kernelSum(data, k / 2, q[0], q[1], kernel);
        PixelColor pColor = OpUtil.kernelSum(data, k / 2, p[0], p[1], kernel);
        double r = Math.exp(-Math.abs(Math.pow(qColor.getRed(), 2) - Math.pow(pColor.getRed(), 2)) / (s * s));
        double g = Math.exp(-Math.abs(Math.pow(qColor.getGreen(), 2) - Math.pow(pColor.getGreen(), 2)) / (s * s));
        double b = Math.exp(-Math.abs(Math.pow(qColor.getBlue(), 2) - Math.pow(pColor.getBlue(), 2)) / (s * s));
        double a = Math.exp(-Math.abs(Math.pow(qColor.getAlpha(), 2) - Math.pow(pColor.getAlpha(), 2)) / (s * s));
        return new double[]{r, g, b, a};
    }

    @Override
    public String getDescr() {
        return "applies a non-local means denoising algorithm to inputs by using a gaussian kernel; this algorithm is SLOW so be warned";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.NDN;
    }
}
