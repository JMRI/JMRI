/* ProgrammingMode.java */

package jmri;

/**
 * Denote a single programming mode.
 * <P>
 * For now, during migration, this is connected to the older numbers 
 * from {@link Programmer}.  Those numbers will eventually go away.
 * <p>
 * We define a number of modes here as constants because they are standard across so
 * many DCC-specific implementations, they're defacto standards.
 * <p>
 * Eventually, the specific definitions (and Bundle entries) should
 * retreat to specific Programmer implementations.  The whole idea
 * is to have code deal with the modes the  specific {@link Programmer} provides, 
 * not make assumptions about global values.
 *
 * @see         jmri.Programmer
 * @see         jmri.ProgrammerManager
 * @see         jmri.jmrix.AbstractProgrammer
 * @author	Bob Jacobsen Copyright (C) 2014
 */
@net.jcip.annotations.Immutable
public final class ProgrammingMode  {


    public ProgrammingMode(String standardName) {
        this.standardName = standardName;
        this.localName = Bundle.getMessage(standardName);  // note in jmri package
    }

    /*
     * Create an instance where you directly
     * provide the localized name.  
     * <p>
     * This is useful when you want to create a ProgrammingMode deep
     * within some system-specific code, and don't want to add
     * translations to the top-level jmri.Bundle.
     */
    public ProgrammingMode(String standardName, String localName) {
        this.standardName = standardName;
        this.localName = localName;
    }

    /**
     * Display the localized (human readable) name
     */
    @Override
    public String toString() { return localName; }
    
    /**
     * Return the standard (not localized, human readable) name
     */
    public String getStandardName() { return standardName; }
        
    @Override
    public boolean equals(Object o) {
        if (this == o ) return true;
        if (! (o instanceof ProgrammingMode)) return false;
        ProgrammingMode that = (ProgrammingMode) o;
        return this.standardName.equals(that.standardName);
    }
    
    @Override
    public int hashCode() {
        return standardName.hashCode();
    }
    
    private String standardName;
    private String localName;
    
    
    /**
     * No programming mode available
     */
    public static final ProgrammingMode NONE	    =  new ProgrammingMode("NONE");

    /**
     * NMRA "Register" mode
     */
    public static final ProgrammingMode REGISTERMODE    = new ProgrammingMode("REGISTERMODE");

    /**
     * NMRA "Paged" mode
     */
    public static final ProgrammingMode PAGEMODE        = new ProgrammingMode("PAGEMODE");
    
    /**
     * NMRA "Direct" mode, using only the bit-wise operations
     */
    public static final ProgrammingMode DIRECTBITMODE   = new ProgrammingMode("DIRECTBITMODE");

    /**
     * NMRA "Direct" mode, using only the byte-wise operations
     */
    public static final ProgrammingMode DIRECTBYTEMODE  = new ProgrammingMode("DIRECTBYTEMODE");
    /**
     * NMRA "Address-only" mode. Often implemented as
     * a proper subset of "Register" mode, as the 
     * underlying operation is the same.
     */
    public static final ProgrammingMode ADDRESSMODE     = new ProgrammingMode("ADDRESSMODE");

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the byte-wise operations
     */
    public static final ProgrammingMode OPSBYTEMODE     = new ProgrammingMode("OPSBYTEMODE");
    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the bit-wise operations
     */
    public static final ProgrammingMode OPSBITMODE      = new ProgrammingMode("OPSBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the byte-wise operations. Note that this is 
     * defined as using the "normal", not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBYTEMODE  = new ProgrammingMode("OPSACCBYTEMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the bit-wise operations. Note that this is 
     * defined as using the "normal", not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBITMODE   = new ProgrammingMode("OPSACCBITMODE");
    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the byte-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBYTEMODE = new ProgrammingMode("OPSACCEXTBYTEMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, 
     * using only the bit-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBITMODE  = new ProgrammingMode("OPSACCEXTBITMODE");


// For the record, these were the original numerical definitions:
//     public static final ProgrammingMode NONE	    =  new ProgrammingMode("NONE", 0);
//     public static final ProgrammingMode REGISTERMODE    = new ProgrammingMode("REGISTERMODE", 11);
//     public static final ProgrammingMode PAGEMODE        = new ProgrammingMode("PAGEMODE", 21);
//     public static final ProgrammingMode DIRECTBITMODE   = new ProgrammingMode("DIRECTBITMODE", 31);
//     public static final ProgrammingMode DIRECTBYTEMODE  = new ProgrammingMode("DIRECTBYTEMODE", 32);
//     public static final ProgrammingMode ADDRESSMODE     = new ProgrammingMode("ADDRESSMODE", 41);
//     public static final ProgrammingMode OPSBYTEMODE     = new ProgrammingMode("OPSBYTEMODE", 101);
//     public static final ProgrammingMode OPSBITMODE      = new ProgrammingMode("OPSBITMODE", 102);
//     public static final ProgrammingMode OPSACCBYTEMODE  = new ProgrammingMode("OPSACCBYTEMODE", 111);
//     public static final ProgrammingMode OPSACCBITMODE   = new ProgrammingMode("OPSACCBITMODE", 112);
//     public static final ProgrammingMode OPSACCEXTBYTEMODE = new ProgrammingMode("OPSACCEXTBYTEMODE", 121);
//     public static final ProgrammingMode OPSACCEXTBITMODE  = new ProgrammingMode("OPSACCEXTBITMODE", 122);


}


/* @(#)ProgrammingMode.java */
