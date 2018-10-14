package jmri.jmrix.cmri.serial;

/**
 * Interface to send/receive serial C/MRI information
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface SerialInterface {

    public void addSerialListener(SerialListener l);

    public void removeSerialListener(SerialListener l);

    boolean status();   // true if the implementation is operational

    void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply
}
