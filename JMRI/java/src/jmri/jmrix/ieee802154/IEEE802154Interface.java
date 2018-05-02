package jmri.jmrix.ieee802154;

/**
 * Interface to send/receive serial information
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
public interface IEEE802154Interface {

    public void addIEEE802154Listener(IEEE802154Listener l);

    public void removeIEEE802154Listener(IEEE802154Listener l);

    //boolean status();   // true if the implementation is operational
    void sendIEEE802154Message(IEEE802154Message m, IEEE802154Listener l);  // 2nd arg gets the reply

}
