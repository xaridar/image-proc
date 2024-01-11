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

    enum OperationCategory {
        INPUT("Image Generation"),
        REBAL("Balancing and Touchup"),
        TRANSFORM("Transformations"),
        EFFECTS("Image Effects and Filters"),
        COL_LIM("Color Limiting"),
        EDGE_DET("Edge Detection"),
        NDN("Noise/Denoise"),
        JOIN("Image Merging"),
        OUTPUT("Image Output"),
        MISC("");

        public String title;
        OperationCategory(String title) {
            this.title = title;
        }
    }

    OperationCategory getCat();
}
