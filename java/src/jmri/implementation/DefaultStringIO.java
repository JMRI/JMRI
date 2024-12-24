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

    /**
     * Sends the string to the layout.
     * The string [u]must not[/u] be longer than the value of getMaximumLength()
     * unless that value is zero. Some microcomputers have little memory and
     * it's very important that this method is never called with too long strings.
     *
     * @param value the desired string value
     * @throws jmri.JmriException general error when setting the value fails
     */
    @Override
    protected void sendStringToLayout(@Nonnull String value) throws JmriException {
        // does nothing in this implementation
    }

    /**
     * Cut long strings instead of throwing an exception?
     * For example, if the StringIO is a display, it could be desired to
     * accept too long strings.
     * On the other hand, if the StringIO is used to send a command, a too
     * long string is an error.
     *
     * @return true if long strings should be cut
     */
    @Override
    protected boolean cutLongStrings() {
        return false;
    }

}
