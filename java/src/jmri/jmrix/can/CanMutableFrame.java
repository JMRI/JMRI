package jmri.jmrix.can;

/**
 * Base interface for mutable messages in a CANbus based message/reply protocol.
 * <p>
 * It is expected that any CAN based system will be based upon basic CAN
 * concepts such as ID/header (standard or extended), Normal and RTR frames and
 * a data field.
 * <p>
 * "header" refers to the full 11 or 29 bit header; which mode is separately set
 * via the "extended" parameter
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2009, 2010
 */
public interface CanMutableFrame extends CanFrame {

    /**
     * Set the CAN Frame header.
     * @param h new header value
     */
    public void setHeader(int h);

    /**
     * Set if the CAN Frame header is extended.
     * @param b true if extended, else false
     */
    public void setExtended(boolean b);

    /**
     * Set if the CAN Frame is an RTR Frame.
     * @param b true if RTR, else false
     */
    public void setRtr(boolean b);

    /**
     * Set the number of data elements in the main CAN Frame body.
     * @param n true number of data bytes, 0-8
     */
    public void setNumDataElements(int n);

    /**
     * Get a single data byte in the frame.
     * @param n the index, 0-7
     * @param v the new value, 0-255
     */
    public void setElement(int n, int v);
    
    /**
     * Set the CAN Frame data elements by int array.
     *
     * @param d array of CAN Frame data bytes, max 8
     */
    default void setData(int[] d) {
        int len = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            setElement(i,d[i]);
        }
    }
    
    /**
     * Set the CAN Frame data elements by byte array.
     *
     * @param d array of CAN Frame data bytes, max 8
     */
    default void setData(byte[] d) {
        int len = (d.length <= 8) ? d.length : 8;
        for (int i = 0; i < len; i++) {
            setElement(i,d[i] & 0xFF);
        }
    }

}
