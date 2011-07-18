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
 * @version $Revision$
 */

public class FileUtil {

    /**
     * Find the resource file corresponding to a name.
     * There are five cases:
     * <UL>
     * <LI> Starts with "resource:", treat the rest as a pathname relative to the program directory
     *                  (deprecated; see "program:" below)
     * <LI> Starts with "program:", treat the rest as a relative pathname below the program directory
     * <LI> Starts with "preference:", treat the rest as a relative path below the preferences directory
     * <LI> Starts with "home:", treat the rest as a relative path below the user.home directory
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
                return temp.replace('/', File.separatorChar);
            else
                return temp.replace('/', File.separatorChar);
         } else if (pName.startsWith("program:")) {
            // both relative and absolute are just returned
            return pName.substring("program:".length()).replace('/', File.separatorChar);
        } else if (pName.startsWith("preference:")) {
            String filename = pName.substring("preference:".length());
            
            // Check for absolute path name
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+filename);
                return filename.replace('/', File.separatorChar);
            }
            // assume this is a relative path from the
            // preferences directory
            filename = jmri.jmrit.XmlFile.userFileLocationDefault()+filename;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+filename);
            return filename.replace('/', File.separatorChar);
        } else if (pName.startsWith("file:")) {
            String filename = pName.substring("file:".length());
            
            // historically, absolute path names could be stored 
            // in the 'file' format.  Check for those, and
            // accept them if present
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+filename);
                return filename.replace('/', File.separatorChar);
            }
            // assume this is a relative path from the
            // preferences directory
            filename = jmri.jmrit.XmlFile.userFileLocationDefault()+"resources"+File.separator+filename;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+filename);
            return filename.replace('/', File.separatorChar);
        } else if (pName.startsWith("home:")) {
            String filename = pName.substring("home:".length());
            
            // Check for absolute path name
            if ((new File(filename)).isAbsolute()) {
                if (log.isDebugEnabled()) log.debug("Load from absolute path: "+filename);
                return filename.replace('/', File.separatorChar);
            }
            // assume this is a relative path from the
            // user.home directory
            filename = System.getProperty("user.home")+File.separator+filename;
            if (log.isDebugEnabled()) log.debug("load from user preferences file: "+filename);
            return filename.replace('/', File.separatorChar);
        }
        // must just be a (hopefully) valid name
        else return pName.replace('/', File.separatorChar);
    }

    /**
     * Convert a File object to our preferred storage form.
     *
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param file File to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(File file) {
        // compare full path name to see if same as preferences
        String filename = file.getAbsolutePath();

        // compare full path name to see if same as preferences
        String preferencePrefix = jmri.jmrit.XmlFile.userFileLocationDefault();

        if (filename.startsWith(preferencePrefix)) 
            return "preference:"+filename.substring(preferencePrefix.length(), filename.length()).replace(File.separatorChar,'/');
        
        // now check for relative to program dir
        String progname = (new File("").getAbsolutePath()+File.separator);
        if (filename.startsWith(progname)) 
            return "program:"+filename.substring(progname.length(), filename.length()).replace(File.separatorChar,'/');

        // compare full path name to see if same as home directory
        // do this last, in case preferences or program dir are in home directory
        String homePrefix = System.getProperty("user.home")+File.separator;

        if (filename.startsWith(homePrefix)) 
            return "home:"+filename.substring(homePrefix.length(), filename.length()).replace(File.separatorChar,'/');
        
        return filename.replace(File.separatorChar,'/');   // absolute, and doesn't match; not really portable...
    }
    
    /**
     * Convert a filename string to our preferred storage form.
     *
     * This is the inverse of {@link #getExternalFilename(String pName)}.
     * Deprecated forms are not created.
     *
     * @param filename Filename to be represented
     * @since 2.7.2
     */
    static public String getPortableFilename(String filename) {
        // if this already contains prefix, run through conversion to normalize
        if (filename.startsWith("file:") ||
            filename.startsWith("resource:") ||
            filename.startsWith("program:") ||
            filename.startsWith("home:") ||
            filename.startsWith("preference:") ) {
            return getPortableFilename(getExternalFilename(filename));
        } else {
            // treat as pure filename
            return getPortableFilename(new File(filename));
        }
    }
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileUtil.class.getName());
}
