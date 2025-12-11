package jmri.util;

/**
 * Compare values.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class CompareUtil {

    public static enum CompareType {
        NumberOrString(Bundle.getMessage("CompareUtil_CompareType_NumberOrString")),
        String(Bundle.getMessage("CompareUtil_CompareType_String")),
        Number(Bundle.getMessage("CompareUtil_CompareType_Number"));

        private final String _text;

        private CompareType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    public static enum CompareOperation {
        LessThan(Bundle.getMessage("CompareUtil_CompareOperation_LessThan")),
        LessThanOrEqual(Bundle.getMessage("CompareUtil_CompareOperation_LessThanOrEqual")),
        Equal(Bundle.getMessage("CompareUtil_CompareOperation_Equal")),
        GreaterThanOrEqual(Bundle.getMessage("CompareUtil_CompareOperation_GreaterThanOrEqual")),
        GreaterThan(Bundle.getMessage("CompareUtil_CompareOperation_GreaterThan")),
        NotEqual(Bundle.getMessage("CompareUtil_CompareOperation_NotEqual"));

        private final String _text;

        private CompareOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    /**
     * Compare two values.
     *
     * @param type            the type
     * @param oper            the operation
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _memoryOperation; false otherwise
     */
    public static boolean compare(CompareType type, CompareOperation oper, Object value1, Object value2, boolean caseInsensitive) {
        switch (type) // both are numbers
        {
            case NumberOrString:
                return compareNumber(false, oper, value1, value2, caseInsensitive);
            case String:
                return compareString(oper, value1, value2, caseInsensitive);
            case Number:
                return compareNumber(true, oper, value1, value2, caseInsensitive);
            default:
                throw new IllegalArgumentException("type has unknown value: "+type.name());
        }
    }

    /**
     * Compare two values.
     *
     * @param oper            the operation
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _memoryOperation; false otherwise
     */
    public static boolean compareString(CompareOperation oper, Object value1, Object value2, boolean caseInsensitive) {
        String s1;
        String s2;
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            s1 = value1.toString().trim();
            s2 = value2.toString().trim();
        }
        int compare;
        if (caseInsensitive) {
            compare = s1.compareToIgnoreCase(s2);
        } else {
            compare = s1.compareTo(s2);
        }
        switch (oper) {
            case LessThan:
                if (compare < 0) {
                    return true;
                }
                break;
            case LessThanOrEqual:
                if (compare <= 0) {
                    return true;
                }
                break;
            case Equal:
                if (compare == 0) {
                    return true;
                }
                break;
            case NotEqual:
                if (compare != 0) {
                    return true;
                }
                break;
            case GreaterThanOrEqual:
                if (compare >= 0) {
                    return true;
                }
                break;
            case GreaterThan:
                if (compare > 0) {
                    return true;
                }
                break;
            default:
                throw new IllegalArgumentException("oper has unknown value: "+oper.name());
        }
        return false;
    }

    /**
     * Compare two values.
     *
     * @param requireNumber   true if two numbers are required, false otherwise
     * @param oper            the operation
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _memoryOperation; false otherwise
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", 
                justification = "User explicitly requested check for equality with EQUAL case")
    public static boolean compareNumber(boolean requireNumber, CompareOperation oper, Object value1, Object value2, boolean caseInsensitive) {
        String s1;
        String s2;
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            s1 = value1.toString().trim();
            s2 = value2.toString().trim();
        }
        try {
            double n1;
            if (value1 instanceof Number) {
                n1 = ((Number)value1).doubleValue();
            } else {
                n1 = Double.parseDouble(s1);
            }
            try {
                double n2;
                if (value2 instanceof Number) {
                    n2 = ((Number)value2).doubleValue();
                } else {
                    n2 = Double.parseDouble(s2);
                }
                log.debug("Compare numbers: n1= {} to n2= {}", n1, n2);
                switch (oper) // both are numbers
                {
                    case LessThan:
                        return (n1 < n2);
                    case LessThanOrEqual:
                        return (n1 <= n2);
                    case Equal:
                        return (n1 == n2);
                    case NotEqual:
                        return (n1 != n2);
                    case GreaterThanOrEqual:
                        return (n1 >= n2);
                    case GreaterThan:
                        return (n1 > n2);
                    default:
                        throw new IllegalArgumentException("oper has unknown value: "+oper.name());
                }
            } catch (NumberFormatException nfe) {
                if (requireNumber) throw new IllegalArgumentException(
                        Bundle.getMessage("CompareUtil_Error_Value1IsNotANumber", value1));
                return oper == CompareOperation.NotEqual;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(s2);
                if (requireNumber) throw new IllegalArgumentException(
                        Bundle.getMessage("CompareUtil_Error_Value1IsNotANumber", value1));
                return oper == CompareOperation.NotEqual;   // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
                if (requireNumber) throw new IllegalArgumentException(
                        Bundle.getMessage("CompareUtil_Error_NeitherValueIsNumber", value1, value2));
            }
        }

        // If here, neither value is a number and it's not required.
        return compareString(oper, value1, value2, caseInsensitive);
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompareUtil.class);

}
