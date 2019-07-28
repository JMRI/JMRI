package jmri.jmrix.nce;

import jmri.CommandStation;
import jmri.JmriException;
import jmri.NmraPacket;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from NCE messages. The "NceInterface" side
 * sends/receives message objects.
 * <p>
 * The connection to a NcePortController is via a pair of *Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author ken cameron Copyright (C) 2013
 */
public class NceTrafficController extends AbstractMRTrafficController implements NceInterface, CommandStation {

    /**
     * Create a new NCE SerialTrafficController instance. Simple implementation.
     */
    public NceTrafficController() {
        super();
    }

    // The methods to implement the NceInterface
    @Override
    public synchronized void addNceListener(NceListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeNceListener(NceListener l) {
        this.removeListener(l);
    }

    @Override
    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming track
        return 1000;
    }

    /**
     * CommandStation implementation
     */
    @Override
    public boolean sendPacket(byte[] packet, int count) {
        NceMessage m;

        boolean isUsb = ((getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERCAB
                || getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN));

        if (NmraPacket.isAccSignalDecoderPkt(packet)
                && (NmraPacket.getAccSignalDecoderPktAddress(packet) > 0)
                && (NmraPacket.getAccSignalDecoderPktAddress(packet) < 2048)) {
            // intercept only those NMRA signal cmds we can handle with NCE binary commands
            int addr = NmraPacket.getAccSignalDecoderPktAddress(packet);
            int aspect = packet[2];
            log.debug("isAccSignalDecoderPkt(packet) sigAddr ={}, aspect ={}", addr, aspect);
            m = NceMessage.createAccySignalMacroMessage(this, 5, addr, aspect);
        } else if (isUsb && NmraPacket.isAccDecoderPktOpsMode(packet)) {
            // intercept NMRA accessory decoder ops programming cmds to USB systems
            int accyAddr = NmraPacket.getAccDecoderPktOpsModeAddress(packet);
            int cvAddr = (((0x03 & packet[2]) << 8) | (0xFF & packet[3])) + 1;
            int cvData = (0xFF & packet[4]);
            log.debug("isAccDecoderPktOpsMode(packet) accyAddr ={}, cvAddr = {}, cvData ={}", accyAddr, cvAddr, cvData);
            m = NceMessage.createAccDecoderPktOpsMode(this, accyAddr, cvAddr, cvData);
        } else if (isUsb && NmraPacket.isAccDecoderPktOpsModeLegacy(packet)) {
            // intercept NMRA accessory decoder ops programming cmds to USB systems
            int accyAddr = NmraPacket.getAccDecoderPktOpsModeLegacyAddress(packet);
            int cvData = (0xFF & packet[3]);
            int cvAddr = (((0x03 & packet[1]) << 8) | (0xFF & packet[2])) + 1;
            log.debug("isAccDecoderPktOpsModeLegacy(packet) accyAddr ={}, cvAddr = {}, cvData ={}", accyAddr, cvAddr, cvData);
            m = NceMessage.createAccDecoderPktOpsMode(this, accyAddr, cvAddr, cvData);
        } else {
            m = NceMessage.sendPacketMessage(this, packet);
            if (m == null) {
                return false;
            }
        }
        this.sendNceMessage(m, null);
        return true;
    }

    /**
     * Forward a NceMessage to all registered NceInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((NceListener) client).message((NceMessage) m);
    }

    /**
     * Forward a NceReply to all registered NceInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((NceListener) client).reply((NceReply) r);
    }

    NceSensorManager mSensorManager = null;

    public void setSensorManager(NceSensorManager m) {
        mSensorManager = m;
    }

    public NceSensorManager getSensorManager() {
        return mSensorManager;
    }

    /**
     * Create all commands in the ASCII format.
     */
    static public final int OPTION_FORCE_ASCII = -1;
    /**
     * Create commands compatible with the 1999 EPROM.
     * <p>
     * This is binary for everything except service-mode CV programming
     * operations.
     */
    static public final int OPTION_1999 = 0;
    /**
     * Create commands compatible with the 2004 EPROM.
     * <p>
     * This is binary for everything except service-mode CV programming
     * operations.
     */
    static public final int OPTION_2004 = 10;
    /**
     * Create commands compatible with the 2006 EPROM.
     * <p>
     * This is binary for everything, including service-mode CV programming
     * operations.
     */
    static public final int OPTION_2006 = 20;
    /**
     * Create commands compatible with the 1.28 EPROM.
     * <p>
     * For PowerCab/SB3 original pre-Nov 2012
     */
    static public final int OPTION_1_28 = 30;
    /**
     * Create commands compatible with the 1.65 EPROM.
     * <p>
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
     * Control which command format should be used for various commands: ASCII
     * or binary.
     * <p>
     * The valid argument values are the class "OPTION" constants, which are
     * interpreted in the various methods to get a particular message.
     * <ul>
     * <li>{@link #OPTION_FORCE_ASCII}
     * <li>{@link #OPTION_1999}
     * <li>{@link #OPTION_2004}
     * <li>{@link #OPTION_2006}
     * <li>{@link #OPTION_1_28}
     * <li>{@link #OPTION_1_65}
     * <li>{@link #OPTION_FORCE_BINARY}
     * </ul>
     *
     * @param val command station options
     *
     */
    public void setCommandOptions(int val) {
        commandOptions = val;
        if (commandOptionSet) {
            log.warn("setCommandOptions called more than once");
        }
        commandOptionSet = true;
    }

