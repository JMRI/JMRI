package jmri.managers;

import jmri.Memory;
import jmri.implementation.DefaultMemory;

/**
 * Provide the concrete implementation for the Internal Memory Manager.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public class DefaultMemoryManager extends AbstractMemoryManager {

    public String getSystemPrefix() {
        return "I";
    }

    protected Memory createNewMemory(String systemName, String userName) {
        // we've decided to enforce that memory system
        // names start with IM by prepending if not present
        if (!systemName.startsWith("IM")) {
            systemName = "IM" + systemName;
        }
        return new DefaultMemory(systemName, userName);
    }

}
