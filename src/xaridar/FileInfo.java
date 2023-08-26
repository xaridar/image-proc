package xaridar;

import java.util.ArrayList;
import java.util.List;

public class FileInfo {
    public int[][] data;
    public String fileName;
    public String filetype;
    public List<String> ops = new ArrayList<>();

    public FileInfo(int[][] data, String filetype, String fileName) {
        this.data = data;
        this.filetype = filetype;
        this.fileName = fileName;
    }

    public FileInfo(int[][] data, String filetype, String fileName, List<String> ops) {
        this(data, filetype, fileName);
        this.ops = new ArrayList<>(ops);
    }

    public void addOp(String op) {
        ops.add(op);
    }

    public FileInfo withData(int[][] data) {
        return new FileInfo(data, filetype, fileName, ops);
    }

    public FileInfo copy() {
        return new FileInfo(data, filetype, fileName, ops);
    }

    public int getWidth() {
        return data.length;
    }

    public int getHeight() {
        return data[0].length;
    }
}
