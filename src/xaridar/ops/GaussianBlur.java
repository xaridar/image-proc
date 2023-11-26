package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GaussianBlur implements ImageOperation {
    @Override
    public String getName() {
        return "gaussianblur";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new ParamsList.Param("kernel-size", "k", List.of(new OddNumberParam(3)), "the side length of the kernel to use; higher value = more blurred"),
                new ParamsList.Param("sigma", "s", List.of(new NumberParam(0)), List.of(0.5), "the sigma value to use; higher value = more blurred"),
                new ParamsList.Param("edge-solution", "e", List.of(new EnumParam(OpUtil.KernelEdgeSolution.getSolsStrs())), List.of("ignore-pixel"), "one of a list of solutions for dealing with edge pixels:\n" +
                        "\t\tcrop the edges (crop-edge)\n" +
                        "\t\tuse 0 for missing pixels (replace-zero)\n" +
                        "\t\tignore nonexistent pixels (ignore-pixel)\n" +
                        "\t\trepeat calculated pixels (repeat-center)\n" +
                        "\t\twrap edges (wrap-color)\n" +
                        "\t\tadd a black border (black-border)\n" +
                        "\t\tcontinue edges (extend-image)")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        int k = (int) args.get("kernel-size");
        double s = (double) args.get("sigma");
        String e = (String) args.get("edge-solution");
        if (s == 0) return images.stream().map(FileInfo::copy).collect(Collectors.toList());
        double[][] kernel = convArray(k, s);
        return OpUtil.kernelOp(images, kernel, true, OpUtil.KernelEdgeSolution.getEdgeSolution(e));
    }

    private double[][] convArray(int k, double s) {
        double[][] vals = new double[k][k];
        int extent = (k - 1) / 2;
        for (int x = -1 * extent; x <= extent; x++) {
            for (int y = -1 * extent; y <= extent; y++) {
                vals[x + extent][y + extent] = OpUtil.gaussian(x, y, s);
            }
        }
        return vals;
    }

    @Override
    public String getDescr() {
        return "applies a gaussian blur effect to every input";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EFFECTS;
    }
}
