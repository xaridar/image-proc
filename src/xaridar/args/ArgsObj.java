package xaridar.args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ArgsObj extends HashMap<String, Object> {
    public boolean exists(String arg) {
        return containsKey(arg) && get(arg) != null;
    }

    public ArgsObj withFileArgs(String[] args) {
        put("files", args);
        return this;
    }
}
