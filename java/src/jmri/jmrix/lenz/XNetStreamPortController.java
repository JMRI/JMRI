// XNetStreamPortController.java
package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010,2014
 * @version	$Revision$
 */
public class XNetStreamPortController extends jmri.jmrix.AbstractStreamPortController implements XNetPortController {

    public XNetStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new XNetSystemConnectionMemo(), in, out, pname);
    }

    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    public XNetSystemConnectionMemo getSystemConnectionMemo() {
        return (XNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    public boolean status() {
        return true;
    }

    /**
     * Can the port accept additional characters?
     */
    public boolean okToSend() {
        return (true);
    }

    /**
     * we need a way to say if the output buffer is empty or full this should
     * only be set to false by external processes
     *
     */
    synchronized public void setOutputBufferEmpty(boolean s) {
    }

}


/* @(#)XNetStreamPortController.java */
