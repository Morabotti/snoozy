package fi.jubic.snoozy.converters.jsr310;

import javax.annotation.Nullable;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

public class InstantConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(
            Class<T> clazz,
            Type type,
            Annotation[] annotations
    ) {
        if (!Instant.class.equals(clazz)) return null;

        boolean isNullable = Stream.of(annotations)
                .anyMatch(
                        annotation -> Objects.equals(
                                annotation.annotationType(),
                                Nullable.class
                        )
                );

        return new ParamConverter<T>() {
            @Override
            public T fromString(String s) {
                if (s == null && isNullable) {
                    return clazz.cast(null);
                }
                return clazz.cast(
                        ParseUtil.parseInstant(s)
                );
            }

            @Override
            public String toString(T t) {
                return t.toString();
            }
        };
    }
}
