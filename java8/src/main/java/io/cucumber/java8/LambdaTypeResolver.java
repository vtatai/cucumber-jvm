package io.cucumber.java8;

import io.cucumber.core.backend.TypeResolver;
import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

final class LambdaTypeResolver implements TypeResolver {
    private final Type type;
    private final String expression;
    private final StackTraceElement location;

    LambdaTypeResolver(Type type, String expression, StackTraceElement location) {
        this.type = type;
        this.expression = expression;
        this.location = location;
    }

    @Override
    public Type resolve() {
        if (net.jodah.typetools.TypeResolver.Unknown.class.equals(type)) {
            return Object.class;
        }
        return requireNonMapOrListType(type);
    }

    public Type getType() {
        return type;
    }

    private Type requireNonMapOrListType(Type argumentType) {
        if (argumentType instanceof Class) {
            Class<?> argumentClass = (Class<?>) argumentType;
            if (List.class.isAssignableFrom(argumentClass) || Map.class.isAssignableFrom(argumentClass)) {
                throw withLocation(
                    new CucumberException(
                        format("Can't use %s in lambda step definition \"%s\". " +
                                "Declare a DataTable argument instead and convert " +
                                "manually with asList/asLists/asMap/asMaps",
                            argumentClass.getName(), expression)));
            }
        }
        return argumentType;
    }

    private CucumberException withLocation(CucumberException exception) {
        exception.setStackTrace(new StackTraceElement[]{location});
        return exception;
    }

}
