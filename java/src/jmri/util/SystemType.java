// FileUtil.java

package jmri.util;

/**
 * Common utility methods for determining which type
 * of operating system is in use.
 *
 * @author Bob Jacobsen  Copyright 2006
 * @version $Revision$
 */

public class SystemType {

    static final public int MACCLASSIC = 1;
    static final public int MACOSX     = 2;
    static final public int WINDOWS    = 4;
    static final public int LINUX      = 5;
    static final public int OS2        = 6;
    
    
    static int type = 0;
    static boolean isSet = false;

    static String osName;
    static String mrjVersion;
    
    public static int getType() {
        setType();
        return type;
    }
    
    static void setType() {
        if (isSet) return;
        isSet = true;
        
        osName       = System.getProperty("os.name","<unknown>");
        mrjVersion   = System.getProperty("mrj.version","<unknown>");

        if ( !mrjVersion.equals("<unknown>")) {
            // Macintosh, test for OS X
            if (osName.toLowerCase().equals("mac os x")) {
                // Mac OS X
                type = MACOSX;
            } else {
                // Mac Classic, by elimination. Check consistency of mrjVersion
                // with that assumption
                if (!(mrjVersion.charAt(0)=='2'))
                    log.error("Decided Mac Classic, but mrj.version is \""
                              +mrjVersion+"\" os.name is \""
                              +osName+"\"");
                type = MACCLASSIC;
            }
        } else if (osName.equals("Linux")) {
            // Linux
            type = LINUX;
        } else if (osName.equals("OS/2")) {
            // OS/2
            type = OS2;
        } else if (osName.startsWith("Windows")) {  // usually a suffix indicates flavor
            // Windows
            type = WINDOWS;
        } else {
            // No match
            type = 0;
            log.error("Could not determine system type from os.name=/"+osName+"/");
        }
    }
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemType.class.getName());
}
