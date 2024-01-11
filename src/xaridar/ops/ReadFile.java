package xaridar.ops;

import xaridar.FileInfo;
import xaridar.args.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ReadFile implements ImageOperation {
    @Override
    public String getName() {
        return "read";
    }

    @Override
    public ParamsList getParams() {
        return new ParamsList(new UnnamedArgsInfo("files", new FileParam(), 1, true, "any number of relative filepaths to read images from"));
    }

    @Override
    public List<FileInfo> process(List<FileInfo> images, ArgsObj args) throws ArgError, IOException {
        List<String> filePaths = (List<String>) args.get("files");
        List<FileInfo> out = new ArrayList<>();
        if (filePaths.size() == 1) {
            String[] d = filePaths.get(0).split("[/\\\\]");
            List<String> dl = List.of(d);
            DirectoryStream<Path> paths = Files.newDirectoryStream(Path.of(System.getProperty("user.dir"), dl.subList(0, dl.size() - 1).toArray(String[]::new)), d[d.length - 1]);
            Iterator<Path> it = paths.iterator();
            if (!it.hasNext()) throw new ArgError("No files found matching: " + filePaths.get(0));
            while (it.hasNext()) {
                Path value = it.next();
                String path = value.toAbsolutePath().normalize().toString();
                BufferedImage tmp = ImageIO.read(new File(path));
                BufferedImage bi = new BufferedImage(tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB);
                bi.getGraphics().drawImage(tmp, 0, 0, null);
                String[] dirs = path.split("[\\\\/]");
                String file = dirs[dirs.length - 1];
                String[] parts = file.split("\\.");
                String name = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
                String ext = parts[parts.length - 1];
                int width = bi.getWidth();
                int height = bi.getHeight();
                int[][] data = new int[width][height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[x][y] = bi.getRGB(x, y);
                    }
                }
                FileInfo fi = new FileInfo(data, ext, name);
                out.add(fi);
            }

        } else {
            for (String path : filePaths) {
                System.out.println(Path.of(System.getProperty("user.dir"), path).toFile());
                BufferedImage tmp = ImageIO.read(Path.of(System.getProperty("user.dir"), path).toFile());
                BufferedImage bi = new BufferedImage(tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB);
                bi.getGraphics().drawImage(tmp, 0, 0, null);
                String[] parts = path.split("\\.");
                String name = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
                String ext = parts[parts.length - 1];
                int width = bi.getWidth();
                int height = bi.getHeight();
                int[][] data = new int[width][height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[x][y] = bi.getRGB(x, y);
                    }
                }
                FileInfo fi = new FileInfo(data, ext, name);
                out.add(fi);
            }
        }
        return out;
    }

    @Override
    public String getDescr() {
        return "generation op (does not take input); reads any number of input files by filepath and converts them into a format accessible to all other operations";
    }

    @Override
    public OperationCategory getCat() {
        return OperationCategory.INPUT;
    }
}
