package xaridar.ops;

import xaridar.*;

import java.util.*;
import java.util.stream.Collectors;

public class OpUtil {
    public static Map<String, int[]> predefColors = new HashMap<>();

    static {
        predefColors.put("red", new int[]{255, 0, 0, 255});
        predefColors.put("orange", new int[]{255, 103, 2, 255});
        predefColors.put("yellow", new int[]{255, 255, 0, 255});
        predefColors.put("green", new int[]{0, 255, 0, 255});
        predefColors.put("cyan", new int[]{0, 255, 255, 255});
        predefColors.put("blue", new int[]{0, 0, 255, 255});
        predefColors.put("violet", new int[]{128, 0, 128, 255});
        predefColors.put("magenta", new int[]{255, 0, 255, 255});
        predefColors.put("black", new int[]{0, 0, 0, 255});
        predefColors.put("gray", new int[]{128, 128, 128, 255});
        predefColors.put("grey", new int[]{128, 128, 128, 255});
        predefColors.put("brown", new int[]{150, 75, 0, 255});
        predefColors.put("white", new int[]{255, 255, 255, 255});
        predefColors.put("transparent", new int[]{0, 0, 0, 0});
    }

    public static List<FileInfo> multiprocess(List<FileInfo> images, ThrowFunction<FileInfo, FileInfo> fn) throws InterruptedException {
        List<FileInfo> ret = new ArrayList<>();
        Map<Thread, ImageRunnable<FileInfo>> threads = new HashMap<>();
        for (FileInfo fi : images) {
            ImageRunnable<FileInfo> ir = new ImageRunnable<>(fi, fn);
            Thread t = new Thread(ir);
            t.start();
            threads.put(t, ir);
        }
        for (Map.Entry<Thread, ImageRunnable<FileInfo>> thread : threads.entrySet()) {
            thread.getKey().join();
            ret.add(thread.getValue().getValue());
        }
        return ret;
    }
    public static List<FileInfo> multiprocessIndex(List<FileInfo> images, ThrowFunction<Integer, FileInfo> fn) throws InterruptedException {
        List<FileInfo> ret = new ArrayList<>();
        Map<Thread, ImageRunnable<Integer>> threads = new HashMap<>();
        for (int i = 0; i < images.size(); i++) {
            ImageRunnable<Integer> ir = new ImageRunnable<>(i, fn);
            Thread t = new Thread(ir);
            t.start();
            threads.put(t, ir);
        }
        for (Map.Entry<Thread, ImageRunnable<Integer>> thread : threads.entrySet()) {
            thread.getKey().join();
            ret.add(thread.getValue().getValue());
        }
        return ret;
    }

    public static int RGBclamp(int i) {
        return clamp(i, 0, 255);
    }

    public static List<FileInfo> kernelOp(List<FileInfo> in, double[][] kernel) throws InterruptedException {
        return kernelOp(in, kernel, false);
    }

    public static List<FileInfo> kernelOp(List<FileInfo> in, double[][] kernel, boolean reNorm) throws InterruptedException {
        return kernelOp(in, kernel, reNorm, KernelEdgeSolution.IGNORE);
    }

    public static List<FileInfo> kernelOp(List<FileInfo> in, double[][] kernel, boolean reNorm, KernelEdgeSolution sol) throws InterruptedException {
        int extent = (kernel.length - 1) / 2;
        return multiprocess(in, img -> {
            int sub = sol == KernelEdgeSolution.CROP ? 2 * extent : 0;
            int[][] out = new int[img.getWidth() - sub][img.getHeight() - sub];
            for (int x = 0; x < out.length; x++) {
                for (int y = 0; y < out[x].length; y++) {
                    PixelColor transformed = OpUtil.kernelSum(img.data, extent, x + sub / 2, y + sub / 2, kernel, reNorm, sol);
                    out[x][y] = transformed.toRGBA();
                }
            }
            return img.withData(out);
        });
    }

