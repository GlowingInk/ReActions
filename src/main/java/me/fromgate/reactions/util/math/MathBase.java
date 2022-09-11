package me.fromgate.reactions.util.math;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// TODO: Update to latest EzMath
public final class MathBase {
    private static final Map<String, Function> functions = new HashMap<>();
    private static final Map<String, Double> constants = new HashMap<>();

    static {
        for (DefaultFunctions func : DefaultFunctions.values())
            registerFunction(func.name(), func);
        registerConstant("e", Math.E);
        registerConstant("ln2", 0.693147180559945);
        registerConstant("ln10", 2.302585092994046);
        registerConstant("log2e", 1.442695040888963);
        registerConstant("euler", 0.577215664901533);
        registerConstant("log10e", 0.434294481903252);
        registerConstant("phi", 1.618033988749895);
        registerConstant("pi", Math.PI);
        registerConstant("dmax", Double.MAX_VALUE);
        registerConstant("dmin", Double.MIN_VALUE);
    }

    private MathBase() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static boolean registerFunction(String name, Function function) {
        name = name.toLowerCase(Locale.ENGLISH);
        if (isAllowedName(name) && !functions.containsKey(name)) {
            functions.put(name, function);
            return true;
        }
        return false;
    }

    public static boolean registerConstant(String name, double value) {
        name = name.toLowerCase(Locale.ENGLISH);
        if (isAllowedName(name) && !constants.containsKey(name)) {
            constants.put(name, value);
            return true;
        }
        return false;
    }

    public static Function getFunction(String name) {
        return functions.get(name);
    }

    public static double getConstant(String name) {
        Double value = constants.get(name);
        return value == null ? 0 : value;
    }

    public static boolean isNumberChar(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    public static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z');
    }

    private static boolean isAllowedName(String str) {
        for (char c : str.toCharArray())
            if (!(isNumberChar(c) && isWordChar(c))) return false;
        return isWordChar(str.charAt(0));
    }

    @FunctionalInterface
    public interface Function {
        double eval(double a, double... num);
    }
}