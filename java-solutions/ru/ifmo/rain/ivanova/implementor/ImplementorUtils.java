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

/**
 * Class providing work with generating files for {@link JarImplementor}.
 *
 * @author sasha.pff
 * @version 1.0
 */
class ImplementorUtils {

    /**
     * Line separator for generated {@code .java} files.
     */
    private final static String LINE_SEPARATOR = System.lineSeparator();
    /**
     * Space char for generated {@code .java} files.
     */
    private final static String SPACE = " ";
    /**
     * Package word for generated {@code .java} files.
     */
    private final static String PACKAGE = "package";
    /**
     * Semicolon char for generated {@code .java} files.
     */
    private final static String SEMICOLON = ";";
    /**
     * Tab char for generated {@code .java} files.
     */
    private final static String TAB = "\t";
    /**
     * Class word for generated {@code .java} files.
     */
    private final static String CLASS = "class";
    /**
     * Impl suffix for generated {@code .java} files.
     */
    private final static String IMPL = "Impl";
    /**
     * Implements word for generated {@code .java} files.
     */
    private final static String IMPLEMENTS = "implements";
    /**
     * Extends word for generated {@code .java} files.
     */
    private final static String EXTENDS = "extends";
    /**
     * Open figure brace for generated {@code .java} files.
     */
    private final static String OPEN_BRACE = "{";
    /**
     * Close figure brace for generated {@code .java} files.
     */
    private final static String CLOSE_BRACE = "}";
    /**
     * Open bracket for generated {@code .java} files.
     */
    private final static String OPEN_BRACKET = "(";
    /**
     * Close bracket for generated {@code .java} files.
     */
    private final static String CLOSE_BRACKET = ")";
    /**
     * Comma char for generated {@code .java} files.
     */
    private final static String COMMA = ", ";
    /**
     * Throws word for generated {@code .java} files.
     */
    private final static String THROWS = "throws";
    /**
     * Super word for generated {@code .java} files.
     */
    private final static String SUPER = "super";
    /**
     * Return word for generated {@code .java} files.
     */
    private final static String RETURN = "return";
    /**
     * Null word for generated {@code .java} files.
     */
    private final static String NULL = "null";
    /**
     * False word for generated {@code .java} files.
     */
    private final static String FALSE = "false";
    /**
     * Zero char for generated {@code .java} files.
     */
    private final static String ZERO = "0";
    /**
     * Public word for generated {@code .java} files.
     */
    private final static String PUBLIC = "public";
    /**
     * Empty char for generated {@code .java} files.
     */
    private final static String EMPTY = "";

    /**
     * Method to concatenate {@code lines} with separate symbol {@code separator}.
     *
     * @param separator symbol to separate
     * @param lines strings to concatenate
     * @return string with given lines separated tab symbol
     */
    private static String concatenate(String separator, String... lines) {
        return Arrays.stream(lines).filter(s -> !s.isEmpty()).collect(Collectors.joining(separator));
    }

    /**
     * Method to repeat tab symbol.
     *
     * @param size number of repeating tab symbols
     * @return string with size tab symbols
     */
    private static String indent(int size) {
        return TAB.repeat(size);
    }

    /**
     * Method to generate package info.
     *
     * @param token type token to take package info
     * @return string with generated package info
     */
    private static String generatePackageInfo(Class<?> token) {
        String packageName = token.getPackageName();
        return packageName.isEmpty() ? EMPTY : concatenate(SPACE, PACKAGE, packageName, SEMICOLON);
    }

