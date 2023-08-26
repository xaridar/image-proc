package xaridar.args;

public class ParsedArg<T> {
    private final boolean valid;
    private final T value;

    private ParsedArg(boolean valid, T value){
        this.valid = valid;
        this.value = value;
    }

    public static <T> ParsedArg<T> Valid(T obj) {
        return new ParsedArg<>(true, obj);
    }

    public static <T> ParsedArg<T> Invalid() {
        return new ParsedArg<>(false, null);
    }

    public boolean isError() {
        return !valid;
    }

    public T getValue() {
        return value;
    }
}
