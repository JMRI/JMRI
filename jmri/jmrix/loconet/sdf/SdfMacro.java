// SdfMacro.java

package jmri.jmrix.loconet.sdf;

/**
 * Common base for all the SDF macros defined by Digitrax
 * for their sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

abstract class SdfMacro {

    /**
     * Name used by the macro in the SDF definition
     */
    abstract public String name();
    
    /**
     * Provide number of bytes defined by this macro
     */
    abstract public int length();
    
    /**
     * Provide a single-line representation,
     * including the trailing newline
     */
    abstract public String toString();
}

/* @(#)SdfMacro.java */
