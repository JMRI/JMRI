package jmri;

/**
 * Represent an analog I/O on the layout.
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public interface AnalogIO extends NamedBean {

    /**
     * The minimum value of an analog value.
     */
    public static final float MIN_VALUE = Float.MIN_VALUE;

    /**
     * The middle value of an analog value.
     * 
     * For example, if the analog value represents the speed of a moving
     * turntable, a positive value may be 'forward', a negative value may be
     * 'reverse' and middle value may be 'stop'.
     */
    public static final float MIDDLE_VALUE = (float) 0.0;

    /**
     * The maximum value of an analog value.
     */
    public static final float MAX_VALUE = Float.MAX_VALUE;

    /**
     * Show whether the analog value is stable.
     * 
     * @return true if the analog value is stable
     */
    default public boolean isConsistentState() {
        return true;
    }

    /**
     * Change the commanded state, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     *
     * @param value the desired analog value
     * @throws jmri.JmriException general error when setting the state fails
     */
    public void setCommandedAnalogValue(float value) throws JmriException;

    /**
     * Query the commanded state. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     *
     * @return the analog value
     */
    public float getCommandedAnalogValue();
    
    /**
     * Query the known analog value. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded state
     * will be used.
     *
     * @return the known analog value
     */
    default public float getKnownAnalogValue() {
        return getCommandedAnalogValue();
    }

    /**
     * Request an update from the layout soft/hardware. May not even happen, and
     * if it does it will happen later; listen for the result.
     */
    default public void requestUpdateFromLayout() {
    }
    
}