    /**
     * Determine which command format should be used for various commands: ASCII
     * or binary.
     * <p>
     * The valid return values are the class "OPTION" constants, which are
     * interpreted in the various methods to get a particular message.
     * <ul>
     * <li>{@link #OPTION_FORCE_ASCII}
     * <li>{@link #OPTION_1999}
     * <li>{@link #OPTION_2004}
     * <li>{@link #OPTION_2006}
     * <li>{@link #OPTION_1_28}
     * <li>{@link #OPTION_1_65}
     * <li>{@link #OPTION_FORCE_BINARY}
     * </ul>
     *
     * @return command station options value
     *
     */
    public int getCommandOptions() {
        return commandOptions;
    }

    /**
     * Default when a NCE USB isn't selected in user system preferences.
     * Also the case when Serial or Simulator is selected.
     */
    public static final int USB_SYSTEM_NONE = 0;

    /**
     * Create commands compatible with a NCE USB connected to a PowerCab.
     */
    public static final int USB_SYSTEM_POWERCAB = 1;

    /**
     * Create commands compatible with a NCE USB connected to a Smart Booster.
     */
    public static final int USB_SYSTEM_SB3 = 2;

    /**
     * Create commands compatible with a NCE USB connected to a PowerPro.
     */
    public static final int USB_SYSTEM_POWERPRO = 3;

    /**
     * Create commands compatible with a NCE USB with {@literal >=7.*} connected
     * to a Twin.
     */
    public static final int USB_SYSTEM_TWIN = 4;

    /**
     * Create commands compatible with a NCE USB with SB5.
     */
    public static final int USB_SYSTEM_SB5 = 5;

    private int usbSystem = USB_SYSTEM_NONE;
    private boolean usbSystemSet = false;

    /**
     * Set the type of system the NCE USB is connected to
     * <ul>
     * <li>{@link #USB_SYSTEM_NONE}
     * <li>{@link #USB_SYSTEM_POWERCAB}
     * <li>{@link #USB_SYSTEM_SB3}
     * <li>{@link #USB_SYSTEM_POWERPRO}
     * <li>{@link #USB_SYSTEM_TWIN}
     * <li>{@link #USB_SYSTEM_SB5}
     * </ul>
     *
     * @param val usb command station options
     *
     */
    public void setUsbSystem(int val) {
        usbSystem = val;
        if (usbSystemSet) {
            log.warn("setUsbSystem called more than once");
        }
        usbSystemSet = true;
    }

    /**
     * Get the type of system the NCE USB is connected to
     * <ul>
     * <li>{@link #USB_SYSTEM_NONE}
     * <li>{@link #USB_SYSTEM_POWERCAB}
     * <li>{@link #USB_SYSTEM_SB3}
     * <li>{@link #USB_SYSTEM_POWERPRO}
     * <li>{@link #USB_SYSTEM_TWIN}
     * <li>{@link #USB_SYSTEM_SB5}
     * </ul>
     *
     * @return usb command station options
     *
     */
    public int getUsbSystem() {
        return usbSystem;
    }

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
     * Supports read AIU status commands {@code 0x9B}
     */
    static public final long CMDS_AUI_READ = 0x004;

    /**
     * Supports USB read/write memory commands {@code 0xB3 -> 0xB5}
     */
    static public final long CMDS_MEM = 0x0008;

