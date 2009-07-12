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
 * @version $Revision: 1.10 $
 */

public class FileUtil {

    /**
     * Find the resource file corresponding to a name.
     * There are five cases:
     * <UL>
     * <LI> Starts with "program:", treat the rest as a relative pathname below the program directory
     * <LI> Starts with "preference:", treat the rest as a relative path below the preferences directory
     * <LI> Starts with "resource:", treat the rest as a pathname relative to the program directory
     *                  (deprecated; see "program:" above)
     * <LI> Starts with "file:", treat the rest as a relative path below the resource directory in the preferences directory
     *                  (deprecated; see "preference:" above)
     * <LI> Otherwise, treat the name as a relative path below the program directory
     * </UL>
     * In any case, absolute pathnames will work.
     *
     * @param pName The name string, possibly starting with file: or resource:
     * @return Absolute file name to use, or null.
     * @since 2.7.2
     */
    static public String getExternalFilename(String pName) {
        if (pName == null || pName.length() == 0) {
            return null;
        }
        if (pName.startsWith("resource:")) {
            // convert to relative filename 
            String temp = pName.substring("resource:".length());
            if ((new File(temp)).isAbsolute())
                return temp;
            else
                return "resources"+File.separator+temp;
         } else if (pName.startsWith("program:")) {
            // both relative and absolute are just returned
            return pName.substring("program:".length());
        } else if (pName.startsWith("preference:")) {
            String filename = pName.substring("preference:".length());
            
            // Check for absolute path name
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+filename);
                return filename;
            }
            // assume this is a relative path from the
            // preferences directory
            filename = jmri.jmrit.XmlFile.userFileLocationDefault()+filename;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+filename);
            return filename;
        } else if (pName.startsWith("file:")) {
            String filename = pName.substring("file:".length());
            
            // historically, absolute path names could be stored 
            // in the 'file' format.  Check for those, and
            // accept them if present
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+filename);
                return filename;
            }
            // assume this is a relative path from the
            // preferences directory
            filename = jmri.jmrit.XmlFile.userFileLocationDefault()+"resources"+File.separator+filename;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+filename);
            return filename;
        }
        // must just be a (hopefully) valid name
        else return pName;
    }

    /**
     * Convert a File object to our preferred storage form.
     *
     * This is the inverse of {@link getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param file File to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(File file) {
        // compare full path name to see if same as preferences
        String filename = file.getAbsolutePath();

        String preferencePrefix = jmri.jmrit.XmlFile.userFileLocationDefault();
        if (filename.startsWith(preferencePrefix)) 
            return "preference:"+filename.substring(preferencePrefix.length(), filename.length());
        
        // now check for relative to program dir
        String progname = new File("").getAbsolutePath()+File.separator;
        if (filename.startsWith(progname)) 
            return "program:"+filename.substring(progname.length(), filename.length());
        return filename;   // absolute, and doesn't match
    }
    
    /**
     * Convert a filename string to our preferred storage form.
     *
     * This is the inverse of {@link getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param filename Filename to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(String filename) {
        // if this already contains prefix, convert
        if (filename.startsWith("file:") ||
            filename.startsWith("resource:") ||
            filename.startsWith("program:") ||
            filename.startsWith("preference:") ) {
            return getPortableFilename(getExternalFilename(filename));
        } else {
            // treat as pure filename
            return getPortableFilename(new File(filename));
        }
    }
    
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
            String retval = file.toURI().toURL().toString();
            if (log.isDebugEnabled()) log.debug("getUrlViaUri: \""+retval+"\"");
            return retval;
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
            log.warn("getUrlViaUri falling back due to exception",e);
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
            String retval = file.toURL().toString();
            if (log.isDebugEnabled()) log.debug("getUrl: \""+retval+"\"");
            return retval;
        } catch (NoSuchMethodError e3) {  // File.toURL not in 1.1.8
            if (file.isDirectory())
                return "file:"+file.getAbsolutePath()+File.separator;
            else
                return "file:"+file.getAbsolutePath();
        } catch (Throwable e) {
            log.warn("getUrl falling back due to exception",e);
            return "file:"+file.getAbsolutePath()+File.separator;
        }
    }

    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileUtil.class.getName());
}
