package jmri.managers;

import jmri.Memory;
import jmri.implementation.DefaultMemory;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    protected Memory createNewMemory(String systemName, String userName) {
        // makeSystemName validates that systemName is correct
        return new DefaultMemory(makeSystemName(systemName), userName);
    }

}
