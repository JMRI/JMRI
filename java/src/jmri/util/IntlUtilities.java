// IntlUtilities.java

package jmri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for Internationalization (I18N) 
 * and Localization (L12N).
 *
 * See http://jmri.org/help/en/html/doc/Technical/I8N.shtml
 *
 * @author Bob Jacobsen  Copyright 2014
 * @version $Revision$
 */

public class IntlUtilities {

    /**
     * Meant as a replacement for Float.valueOf("1").floatValue();
     * and Float.parseFloat("1").floatValue();
     */
    static float floatValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).floatValue();
    }
    
    /**
     * Meant as a replacement for Double.valueOf("1").doubleValue();
     * and Double.parseDouble("1").doubleValue();
     */
    static double doubleValue(String val) throws java.text.ParseException {
        return java.text.NumberFormat.getInstance().parse(val).doubleValue();
    }
    
    
    // initialize logging
    static private Logger log = LoggerFactory.getLogger(IntlUtilities.class.getName());
}
