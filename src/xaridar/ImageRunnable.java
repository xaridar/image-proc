package xaridar;

import java.util.function.Function;

public class ImageRunnable<T> implements Runnable {

    private T fi;
    private Function<T, FileInfo> fn;
    private FileInfo val;

    public ImageRunnable(T input, Function<T, FileInfo> fn) {
        this.fi = input;
        this.fn = fn;
    }
    @Override
    public void run() {
        val = fn.apply(fi);
    }

    public FileInfo getValue() {
        return val;
    }
}
