// z21Interface.java

package jmri.jmrix.roco.z21;


/**
 * Interface to send/receive serial information
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 * @version			$Revision$
 */
public interface z21Interface {

    public void addz21Listener( z21Listener l);
    public void removez21Listener( z21Listener l);

    //boolean status();   // true if the implementation is operational

    void sendz21Message(z21Message m, z21Listener l);  // 2nd arg gets the reply
}


/* @(#)z21Interface.java */
