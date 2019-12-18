package jmri.jmrix.can;

/**
 * Base interface for immutable messages in a CANbus based message/reply
 * protocol.
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
@javax.annotation.concurrent.Immutable
public interface CanFrame {

    /**
     * Get the CAN Frame header.
     * @return header value
     */
    public int getHeader();

    /**
     * Get if the CAN Frame has an extended header.
     * @return true if extended, else false
     */
    public boolean isExtended();

    /**
     * Get if the CAN Frame is an RTR Frame.
     * @return true if RTR, else false
     */
    public boolean isRtr();

    /**
     * Get number of data bytes in the frame.
     * @return 0-8
     */
    public int getNumDataElements();

    /**
     * Get a single data byte in the frame.
     * @param n the index, 0-7
     * @return the data element value
     */
    public int getElement(int n);

    /**
     * Get formatted monitor String.
     * Includes if Frame is Extended.
     * Header value at start.
     * All values hex format.
     * Only valid data elements are included.
     * @return eg. "(1A ext) 81 EA 83 00 12"
     */
    default String monString() {
        StringBuilder buf = new StringBuilder(32);
        buf.append("(");
        buf.append( Integer.toHexString(getHeader()));
        buf.append(isExtended() ? " ext)" : ")");
        appendHexElements(buf);
        return buf.toString();
    }
    
    
    /**
     * Get formatted toString.
     * Does NOT include if Frame is Extended.
     * All values hex format.
     * Only valid data elements are included.
     * @return eg. "[12] 81 EA 83"
     */
    default String getToString() {
        StringBuilder buf = new StringBuilder(28);
        buf.append("[");
        buf.append( Integer.toHexString(getHeader()));
        buf.append("]");
        appendHexElements(buf);
        return buf.toString();
    }
    
    /**
     * Append the hex value of the data elements to a StringBuilder.
     * @param sb to append the hex values to
     */
    default void appendHexElements(StringBuilder sb) {
        for (int i = 0; i < getNumDataElements(); i++) {
            sb.append(" ");
            sb.append(jmri.util.StringUtil.twoHexFromInt(getElement(i)));
        }
    }
    
    /**
     * Compare 2 CanFrames for equality.
     * @param a CanFrame to test
     * @param b CanFrame to test
     * @return true if RTR, Extended, Header and Data elements match, else false
     */
    default boolean isEqual ( Object a, Object b ) {
        if (a instanceof CanFrame && b instanceof CanFrame) {
            CanFrame aa = (CanFrame) a;
            CanFrame bb = (CanFrame) b;
            if ( aa.isRtr() == bb.isRtr()
                && aa.isExtended() == bb.isExtended()
                && aa.getHeader() == bb.getHeader()
                && dataFramesEqual(aa,bb)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compare 2 CanFrame data elements for equality.
     * @param a CanFrame to test
     * @param b CanFrame to test
     * @return true if Data elements match, else false
     */
    default boolean dataFramesEqual( CanFrame a, CanFrame b) {
        if (a.getNumDataElements() != b.getNumDataElements()) {
            return false;
        }
        for (int i = 0; i < a.getNumDataElements(); i++) {
            if (a.getElement(i) != b.getElement(i)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if the CAN Frame is extended OR RtR.
     * @return true if either extended or RtR, else false
     */
    default boolean extendedOrRtr() {
        return ( isExtended() || isRtr() );
    }

}
