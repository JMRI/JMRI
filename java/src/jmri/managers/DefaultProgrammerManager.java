package jmri.managers;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.ProgrammerManager;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

/**
 * Provides a very basic implementation of ProgrammerManager. You give it a
 * service-mode Programmer (perhaps null) at construction time that it returns when requested; 
 * Ops Mode requests get a null in response to a request, showing there's no programmer
 * of that type.
 *<p>
 * This class also defines basic ProgrammingMode constants for the NMRA-defined modes
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2001, 2015, 2016
 */
public class DefaultProgrammerManager implements ProgrammerManager {

    /**
     * NMRA "Paged" mode
     */
    public static final ProgrammingMode PAGEMODE = new ProgrammingMode("PAGEMODE");

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the
     * bit-wise operations
     */
    public static final ProgrammingMode OPSBITMODE = new ProgrammingMode("OPSBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the byte-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBYTEMODE = new ProgrammingMode("OPSACCEXTBYTEMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the bit-wise operations. Note that this is defined as using the "normal",
     * not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBITMODE = new ProgrammingMode("OPSACCBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the bit-wise operations and "extended" addressing.
     */
    public static final ProgrammingMode OPSACCEXTBITMODE = new ProgrammingMode("OPSACCEXTBITMODE");

    /**
     * NMRA "Programming on the main" mode for stationary decoders, using only
     * the byte-wise operations. Note that this is defined as using the
     * "normal", not "extended" addressing.
     */
    public static final ProgrammingMode OPSACCBYTEMODE = new ProgrammingMode("OPSACCBYTEMODE");

    /**
     * NMRA "Address-only" mode. Often implemented as a proper subset of
     * "Register" mode, as the underlying operation is the same.
     */
    public static final ProgrammingMode ADDRESSMODE = new ProgrammingMode("ADDRESSMODE");

    /**
     * NMRA "Operations" or "Programming on the main" mode, using only the
     * byte-wise operations
     */
    public static final ProgrammingMode OPSBYTEMODE = new ProgrammingMode("OPSBYTEMODE");

    /**
     * NMRA "Direct" mode, using only the byte-wise operations
     */
    public static final ProgrammingMode DIRECTBYTEMODE = new ProgrammingMode("DIRECTBYTEMODE");

    /**
     * NMRA "Register" mode
     */
    public static final ProgrammingMode REGISTERMODE = new ProgrammingMode("REGISTERMODE");

    /**
     * NMRA "Direct" mode, using only the bit-wise operations
     */
    public static final ProgrammingMode DIRECTBITMODE = new ProgrammingMode("DIRECTBITMODE");

    /**
     * NMRA "Direct" mode, using both the bit-wise and byte-wise operations
     */
    public static final ProgrammingMode DIRECTMODE = new ProgrammingMode("DIRECTMODE");

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
    
    private Programmer mProgrammer;

    /**
     * For case where no global programmer is available
     */
    public DefaultProgrammerManager() {
        mProgrammer = null;  // indicates not present
    }
     
    public DefaultProgrammerManager(@Nonnull Programmer pProgrammer) {
        mProgrammer = pProgrammer;
    }

    public DefaultProgrammerManager(@Nonnull Programmer pProgrammer, @Nonnull jmri.jmrix.SystemConnectionMemo memo) {
        this(pProgrammer);
        this.userName = memo.getUserName();
    }

    String userName = "<Default>";

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Provides the human-readable representation for including
     * ProgrammerManagers directly in e.g. JComboBoxes, so it should return a
     * user-provided name for this particular one.
     */
    public String toString() {
        return getUserName();
    }

    public Programmer getGlobalProgrammer() {
        log.debug("return default service-mode programmer of type {}", (mProgrammer != null ? mProgrammer.getClass() : "(null)") );
        return mProgrammer;
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public Programmer reserveGlobalProgrammer() {
        return mProgrammer;
    }

    public void releaseGlobalProgrammer(@Nonnull  Programmer p) {
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }

    public void releaseAddressedProgrammer(@Nonnull AddressedProgrammer p) {
    }

    /**
     * Default programmer does not provide Ops Mode
     *
     * @return false since there's no chance of getting one
     */
    public boolean isAddressedModePossible() {
        return false;
    }

    /**
     * Default programmer doesn't depend on address
     *
     * @return false since there's no chance of getting one
     */
    public boolean isAddressedModePossible(@Nonnull jmri.LocoAddress l) {
        return isAddressedModePossible();
    }

    /**
     * Allow for implementations that do not support Service mode programming
     *
     * @return false if there's no chance of getting one
     */
    public boolean isGlobalProgrammerAvailable() {
        return true;
    }

    /**
     * Provide a default implementation of the mode (most) AddressProgrammers
     * make available.
     */
    public java.util.List<ProgrammingMode> getDefaultModes() {
        java.util.ArrayList<ProgrammingMode> retval = new java.util.ArrayList<>();
        retval.add(OPSBYTEMODE);
        return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultProgrammerManager.class.getName());
}

