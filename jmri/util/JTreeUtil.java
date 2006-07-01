// JTreeUtil.java

package jmri.util;

import javax.swing.JTree;

/**
 * Common utility methods for working with JTrees.
 * <P>
 * We needed a place to refactor common JTree-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a
 * Java 1.1.8 system, or at least try to fake it.
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.1 $
 */

public class JTreeUtil {

    static public void setExpandsSelectedPaths(JTree dTree, boolean value) {
        try {   // following might not be present on Mac Classic, but
                //doesn't have a big effect
            dTree.setExpandsSelectedPaths(value);
        } catch (java.lang.NoSuchMethodError e) {}
    }

}