    /**
     * Support Ops Mode Pgm commands {@code 0xAE -> 0xAF}
     */
    static public final long CMDS_OPS_PGM = 0x0010;

    /**
     * Support Clock commands {@code 0x82 -> 0x87}
     */
    static public final long CMDS_CLOCK = 0x0020;

    /**
     * Support USB Interface commands {@code 0xB1}
     */
    static public final long CMDS_USB = 0x0040;

    /**
     * Disable for USB commands
     */
    static public final long CMDS_NOT_USB = 0x0080;

    /**
     * All Connections Support commands
     */
    static public final long CMDS_ALL_SYS = 0x0100;

    private long cmdGroups = CMDS_NONE;
    private boolean cmdGroupsSet = false;

    /**
     * Set the types of commands valid connected system
     * <ul>
     * <li>{@link #CMDS_NONE}
     * <li>{@link #CMDS_ACCYADDR250}
     * <li>{@link #CMDS_PROGTRACK}
     * <li>{@link #CMDS_AUI_READ}
     * <li>{@link #CMDS_MEM}
     * <li>{@link #CMDS_OPS_PGM}
     * <li>{@link #CMDS_CLOCK}
     * <li>{@link #CMDS_USB}
     * <li>{@link #CMDS_NOT_USB}
     * <li>{@link #CMDS_ALL_SYS}
     * </ul>
     *
     * @param val command group supported options
     *
     */
    public void setCmdGroups(long val) {
        cmdGroups = val;
        if (cmdGroupsSet) {
            log.warn("setCmdGroups called more than once");
        }
        cmdGroupsSet = true;
    }

    /**
     * Get the types of commands valid for the NCE USB and connected system
     * <ul>
     * <li>{@link #CMDS_NONE}
     * <li>{@link #CMDS_ACCYADDR250}
     * <li>{@link #CMDS_PROGTRACK}
     * <li>{@link #CMDS_AUI_READ}
     * <li>{@link #CMDS_MEM}
     * <li>{@link #CMDS_OPS_PGM}
     * <li>{@link #CMDS_CLOCK}
     * <li>{@link #CMDS_USB}
     * <li>{@link #CMDS_NOT_USB}
     * <li>{@link #CMDS_ALL_SYS}
     * </ul>
     *
     * @return command group supported options
     *
     */
    public long getCmdGroups() {
        return cmdGroups;
    }

    private boolean nceProgMode = false;     // Do not use exit program mode unless active

    /**
     * Gets the state of the command station
     *
     * @return true if in programming mode
     */
    public boolean getNceProgMode() {
        return nceProgMode;
    }

    /**
     * Sets the state of the command station
     *
     * @param b when true, set programming mode
     */
    public void setNceProgMode(boolean b) {
        nceProgMode = b;
    }

    /**
     * Check NCE EPROM and start NCE CS accessory memory poll
     */
    @Override
    protected AbstractMRMessage pollMessage() {

        // Check to see if command options are valid
        if (commandOptionSet == false) {
            if (log.isDebugEnabled()) {
                log.debug("Command options are not valid yet!!");
            }
            return null;
        }

        // Keep checking the state of the communication link by polling
        // the command station using the EPROM checker
        NceMessage m = pollEprom.nceEpromPoll();
        if (m != null) {
            expectReplyEprom = true;
            return m;
        } else {
            expectReplyEprom = false;
        }

        // Have we checked to see if AIU broadcasts are enabled?
        if (pollAiuStatus == null) {
            // No, do it this time
            pollAiuStatus = new NceAIUChecker(this);
            return pollAiuStatus.nceAiuPoll();
        }

        // Start NCE memory poll for accessory states
        if (pollHandler == null) {
            pollHandler = new NceTurnoutMonitor(this);
        }

        // minimize impact to NCE CS
        mWaitBeforePoll = NceTurnoutMonitor.POLL_TIME; // default = 25

        return pollHandler.pollMessage();

    }

    NceConnectionStatus pollEprom = new NceConnectionStatus(this);
    NceAIUChecker pollAiuStatus = null;
    NceTurnoutMonitor pollHandler = null;

    boolean expectReplyEprom = false;

