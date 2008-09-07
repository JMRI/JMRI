// EcosTrafficController.java

package jmri.jmrix.ecos;

import jmri.CommandStation;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
//import jmri.jmrix.ecos.serialdriver.SerialDriverAdapter;

/**
 * Converts Stream-based I/O to/from ECOS messages.  The "EcosInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a EcosPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class EcosTrafficController extends AbstractMRTrafficController implements EcosInterface, CommandStation {

	public EcosTrafficController() {
        super();
    }

    // The methods to implement the EcosInterface

    public synchronized void addEcosListener(EcosListener l) {
        this.addListener(l);
    }

    public synchronized void removeEcosListener(EcosListener l) {
        this.removeListener(l);
    }

	protected int enterProgModeDelayTime() {
		// we should to wait at least a second after enabling the programming track
		return 1000;
	}

    /**
     * CommandStation implementation
     */
    public void sendPacket(byte[] packet,int count) {
        EcosMessage m = EcosMessage.sendPacketMessage(packet);
	    EcosTrafficController.instance().sendEcosMessage(m, null);
    }
    
    /**
     * Forward a EcosMessage to all registered EcosInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((EcosListener)client).message((EcosMessage)m);
    }

    /**
     * Forward a EcosReply to all registered EcosInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((EcosListener)client).reply((EcosReply)r);
    }

    
    /**
	 * Check NCE EPROM and start NCE CS accessory memory poll
	 */
	protected AbstractMRMessage pollMessage() {
				
/* 		// Keep checking the state of the communication link by polling */
/* 		// the command station using the EPROM checker */
/* 		EcosMessage m = pollEprom.EcosEpromPoll(); */
/* 		if (m != null){ */
/* 			expectReplyEprom = true; */
/* 			return m; */
/* 		}else{ */
/* 			expectReplyEprom = false; */
/* 		} */
		

/* 		// Start Ecos memory poll for accessory states */
/* 		if (pollHandler == null) */
/* 			pollHandler = new EcosTurnoutMonitor(); */
/*  */
/* 		// minimize impact to NCE CS */
/* 		mWaitBeforePoll = NceTurnoutMonitor.POLL_TIME; // default = 25 */

/* 		return pollHandler.pollMessage(); */

        return null;
	}

	
	boolean expectReplyEprom = false;
    
 
    protected AbstractMRListener pollReplyHandler() {
/*         // First time through, handle reply by checking EPROM revision */
/*     	// Second time through, handle AIU broadcast check */
/*     	if (expectReplyEprom) return pollEprom; */
/*     	else if (pollHandler == null) return pollAiuStatus; */
/*     	else  return pollHandler; */

        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendEcosMessage(EcosMessage m, EcosListener reply) {
        sendMessage(m, reply);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        super.forwardToPort(m, reply);
    }
    
    protected boolean unsolicitedSensorMessageSeen = false;
    
    protected AbstractMRMessage enterProgMode() {
        return EcosMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return EcosMessage.getExitProgMode();
    }

    /**
     * static function returning the EcosTrafficController instance to use.
     * @return The registered EcosTrafficController instance for general use,
     *         if need be creating one.
     */
    static public EcosTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new EcosTrafficController object");
            self = new EcosTrafficController();
            // set as command station too
            jmri.InstanceManager.setCommandStation(self);
        }
        return self;
    }

    static protected EcosTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { 
        EcosReply reply = new EcosReply();
        return reply;
    }
    
    // for now, receive always OK
	protected boolean canReceive() {
        return true;
  	}

    protected boolean endOfMessage(AbstractMRReply msg) {
        // detect that the reply buffer ends with "COMMAND: " (note ending
        // space)
        int num = msg.getNumDataElements();
        // ptr is offset of last element in EcosReply
        int ptr = num-1;

        if ( (num >= 2) && 
            // check NL at end of buffer
            (msg.getElement(ptr)  == 0x0A) &&
            (msg.getElement(ptr-1) == 0x0D) &&
            (msg.getElement(ptr-2) == '>') )  {
            
            // this might be end of element, check for "<END "
                return ((EcosReply)msg).containsEnd();
            }
        
        // otherwise, it's not the end
        return false;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EcosTrafficController.class.getName());
}


/* @(#)EcosTrafficController.java */






