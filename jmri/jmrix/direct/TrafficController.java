// TrafficController.java

package jmri.jmrix.direct;

import jmri.jmrix.AbstractPortController;

import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * Converts Stream-based I/O to/from NMRA packets and controls sending
 * to the direct interface.
 * <P>
 * This is much simpler than many other "TrafficHandler" classes, because
 * <UL>
 * <LI>It's not handling mode information, or even any information back from the
 * device; it's just sending
 * <LI>It can work with the direct packets.
 * </UL>
 * This actually bears more similarity to a pure implementation 
 * of the CommandStation interface, which is where the real guts of it is.
 * In particular, note that transmission is not a threaded operation.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class TrafficController implements jmri.CommandStation {

    public TrafficController() {
        super();
    }
    
    /**
     * static function returning the instance to use.
     * @return The registered instance for general use,
     *         if need be creating one.
     */
    static public TrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new TrafficController object");
            self = new TrafficController();
        }
        return self;
    }

    static protected TrafficController self = null;
    protected void setInstance() { self = this; }

    /**
     * Send a specific packet to the rails.
     *
     * @param packet Byte array representing the packet, including
     * the error-correction byte.  Must not be null.
     * @param repeats Number of times to repeat the transmission,
     *      but is ignored in the current implementation
     */
    public void sendPacket(byte[] packet, int repeats) {

        if (repeats!=1) log.warn("Only single transmissions currently available");

		// convert packet (in byte form) into bits
		int[] msgAsInt = MakePacket.createStream(packet);
		
		if (msgAsInt[0] == 0) {
			// failed to make packet
			log.error("Failed to convert packet to transmitable form: "+packet);
			return;
		}
		
		// have to recopy & reformat, as there's only a byte write in Java 1
		// note that msgAsInt has 0th byte containing length still
		byte[] msg = new byte[msgAsInt[0]];
		for (int i = 1; i<msg.length; i++) msg[i] = (byte) (msgAsInt[i]&0xFF);
		
        // and stream the resulting byte array
        try {
            if (ostream != null) {
                if (log.isDebugEnabled()) {
                    String f = "write message: ";
                    for (int i = 0; i<msg.length; i++) f=f+Integer.toHexString(0xFF&msg[i])+" ";
                    log.debug(f);
                }
                ostream.write(msg);
            }
            else {
                // no stream connected
                log.warn("sendMessage: no connection established");
            }
        }
        catch (Exception e) {
            log.warn("sendMessage: Exception: "+e.toString());
        }

    }

    // methods to connect/disconnect to a source of data in a AbstractPortController
    private AbstractPortController controller = null;

    public boolean status() { 
    	return (ostream != null & istream != null);
    }

    /**
     * Make connection to existing PortController object.
     */
    public void connectPort(AbstractPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null)
            log.warn("connectPort: connect called while connected");
        else
            log.debug("connectPort invoked");
        controller = p;
    }

    /**
     * Break connection to existing PortController object. Once broken,
     * attempts to send via "message" member will fail.
     */
    public void disconnectPort(AbstractPortController p) {
        istream = null;
        ostream = null;
        if (controller != p)
            log.warn("disconnectPort: disconnect called from non-connected AbstractPortController");
        controller = null;
    }

    // data members to hold the streams
    protected DataInputStream istream = null;
    protected OutputStream ostream = null;


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrafficController.class.getName());
}


/* @(#)TrafficController.java */

