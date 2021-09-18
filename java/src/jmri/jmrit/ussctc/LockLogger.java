package jmri.jmrit.ussctc;

import javax.annotation.Nonnull;
import java.util.*;
import jmri.*;

/**
 * Manages user-level logging information from Locks and Lock-like functions.
 * Locking objects store their status as a string, which is kept for them until
 * overwritten by a newer one.
 * <p>
 * This implementation stores one (if more than one) in a Memory for display.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2019
 */
public class LockLogger {

    LockLogger(String name) {
        logMemoryName = name;
        memory = InstanceManager.getDefault(MemoryManager.class).provideMemory(logMemoryName);
    }

    /**
     * Set the current status of some Lock
     * @param logger The object providing status
     * @param status The new status value, "" for "nothing of interest"
     */
    public void setStatus(@Nonnull Object logger, @Nonnull String status) {
        log.debug("Object {} set \"{}\" was \"{}\"", logger.getClass(), status, statusMap.get(logger));
        statusMap.put(logger, status);
        // most recent always displayed if not null
        if ( ! status.isEmpty()) {
            log.trace("  writing status from call");
            memory.setValue(status);
            log(status);
            return;
        }
        // see if anything else needs to be displayed
        for (String value : statusMap.values()) {
            if ( ! value.isEmpty() ) {
                log.trace("   writing status from map: \"{}\"", value);
                memory.setValue(value);
                log(status);
                return;
            }
        }
        // should be "", but make sure
        log.trace("   clearing status");
        memory.setValue("");
    }

    public void clear() {
        log.debug("LockLogger cleared from \"{}\"",
                        memory.getValue()
        );
        statusMap.clear();
        memory.setValue("");
    }

    /**
     * Log a copy of the status. Intended to be overridden by specific LockLogger implementations.
     * @param message The status message, all filled out
     */
    void log(String message) {
        log.debug(message);
    }

    Map<Object, String> statusMap = new LinkedHashMap<>();

    // static while we decide whether to access via scripts
    final public String logMemoryName;
    final public Memory memory;

    final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LockLogger.class);
}