    @Override
    protected AbstractMRListener pollReplyHandler() {
        // First time through, handle reply by checking EPROM revision
        // Second time through, handle AIU broadcast check
        if (expectReplyEprom) {
            return pollEprom;
        } else if (pollHandler == null) {
            return pollAiuStatus;
        } else {
            return pollHandler;
        }
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendNceMessage(NceMessage m, NceListener reply) {
        try {
            NceMessageCheck.checkMessage(getAdapterMemo(), m);
        } catch (JmriException e) {
            log.error(e.getMessage(), e);
            return;  // don't send bogus message to interface
        }
        sendMessage(m, reply);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        replyBinary = m.isBinary();
        replyLen = ((NceMessage) m).getReplyLen();
        super.forwardToPort(m, reply);
    }

    protected int replyLen;
    protected boolean replyBinary;
    protected boolean unsolicitedSensorMessageSeen = false;

    @Override
    protected AbstractMRMessage enterProgMode() {
        return NceMessage.getProgMode(this);
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        return NceMessage.getExitProgMode(this);
    }

    /**
     *
     * @param adaptermemo the SystemConnectionMemo to associate with this TrafficController
     * @deprecated Since 4.13.5 duplicate of setAdapterMemo
     */
    @Deprecated
    public void setSystemConnectionMemo(NceSystemConnectionMemo adaptermemo) {
        memo = adaptermemo;
    }

    /**
     *
     * @param adaptermemo the SystemConnectionMemo to associate with this TrafficController
     */
    public void setAdapterMemo(NceSystemConnectionMemo adaptermemo) {
        memo = adaptermemo;
    }

    public NceSystemConnectionMemo getAdapterMemo() {
        return memo;
    }

    private NceSystemConnectionMemo memo = null;

    @Override
    protected AbstractMRReply newReply() {
        NceReply reply = new NceReply(this);
        reply.setBinary(replyBinary);
        return reply;
    }

    // pre 2006 EPROMs can't stop AIU broadcasts so we have to accept them
    @Override
    protected boolean canReceive() {
        if (getCommandOptions() < OPTION_2006) {
            return true;
        } else if (replyLen > 0) {
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.error("unsolicited character received");
            }
            return false;
        }
    }

    @Override
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
                if (msg.getNumDataElements() == 1 && msg.getElement(0) == 0x61) {
                    return false;
                }
                if (msg.getNumDataElements() == 2 && msg.getElement(0) == 0x61
                        && msg.getElement(1) >= 0x30) {
                    return false;
                }
                if (msg.getNumDataElements() == 3 && msg.getElement(0) == 0x61
                        && msg.getElement(1) >= 0x30
                        && msg.getElement(2) >= 0x41
                        && msg.getElement(2) <= 0x6F) {
                    return true;
                }
            }
            if (msg.getNumDataElements() >= replyLen) {
                // reset reply length so we can detect an unsolicited AIU message
                replyLen = 0;
                return true;
            } else {
                return false;
            }
        } else {
            // detect that the reply buffer ends with "COMMAND: " (note ending
            // space)
            int num = msg.getNumDataElements();
            // ptr is offset of last element in NceReply
            int ptr = num - 1;
            if ((num >= 9)
                    && (msg.getElement(ptr) == ' ')
                    && (msg.getElement(ptr - 1) == ':')
                    && (msg.getElement(ptr - 2) == 'D')) {
                return true;
            } // this got harder with the new PROM at the beginning of 2005.
            // It doesn't always send the "COMMAND: " prompt at the end
            // of each response. Try for the error message:
            else if ((num >= 19)
                    && // don't check space,NL at end of buffer
                    (msg.getElement(ptr - 2) == '*')
                    && (msg.getElement(ptr - 3) == '*')
                    && (msg.getElement(ptr - 4) == '*')
                    && (msg.getElement(ptr - 5) == '*')
                    && (msg.getElement(ptr - 6) == ' ')
                    && (msg.getElement(ptr - 7) == 'D')
                    && (msg.getElement(ptr - 8) == 'O')
                    && (msg.getElement(ptr - 9) == 'O')
                    && (msg.getElement(ptr - 10) == 'T')
                    && (msg.getElement(ptr - 11) == 'S')
                    && (msg.getElement(ptr - 12) == 'R')) {
                return true;
            }

            // otherwise, it's not the end
            return false;
        }
    }

    @Override
    public String getUserName() {
        if (memo == null) {
            return "NCE";
        }
        return memo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (memo == null) {
            return "N";
        }
        return memo.getSystemPrefix();
    }

    private final static Logger log = LoggerFactory.getLogger(NceTrafficController.class);

}
