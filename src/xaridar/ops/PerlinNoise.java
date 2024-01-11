package xaridar.ops;

import xaridar.FileInfo;
import xaridar.PixelColor;
import xaridar.args.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerlinNoise implements ImageOperation {
    @Override
    public String getName() {
        return "perlin";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(
                new UnnamedArgsInfo("size", new NumberParam(1, true), 2, "width and height of the desired results"),
                new ParamsList.Param("instances", "i", List.of(new NumberParam(1, true)), List.of(1), "the number of perlin noise instances to be generated"),
                new ParamsList.Param("seed", "s", List.of(new NumberParam()), true, "optional seed to guarantee consistency between calls")
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException {
        List<Integer> dims = (List<Integer>) args.get("size");
        Double seed = null;
        if (args.exists("seed")) seed = (double) args.get("seed");
        int i = (int) args.get("instances");
        List<FileInfo> fis = new ArrayList<>();
        for (int n = 0; n < i; n++) {
            PerlinNoiseGenerator gen;
            if (seed == null) gen = new PerlinNoiseGenerator();
            else gen = new PerlinNoiseGenerator(seed);
            int[][] data = new int[dims.get(0)][dims.get(1)];
            for (int y = 0; y < data[0].length; y++) {
                for (int x = 0; x < data.length; x++) {
                    data[x][y] = PixelColor.gray(gen.noise(x + 1, y + 1)).toRGBA();
                }
            }
            fis.add(new FileInfo(data, "png", null));
        }
        return fis;
    }

    @Override
    public String getDescr() {
        return "generation op (does not take input); generates perlin noise, which results in grayscale random noise that is more gradated and smooth than complete randomness";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.INPUT;
    }
}
