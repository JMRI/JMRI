// JTabbedPaneUtil.java

package jmri.util;

import javax.swing.JTabbedPane;

/**
 * Common utility methods for working with JTabbedPanes.
 * <P>
 * We needed a place to refactor common JTabbedPane-processing idioms in JMRI
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

public class JTabbedPaneUtil {

    static public void setToolTipTextAt(JTabbedPane p, int i, String s) {
        try {
            p.setToolTipTextAt(i, s);
        } catch (NoSuchMethodError e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            // nothing to do here, just skip
        }
        return;
    }

}