package xaridar.args;

public class UnnamedArgsInfo {
    public String name;
    public ParamType<?> type;
    public int required;
    public int max;
    public final String description;

    public UnnamedArgsInfo(String name, ParamType<?> type, int required, String description) {
        this(name, type, required, false, description);
    }

    public UnnamedArgsInfo(String name, ParamType<?> type, int required, boolean noMax, String description) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.max = noMax ? -1 : required;
        this.description = description;
    }

    public UnnamedArgsInfo(String name, ParamType<?> type, int required, int max, String description) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.max = max;
        this.description = description;
    }
}
