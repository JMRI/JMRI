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
 * @author Bob Jacobsen  Copyright 2003, 2005, 2006
 * @version $Revision: 1.7 $
 */

public class FileUtil {

    /**
     * Provide the URL for a particular file/directory via the
     * URI form.
     * 
     * Depending on the machine and context, either a true URL or 
     * a URI is needed to open a file.  This method and getURL
     * provide the two alternative ways to create th
     *
     * @since 1.7.3
     */
    static public String getUrlViaUri(File file) {
        try {
            return file.toURI().toURL().toString();
        } catch (NoSuchMethodError e2) {  // File.toURI first available in Java 1.4
            try {
                return file.toURL().toString();
            } catch (NoSuchMethodError e3) {  // File.toURL not in 1.1.8
                if (file.isDirectory())
                    return "file:"+file.getAbsolutePath()+File.separator;
                else
                    return "file:"+file.getAbsolutePath();
            } catch (Throwable e) {
                if (log.isDebugEnabled()) log.debug(" Exception 1: "+e);
                return "file:"+file.getAbsolutePath()+File.separator;
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) log.debug(" Exception 2: "+e);
                return "file:"+file.getAbsolutePath()+File.separator;
        }
    }

    /**
     * Provide a URL for a given file.
     * <P>
     * In version 1.7.2 and before, this would attempt to create 
     * the URL via the toURI() method of File.  This is found
     * to not work on all Windows PCs and JVMs. To enable 
     * the using code to do appropriate retries, this method
     * doesn't use toURI(), and the FileUtil.getUrlViaUri() method
     * does.  To see an example of hos to use this, look at the XmlFIle
     * class (which should be handling your XML I/O in any case).
     *
     */
    static public String getUrl(File file) {
        try {
            return file.toURL().toString();
        } catch (NoSuchMethodError e3) {  // File.toURL not in 1.1.8
            if (file.isDirectory())
                return "file:"+file.getAbsolutePath()+File.separator;
            else
                return "file:"+file.getAbsolutePath();
        } catch (Throwable e) {
            if (log.isDebugEnabled()) log.debug(" Exception 1: "+e);
            return "file:"+file.getAbsolutePath()+File.separator;
        }
    }

    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FileUtil.class.getName());
}
