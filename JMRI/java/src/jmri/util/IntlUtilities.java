package jmri.util;

/**
 * Common utility methods for Internationalization (I18N) and Localization
 * (L12N) in the default {@link java.util.Locale}.
 *
 * See http://jmri.org/help/en/html/doc/Technical/I8N.shtml
 *
 * @author Bob Jacobsen Copyright 2014
 * @since 3.9.3
 */
public class IntlUtilities {

    /**
     * Parse a number.
     *
     * Use as a locale-aware replacement for Float.valueOf("1").floatValue() and
     * Float.parseFloat("1").floatValue().
     *
     * @param val the value to parse
     * @return a parsed number
     * @throws java.text.ParseException if val cannot be parsed as a number
     */
    static public float floatValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).floatValue();
    }

    /**
     * Parse a number.
     *
     * Use as a locale-aware replacement for Double.valueOf("1").doubleValue()
     * and Double.parseDouble("1").doubleValue().
     *
     * @param val the value to parse
     * @return a parsed number
     * @throws java.text.ParseException if val cannot be parsed as a number
     */
    static public double doubleValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).doubleValue();
    }

    /**
     * Get the text representation of a number.
     *
     * Use as a locale-aware replacement for String.valueOf(2.3).
     *
     * @param val the number
     * @return the text representation
     */
    static public String valueOf(double val) {
        return java.text.NumberFormat.getInstance().format(val);
    }

    /**
     * Get the text representation of a number.
     *
     * Use as a locale-aware replacement for String.valueOf(5).
     *
     * @param val the number
     * @return the text representation
     */
    static public String valueOf(int val) {
        return java.text.NumberFormat.getInstance().format(val);
    }

    // initialize logging
    //private final static Logger log = LoggerFactory.getLogger(IntlUtilities.class);
}
