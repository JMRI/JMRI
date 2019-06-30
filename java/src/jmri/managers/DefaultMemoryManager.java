package jmri.managers;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Memory;
import jmri.implementation.DefaultMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String prefix = getSystemNamePrefix();
        if (systemName.isEmpty() || systemName.equals(prefix)) {
            log.error("Invalid system name for memory: \"{}\" but needed {} followed by a suffix", systemName, prefix);
            throw new IllegalArgumentException("Invalid system name for memory: \"" + systemName + "\" but needed " + prefix + " followed by a suffix");
        }
        // we've decided to enforce that memory system
        // names start with IM by prepending if not present
        if (!systemName.startsWith(prefix)) {
            systemName = makeSystemName(systemName);
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

    private final static Logger log = LoggerFactory.getLogger(DefaultMemoryManager.class);

}
