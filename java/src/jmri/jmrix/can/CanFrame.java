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
     * Get if the CAN Frame has an extended header
     * @return true if extended, else false
     */
    public int getHeader();

    /**
     * Get if the CAN Frame has an extended header
     * @return true if extended, else false
     */
    public boolean isExtended();

    /**
     * Get if the CAN Frame is an RTR Frame
     * @return true if RTR, else false
     */
    public boolean isRtr();

    /**
     * Get number of data bytes in the frame
     * @return 0-8
     */
    public int getNumDataElements();

    /**
     * Get a single data byte in the frame
     * @param n the index, 0-7
     * @return the data element value
     */
    public int getElement(int n);

    /**
     * Get formatted monitor String
     * @return eg. "(12) 81 02 83"
     */
    default String monString() {
        StringBuffer buf = new StringBuffer("(");
        buf.append( Integer.toHexString(getHeader()));
        buf.append(isExtended() ? " ext)" : ")");
        for (int i = 0; i < getNumDataElements(); i++) {
            buf.append(" " + jmri.util.StringUtil.twoHexFromInt(getElement(i)));
        }
        return buf.toString();
    }
    
    
    /**
     * Get formatted toString
     * @return eg. "[12] 81 02 83"
     */
    default String getToString() {
        String s = String.format("[%x] ", getHeader());
        for (int i = 0; i < getNumDataElements(); i++) {
            if (i != 0) {
                s += " ";
            }
            s = jmri.util.StringUtil.appendTwoHexFromInt(getElement(i) & 255, s);
        }
        return s;
    }
    
    /**
     * Compare 2 CanFrames for equality
     * @param a CanFrame to test
     * @param b CanFrame to test
     * @return true if RTR, Extended, Header and Data elements match, else false
     */
    default boolean isEqual ( Object a, Object b ) {
        if (a instanceof CanFrame && b instanceof CanFrame) {
            CanFrame aa = (CanFrame) a;
            CanFrame bb = (CanFrame) b;
            if ( aa.isRtr() == bb.isRtr()
                && aa.monString().equals(bb.monString())) {
                return true;
            }
        }
        return false;
    }

}
