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
 * @version $Revision: 1.3 $
 */

public class FileUtil {

    static public String getUrl(File file) {
        try {
            return file.toURI().toURL().toString();
        } catch (NoSuchMethodError e2) {  // File.toURI first available in Java 1.4
            try {
                return file.toURL().toString();
            } catch (NoSuchMethodError e3) {  // File.toURL not in 1.1.8
                return "file:"+file.getAbsolutePath()+File.separator;
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug(" Exception 1: "+e);
                return "file:"+file.getAbsolutePath()+File.separator;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) log.debug(" Exception 2: "+e);
            return "file:"+file.getAbsolutePath()+File.separator;
        }
    }

    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FileUtil.class.getName());
}
