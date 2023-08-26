package xaridar;

import xaridar.ops.OpUtil;

import java.util.function.Function;

public class PixelColor {
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public PixelColor(int r, int g, int b, int a) {
        this.r = OpUtil.RGBclamp(r);
        this.g = OpUtil.RGBclamp(g);
        this.b = OpUtil.RGBclamp(b);
        this.a = OpUtil.RGBclamp(a);
    }

    public PixelColor(int[] rgb, int a) {
        this(rgb[0], rgb[1], rgb[2], a);
    }

    public PixelColor(double[] rgb, double a) {
        this((int) rgb[0], (int) rgb[1], (int) rgb[2], (int) a);
    }

    public PixelColor(int[] rgba) {
        this(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public PixelColor(Integer[] rgb, Integer a) {
        this(rgb[0], rgb[1], rgb[2], a);
    }

    public PixelColor(Integer[] rgba) {
        this(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public PixelColor(double[] rgba) {
        this((int) rgba[0], (int) rgba[1], (int) rgba[2], (int) rgba[3]);
    }

    public static PixelColor fromRGBA(int color) {
        int a = (int) ((color & 0xff000000L) >> 24);
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0xff00) >> 8;
        int b = color & 0xff;
        return new PixelColor(r, g, b, a);
    }

    public static PixelColor fromHex(String hex) {
        String hexStr = hex;
        if (hexStr.startsWith("#")) hexStr = hexStr.substring(1);
        int RGB = Integer.parseInt(hexStr.substring(0, 6), 16);
        if (hexStr.length() == 6) return PixelColor.fromRGBA(RGB).withAlpha(255);
        return PixelColor.fromRGBA(RGB).withAlpha(Integer.parseInt(hex.substring(6), 16));
    }

    public static PixelColor gray(double percentage) {
        return gray(percentage, 255);
    }

    public static PixelColor gray(double percentage, int alpha) {
        int rescaled = (int) (percentage * 255);
        return new PixelColor(rescaled, rescaled, rescaled, alpha);
    }

    public static PixelColor hue(PixelColor hue, double percentage, int alpha) {
        int rescaledR = (int) (percentage * hue.getRed());
        int rescaledG = (int) (percentage * hue.getGreen());
        int rescaledB = (int) (percentage * hue.getBlue());
        return new PixelColor(rescaledR, rescaledG, rescaledB, alpha);
    }

    public static PixelColor black() {
        return new PixelColor(0, 0, 0, 255);
    }

    public static PixelColor white() {
        return new PixelColor(255, 255, 255, 255);
    }

    public static PixelColor transparent() {
        return new PixelColor(0, 0, 0, 0);
    }

    public int toRGBA() {
        int ret = 0;
        ret += a << 24;
        ret += r << 16;
        ret += g << 8;
        ret += b;
        return ret;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getAlpha() {
        return a;
    }

    public PixelColor withRed(int r) {
        return new PixelColor(r, g, b, a);
    }

    public PixelColor withGreen(int g) {
        return new PixelColor(r, g, b, a);
    }

    public PixelColor withBlue(int b) {
        return new PixelColor(r, g, b, a);
    }

    public PixelColor withAlpha(int a) {
        return new PixelColor(r, g, b, a);
    }

    public double getRedP() {
        return r / 255f;
    }

    public double getGreenP() {
        return g / 255f;
    }

    public double getBlueP() {
        return b / 255f;
    }

    public double getAlphaP() {
        return a / 255f;
    }

    public PixelColor scaled(double r, double g, double b, double a) {
        return new PixelColor((int) (this.r * r), (int) (this.b * b), (int) (this.g * g), (int) (this.a * a));
    }

    public PixelColor scaled(double r, double g, double b) {
        return scaled(r, g, b, 1);
    }

    public PixelColor scaleR(double r) {
        return scaled(r, 1, 1, 1);
    }

    public PixelColor scaleG(double g) {
        return scaled(1, g, 1, 1);
    }

    public PixelColor scaleB(double b) {
        return scaled(1, 1, b, 1);
    }

    public PixelColor scaleA(double a) {
        return scaled(1, 1, 1, a);
    }

    public double luminance() {
        double linearRed = getRedP();
        double linearGreen = getGreenP();
        double linearBlue = getBlueP();
        return linearRed * 0.2126 + linearGreen * 0.7152 + linearBlue * 0.0722;
    }

    public boolean equals(Object other) {
        return other instanceof PixelColor && r == ((PixelColor) other).r && g == ((PixelColor) other).g && b == ((PixelColor) other).b && a == ((PixelColor) other).a;
    }

    public PixelColor withOp(Function<Integer, Integer> op, boolean withAlpha) {
        return new PixelColor(
                Math.max(0, Math.min(op.apply(r), 255)),
                Math.max(0, Math.min(op.apply(g), 255)),
                Math.max(0, Math.min(op.apply(b), 255)),
                withAlpha ? Math.max(0, Math.min(op.apply(a), 255)) : a
        );
    }

    public int[] asArr() {
        return new int[]{r,g,b,a};
    }

    @Override
    public String toString() {
        return "PixelColor{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }

    public PixelColor closestColor(PixelColor[] palette) {
        double minDist = Double.POSITIVE_INFINITY;
        PixelColor closest = null;
        for (PixelColor col : palette) {
            double dist = dist(col);
            if (dist < minDist) {
                minDist = dist;
                closest = col;
            }
        }
        return closest;
    }

    public double dist(PixelColor other) {
        return Math.sqrt(
                Math.pow(r - other.r, 2) +
                        Math.pow(g - other.g, 2) +
                        Math.pow(b - other.b, 2)
        );
    }

    public double distDir(PixelColor other) {
        return (r - other.r) + (g - other.g) + (b - other.b);
    }
}
