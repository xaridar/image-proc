package xaridar.args;

public interface ParamType<T> {

    String getType();

    ParsedArg<T> validate(String arg);

}
