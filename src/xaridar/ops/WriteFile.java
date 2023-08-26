package xaridar.ops;

import xaridar.FileInfo;
import xaridar.OutFileVarExtractor;
import xaridar.args.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WriteFile implements ImageOperation {
    @Override
    public String getName() {
        return "write";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("files", new StringParam(), 1, true, "either a list of relative paths for output files in the number and order passed to 'write', or a single filepath using placeholders to differentiate files"));
    }

    public static Map<String, Integer> validFTs = Map.of(
            "png", BufferedImage.TYPE_INT_ARGB,
            "jpg", BufferedImage.TYPE_INT_RGB,
            "jpeg", BufferedImage.TYPE_INT_RGB,
            "gif", BufferedImage.TYPE_INT_ARGB,
            "bmp", BufferedImage.TYPE_INT_RGB,
            "tif", BufferedImage.TYPE_INT_ARGB,
            "tiff", BufferedImage.TYPE_INT_ARGB,
            "webp", BufferedImage.TYPE_INT_ARGB,
            "pict", BufferedImage.TYPE_INT_RGB,
            "iff", BufferedImage.TYPE_INT_RGB
    );

    @Override
    public List<FileInfo> process(List<FileInfo> imgs, ArgsObj args) throws ArgError, IOException, InterruptedException {
        List<String> namesL = (List<String>) args.get("files");
        String[] names = namesL.toArray(String[]::new);
        if (names.length == 1 && imgs.size() != 1) {
            if (OpUtil.templates.keySet().stream().noneMatch(template -> names[0].contains("{" + template + "}")) && !names[0].matches(".*\\{\\[((?:\\w+,\\s*)*\\w+)]}.*")) {
                List<String> set = new ArrayList<>(OpUtil.templates.keySet()).stream().map(t -> "{" + t + "}").collect(Collectors.toList());
                throw new ArgError("Multiple images cannot be output to the same file; use one or more of: "
                        + String.join(", ", set.subList(0, set.size() - 1)) + ", or " + set.get(set.size() - 1)
                        + " in the filename to use wildcards (or define an output array)");
            }
        } else if (imgs.size() != names.length) {
            throw new ArgError("Incorrect number of output paths provided; expected " + imgs.size() + ", received " + names.length);
        }
        List<List<String>> nameVars = new ArrayList<>();
        if (names.length != imgs.size()) {
            Matcher m = Pattern.compile("\\{\\[((?:\\w+,\\s*)*\\w+)]}").matcher(names[0]);
            while (m.find()) {
                String list = m.group(1);
                List<String> strs = Arrays.stream(list.split(",")).map(String::trim).collect(Collectors.toList());
                if (strs.size() != imgs.size()) throw new ArgError("Variable templates must be the same length as the number of files to be output");
                nameVars.add(strs);
            }
        }
        for (int i = 0; i < imgs.size(); i++) {
            FileInfo fi = imgs.get(i);
            int outWidth = fi.getWidth();
            int outHeight = fi.getHeight();
            String realFilename = names.length == imgs.size() ? names[i] : names[0];
            for (Map.Entry<String, OutFileVarExtractor> e : OpUtil.templates.entrySet()) {
                if (realFilename.contains(e.getKey())) {
                    if (e.getKey().equals("fname") && fi.fileName == null)
                        throw new ArgError("Output #" + (i + 1) + " does not have a filename, so fname cannot be used as a parameter to the output name");
                    realFilename = realFilename.replace("{" + e.getKey() + "}", e.getValue().extract(fi, i));
                }
            }
            if (names.length != imgs.size()) {
                Matcher m = Pattern.compile("\\{\\[(?:\\w+,\\s*)*\\w+]}").matcher(realFilename);
                for (int n = 0; m.find(); n++) {
                    realFilename = realFilename.replace(m.group(), nameVars.get(n).get(i));
                }
            }
            String[] dirs = realFilename.split("[\\\\/]");
            String file = dirs[dirs.length - 1];
            String[] parts = file.split("\\.");
            String ext = parts[parts.length - 1];
            if (!validFTs.containsKey(ext)) {
                realFilename += "." + fi.filetype;
                ext = fi.filetype;
            }
            File outFile = new File(System.getProperty("user.dir") + "/" + realFilename);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            BufferedImage out = new BufferedImage(outWidth, outHeight, validFTs.get(ext));
            for (int y = 0; y < outHeight; y++) {
                for (int x = 0; x < outWidth; x++) {
                    out.setRGB(x, y, fi.data[x][y]);
                }
            }
            ImageIO.write(out, ext, outFile);
        }
        return imgs;
    }

    @Override
    public String getDescr() {
        return "writes all input files to either separately designated filepaths or a single variable one using placeholders for index, type, etc\n" +
                "output will default to each input's filetype, but can be specified as one of: png, jpg, jpeg, gif, bmp, tif, tiff, webp, pict, iff";
    }
}
