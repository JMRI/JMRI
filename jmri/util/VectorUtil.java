// VectorUtil.java

package jmri.util;

import java.util.Vector;
import com.sun.java.util.collections.Comparable;

/**
 * Common utility methods for working with Vectors.
 * <P>
 * We needed a place to refactor common vector-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * In some cases, these routines use a Java 1.3 or later method, falling
 * back to an explicit implementation when running on Java 1.1; this also
 * includes some stand-in methods for java.util.Collections, etc.
 *
 * @author Bob Jacobsen  Copyright 2005
 * @version $Revision: 1.2 $
 */

public class VectorUtil {

    /*
     * Identical entries are retained, so the output length is the same
     * as the input length.
     * @param values
     */
    static public void sort(Vector values) {
        try {
            java.util.Collections.sort(values);
        } catch (Throwable e1) {  // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            bubblesort(values);
        }
    }

    static void bubblesort(Vector values) {
        for (int i=0; i<=values.size()-2; i++) { // stop sort early to save time!
            for (int j=values.size()-2; j>=i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < ((Comparable)values.elementAt(j)).compareTo(values.elementAt(j+1))) {
                    // swap
                    Object temp = values.elementAt(j);
                    values.setElementAt(values.elementAt(j+1),j);
                    values.setElementAt(temp, j+1);
                }
            }
        }
    }

}