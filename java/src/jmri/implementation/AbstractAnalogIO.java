package jmri.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.AnalogIO;

/**
 * Base implementation of the AnalogIO interface.
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public abstract class AbstractAnalogIO extends AbstractNamedBean implements AnalogIO {

    private double _commandedValue = 0.0;
    private double _knownValue = 0.0;

    /**
     * Abstract constructor for new AnalogIO with system name
     *
     * @param systemName AnalogIO system name
     */
    public AbstractAnalogIO(@Nonnull String systemName) {
        super(systemName);
    }

    /**
     * Abstract constructor for new AnalogIO with system name and user name
     *
     * @param systemName AnalogIO system name
     * @param userName   AnalogIO user name
     */
    public AbstractAnalogIO(@Nonnull String systemName, @CheckForNull String userName) {
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
    abstract protected void sendValueToLayout(double value) throws JmriException;

    /**
     * Set the string of this AnalogIO.Called from the implementation class
     * when the layout updates this AnalogIO.
     * 
     * @param newValue the new value
     */
    protected void setValue(double newValue) {
        Object _old = this._knownValue;
        this._knownValue = newValue;
        firePropertyChange("State", _old, _knownValue); //NOI18N
    }

    /** {@inheritDoc} */
    @Override
    public void setCommandedAnalogValue(double value) throws JmriException {
        if (value == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("value is negative infinity");
        }
        if (value == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("value is positive infinity");
        }
        if (value == Double.NaN) {
            throw new IllegalArgumentException("value is not-a-number");
        }
        
        double min = getMin();
        double max = getMax();
        
        if (value < min) {
            if (cutOutOfBoundsValues()) value = min;
            else throw new JmriException("value out of bounds");
        }
        if (value > max) {
            if (cutOutOfBoundsValues()) value = max;
            else throw new JmriException("value out of bounds");
        }
        _commandedValue = value;
        sendValueToLayout(_commandedValue);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public double getCommandedAnalogValue() {
        return _commandedValue;
    }

    /** {@inheritDoc} */
    @Override
    public double getKnownAnalogValue() {
        return _knownValue;
    }

    /**
     * Cut out of bounds values instead of throwing an exception?
     * For example, if the AnalogIO is a display, it could be desired to
     * accept too long strings.
     * On the other hand, if the AnalogIO is used to send a command, a too
     * long string is an error.
     *
     * @return true if long strings should be cut
     */
    abstract protected boolean cutOutOfBoundsValues();

    /** {@inheritDoc} */
    @Override
    public int getState() {
        // A AnalogIO doesn't have a state
        return NamedBean.UNKNOWN;
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int newState) {
        // A AnalogIO doesn't have a state
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanType() {
        return Bundle.getMessage("BeanNameAnalogIO");
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

}
