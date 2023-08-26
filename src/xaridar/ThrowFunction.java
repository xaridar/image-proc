package xaridar;

import java.util.function.Function;

public interface ThrowFunction<T, R> extends Function<T, R> {
    @Override
    default R apply(T o) {
        try {
            return applyThrows(o);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T o) throws Exception;
}
