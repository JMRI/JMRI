package jmri.jmrit.ussctc;

/**
 * A Lock is the base interface for implementations that check layout conditions.
 * <p>
 * Locks are used in multiple places: Machine and Field.
 * They can be used to lock out various operations: Turnout, Signal.
 * Those contexts are handled in how Locks are configured into other objects.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public interface Lock {

    enum Valid {
        FIELD_TURNOUT,
        FIELD_SIGNAL,
        MACHINE_TURNOUT,
        MACHINE_SIGNAL
    }
    
    /**
     * Test the lock conditions
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear();  
    
    static String logMemoryName = "IMUSS CTC:LOCK:1:LOG";
}
