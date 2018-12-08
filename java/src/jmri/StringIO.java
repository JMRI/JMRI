package jmri;

/**
 * Represent an string I/O on the layout.
 * <P>
 * A StringIO could for example being a display connected to an Arduino
 * microcomputer that shows train departures of a station.
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public interface StringIO extends NamedBean {

    /**
     * Change the commanded state, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     *
     * @param value the desired string value
     * @throws jmri.JmriException general error when setting the state fails
     */
    public void setCommandedStringValue(String value) throws JmriException;

    /**
     * Query the commanded string. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     *
     * @return the analog value
     */
    public String getCommandedStringValue();

    /**
     * Query the known analog value. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded state
     * will be used.
     *
     * @return the known analog value
     */
    default public String getKnownStringValue() {
        return getCommandedStringValue();
    }

    /**
     * Get the maximum length of string that this StringIO can handle.
     * @return the maximum length or 0 if arbitrary lengths are accepted.
     */
    default public int getMaximumLength() {
        return 0;
    }

    /**
     * Request an update from the layout soft/hardware. May not even happen, and
     * if it does it will happen later; listen for the result.
     */
    default public void requestUpdateFromLayout() {
    }

}
