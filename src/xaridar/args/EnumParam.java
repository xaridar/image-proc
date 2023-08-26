package xaridar.args;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumParam implements ParamType<String> {
    private List<String> options;

    public EnumParam(String... options) {
        this.options = Arrays.asList(options);
    }

    public EnumParam(List<String> options) {
        this.options = options;
    }

    @Override
    public String getType() {
        if (options.size() == 2) return String.join(" or ", options);
        return String.join(", ", options.subList(0, options.size() - 1)) + ", or " + options.get(options.size() - 1);
    }

    @Override
    public ParsedArg<String> validate(String arg) {
        if (options.contains(arg)) return ParsedArg.Valid(arg);
        return ParsedArg.Invalid();
    }
}
