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

    public void setHeader(int h);

    public void setExtended(boolean b);

    public void setRtr(boolean b);

    public void setNumDataElements(int n);

    public void setElement(int n, int v);

}
