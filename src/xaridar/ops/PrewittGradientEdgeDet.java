package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.util.List;

public class PrewittGradientEdgeDet implements ImageOperation {
    @Override
    public String getName() {
        return "prewitt";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList();
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        double[][] lKernel = new double[][]{{1,0,-1},{1,0,-1},{1,0,-1}};
        double[][] rKernel = new double[][]{{-1,0,1},{-1,0,1},{-1,0,1}};
        double[][] tKernel = new double[][]{{1,1,1},{0,0,0},{-1,-1,-1}};
        double[][] bKernel = new double[][]{{-1,-1,-1},{0,0,0},{1,1,1}};
        return OpUtil.multiprocess(images, img -> {
            int[][] out = new int[img.getWidth()][img.getHeight()];
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    PixelColor transformedL = OpUtil.kernelSum(img.data, 1, x, y, lKernel);
                    PixelColor transformedR = OpUtil.kernelSum(img.data, 1, x, y, rKernel);
                    PixelColor transformedT = OpUtil.kernelSum(img.data, 1, x, y, tKernel);
                    PixelColor transformedB = OpUtil.kernelSum(img.data, 1, x, y, bKernel);
                    int rL = transformedL.getRed();
                    int gL = transformedL.getGreen();
                    int bL = transformedL.getBlue();

                    int rR = transformedR.getRed();
                    int gR = transformedR.getGreen();
                    int bR = transformedR.getBlue();

                    int rT = transformedT.getRed();
                    int gT = transformedT.getGreen();
                    int bT = transformedT.getBlue();

                    int rB = transformedB.getRed();
                    int gB = transformedB.getGreen();
                    int bB = transformedB.getBlue();

                    int xr = (int) Math.sqrt(rL * rL + rR * rR);
                    int xg = (int) Math.sqrt(gL * gL + gR * gR);
                    int xb = (int) Math.sqrt(bL * bL + bR * bR);

                    int yr = (int) Math.sqrt(rT * rT + rB * rB);
                    int yg = (int) Math.sqrt(gT * gT + gB * gB);
                    int yb = (int) Math.sqrt(bT * bT + bB * bB);

                    int r = (int) Math.sqrt(xr * xr + yr * yr);
                    int g = (int) Math.sqrt(xg * xg + yg * yg);
                    int b = (int) Math.sqrt(xb * xb + yb * yb);
                    out[x][y] = new PixelColor(r,g,b, 255).toRGBA();
                }
            }
            return img.withData(out);
        });
    }

    @Override
    public String getDescr() {
        return "runs the Prewitt Gradient edge detection algorithm for determining edges";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EDGE_DET;
    }
}
