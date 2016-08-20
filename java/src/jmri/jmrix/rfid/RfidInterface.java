package jmri.jmrix.rfid;

/**
 * Interface to send/receive serial information
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008, 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public interface RfidInterface {

    public void addRfidListener(RfidListener l);

    public void removeRfidListener(RfidListener l);

    boolean status();   // true if the implementation is operational

    void sendRfidMessage(RfidMessage m, RfidListener l);  // 2nd arg gets the reply
}