    /**
     * Method to get simple class name
     *
     * @param token type token to get class name
     * @return {@code token} simple name
     */
    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + IMPL;
    }

    /**
     * Method to get class type.
     *
     * @param token type token to get class type
     * @return implements if {@code token} is interface and extends if {@code  token} is class
     */
    private static String getClassType(Class<?> token) {
        return token.isInterface() ? IMPLEMENTS : EXTENDS;
    }

    /**
     * Method to generate class headline.
     *
     * @param token type token to generate class headline
     * @return {@code token} headline
     */
    private static String generateClassHeadline(Class<?> token) {
        return concatenate(SPACE, indent(1), PUBLIC, CLASS,
                getClassName(token), getClassType(token), token.getCanonicalName(), OPEN_BRACE);
    }

    /**
     * Method to get modifiers. {@link Modifier#ABSTRACT} and {@link Modifier#TRANSIENT} is not included.
     *
     * @param executable constructor or method to get modifiers
     * @return string with modifiers
     */
    private static String getExecutableModifiers(Executable executable) {
        return Modifier.toString(executable.getModifiers() & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * Method to get {@code executable} result type.
     *
     * @param executable constructor or method to get result type
     * @return result type
     */
    private static String getResultType(Executable executable) {
        return executable instanceof Constructor ? EMPTY : ((Method) executable).getReturnType().getCanonicalName();
    }

    /**
     * Method to get {@code executable} name.
     *
     * @param executable constructor or method to get name
     * @return {@code executable} name
     */
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

    /**
     * Method to get arguments names.
     *
     * @param executable constructor or interface to get arguments name
     * @return string with {@code executable} arguments names
     */
    private static String getArgumentsNames(Executable executable) {
        ArgumentNames argumentNames = new ArgumentNames();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> concatenate(SPACE, argumentNames.get()))
                .collect(Collectors.joining(COMMA));
    }

    /**
     * Method to get arguments.
     *
     * @param executable constructor or interface to get arguments
     * @return string with {@code executable} arguments
     */
    private static String getArguments(Executable executable) {
        ArgumentNames argumentNames = new ArgumentNames();
        return Arrays.stream(executable.getParameterTypes())
                .map(c -> concatenate(SPACE, c.getCanonicalName(), argumentNames.get()))
                .collect(Collectors.joining(COMMA));
    }

    /**
     * Method to get exceptions.
     *
     * @param executable constructor or interface to get exceptions
     * @return string with {@code executable} exceptions
     */
    private static String getExceptions(Executable executable) {
        Class[] exceptions = executable.getExceptionTypes();
        if (exceptions.length > 0) {
            return concatenate(SPACE, THROWS, Arrays.stream(exceptions)
                    .map(c -> concatenate(SPACE, c.getCanonicalName()))
                    .collect(Collectors.joining(COMMA)));
        }
        return EMPTY;
    }

    /**
     * Method to generate executable headline.
     *
     * @param executable constructor or interface to generate headline
     * @return {@code executable} headline
     */
    private static String generateExecutableHeadline(Executable executable) {
        return concatenate(SPACE, indent(1), getExecutableModifiers(executable),
                getResultType(executable), getName(executable), OPEN_BRACKET, getArguments(executable),
                CLOSE_BRACKET, getExceptions(executable), OPEN_BRACE);
    }

    /**
     * Method to get constructor body.
     *
     * @param constructor constructor to get body
     * @return {@code constructor} body
     */
    private static String getConstructorBody(Constructor<?> constructor) {
        return concatenate(SPACE, indent(2), SUPER, OPEN_BRACKET,
                getArgumentsNames(constructor), CLOSE_BRACKET, SEMICOLON);
    }

    /**
     * Method to get default value.
     *
     * @param method method to get default value
     * @return string with default value
     */
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

    /**
     * Method to get method body.
     *
     * @param method method to get body
     * @return {@code method} body
     */
    private static String getMethodBody(Method method) {
        return concatenate(SPACE, indent(2), RETURN, getDefaultValue(method), SEMICOLON);
    }

    /**
     * Method to generate executable.
     *
     * @param executable constructor or method to generate code
     * @return string with {@code executable} code
     */
    private static String generateExecutable(Executable executable) {
        return concatenate(LINE_SEPARATOR, generateExecutableHeadline(executable),
                executable instanceof Constructor ? getConstructorBody((Constructor) executable) :
                        getMethodBody((Method) executable),
                indent(1) + CLOSE_BRACE);
    }

    /**
     * Method to generate constructor.
     *
     * @param constructor constructor to generate
     * @return string with {@code constructor} code
     */
    private static String generateConstructor(Constructor<?> constructor) {
        return concatenate(LINE_SEPARATOR, generateExecutable(constructor));
    }

    /**
     * Method to generate constructors.
     *
     * @param token type token to take constructors
     * @return string with code of constructors
     * @throws ImplerException if constructor is private
     */
    private static String generateConstructors(Class<?> token) throws ImplerException {
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Private interfaces and classes are forbidden");
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

    /**
     * Method to generate method.
     *
     * @param method method to generate code
     * @return string with code of method
     */
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

    /**
     * Method to generate methods.
     *
     * @param token type token to take methods
     * @return string with code of constructors
     */
    private static String generateMethods(Class<?> token) {
        HashSet<HashMethod> hashMethods = Arrays.stream(token.getMethods())
                .map(HashMethod::new)
                .collect(Collectors.toCollection(HashSet::new));
        for (; token != null; token = token.getSuperclass()) {
            hashMethods.addAll(Arrays.stream(token.getDeclaredMethods())
                    .map(HashMethod::new).collect(Collectors.toCollection(HashSet::new)));
        }
        return hashMethods.stream()
                .filter(method -> Modifier.isAbstract(method.get().getModifiers()))
                .map(ImplementorUtils::generateMethod)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Method to generate source code.
     *
     * @param token type token to generate code
     * @return implementation of {@code token}
     * @throws ImplerException if any errors occurs during generating
     */
    static String generateSourceCode(Class<?> token) throws ImplerException {
        return concatenate(LINE_SEPARATOR, generatePackageInfo(token), generateClassHeadline(token),
                generateConstructors(token), generateMethods(token), CLOSE_BRACE);
    }

}