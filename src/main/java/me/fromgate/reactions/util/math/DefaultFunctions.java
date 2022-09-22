package me.fromgate.reactions.util.math;

/**
 * Some default math functions
 */
public enum DefaultFunctions implements MathBase.Function {
    ABS {
        @Override
        public double eval(double a, double... num) {
            return Math.abs(a);
        }
    },
    ACOS {
        @Override
        public double eval(double a, double... num) {
            return Math.acos(a);
        }
    },
    ASIN {
        @Override
        public double eval(double a, double... num) {
            return Math.asin(a);
        }
    },
    ATAN {
        @Override
        public double eval(double a, double... num) {
            return Math.atan(a);
        }
    },
    ATAN2 {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.atan2(a, num[0]) : a;
        }
    },
    CBRT {
        @Override
        public double eval(double a, double... num) {
            return Math.cbrt(a);
        }
    },
    CEIL {
        @Override
        public double eval(double a, double... num) {
            return Math.ceil(a);
        }
    },
    COS {
        @Override
        public double eval(double a, double... num) {
            return Math.cos(a);
        }
    },
    COSH {
        @Override
        public double eval(double a, double... num) {
            return Math.cosh(a);
        }
    },
    EXP {
        @Override
        public double eval(double a, double... num) {
            return Math.exp(a);
        }
    },
    EXPM1 {
        @Override
        public double eval(double a, double... num) {
            return Math.expm1(a);
        }
    },
    FLOOR {
        @Override
        public double eval(double a, double... num) {
            return Math.floor(a);
        }
    },
    GETEXPONENT {
        @Override
        public double eval(double a, double... num) {
            return Math.getExponent(a);
        }
    },
    LOG {
        @Override
        public double eval(double a, double... num) {
            return Math.log(a);
        }
    },
    LOG10 {
        @Override
        public double eval(double a, double... num) {
            return Math.log10(a);
        }
    },
    LOG1P {
        @Override
        public double eval(double a, double... num) {
            return Math.log1p(a);
        }
    },
    MAX {
        @Override
        public double eval(double a, double... num) {
            if (num.length > 0) {
                double max = a;
                for (double j : num)
                    max = Math.max(max, j);
                return max;
            }
            return a;
        }
    },
    MIN {
        @Override
        public double eval(double a, double... num) {
            if (num.length > 0) {
                double min = a;
                for (double j : num)
                    min = Math.min(min, j);
                return min;
            }
            return a;
        }
    },
    NEXTAFTER {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.nextAfter(a, num[0]) : a;
        }
    },
    NEXTDOWN {
        @Override
        public double eval(double a, double... num) {
            return Math.nextDown(a);
        }
    },
    NEXTUP {
        @Override
        public double eval(double a, double... num) {
            return Math.nextUp(a);
        }
    },
    ROUND {
        @Override
        public double eval(double a, double... num) {
            return Math.round(a);
        }
    },
    RINT {
        @Override
        public double eval(double a, double... num) {
            return Math.rint(a);
        }
    },
    SIGNUM {
        @Override
        public double eval(double a, double... num) {
            return Math.signum(a);
        }
    },
    SIN {
        @Override
        public double eval(double a, double... num) {
            return Math.sin(a);
        }
    },
    SINH {
        @Override
        public double eval(double a, double... num) {
            return Math.sinh(a);
        }
    },
    SQRT {
        @Override
        public double eval(double a, double... num) {
            return Math.sqrt(a);
        }
    },
    TAN {
        @Override
        public double eval(double a, double... num) {
            return Math.tan(a);
        }
    },
    TANH {
        @Override
        public double eval(double a, double... num) {
            return Math.tanh(a);
        }
    },
    TODEGREES {
        @Override
        public double eval(double a, double... num) {
            return Math.toDegrees(a);
        }
    },
    TORADIANS {
        @Override
        public double eval(double a, double... num) {
            return Math.toRadians(a);
        }
    },
    ULP {
        @Override
        public double eval(double a, double... num) {
            return Math.ulp(a);
        }
    },
    HYPOT {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.hypot(a, num[0]) : a;
        }
    },
    IEEEREMAINDER {
        @Override
        public double eval(double a, double... num) {
            return num.length > 0 ? Math.IEEEremainder(a, num[0]) : a;
        }
    },
    FORMATFLOAT {
        @Override
        public double eval(double a, double... num) {
            return Math.round(a * 100) / 100d;
        }
    }
}