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
     * @param lockLogger the logger on which to emit status messages
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear(LockLogger lockLogger);

    /**
     * Check a collection of Locks, handling the logging etc as needed.
     * @param locks collection of locks.
     * @param lockLogger the logger on which to emit status messages
     * @return false if a lock is not clear, else true.
     */
    static public boolean checkLocksClear(List<Lock> locks, LockLogger lockLogger) {
        lockLogger.clear();
        if (locks != null) {
            for (Lock lock : locks) {
                if ( ! lock.isLockClear(lockLogger)) return false; // return immediately so that lockLogger isn't overwritten
            }
        }
        return true;
    }

    // static while we decide whether to access via scripts
    // final static LockLogger lockLogger = new LockLogger();
    final static LockLogger signalLockLogger  = new LockLogger("IMUSS CTC:SIGNAL LOCK:1:LOG");
    final static LockLogger turnoutLockLogger = new LockLogger("IMUSS CTC:TURNOUT LOCK:1:LOG");
}
