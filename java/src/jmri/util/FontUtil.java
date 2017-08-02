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
 * @deprecated since 4.7.1; use methods in {@link java.awt.Font}
 */
@Deprecated
public class FontUtil {

    /**
     * Return a constant value of true.
     *
     * @return true
     * @deprecated since 4.7.1 without replacement
     */
    @Deprecated
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

    /**
     * Creates a new Font object by replicating the current Font object and
     * applying a new style to it.
     *
     * @param f     the font
     * @param style the style for the new Font
     * @return a new Font object
     * @deprecated since 4.7.1; use {@link java.awt.Font#deriveFont(int)}
     * instead
     */
    @Deprecated
    static public Font deriveFont(Font f, int style) {
        if (doInit) {
            init();
        }

        // dont even attempt this on certain systems
        if (skip) {
            return f;
        }

        return f.deriveFont(style);
    }

    /**
     * Return a constant value of true.
     *
     * @return true
     * @deprecated since 4.7.1 without replacement
     */
    @Deprecated
    static public boolean canResize() {
        return true;
    }

    /**
     * Creates a new Font object by replicating the current Font object and
     * applying a new size to it.
     *
     * @param f    the font
     * @param size the size for the new Font
     * @return a new Font object
     * @deprecated since 4.7.1; use {@link java.awt.Font#deriveFont(float)}
     * instead
     */
    @Deprecated
    static public Font deriveFont(Font f, float size) {
        if (doInit) {
            init();
        }

        // dont even attempt this on certain systems
        if (skip) {
            return f;
        }

        return f.deriveFont(size);
    }
}
