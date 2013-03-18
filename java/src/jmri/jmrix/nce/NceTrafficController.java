// NceTrafficController.java

package jmri.jmrix.nce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.CommandStation;
import jmri.JmriException;

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
 * @version $Revision$
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
        NceMessage m = NceMessage.sendPacketMessage(this, packet);
	    this.sendNceMessage(m, null);
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
     * Create all commands in the ASCII format.
     */
    static public final int OPTION_FORCE_ASCII  = -1;
    /**
     * Create commands compatible with the 1999 EPROM.
     *<P>
     * This is binary for everything except service-mode CV programming operations.
     */
    static public final int OPTION_1999 = 0;
    /**
     * Create commands compatible with the 2004 EPROM.
     *<P>
     * This is binary for everything except service-mode CV programming operations.
     */
    static public final int OPTION_2004 = 10;
    /**
     * Create commands compatible with the 2006 EPROM.
     *<P>
     * This is binary for everything, including service-mode CV programming operations.
     */
    static public final int OPTION_2006 = 20;
    /**
     * Create commands compatible with the 1.28 EPROM.
     *<P>
     * For PowerCab/SB3 original pre-Nov 2012
     */
    static public final int OPTION_1_28 = 30;
    /**
     * Create commands compatible with the 1.65 EPROM.
     *<P>
     * For PowerCab/SB5/Twin update post-Nov 2012
     */
    static public final int OPTION_1_65 = 40;
    /**
     * Create all commands in the binary format.
     */
    static public final int OPTION_FORCE_BINARY = 10000;
    
    private int commandOptions = OPTION_2006;
    public boolean commandOptionSet = false;
    
    /** 
     * Control which command format should be used for various
     * commands: ASCII or binary.
     *<P>
     * The valid argument values are the class "OPTION"
     * constants, which are interpreted in the various methods to
     * get a particular message.
     *<UL>
     *<LI>{@link #OPTION_FORCE_ASCII}
     *<LI>{@link #OPTION_1999}
     *<LI>{@link #OPTION_2004}
     *<LI>{@link #OPTION_2006}
     *<LI>{@link #OPTION_1_28}
     *<LI>{@link #OPTION_1_65}
     *<LI>{@link #OPTION_FORCE_BINARY}
     *</UL>
     *
     */
     public void setCommandOptions(int val) {
        commandOptions = val;
        if (commandOptionSet) {
            log.warn("setCommandOptions called more than once");
            //new Exception().printStackTrace(); TODO need to remove for testing
        }
        commandOptionSet = true;
    }
     
    /** 
     * Determine which command format should be used for various
     * commands: ASCII or binary.
     *<P>
     * The valid return values are the class "OPTION"
     * constants, which are interpreted in the various methods to
     * get a particular message.
     *<UL>
     *<LI>{@link #OPTION_FORCE_ASCII}
     *<LI>{@link #OPTION_1999}
     *<LI>{@link #OPTION_2004}
     *<LI>{@link #OPTION_2006}
     *<LI>{@link #OPTION_1_28}
     *<LI>{@link #OPTION_1_65}
     *<LI>{@link #OPTION_FORCE_BINARY}
     *</UL>
     *
     */
    public int getCommandOptions() { return commandOptions; }
    
	/**
	 * Default when a NCE USB isn't selected in user system preferences
	 */
	static public final int USB_SYSTEM_NONE = 0;
	
	/**
	 * Create commands compatible with a NCE USB connected to a PowerCab
	 */
	static public final int USB_SYSTEM_POWERCAB = 1;
	
	/**
	 * Create commands compatible with a NCE USB connected to a Smart Booster
	 */
	static public final int USB_SYSTEM_SB3 = 2;
	
	/**
	 * Create commands compatible with a NCE USB connected to a PowerHouse
	 */
	static public final int USB_SYSTEM_POWERHOUSE = 3;
	
	/**
	 * Create commands compatible with a NCE USB with >=7.* connected to a Twin
	 */
	static public final int USB_SYSTEM_TWIN = 4;

	/**
	 * Create commands compatible with a NCE USB with SB5
	 */
	static public final int USB_SYSTEM_SB5 = 5;
		
	private int usbSystem = USB_SYSTEM_NONE;
	private boolean usbSystemSet = false;

	/**
	 * Set the type of system the NCE USB is connected to
	 * <UL>
	 * <LI>{@link #USB_SYSTEM_NONE}
	 * <LI>{@link #USB_SYSTEM_POWERCAB}
	 * <LI>{@link #USB_SYSTEM_SB3}
	 * <LI>{@link #USB_SYSTEM_POWERHOUSE}
	 * <LI>{@link #USB_SYSTEM_TWIN}
	 * <LI>{@link #USB_SYSTEM_SB5}
	 * </UL>
	 * 
	 * @param val
	 */
	public void setUsbSystem(int val) {
		usbSystem = val;
		if (usbSystemSet) {
			log.warn("setUsbSystem called more than once");
			//new Exception().printStackTrace();
		}
		usbSystemSet = true;
	}

	/**
	 * Get the type of system the NCE USB is connected to
	 * <UL>
	 * <LI>{@link #USB_SYSTEM_NONE}
	 * <LI>{@link #USB_SYSTEM_POWERCAB}
	 * <LI>{@link #USB_SYSTEM_SB3}
	 * <LI>{@link #USB_SYSTEM_POWERHOUSE}
	 * <LI>{@link #USB_SYSTEM_TWIN}
	 * <LI>{@link #USB_SYSTEM_SB5}
	 * </UL>
	 * 
	 */
	public int getUsbSystem() {return usbSystem;}
	
	/**
	 * Initializer for supported command groups
	 */
	static public final long CMDS_NONE = 0;
	
	/**
	 * Limit max accy decoder to addr 250
	 */
	static public final long CMDS_ACCYADDR250 = 0x0001;
	
	/**
	 * Supports programming track and related commands
	 */
	static public final long CMDS_PROGTRACK = 0x0002;
			
	/**
	 * Supports read AIU status commands 0x9B
	 */
	static public final long CMDS_AUI_READ = 0x004;
	
	/**
	 * Supports USB read/write memory commands 0xB3 -> 0xB5
	 */
	static public final long CMDS_MEM = 0x008;
	
	/**
	 * Support Ops Mode Pgm commands 0xAE -> 0xAF
	 */
	static public final long CMDS_OPS_PGM = 0x0010;
	
	/**
	 * Support Clock commands 0x82 -> 0x87
	 */
	static public final long CMDS_CLOCK = 0x0020;
	
	/**
	 * Support USB Interface commands 0xB1
	 */
	static public final long CMDS_USB = 0x0040;
		
	private long cmdGroups = CMDS_NONE;
	private boolean cmdGroupsSet = false;

	/**
	 * Set  the types of commands valid connected system
	 * <UL>
	 * <LI>{@link #CMDS_NONE}
	 * <LI>{@link #CMDS_ACCYADDR250)
	 * <LI>{@link #CMDS_PROGTRACK)
	 * <LI>{@link #CMDS_AUI_READ)
	 * <LI>{@link #CMDS_MEM)
	 * <LI>(@line #CMDS_OPS_PGM)
	 * </UL>
	 * 
	 * @param val
	 */
	public void setCmdGroups(long val) {
		cmdGroups = val;
		if (cmdGroupsSet) {
			log.warn("setCmdGroups called more than once");
			//new Exception().printStackTrace();
		}
		cmdGroupsSet = true;
	}

	/**
	 * Get the types of commands valid for the NCE USB and connected system
	 * <UL>
	 * <LI>{@link #CMDS_NONE}
	 * <LI>{@link #CMDS_ACCYADDR250)
	 * <LI>{@link #CMDS_PROGTRACK)
	 * <LI>{@link #CMDS_AUI_READ)
	 * <LI>{@link #CMDS_MEM)
	 * <LI>(@line #CMDS_OPS_PGM)
	 * </UL>
	 * 
	 */
	public long getCmdGroups() {return cmdGroups;}
	
	private boolean nceProgMode = false;					// Do not use exit program mode unless active
	
	/**
	 * Gets the state of the command station
	 * @return true if in programming mode
	 */
	public boolean getNceProgMode(){
		return nceProgMode;
	}
	
	/**
	 * Sets the state of the command station
	 * @param b when true, set programming mode
	 */
	public void setNceProgMode(boolean b){
		nceProgMode = b;
	}
    
    /**
	 * Check NCE EPROM and start NCE CS accessory memory poll
	 */
	protected AbstractMRMessage pollMessage() {
		
		// Check to see if command options are valid
		if (commandOptionSet == false){
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
			pollAiuStatus = new NceAIUChecker(this);
			return pollAiuStatus.nceAiuPoll();
		}

		// Start NCE memory poll for accessory states
		if (pollHandler == null)
			pollHandler = new NceTurnoutMonitor(this);

		// minimize impact to NCE CS
		mWaitBeforePoll = NceTurnoutMonitor.POLL_TIME; // default = 25

		return pollHandler.pollMessage();

	}

	NceConnectionStatus pollEprom = new NceConnectionStatus(this);
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
    	try{
    		NceMessageCheck.checkMessage(getAdapterMemo(), m);
    	} catch (JmriException e) {
    		log.error(e.getMessage());
    		new Exception().printStackTrace();
    		return;		// don't send bogus message to interface
    	}
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
        return NceMessage.getProgMode(this);
    }
    protected AbstractMRMessage enterNormalMode() {
        return NceMessage.getExitProgMode(this);
    }

    /**
     * static function returning the NceTrafficController instance to use.
     * @return The registered NceTrafficController instance for general use,
     *         if need be creating one.
     */
    @Deprecated
    public static synchronized NceTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new NceTrafficController object");
            self = new NceTrafficController();
            // set as command station too
            jmri.InstanceManager.setCommandStation(self);
        }
        return self;
    }


	public void setAdapterMemo(NceSystemConnectionMemo adaptermemo) {
		memo = adaptermemo;
	}
	
	public NceSystemConnectionMemo getAdapterMemo() {
		return memo;
	}

	private NceSystemConnectionMemo memo = null;
	static NceTrafficController self = null;
    
    /**
     * instance use of the traffic controller is no longer used for multiple connections
     */
	@Deprecated
    public void setInstance(){}
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
//    protected synchronized void setInstance() { self = this; }

    protected AbstractMRReply newReply() { 
        NceReply reply = new NceReply(this);
        reply.setBinary(replyBinary);
        return reply;
    }
    
    // pre 2006 EPROMs can't stop AIU broadcasts so we have to accept them
	protected boolean canReceive() {
		if (getCommandOptions() < OPTION_2006) {
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
			if (replyLen == 0 && getCommandOptions() < OPTION_2006) {
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
    
   public void setSystemConnectionMemo(NceSystemConnectionMemo memo){
        adaptermemo = memo;
    }
    
    NceSystemConnectionMemo adaptermemo;
   
    public String getUserName() { 
        if(adaptermemo==null) return "NCE";
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "N";
        return adaptermemo.getSystemPrefix();
    }
    
    static Logger log = LoggerFactory.getLogger(NceTrafficController.class.getName());
}


/* @(#)NceTrafficController.java */






