package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Repeat implements ImageOperation {
    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("repeats", new NumberParam(2, true), 1, "the number of times to repeat each input")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        int repeats = (int) args.get("repeats");
        List<FileInfo> out = new ArrayList<>();
        OpUtil.multiprocess(images, fi -> {
            for (int n = 0; n < repeats; n++) {
                out.add(fi.copy());
            }
            return null;
        });
        return out;
    }

    @Override
    public String getDescr() {
        return "returns a specified number of copies of each input";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.NONE;
    }
}
