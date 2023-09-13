package jmri;

import javax.annotation.CheckReturnValue;

/**
 * Represent an analog I/O on the layout.
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public interface AnalogIO extends NamedBean {

    /**
     * Is the value an absolute value or a relative value?
     * In both cases, AnalogIO.getMin() and AnalogIO.getMax() tells the
     * limits of the value.
     */
    enum AbsoluteOrRelative {
        
        ABSOLUTE(Bundle.getMessage("AnalogIO_Absolute")),
        RELATIVE(Bundle.getMessage("AnalogIO_Relative"));
        
        private final String _str;
        
        private AbsoluteOrRelative(String str) {
            _str = str;
        }
        
        @CheckReturnValue
        @Override
        public String toString() {
            return _str;
        }
    }
    
    /**
     * Show whether the analog value is stable.
     * 
     * @return true if the analog value is stable
     */
    @CheckReturnValue
    default boolean isConsistentValue() {
        return true;
    }
    
    /**
     * Change the commanded value, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     * <p>
     * The value must be a valid number, not a NaN or infinity number.
     *
     * @param value the desired analog value
     * @throws jmri.JmriException general error when setting the value fails
     * @throws IllegalArgumentException if the value is Double.NaN,
     *                                  Double.NEGATIVE_INFINITY or
     *                                  Double.POSITIVE_INFINITY
     */
    void setCommandedAnalogValue(double value) throws JmriException;

    /**
     * Query the commanded value. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     * <p>
     * The result must be a valid number, not a NaN or infinity number.
     *
     * @return the analog value
     */
    @CheckReturnValue
    double getCommandedAnalogValue();
    
    /**
     * Query the known analog value. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded value
     * will be used.
     * <p>
     * The result must be a valid number, not a NaN or infinity number.
     *
     * @return the known analog value
     */
    @CheckReturnValue
    default double getKnownAnalogValue() {
        return getCommandedAnalogValue();
    }
    
    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead (setCommandedAnalogValue). This is provided to
     * make scripts access easier to read.
     *
     * @param value the analog value
     * @throws JmriException general error when setting the state fails
     */
    @InvokeOnLayoutThread
    void setState(double value) throws JmriException;

    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead (getCommandedAnalogValue). This is provided to
     * make scripts easier to read.
     * 
     * @param v only used to select this method which returns an analog value.
     *          It's recommended to use 0.0 as the parameter.
     * @return the state
     */
    @CheckReturnValue
    double getState(double v);

    /**
     * Get the minimum value of this AnalogIO.
     * @return minimum value.
     */
    @CheckReturnValue
    double getMin();
    
    /**
     * Get the maximum value of this AnalogIO.
     * @return maximum value.
     */
    @CheckReturnValue
    double getMax();
    
    /**
     * Get the resolution of this AnalogIO.
     * @return analog resolution.
     */
    @CheckReturnValue
    double getResolution();

    /**
     * Is this AnalogIO absolute or relative?
     * @return if absolute or relative.
     */
    @CheckReturnValue
    AbsoluteOrRelative getAbsoluteOrRelative();

    /**
     * Request an update from the layout soft/hardware. May not even happen, and
     * if it does it will happen later; listen for the result.
     */
    default void requestUpdateFromLayout() {
    }

}
