package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Sharpen implements ImageOperation {
    @Override
    public String getName() {
        return "sharpen";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("sharpen-radius", "r", List.of(new NumberParam(1, true)), "the radius of the box to use to sharpen each pixel"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int extent = ((int) args.get("sharpen-radius"));
        int k = (extent + 1) * 2;
        double[][] kernel = Arrays.stream(new double[k][k]).map(r -> Arrays.stream(r).map(d -> -1f / (k * k)).toArray()).toArray(double[][]::new);
        kernel[extent][extent] += 2;
        return OpUtil.kernelOp(images, kernel);
    }

    @Override
    public String getDescr() {
        return "applies a sharpening filter to every input, using a chosen radius to do so";
    }
}