    public static PixelColor[] quantize(int[][] data, int colors) {
        List<PixelColor> cols = Arrays.stream(data).flatMapToInt(Arrays::stream).mapToObj(PixelColor::fromRGBA).map(c -> c.withAlpha(255)).collect(Collectors.toList());
        List<List<PixelColor>> buckets = quantizeRecursive(cols, (int) Math.min(colors, cols.stream().map(PixelColor::toRGBA).distinct().count()));
        PixelColor[] palette = new PixelColor[buckets.size()];
        for (int i = 0, bucketsSize = buckets.size(); i < bucketsSize; i++) {
            List<PixelColor> bucket = buckets.get(i);
            int avgR = bucket.stream().map(PixelColor::getRed).reduce(0, Integer::sum) / bucket.size();
            int avgG = bucket.stream().map(PixelColor::getGreen).reduce(0, Integer::sum) / bucket.size();
            int avgB = bucket.stream().map(PixelColor::getBlue).reduce(0, Integer::sum) / bucket.size();
            palette[i] = new PixelColor(avgR, avgG, avgB, 255);
        }
        return palette;
    }

    private static List<List<PixelColor>> quantizeRecursive(List<PixelColor> colors, int bucketsLeft) {
        if (bucketsLeft == 1) return List.of(colors);
        if (colors.size() == 1) return List.of(colors);
        List<List<PixelColor>> ret = new ArrayList<>();
        List<PixelColor> rSorted = colors.stream().sorted(Comparator.comparingInt(PixelColor::getRed)).collect(Collectors.toList());
        List<PixelColor> gSorted = colors.stream().sorted(Comparator.comparingInt(PixelColor::getGreen)).collect(Collectors.toList());
        List<PixelColor> bSorted = colors.stream().sorted(Comparator.comparingInt(PixelColor::getBlue)).collect(Collectors.toList());
        int rRange = Math.abs(rSorted.get(0).getRed() - rSorted.get(rSorted.size() - 1).getRed());
        int gRange = Math.abs(gSorted.get(0).getGreen() - gSorted.get(gSorted.size() - 1).getGreen());
        int bRange = Math.abs(bSorted.get(0).getBlue() - bSorted.get(bSorted.size() - 1).getBlue());
        List<PixelColor> left, right;
        if (rRange >= gRange && rRange >= bRange) {
            left = new ArrayList<>(rSorted.subList(0, rSorted.size() / 2));
            right = new ArrayList<>(rSorted.subList(rSorted.size() / 2, rSorted.size()));
        } else if (gRange >= rRange && gRange >= bRange) {
            left = new ArrayList<>(gSorted.subList(0, gSorted.size() / 2));
            right = new ArrayList<>(gSorted.subList(gSorted.size() / 2, gSorted.size()));
        } else {
            left = new ArrayList<>(bSorted.subList(0, bSorted.size() / 2));
            right = new ArrayList<>(bSorted.subList(bSorted.size() / 2, bSorted.size()));
        }
        if (bucketsLeft <= 2) {
            List<PixelColor> lrSorted = left.stream().sorted(Comparator.comparingInt(PixelColor::getRed)).collect(Collectors.toList());
            List<PixelColor> lgSorted = left.stream().sorted(Comparator.comparingInt(PixelColor::getGreen)).collect(Collectors.toList());
            List<PixelColor> lbSorted = left.stream().sorted(Comparator.comparingInt(PixelColor::getBlue)).collect(Collectors.toList());
            int lrRange = Math.abs(lrSorted.get(0).getRed() - lrSorted.get(lrSorted.size() - 1).getRed());
            int lgRange = Math.abs(lgSorted.get(0).getGreen() - lgSorted.get(lgSorted.size() - 1).getGreen());
            int lbRange = Math.abs(lbSorted.get(0).getBlue() - lbSorted.get(lbSorted.size() - 1).getBlue());
            int maxLRange = Math.max(lrRange, Math.max(lgRange, lbRange));
            List<PixelColor> rrSorted = right.stream().sorted(Comparator.comparingInt(PixelColor::getRed)).collect(Collectors.toList());
            List<PixelColor> rgSorted = right.stream().sorted(Comparator.comparingInt(PixelColor::getGreen)).collect(Collectors.toList());
            List<PixelColor> rbSorted = right.stream().sorted(Comparator.comparingInt(PixelColor::getBlue)).collect(Collectors.toList());
            int rrRange = Math.abs(rrSorted.get(0).getRed() - rrSorted.get(rrSorted.size() - 1).getRed());
            int rgRange = Math.abs(rgSorted.get(0).getGreen() - rgSorted.get(rgSorted.size() - 1).getGreen());
            int rbRange = Math.abs(rbSorted.get(0).getBlue() - rbSorted.get(rbSorted.size() - 1).getBlue());
            int maxRRange = Math.max(rrRange, Math.max(rgRange, rbRange));
            if (maxLRange > maxRRange) {
                ret.addAll(quantizeRecursive(left, bucketsLeft - 1));
                ret.add(right);
            } else {
                ret.add(left);
                ret.addAll(quantizeRecursive(right, bucketsLeft - 1));
            }
        } else {
            ret.addAll(quantizeRecursive(left, bucketsLeft / 2));
            ret.addAll(quantizeRecursive(right, bucketsLeft / 2));
        }
        return ret;
    }

