package jmri.util;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.annotation.Nonnull;

/**
 * Common utility methods for Internationalization (I18N) and Localization
 * (L12N) in the default {@link java.util.Locale}.
 *
 * See http://jmri.org/help/en/html/doc/Technical/I8N.shtml
 *
 * @author Bob Jacobsen Copyright 2014
 * @author Steve Young Copyright 2025
 * @since 3.9.3
 */
public class IntlUtilities {

    // class only supplies static methods
    private IntlUtilities(){}

    /**
     * Parse a number.
     *
     * Use as a locale-aware replacement for Float.valueOf("1").floatValue() and
     * Float.parseFloat("1").floatValue().
     * <p>
     * White space, potentially included as a thousand separator is removed.
     * @param val the value to parse
     * @return a parsed number
     * @throws java.text.ParseException if val cannot be parsed as a number
     */
    public static float floatValue(@Nonnull String val) throws ParseException {
        return NumberFormat.getInstance().parse(trimWhiteSpace(val)).floatValue();
    }

    /**
     * Parse a number.
     *
     * Use as a locale-aware replacement for Double.valueOf("1").doubleValue()
     * and Double.parseDouble("1").doubleValue().
     * <p>
     * White space, potentially included as a thousand separator is removed.
     * @param val the value to parse
     * @return a parsed number
     * @throws java.text.ParseException if val cannot be parsed as a number
     */
    public static double doubleValue(@Nonnull String val) throws ParseException {
        return NumberFormat.getInstance().parse(trimWhiteSpace(val)).doubleValue();
    }

    /**
     * Parse a number.
     *
     * Use as a locale-aware replacement for Integer.valueOf("1").intValue()
     * and Integer.parseInt("1").intValue().
     * <p>
     * White space, potentially included as a thousand separator is removed.
     * @param val the value to parse
     * @return a parsed number
     * @throws java.text.ParseException if val cannot be parsed as a number
     */
    public static int intValue(@Nonnull String val) throws ParseException {
        return NumberFormat.getInstance().parse(trimWhiteSpace(val)).intValue();
    }

    private static String trimWhiteSpace(String val) {
        return val.trim().replaceAll("\\s", "");
    }

    /**
     * Get the text representation of a number.
     *
     * Use as a locale-aware replacement for String.valueOf(2.3).
     *
     * @param val the number
     * @return the text representation
     */
    public static String valueOf(double val) {
        return NumberFormat.getInstance().format(val);
    }

    /**
     * Get the text representation of a number.
     *
     * Use as a locale-aware replacement for String.valueOf(5).
     *
     * @param val the number
     * @return the text representation
     */
    public static String valueOf(int val) {
        return NumberFormat.getInstance().format(val);
    }

    //private final static Logger log = LoggerFactory.getLogger(IntlUtilities.class);
}
