package jmri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utility methods for determining which type of operating system is in
 * use.
 *
 * @author Bob Jacobsen Copyright 2006
 * @author Daniel Boudreau Copyright 2012 (add Unix)
 * @author Randall Wood Copyright 2013
 */
public class SystemType {

    static final public int MACCLASSIC = 1; // no longer supported - latest JVM is 1.1.8
    static final public int MACOSX = 2;
    static final public int WINDOWS = 4;
    static final public int LINUX = 5;
    static final public int OS2 = 6;
    static final public int UNIX = 7;

    static int type = 0;
    static boolean isSet = false;

    static String osName;

    /**
     * Get the integer constant for the OS. Useful in switch statements.
     *
     * @return Type as an integer
     */
    public static int getType() {
        setType();
        return type;
    }

    /**
     * The os.name property
     *
     * @return OS name
     */
    public static String getOSName() {
        setType();
        return osName;
    }

    /**
     * Convenience method to determine if OS is Mac OS X. Useful if an exception
     * needs to be made for Mac OS X.
     *
     * @return true if on Mac OS X.
     */
    public static boolean isMacOSX() {
        setType();
        return (type == MACOSX);
    }

    /**
     * Convenience method to determine if OS is Linux. Useful if an exception
     * needs to be made for Linux.
     *
     * @return true if on Linux
     */
    public static boolean isLinux() {
        setType();
        return (type == LINUX);
    }

    /**
     * Convenience method to determine if OS is Microsoft Windows. Useful if an
     * exception needs to be made for Microsoft Windows.
     *
     * @return true if on Microsoft Windows
     */
    public static boolean isWindows() {
        setType();
        return (type == WINDOWS);
    }

    /**
     * Convenience method to determine if OS is OS/2. Useful if an exception
     * needs to be made for OS/2.
     *
     * @return true if on OS/2
     */
    public static boolean isOS2() {
        setType();
        return (type == OS2);
    }

    /**
     * Convenience method to determine if OS is Unix. Useful if an exception
     * needs to be made for Unix.
     *
     * @return true if on Unix
     */
    public static boolean isUnix() {
        setType();
        return (type == UNIX);
    }

    static void setType() {
        if (isSet) {
            return;
        }
        isSet = true;

        osName = System.getProperty("os.name");
        String lowerCaseName = osName.toLowerCase();

        if (lowerCaseName.contains("os x")) { // Prefered test per http://developer.apple.com/library/mac/#technotes/tn2002/tn2110.html
            // Mac OS X
            type = MACOSX;
        } else if (lowerCaseName.contains("linux")) {
            // Linux
            type = LINUX;
        } else if (lowerCaseName.contains("os/2")) {
            // OS/2
            type = OS2;
        } else if (lowerCaseName.contains("windows")) {  // usually a suffix indicates flavor
            // Windows
            type = WINDOWS;
        } else if (lowerCaseName.contains("nix") || lowerCaseName.contains("nux") || lowerCaseName.contains("aix") || lowerCaseName.contains("solaris")) {
            // Unix
            type = UNIX;
        } else {
            // No match
            type = 0;
            log.error("Could not determine system type from os.name=/" + osName + "/");
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SystemType.class);
}
