// NceTrafficController.java

package jmri.jmrix.nce;

import jmri.CommandStation;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from NCE messages. The "NceInterface" side
 * sends/receives message objects.
 * <P>
 * The connection to a NcePortController is via a pair of *Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.33 $
 */
public class NceTrafficController extends AbstractMRTrafficController implements NceInterface, CommandStation {

	public NceTrafficController() {
        super();
    }

    // The methods to implement the NceInterface

    public synchronized void addNceListener(NceListener l) {
        this.addListener(l);
    }

    public synchronized void removeNceListener(NceListener l) {
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
        NceMessage m = NceMessage.sendPacketMessage(packet);
	    NceTrafficController.instance().sendNceMessage(m, null);
    }
    
    /**
     * Forward a NceMessage to all registered NceInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((NceListener)client).message((NceMessage)m);
    }

    /**
     * Forward a NceReply to all registered NceInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((NceListener)client).reply((NceReply)r);
    }

    NceSensorManager mSensorManager = null;
    public void setSensorManager(NceSensorManager m) { mSensorManager = m; }
    public NceSensorManager getSensorManager() { return mSensorManager; }
    
    
    /**
	 * Check NCE EPROM and start NCE CS accessory memory poll
	 */
	protected AbstractMRMessage pollMessage() {
		
		// Check to see if command options are valid
		if (NceMessage.commandOptionSet == false){
			if (log.isDebugEnabled())log.debug("Command options are not valid yet!!");
			return null;
		}
		
		// Keep checking the state of the communication link by polling
		// the command station using the EPROM checker
		NceMessage m = pollEprom.nceEpromPoll();
		if (m != null){
			expectReplyEprom = true;
			return m;
		}else{
			expectReplyEprom = false;
		}
		
		// Have we checked to see if AIU broadcasts are enabled?
		
		if (pollAiuStatus == null){
			// No, do it this time
			pollAiuStatus = new NceAIUChecker();
			return pollAiuStatus.nceAiuPoll();
		}

		// Start NCE memory poll for accessory states
		if (pollHandler == null)
			pollHandler = new NceTurnoutMonitor();

		// minimize impact to NCE CS
		mWaitBeforePoll = NceTurnoutMonitor.POLL_TIME; // default = 25

		return pollHandler.pollMessage();

	}

	NceConnectionStatus pollEprom = new NceConnectionStatus();
	NceAIUChecker pollAiuStatus = null;
	NceTurnoutMonitor pollHandler = null;
	
	boolean expectReplyEprom = false;
    
 
    protected AbstractMRListener pollReplyHandler() {
        // First time through, handle reply by checking EPROM revision
    	// Second time through, handle AIU broadcast check
    	if (expectReplyEprom) return pollEprom;
    	else if (pollHandler == null) return pollAiuStatus;
    	else return pollHandler;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendNceMessage(NceMessage m, NceListener reply) {
        sendMessage(m, reply);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        replyBinary = m.isBinary();
        replyLen = ((NceMessage)m).getReplyLen();
        super.forwardToPort(m, reply);
    }
    
    protected int replyLen;
    protected boolean replyBinary;
    protected boolean unsolicitedSensorMessageSeen = false;
    
    protected AbstractMRMessage enterProgMode() {
        return NceMessage.getProgMode();
    }
    protected AbstractMRMessage enterNormalMode() {
        return NceMessage.getExitProgMode();
    }

    /**
     * static function returning the NceTrafficController instance to use.
     * @return The registered NceTrafficController instance for general use,
     *         if need be creating one.
     */
    public static synchronized NceTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new NceTrafficController object");
            self = new NceTrafficController();
            // set as command station too
            jmri.InstanceManager.setCommandStation(self);
        }
        return self;
    }

    static NceTrafficController self = null;
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
    protected synchronized void setInstance() { self = this; }

    protected AbstractMRReply newReply() { 
        NceReply reply = new NceReply();
        reply.setBinary(replyBinary);
        return reply;
    }
    
    // pre 2006 EPROMs can't stop AIU broadcasts so we have to accept them
	protected boolean canReceive() {
		if (NceMessage.getCommandOptions() < NceMessage.OPTION_2006) {
			return true;
		} else if (replyLen > 0) {
			return true;
		} else {
			if (log.isDebugEnabled())
				log.error("unsolicited character received");
			return false;
		}
	}

    protected boolean endOfMessage(AbstractMRReply msg) {
    	msg.setBinary(replyBinary);
        // first try boolean
        if (replyBinary) {
			// Attempt to detect and correctly forward AIU broadcast from pre
			// 2006 EPROMS. We'll check for three byte unsolicited message
			// starting with "A" 0x61. The second byte contains the AIU number +
			// 0x30. The third byte contains the sensors, 0x41 < s < 0x6F
			// This code is problematic, it is data sensitive.
			// We can also incorrectly forward an AIU broadcast to a routine
			// that is waiting for a reply
			if (replyLen == 0 && NceMessage.getCommandOptions() < NceMessage.OPTION_2006) {
				if (msg.getNumDataElements() == 1 && msg.getElement(0) == 0x61)
					return false;
				if (msg.getNumDataElements() == 2 && msg.getElement(0) == 0x61
						&& msg.getElement(1) >= 0x30)
					return false;
				if (msg.getNumDataElements() == 3 && msg.getElement(0) == 0x61
						&& msg.getElement(1) >= 0x30
						&& msg.getElement(2) >= 0x41
						&& msg.getElement(2) <= 0x6F)
					return true;
			}
			if (msg.getNumDataElements() >= replyLen) {
				// reset reply length so we can detect an unsolicited AIU message 
				replyLen = 0;
				return true;
			} else
				return false;
		} else {
            // detect that the reply buffer ends with "COMMAND: " (note ending
			// space)
            int num = msg.getNumDataElements();
            // ptr is offset of last element in NceReply
            int ptr = num-1;
            if ( (num >= 9) && 
                (msg.getElement(ptr)   == ' ') &&
                (msg.getElement(ptr-1) == ':') &&
                (msg.getElement(ptr-2) == 'D') )
                    return true;
                    
            // this got harder with the new PROM at the beginning of 2005.
            // It doesn't always send the "COMMAND: " prompt at the end
            // of each response. Try for the error message:
            else if ( (num >= 19) && 
                // don't check space,NL at end of buffer
                (msg.getElement(ptr-2)  == '*') &&
                (msg.getElement(ptr-3)  == '*') &&
                (msg.getElement(ptr-4)  == '*') &&
                (msg.getElement(ptr-5)  == '*') &&
                (msg.getElement(ptr-6)  == ' ') &&
                (msg.getElement(ptr-7)  == 'D') &&
                (msg.getElement(ptr-8)  == 'O') &&
                (msg.getElement(ptr-9)  == 'O') &&
                (msg.getElement(ptr-10) == 'T') &&
                (msg.getElement(ptr-11) == 'S') &&
                (msg.getElement(ptr-12) == 'R') )
                    return true;
            
            // otherwise, it's not the end
            return false;
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceTrafficController.class.getName());
}


/* @(#)NceTrafficController.java */






