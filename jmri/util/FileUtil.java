// FileUtil.java

package jmri.util;

import java.io.File;

/**
 * Common utility methods for working with Files.
 * <P>
 * We needed a place to refactor common File-processing idioms in JMRI
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

public class FileUtil {

    static public String getUrl(File file) {
        try {
            return file.toURI().toURL().toString();
        } catch (Exception e) {
            return "file:"+file.getAbsolutePath();
        }
    }

}