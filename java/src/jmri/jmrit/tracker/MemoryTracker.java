package jmri.jmrit.tracker;

import jmri.*;

/**
 * Tracks train into memory object
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class MemoryTracker {

    /**
     * Create a Tracker object, providing a list of blocks to watch
     * @param b block to track.
     * @param namePrefix system name prefix.
     * @throws IllegalArgumentException when needed
     */
    public MemoryTracker(Block b, String namePrefix) throws IllegalArgumentException {
        block = b;

        // make sure Memory objects exist & remember it
        Memory m = jmri.InstanceManager.memoryManagerInstance()
                .provideMemory(namePrefix + block.getSystemName());
        namedMemory = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)
            .getNamedBeanHandle(namePrefix + block.getSystemName(), m);
        // set listener in the block
        block.addPropertyChangeListener( e -> handleChange());

        // first update
        handleChange();
    }

    private void handleChange() {
        if (log.isDebugEnabled() && (block.getValue() != null)) {
            log.debug("set value {} in block {}", block.getValue(), block.getSystemName());
        }
        Object o = block.getValue();
        if (o != null) {
            o = o.toString();
        }
        namedMemory.getBean().setValue(o);
    }

    private final NamedBeanHandle<Memory> namedMemory;
    private final Block block;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryTracker.class);
}