    enum KernelEdgeSolution {
        CROP("crop-edge"),
        ZERO("replace-zero"),
        IGNORE("ignore-pixel"),
        CENTER("repeat-center"),
        WRAP("wrap-color"),
        BLACK("black-border"),
        OUTER_PIXELS("extend-image");

        private String val;

        KernelEdgeSolution(String val) {
            this.val = val;
        }

        public static KernelEdgeSolution getEdgeSolution(String v) {
            return Arrays.stream(KernelEdgeSolution.values()).filter(s -> s.val.equals(v)).collect(Collectors.toList()).get(0);
        }

        public static List<String> getSolsStrs() {
            return Arrays.stream(KernelEdgeSolution.values()).map(s -> s.val).collect(Collectors.toList());
        }
    }

    public static PixelColor kernelSum(int[][] array, int extent, int xCenter, int yCenter, double[][] kernel) {
        return kernelSum(array, extent, xCenter, yCenter, kernel, false);
    }
    public static PixelColor kernelSum(int[][] array, int extent, int xCenter, int yCenter, double[][] kernel, boolean reNorm) {
        return kernelSum(array, extent, xCenter, yCenter, kernel, reNorm, KernelEdgeSolution.IGNORE);
    }
    public static PixelColor kernelSum(int[][] array, int extent, int xCenter, int yCenter, double[][] kernel, boolean reNorm, KernelEdgeSolution sol) {
        double[] weighedSum = new double[]{0,0,0,0};
        double newSum = 0;
        double origSum = 0;
        for (int x = -1 * extent; x <= extent; x++) {
            for (int y = -1 * extent; y <= extent; y++) {
                int d;
                double mod = kernel[x + extent][y + extent];
                origSum += mod;
                if (clamp(xCenter + x, 0, array.length - 1) != xCenter + x || clamp(yCenter + y, 0, array[0].length - 1) != yCenter + y) {
                    switch (sol) {
                        case IGNORE:
                            continue;
                        case CROP:
                        case BLACK:
                            return PixelColor.transparent();
                        case ZERO:
                            d = 0;
                            break;
                        case CENTER:
                            d = array[xCenter][yCenter];
                            break;
                        case WRAP:
                            d = array[(array.length + xCenter + x) % array.length][(array[0].length + yCenter + y) % array[0].length];
                            break;
                        case OUTER_PIXELS:
                            d = array[clamp(xCenter + x, 0, array.length - 1)][clamp(yCenter + y, 0, array[0].length - 1)];
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + sol);
                    }
                } else {
                    d = array[xCenter + x][yCenter + y];
                }
                PixelColor c = PixelColor.fromRGBA(d);
                newSum += mod;
                weighedSum[0] += c.getRed() * mod;
                weighedSum[1] += c.getGreen() * mod;
                weighedSum[2] += c.getBlue() * mod;
                weighedSum[3] += c.getAlpha() * mod;
            }
        }
        double normFactor = reNorm ? origSum / newSum : 1;
        return new PixelColor((int) weighedSum[0], (int) weighedSum[1], (int) weighedSum[2], (int) weighedSum[3]).withOp(i -> (int) (i * normFactor), true);
    }

    public static double gaussian(int x, int y, double s) {
        return Math.exp(-1 * (Math.pow(x, 2) + Math.pow(y, 2)) / (2 * (Math.pow(s, 2)))) / (2 * Math.PI * (Math.pow(s, 2)));
    }

    public static int clamp(int d, int min, int max) {
        return Math.max(min, Math.min(max, d));
    }

    public static Map<String, OutFileVarExtractor> templates = Map.of(
            "fname", (fi, i) -> fi.fileName,
            "i", (fi, i) -> String.valueOf(i + 1),
            "_ops", (fi, i) -> String.join("_", fi.ops),
            "-ops", (fi, i) -> String.join("-", fi.ops),
            "lastOp", (fi, i) -> fi.ops.get(fi.ops.size() - 1)
    );
}
