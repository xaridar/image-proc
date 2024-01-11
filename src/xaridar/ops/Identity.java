package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.List;

public class Identity implements ImageOperation {
    @Override
    public String getName() {
        return "ident";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList();
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException {
        return images;
    }

    @Override
    public String getDescr() {
        return "returns a single copy of same image";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.MISC;
    }
}
