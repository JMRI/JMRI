package jmri.jmrit.ussctc;

import java.util.*;

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

    /**
     * Check a collection of Locks, handling the logging etc as needed.
     * @param locks collection of locks.
     * @return false if a lock is not clear, else true.
     */
    static public boolean checkLocksClear(List<Lock> locks) {
        lockLogger.clear();
        if (locks != null) {
            for (Lock lock : locks) {
                if ( ! lock.isLockClear()) return false; // return immediately so that lockLogger isn't overwritten
            }
        }
        return true;
    }

    // static while we decide whether to access via scripts
    final static LockLogger lockLogger = new LockLogger();
}
