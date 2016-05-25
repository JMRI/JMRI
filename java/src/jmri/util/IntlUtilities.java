// IntlUtilities.java
package jmri.util;

/**
 * Common utility methods for Internationalization (I18N) and Localization
 * (L12N).
 *
 * See http://jmri.org/help/en/html/doc/Technical/I8N.shtml
 *
 * @author Bob Jacobsen Copyright 2014
 * @version $Revision$
 * @since 3.9.3
 */
public class IntlUtilities {

    /**
     * Use as a replacement for Float.valueOf("1").floatValue() and
     * Float.parseFloat("1").floatValue()
     */
    static public float floatValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).floatValue();
    }

    /**
     * Use as a replacement for Double.valueOf("1").doubleValue() and
     * Double.parseDouble("1").doubleValue()
     */
    static public double doubleValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).doubleValue();
    }

    /**
     * Use as a replacement for String.valueOf(2.3)
     */
    static public String valueOf(double val) {
        return java.text.NumberFormat.getInstance().format(val);
    }

    /**
     * Use as a replacement for String.valueOf(5)
     */
    static public String valueOf(int val) {
        return java.text.NumberFormat.getInstance().format(val);
    }

    // initialize logging
    //static private Logger log = LoggerFactory.getLogger(IntlUtilities.class.getName());
}
