package jmri.implementation;

import jmri.JmriException;

import javax.annotation.Nonnull;

/**
 * Base implementation of the StringIO interface.
 *
 * @author Bob Jacobsen  (C) 2024
 */
public class DefaultStringIO extends AbstractStringIO {

    public DefaultStringIO(@Nonnull String systemName) {
        super(systemName);
    }

    public DefaultStringIO(@Nonnull String systemName, String userName) {
        super(systemName, userName);
    }

    /** {@inheritDoc} */ 
    @Override
    protected void sendStringToLayout(@Nonnull String value) throws JmriException {
        // Only sets the known string and fires listeners.
        setString(value);
    }

    /** {@inheritDoc} */ 
    @Override
    protected boolean cutLongStrings() {
        return false;
    }

}
