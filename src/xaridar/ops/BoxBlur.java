package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.util.Arrays;
import java.util.List;

public class BoxBlur implements ImageOperation {
    @Override
    public String getName() {
        return "boxblur";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("blur-radius", "r", List.of(new NumberParam(1, true)), "radius of the convolution kernel used for the blur"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws InterruptedException {
        int e = ((int) args.get("blur-radius"));
        int k = 2 * (e + 1);
        double[][] kernel = Arrays.stream(new double[k][k]).map(r -> Arrays.stream(r).map(d -> 1f / (k * k)).toArray()).toArray(double[][]::new);
        List<FileInfo> out = OpUtil.kernelOp(images, kernel);
        return out;
    }

    @Override
    public String getDescr() {
        return "a box blur, or linear blur, which linearly blurs each pixel with its surrounding pixels";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EFFECTS;
    }
}
