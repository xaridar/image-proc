package xaridar.args;

public class OddNumberParam extends NumberParam {
    public OddNumberParam(double min) {
        super(min, Double.POSITIVE_INFINITY, true);
    }

    public OddNumberParam(double min, double max) {
        super(min, max, true);
    }

    @Override
    public ParsedArg<Number> validate(String arg) {
        ParsedArg<Number> parsed = super.validate(arg);
        if (parsed.isError() || ((int) parsed.getValue()) % 2 == 0) return ParsedArg.Invalid();
        return parsed;
    }

    @Override
    public String getType() {
        return "odd integer above " + min;
    }
}
