package jmri;

import javax.annotation.Nonnull;

/**
 * Represent an string I/O on the layout.
 * <p>
 * A StringIO could for example be a display connected to an Arduino
 * microcomputer that shows train departures of a station.
 *
 * @author Daniel Bergqvist Copyright (c) 2018
 */
public interface StringIO extends NamedBean {

    /**
     * Change the commanded value, which results in the relevant command(s)
     * being sent to the hardware. The exception is thrown if there are problems
     * communicating with the layout hardware.
     *
     * @param value the desired string value
     * @throws jmri.JmriException general error when setting the value fails
     */
    public void setCommandedStringValue(@Nonnull String value) throws JmriException;

    /**
     * Query the commanded string. This is a bound parameter, so you can also
     * register a listener to be informed of changes.
     *
     * @return the string value
     */
    @Nonnull
    public String getCommandedStringValue();

    /**
     * Query the known string value. This is a bound parameter, so you can also
     * register a listener to be informed of changes. A result is always
     * returned; if no other feedback method is available, the commanded value
     * will be used.
     *
     * @return the known string value
     */
    @Nonnull
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
