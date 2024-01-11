package xaridar.ops;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import xaridar.ops.ImageOperation.OperationCategory;

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

    public static List<ImageOperation> getGenOps() {
        return alphaOps().stream().filter(op -> op.getCat() == OperationCategory.INPUT).collect(Collectors.toList());
    }

    public static Map<String, List<ImageOperation>> getCats() {
        Map<String, List<ImageOperation>> cats = new HashMap<>();
        alphaOps().stream().filter(op -> op.getCat() != ImageOperation.OperationCategory.MISC).forEach(op -> {
            if (!cats.containsKey(op.getCat().title)) cats.put(op.getCat().title, new ArrayList<>());
            cats.get(op.getCat().title).add(op);
        });
        return cats;
    }
}
