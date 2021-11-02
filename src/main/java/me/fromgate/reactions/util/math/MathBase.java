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
            MathBase.registerFunction(func.name(), func);
        MathBase.registerConstant("e", Math.E);
        MathBase.registerConstant("ln2", 0.693147180559945);
        MathBase.registerConstant("ln10", 2.302585092994046);
        MathBase.registerConstant("log2e", 1.442695040888963);
        MathBase.registerConstant("euler", 0.577215664901533);
        MathBase.registerConstant("log10e", 0.434294481903252);
        MathBase.registerConstant("phi", 1.618033988749895);
        MathBase.registerConstant("pi", Math.PI);
        MathBase.registerConstant("dmax", Double.MAX_VALUE);
        MathBase.registerConstant("dmin", Double.MIN_VALUE);
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
    public static interface Function {
        double eval(double a, double... num);
    }
}