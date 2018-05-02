package jmri.jmrix.srcp;

/**
 * Layout interface, similar to command station
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public interface SRCPInterface {

    public void addSRCPListener(SRCPListener l);

    public void removeSRCPListener(SRCPListener l);

    boolean status();   // true if the implementation is operational

    void sendSRCPMessage(SRCPMessage m, SRCPListener l);  // 2nd arg gets the reply
}



