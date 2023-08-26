package xaridar.ops;

import net.sf.image4j.codec.ico.ICOEncoder;
import xaridar.FileInfo;
import xaridar.OutFileVarExtractor;
import xaridar.args.*;

import de.pentabyte.imageio.icns.ICNS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Iconify implements ImageOperation {
    @Override
    public String getName() {
        return "iconify";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("files", new StringParam(), 1, true, "list of relative output file names, with template names allowed if one name is provided; no filename should be included"),
                List.of(
                        new ParamsList.Param("ico", "", Collections.emptyList(), true, "flag for producing Windows-compatible ICO files"),
                        new ParamsList.Param("icns", "", Collections.emptyList(), true, "flag for producing MacOS-compatible ICNS files"),
                        new ParamsList.Param("icon-size", "s", List.of(new EnumParam("32", "64", "256", "512", "1024")), List.of("256"), "a granular icon size for output files (default 256x256)")
                ),
                Map.of(new String[]{"ico", "icns"}, 1)
        );
    }

    @Override
    public List<FileInfo> process(List<FileInfo> imgs, ArgsObj args) throws ArgError, IOException, InterruptedException {
        List<String> namesL = (List<String>) args.get("files");
        String[] names = namesL.toArray(String[]::new);
        boolean ico = args.exists("ico");
        boolean icns = args.exists("icns");
        int size = Integer.parseInt((String) args.get("icon-size"));
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
            File outFile = new File(System.getProperty("user.dir") + "/" + realFilename);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            BufferedImage out = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < outHeight; y++) {
                for (int x = 0; x < outWidth; x++) {
                    out.setRGB(x, y, fi.data[x][y]);
                }
            }
            Image scaledImg = out.getScaledInstance(size, size, 0);
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(scaledImg, 0, 0, null);
            if (ico) ICOEncoder.write(scaled, new File(System.getProperty("user.dir") + "/" + realFilename + ".ico"));

            if (icns) {
                ImageIO.write(scaled, ICNS.NAME, new File(System.getProperty("user.dir") + "/" + realFilename + "." + ICNS.FILE_EXTENSION));
            }
        }
        return imgs;
    }

    @Override
    public String getDescr() {
        return "outputs icon files for each input file according to literal or parameterized file names (these can be ICO and/or ICNS files)";
    }
}
