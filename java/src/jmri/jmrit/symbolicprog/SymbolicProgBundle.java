// SymbolicProgBundle.java
package jmri.jmrit.symbolicprog;

import java.util.ResourceBundle;

/**
 * Common access to the SymbolicProgBundle of properties.
 *
 * Putting this in a class allows it to be loaded only once.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 * @version $Revision$
 */
public class SymbolicProgBundle {

    @Deprecated
    static public final ResourceBundle b = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");

    /**
     * Get a reference to the Symbolic Programmer resource bundle.
     *
     * It is preferable to use the {@link #getMessage(java.lang.String) } or {@link #getMessage(java.lang.String, java.lang.Object[])
     * } methods instead.
     *
     * @return
     * @deprecated
     */
    @Deprecated
    static public ResourceBundle bundle() {
        return b;
    }

    static public String getMessage(String key) {
        return Bundle.getMessage(key);
    }

    static public String getMessage(String key, Object... subs) {
        return Bundle.getMessage(key, subs);
    }
}