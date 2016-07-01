package jmri.util;

import java.awt.Font;

/**
 * Common utility methods for working with Fonts.
 * <P>
 * We needed a place to refactor common Font-processing idioms in JMRI code, so
 * this class was created. It's more of a library of procedures than a real
 * class, as (so far) all of the operations have needed no state information.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a Java
 * 1.1.8 system, or at least try to fake it.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class FontUtil {

    static public boolean canRestyle() {
        return true;
    }

    static boolean doInit = true;
    static boolean skip = false;

    static void init() {
        doInit = false;
        // see if on a Mac Classic system where shouldnt even try
        if (SystemType.getOSName().equals("Mac OS")) {
            skip = true;
        }
    }

    static public Font deriveFont(Font f, int style) {
        if (doInit) {
            init();
        }

        // dont even attempt this on certain systems
        if (skip) {
            return f;
        }

        // on other platforms, try it
        try {
            return f.deriveFont(style);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            // just carry on with original fonts
            return f;
        }
    }

    static public boolean canResize() {
        return true;
    }

    static public Font deriveFont(Font f, float size) {
        if (doInit) {
            init();
        }

        // dont even attempt this on certain systems
        if (skip) {
            return f;
        }

        // on other platforms, try it
        try {
            return f.deriveFont(size);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            return f; // just carry on with original fonts
        }
    }
}
