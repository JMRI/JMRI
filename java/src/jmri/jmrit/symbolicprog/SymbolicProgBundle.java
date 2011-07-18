// SymbolicProgBundle.java

package jmri.jmrit.symbolicprog;

import java.util.ResourceBundle;

/**
 * Common access to the SymbolicProgBundle of properties.
 *
 * Putting this in a class allows it to be loaded only
 * once.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision$
 */

public class SymbolicProgBundle {

    static public final ResourceBundle b
            = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");

    static public ResourceBundle bundle() { return b; }
    
}