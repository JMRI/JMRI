// LocaleUtil.java

package jmri.util;

import java.util.Locale;

/**
 * Common utility methods for working with Locales.
 * <P>
 * We needed a place to refactor common Locale-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.2 $
 */

public class LocaleUtil {

    static public Locale[] getAvailableLocales() {
        try {
            return Locale.getAvailableLocales();
        } catch (NoSuchMethodError e) {
            return  new Locale[] {  // just carry on with identified Locales
                Locale.CANADA,
                Locale.CANADA_FRENCH,
                Locale.ENGLISH,
                Locale.FRENCH,
                Locale.GERMAN,
                Locale.ITALIAN,
                Locale.UK,
                Locale.US
            };
        }
    }

}