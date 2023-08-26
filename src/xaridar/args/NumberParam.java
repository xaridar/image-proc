package xaridar.args;

public class NumberParam implements ParamType<Number> {
    double min;
    double max;
    boolean intReq;

    public NumberParam() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
    }

    public NumberParam(boolean integer) {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, integer);
    }

    public NumberParam(double min, double max) {
        this(min, max, false);
    }

    public NumberParam(double min, double max, boolean integer) {
        this.min = min;
        this.max = max;
        this.intReq = integer;
    }

    public NumberParam(double min) {
        this(min, Double.POSITIVE_INFINITY, false);
    }

    public NumberParam(double min, boolean integer) {
        this(min, Double.POSITIVE_INFINITY, true);
    }

    @Override
    public String getType() {
        String ret = intReq ? "integer" : "double";
        if (min > Double.NEGATIVE_INFINITY && max < Double.POSITIVE_INFINITY) {
            ret += " between " + min + " and " + max;
        } else if (min > Double.NEGATIVE_INFINITY) {
            ret += " above " + min;
        } else if (max < Double.POSITIVE_INFINITY) {
            ret += " below " + max;
        }
        return ret;
    }

    @Override
    public ParsedArg<Number> validate(String arg) {
        try {
            Number num;
            if (!intReq) num = Double.parseDouble(arg);
            else num = Integer.parseInt(arg);
            if (num.doubleValue() <= max && num.doubleValue() >= min) return ParsedArg.Valid(num);
        } catch (NullPointerException | NumberFormatException ignored) {}
        return ParsedArg.Invalid();
    }
}
