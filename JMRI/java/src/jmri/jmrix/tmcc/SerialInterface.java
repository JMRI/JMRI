package jmri.jmrix.tmcc;

/**
 * Interface to send/receive serial TMCC information.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006
 */
public interface SerialInterface {

    public void addSerialListener(SerialListener l);

    public void removeSerialListener(SerialListener l);

    boolean status(); // true if the implementation is operational

    void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply

}
