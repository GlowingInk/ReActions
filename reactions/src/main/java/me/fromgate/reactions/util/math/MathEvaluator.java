package me.fromgate.reactions.util.math;

import me.fromgate.reactions.util.Utils;

import java.util.Arrays;
import java.util.Locale;

/*
   All the code is inspired by Boann's answer on https://stackoverflow.com/questions/3422673
   You can use this code however you want to
*/

/**
 * Math evaluator for "calc'n'go". Supports functions, constants
 * More on <a href="https://github.com/imDaniX/EzMath">EzMath</a>
 */
public class MathEvaluator {
    private final String expression;
    private int pointer;

    private MathEvaluator(String expression) {
        this.expression = Utils.removeChar(expression.toLowerCase(Locale.ROOT), ' ');
    }

    public static double eval(String expression) {
        return new MathEvaluator(expression).eval();
    }

    private double eval() {
        this.pointer = 0;
        return thirdImportance();
    }

    private double thirdImportance() {
        double x = secondImportance();
        while (true) {
            if (tryNext('+')) x += secondImportance();
            else if (tryNext('-')) x -= secondImportance();
            else
                return x;
        }
    }

    private double secondImportance() {
        double x = firstImportance();
        while (true) {
            if (tryNext('*')) x *= firstImportance();
            else if (tryNext('/')) x /= firstImportance();
            else if (tryNext('%')) x %= firstImportance();
            else
                return x;
        }
    }

    private double firstImportance() {
        if (tryNext('-')) return -firstImportance(); // "-5", "--5"..
        while (tryNext('+')) ; // "+5", "++5"..
        double x = 0;
        int start = pointer;
        if (tryNext('(')) {
            x = thirdImportance();
            tryNext(')');
        } else if (MathBase.isNumberChar(current())) {
            while (MathBase.isNumberChar(current())) pointer++;
            x = NumberUtils.getDouble(expression.substring(start, pointer), 0);
        } else if (MathBase.isWordChar(current())) {
            while (MathBase.isWordChar(current()) || MathBase.isNumberChar(current())) pointer++;
            String str = expression.substring(start, pointer);
            if (tryNext('(')) {
                MathBase.Function function = MathBase.getFunction(str);
                x = thirdImportance();
                double[] args = {};
                while (tryNext(',')) {
                    args = Arrays.copyOfRange(args, 0, args.length + 1);
                    args[args.length - 1] = thirdImportance();
                }
                x = function == null ? 0 : function.eval(x, args);
                tryNext(')');
            } else {
                x = MathBase.getConstant(str);
            }
        }

        if (tryNext('^')) x = Math.pow(x, firstImportance());
        return x;
    }

    private char current() {
        return expression.length() > pointer ? expression.charAt(pointer) : ' ';
    }

    private boolean tryNext(char c) {
        if (current() == c) {
            pointer++;
            return true;
        }
        return false;
    }
}
