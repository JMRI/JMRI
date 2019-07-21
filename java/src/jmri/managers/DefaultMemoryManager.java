package jmri.managers;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.implementation.DefaultMemory;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

    public DefaultMemoryManager(InternalSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected Memory createNewMemory(String systemName, String userName) {
        // makeSystemName validates that systemName is correct
        return new DefaultMemory(makeSystemName(systemName), userName);
    }

}
