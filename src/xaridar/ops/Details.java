package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Details implements ImageOperation {
    @Override
    public String getName() {
        return "details";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new ParamsList.Param("radius", "r", List.of(new NumberParam(1, true)), "the radius of the details filter; larger radius will result in less detail preserved"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int extent = ((int) args.get("radius"));
        int k = (extent + 1) * 2;
        double[][] kernel = Arrays.stream(new double[k][k]).map(r -> Arrays.stream(r).map(d -> -1f / (k * k)).toArray()).toArray(double[][]::new);
        kernel[extent][extent]++;
        return OpUtil.kernelOp(images, kernel);
    }

    @Override
    public String getDescr() {
        return "applies a details filter to obtain just the details of an image";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.EDGE_DET;
    }
}
