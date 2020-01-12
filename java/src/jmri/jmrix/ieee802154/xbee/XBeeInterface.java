package jmri.jmrix.ieee802154.xbee;

/**
 * Interface to send/receive xbee information
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
public interface XBeeInterface {

    public void addXBeeListener(XBeeListener l);

    public void removeXBeeListener(XBeeListener l);

    //boolean status();   // true if the implementation is operational
    public void sendXBeeMessage(XBeeMessage m, XBeeListener l);  // 2nd arg gets the reply
}



