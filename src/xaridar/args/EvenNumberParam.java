package xaridar.args;

public class EvenNumberParam extends NumberParam {
    public EvenNumberParam(double min) {
        super(min, Double.POSITIVE_INFINITY, true);
    }

    public EvenNumberParam(double min, double max) {
        super(min, max, true);
    }

    @Override
    public ParsedArg<Number> validate(String arg) {
        ParsedArg<Number> parsed = super.validate(arg);
        if (parsed.isError() || ((int) parsed.getValue()) % 2 == 1) return ParsedArg.Invalid();
        return parsed;
    }
}
