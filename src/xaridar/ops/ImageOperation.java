package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.ArgError;
import xaridar.args.ArgsObj;
import xaridar.args.ParamsList;

import java.io.IOException;
import java.util.List;

public interface ImageOperation {
    String getName();

    ParamsList getParams();

    List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException, InterruptedException;

    String getDescr();

    default boolean inputNeeded() {
        return true;
    }
}
