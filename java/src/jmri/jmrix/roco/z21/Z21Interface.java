package jmri.jmrix.roco.z21;

/**
 * Interface to send/receive serial information.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
public interface Z21Interface {

    void addz21Listener(Z21Listener l);

    void removez21Listener(Z21Listener l);

    //boolean status();   // true if the implementation is operational
    void sendz21Message(Z21Message m, Z21Listener l);  // 2nd arg gets the reply

}
