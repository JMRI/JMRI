// SRCPTrafficController.java

package jmri.jmrix.srcp;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from SRCP messages.  The "SRCPInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SRCPPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 * 
 * @author Bob Jacobsen  Copyright (C) 2001
 * @version $Revision: 1.3 $
 */
public class SRCPTrafficController extends AbstractMRTrafficController
	implements SRCPInterface {

    public SRCPTrafficController() {
        super();
    }

    // The methods to implement the SRCPInterface

    public synchronized void addSRCPListener(SRCPListener l) {
        this.addListener(l);
    }

    public synchronized void removeSRCPListener(SRCPListener l) {
        this.removeListener(l);
    }


    /**
     * Forward a SRCPMessage to all registered SRCPInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SRCPListener)client).message((SRCPMessage)m);
    }

    /**
     * Forward a SRCPReply to all registered SRCPInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SRCPListener)client).reply((SRCPReply)m);
    }

    public void setSensorManager(jmri.SensorManager m) { }
    protected AbstractMRMessage pollMessage() {
		return null;
    }
    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
        sendMessage(m, reply);
    }

    protected AbstractMRMessage enterProgMode() {
        return SRCPMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return SRCPMessage.getExitProgMode();
    }

    /**
     * static function returning the SRCPTrafficController instance to use.
     * 
     * @return The registered SRCPTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SRCPTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new SRCP TrafficController object");
            self = new SRCPTrafficController();
        }
        return self;
    }

    static volatile protected SRCPTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { return new SRCPReply(); }

    protected boolean endOfMessage(AbstractMRReply msg) {
        int index = msg.getNumDataElements()-1;
        if (msg.getElement(index) == 0x0D) return true;
        if (msg.getElement(index) == 0x0A) return true;
        else return false;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPTrafficController.class.getName());
}


/* @(#)SRCPTrafficController.java */

