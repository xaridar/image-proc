package xaridar.ops;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperationManager {
    public static List<ImageOperation> ops = List.of(
            new Add(),
            new Average(),
            new Border(),
            new BoxBlur(),
            new Brightness(),
            new ChannelFilter(),
            new ColorLimit(),
            new Collate(),
            new ColorFilter(),
            new Contrast(),
            new CropEdge(),
            new CropToFit(),
            new Cutout(),
            new Details(),
            new Dither(),
            new FilterOut(),
            new Flip(),
            new GaussianBlur(),
            new Grayscale(),
            new HistogramEqualization(),
            new Hue(),
            new Iconify(),
            new Identity(),
            new Invert(),
            new Kuwahara(),
            new Mask(),
            new NonLocalMeans(),
            new PerlinNoise(),
            new PrewittGradientEdgeDet(),
            new Quantize(),
            new ReadFile(),
            new Repeat(),
            new Rotate(),
            new SaltAndPepper(),
            new Saturation(),
            new Scale(),
            new Sharpen(),
            new Shear(),
            new SobelEdgeDet(),
            new Solid(),
            new Threshold(),
            new UnsharpMask(),
            new Vignette(),
            new WriteFile()
    );

    public static ImageOperation findOp(String name) {
        for (ImageOperation op : ops) {
            if (name.equals(op.getName())) return op;
        }
        return null;
    }

    public static List<ImageOperation> alphaOps() {
        List<ImageOperation> temp = new ArrayList<>(ops);
        temp.sort((l, r) -> l.getName().compareToIgnoreCase(r.getName()));
        return temp;
    }
}
