package xaridar.args;

public class StringParam implements ParamType<String> {
    public String format = "";

    public StringParam() {}
    public StringParam(String format) {
        this.format = format;
    }

    @Override
    public String getType() {
        if (format.length() < 1) return "string";
        return "string matching: " + format;
    }

    @Override
    public ParsedArg<String> validate(String arg) {
        if (format.length() == 0) return ParsedArg.Valid(arg);
        if (arg.matches(format)) return ParsedArg.Valid(arg);
        return ParsedArg.Invalid();
    }
}
