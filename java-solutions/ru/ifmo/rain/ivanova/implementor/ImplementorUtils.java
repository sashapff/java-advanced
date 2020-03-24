package ru.ifmo.rain.ivanova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ImplementorUtils {

    private final static String LINE_SEPARATOR = System.lineSeparator();
    private final static String SPACE = " ";
    private final static String PACKAGE = "package";
    private final static String SEMICOLON = ";";
    private final static String TAB = "\t";
    private final static String CLASS = "class";
    private final static String IMPL = "Impl";
    private final static String IMPLEMENTS = "implements";
    private final static String EXTENDS = "extends";
    private final static String OPEN_BRACE = "{";
    private final static String CLOSE_BRACE = "}";
    private final static String OPEN_BRACKET = "(";
    private final static String CLOSE_BRACKET = ")";
    private final static String COMMA = ", ";
    private final static String THROWS = "throws";
    private final static String SUPER = "super";
    private final static String RETURN = "return";
    private final static String NULL = "null";
    private final static String FALSE = "false";
    private final static String ZERO = "0";
    private final static String PUBLIC = "public";
    private final static String EMPTY = "";

    private static String concatenate(String separator, String... lines) {
        return Arrays.stream(lines).filter(s -> !s.isEmpty()).collect(Collectors.joining(separator));
    }

    private static String indent(int size) {
        return TAB.repeat(size);
    }

    private static String generatePackageInfo(Class<?> token) {
        String packageName = token.getPackageName();
        return packageName.isEmpty() ? EMPTY : concatenate(SPACE, PACKAGE, packageName, SEMICOLON);
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL;
    }

    private static String getClassType(Class<?> token) {
        return token.isInterface() ? IMPLEMENTS : EXTENDS;
    }

    private static String generateClassHeadline(Class<?> token) {
        return concatenate(SPACE, indent(1), PUBLIC, CLASS,
                getClassName(token), getClassType(token), token.getCanonicalName(), OPEN_BRACE);
    }

    private static String getExecutableModifiers(Executable executable) {
        return Modifier.toString(executable.getModifiers() & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    private static String getResultType(Executable executable) {
        return executable instanceof Constructor ? EMPTY : ((Method) executable).getReturnType().getCanonicalName();
    }

    private static String getName(Executable executable) {
        return executable instanceof Constructor ? executable.getDeclaringClass().getSimpleName() + IMPL
                : executable.getName();
    }

    private static class ArgumentNames implements Supplier<String> {
        int num = 0;
        static final String prefix = "argument";

        @Override
        public String get() {
            return prefix + num++;
        }
    }

    private static String getArgumentsNames(Executable executable) {
        ArgumentNames argumentNames = new ArgumentNames();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> concatenate(SPACE, argumentNames.get()))
                .collect(Collectors.joining(COMMA));
    }

    private static String getArguments(Executable executable) {
        ArgumentNames argumentNames = new ArgumentNames();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> concatenate(SPACE, c.getCanonicalName(), argumentNames.get()))
                .collect(Collectors.joining(COMMA));
    }

    private static String getExceptions(Executable executable) {
        Class[] exceptions = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            return concatenate(SPACE, THROWS, Arrays.stream(exceptions)
                    .map(c -> concatenate(SPACE, c.getCanonicalName()))
                    .collect(Collectors.joining(COMMA)));
        }
        return EMPTY;
    }

    private static String generateExecutableHeadline(Executable executable) {
        return concatenate(SPACE, indent(1), getExecutableModifiers(executable),
                getResultType(executable), getName(executable), OPEN_BRACKET, getArguments(executable),
                CLOSE_BRACKET, getExceptions(executable), OPEN_BRACE);
    }

    private static String getConstructorBody(Constructor<?> constructor) {
        return concatenate(SPACE, indent(2), SUPER, OPEN_BRACKET,
                getArgumentsNames(constructor), CLOSE_BRACKET, SEMICOLON);
    }

    private static String getDefaultValue(Method method) {
        Class clazz = method.getReturnType();
        if (!clazz.isPrimitive()) {
            return NULL;
        } else if (clazz.equals(boolean.class)) {
            return FALSE;
        } else if (clazz.equals(void.class)) {
            return EMPTY;
        } else {
            return ZERO;
        }
    }

    private static String getMethodBody(Method method) {
        return concatenate(SPACE, indent(2), RETURN, getDefaultValue(method), SEMICOLON);
    }

    private static String generateExecutable(Executable executable) {
        return concatenate(LINE_SEPARATOR, generateExecutableHeadline(executable),
                executable instanceof Constructor ? getConstructorBody((Constructor) executable) :
                        getMethodBody((Method) executable),
                indent(1) + CLOSE_BRACE);
    }

    private static String generateConstructor(Constructor<?> constructor) {
        return concatenate(LINE_SEPARATOR, generateExecutable(constructor));
    }

    private static String generateConstructors(Class<?> token) throws ImplerException {
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Private interface is forbidden");
        }
        if (token.isInterface()) {
            return EMPTY;
        }
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new ImplerException("None constructor");
        }
        return constructors.stream()
                .map(ImplementorUtils::generateConstructor)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    private static String generateMethod(HashMethod method) {
        return concatenate(LINE_SEPARATOR, generateExecutable(method.get()));
    }

    private static class HashMethod {
        private final Method method;
        private final String methodName;

        HashMethod(Method method) {
            this.method = method;
            this.methodName = method.getName() + Arrays.stream(method.getParameterTypes())
                    .map(Class::getCanonicalName).collect(Collectors.joining(COMMA));
        }

        Method get() {
            return method;
        }

        @Override
        public int hashCode() {
            return methodName.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof HashMethod) {
                return ((HashMethod) object).methodName.equals(methodName);
            }
            return false;
        }
    }

    private static String generateMethods(Class<?> token) {
        HashSet<HashMethod> hashMethods = Arrays.stream(token.getMethods())
                .map(HashMethod::new)
                .collect(Collectors.toCollection(HashSet::new));
        for (; token != null; token = token.getSuperclass()) {
            hashMethods.addAll(Arrays.stream(token.getDeclaredMethods()).map(HashMethod::new).collect(Collectors.toCollection(HashSet::new)));
        }
        return hashMethods.stream()
                .filter(method -> Modifier.isAbstract(method.get().getModifiers()))
                .map(ImplementorUtils::generateMethod)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    static String generateSourceCode(Class<?> token) throws ImplerException {
        return concatenate(LINE_SEPARATOR, generatePackageInfo(token), generateClassHeadline(token),
                generateConstructors(token), generateMethods(token), CLOSE_BRACE);
    }

}