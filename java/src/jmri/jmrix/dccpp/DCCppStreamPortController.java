// DCCppStreamPortController.java
package jmri.jmrix.dccpp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a DCCpp communications port
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010,2014
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 */
public class DCCppStreamPortController extends jmri.jmrix.AbstractStreamPortController implements DCCppPortController {

    public DCCppStreamPortController(DataInputStream in, DataOutputStream out, String pname) {
        super(new DCCppSystemConnectionMemo(), in, out, pname);
    }

    public void configure() {
        // connect to a packetizing traffic controller
        DCCppTrafficController packets = new DCCppPacketizer(new DCCppCommandStation());
        packets.connectPort(this);

        this.getSystemConnectionMemo().setDCCppTrafficController(packets);

        new DCCppInitializationManager(this.getSystemConnectionMemo());

        jmri.jmrix.lenz.ActiveFlag.setActive();
    }

    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return (DCCppSystemConnectionMemo) super.getSystemConnectionMemo();
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


/* @(#)DCCppStreamPortController.java */
