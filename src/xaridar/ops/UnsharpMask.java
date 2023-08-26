package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UnsharpMask implements ImageOperation {
    @Override
    public String getName() {
        return "unsharp";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("multiplier", new NumberParam(true), 1, "a multiplier to be used for the Unsharp Mask; a higher multiplier results in a sharper output"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int m = ((int) args.get("multiplier"));
        double[][] kernel = new double[][]{{0,-1,0},{-1,m,-1},{0,-1,0}};
        return OpUtil.kernelOp(images, kernel);
    }

    @Override
    public String getDescr() {
        return "applies an Unsharp Mask to inputs, increasing image contrast along edges";
    }
}
