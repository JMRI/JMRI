package jmri.implementation;

import javax.annotation.CheckReturnValue;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.StringIO;

import javax.annotation.Nonnull;

/**
 * Base implementation of the StringIO interface.
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public abstract class AbstractStringIO extends AbstractNamedBean implements StringIO {

    private String _commandedString = "";
    private String _knownString = "";

    /**
     * Abstract constructor for new StringIO with system name
     *
     * @param systemName StringIO system name
     */
    public AbstractStringIO(@Nonnull String systemName) {
        super(systemName);
    }

    /**
     * Abstract constructor for new StringIO with system name and user name
     *
     * @param systemName StringIO system name
     * @param userName   StringIO user name
     */
    public AbstractStringIO(@Nonnull String systemName, String userName) {
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
    abstract protected void sendStringToLayout(@Nonnull String value) throws JmriException;

    /**
     * Set the string of this StringIO.
     * Called from the implementation class when the layout updates this StringIO.
     */
    protected void setString(@Nonnull String newValue) {
        Object _old = this._knownString;
        this._knownString = newValue;
        firePropertyChange("State", _old, _knownString); //NOI18N
    }

    /** {@inheritDoc} */
    @Override
    public void setCommandedStringValue(@Nonnull String value) throws JmriException {
        int maxLength = getMaximumLength();
        if ((maxLength > 0) && (value.length() > maxLength)) {
            if (cutLongStrings()) {
                value = value.substring(0, maxLength);
            } else {
                throw new JmriException("String too long");
            }
        }
        _commandedString = value;
        sendStringToLayout(_commandedString);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getCommandedStringValue() {
        return _commandedString;
    }

    /** {@inheritDoc} */
    @Override
    public String getKnownStringValue() {
        return _knownString;
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
    abstract protected boolean cutLongStrings();

    /** {@inheritDoc} */
    @Override
    public int getState() {
        // A StringIO doesn't have a state
        return NamedBean.UNKNOWN;
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int newState) {
        // A StringIO doesn't have a state
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameStringIO");
    }

    /**
     * {@inheritDoc} 
     * 
     * Do a string comparison.
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        return suffix1.compareTo(suffix2);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String toString() {
        return this.getClass().getName() + " (" + this.getSystemName() + ")"; //NOI18N
    }

}
