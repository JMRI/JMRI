package jmri.managers;

import jmri.Memory;
import jmri.implementation.DefaultMemory;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

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
        // we've decided to enforce that memory system
        // names start with IM by prepending if not present
        if (!systemName.startsWith("IM")) {
            systemName = "IM" + systemName;
        }
        return new DefaultMemory(systemName, userName);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
    }

}
