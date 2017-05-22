package jmri.jmrix.cmri.serial;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRNodeTrafficController;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from C/MRI serial messages.
 * <P>
 * The "SerialInterface" side sends/receives message objects.
 * <P>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the necessary state in each
 * message.
 * <P>
 * Handles initialization, polling, output, and input for multiple Serial Nodes.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialTrafficController extends AbstractMRNodeTrafficController implements SerialInterface {

    /**
     * Public static method to the user name for a valid system name Returns ""
     * (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset < 1) return "";
        
        if (systemName.length() < 3) {
            // not a valid system name for C/MRI
            return "";
        }
        if (systemName.charAt(offset) == 'S') {
            Sensor s = null;
            s = InstanceManager.sensorManagerInstance().getBySystemName(systemName);
            if (s != null) {
                return s.getUserName();
            } else {
                return "";
            }
        } else if (systemName.charAt(offset) == 'T') {
            Turnout t = null;
            t = InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
            if (t != null) {
                return t.getUserName();
            } else {
                return "";
            }
        } else if (systemName.charAt(offset) == 'L') {
            Light lgt = null;
            lgt = InstanceManager.lightManagerInstance().getBySystemName(systemName);
            if (lgt != null) {
                return lgt.getUserName();
            } else {
                return "";
            }
        }
        // not any known sensor, light, or turnout
        return "";
    }

    /**
     * Public static method to test if a C/MRI output bit is free for assignment
     * Returns "" (null string) if the specified output bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     * Test is not performed if the node address or bit number are illegal.
     */
    public static String isOutputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number in free bit test");
            return "";
        }
        Turnout t = null;
        String sysName = "";
        sysName = makeSystemName("T", nAddress, bitNum);
        t = InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
        if (t != null) {
            return sysName;
        }
        String altName = "";
        altName = convertSystemNameToAlternate(sysName);
        t = InstanceManager.turnoutManagerInstance().getBySystemName(altName);
        if (t != null) {
            return altName;
        }
        if (bitNum > 1) {
            sysName = makeSystemName("T", nAddress, bitNum - 1);
            t = InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
            if (t != null) {
                if (t.getNumberOutputBits() == 2) {
                    return sysName;
                }
            } else {
                altName = convertSystemNameToAlternate(sysName);
                if (altName != null) {
                    t = InstanceManager.turnoutManagerInstance().getBySystemName(altName);
                    if (t != null) {
                        if (t.getNumberOutputBits() == 2) {
                            return altName;
                        }
                    }
                }
            }
        }
        Light lgt = null;
        sysName = makeSystemName("L", nAddress, bitNum);
        lgt = InstanceManager.lightManagerInstance().getBySystemName(sysName);
        if (lgt != null) {
            return sysName;
        }
        altName = convertSystemNameToAlternate(sysName);
        lgt = InstanceManager.lightManagerInstance().getBySystemName(altName);
        if (lgt != null) {
            return altName;
        }
        // not assigned to a turnout or a light
        return "";
    }

    /**
     * Public static method to test if a C/MRI input bit is free for assignment
     * Returns "" (null string) if the specified input bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     * Test is not performed if the node address is illegal or bit number is
     * greater than 2048.
     */
    public static String isInputBitFree(int nAddress, int bitNum) {
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress in free bit test");
            return "";
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number in free bit test");
            return "";
        }
        Sensor s = null;
        String sysName = "";
        sysName = makeSystemName("S", nAddress, bitNum);
        s = InstanceManager.sensorManagerInstance().getBySystemName(sysName);
        if (s != null) {
            return sysName;
        }
        String altName = "";
        altName = convertSystemNameToAlternate(sysName);
        s = InstanceManager.sensorManagerInstance().getBySystemName(altName);
        if (s != null) {
            return altName;
        }
        // not assigned to a sensor
        return "";
    }

    /**
     * Public static method to parse a C/MRI system name and return the bit
     * number. Notes: Bits are numbered from 1. If an error is found, 0 is
     * returned. Does not check whether that node is defined on current system.
     */
    public static int getBitFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset<1) return 0;

        if ((systemName.charAt(offset) != 'L') && (systemName.charAt(offset) != 'S') && (systemName.charAt(offset) != 'T')) {
            log.error("illegal character in header field of system name: " + systemName);
            return 0;
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = offset+1; (i < systemName.length()) && (k == 0); i++) {
            if (systemName.charAt(i) == 'B') {
                k = i + 1;
            }
        }
        int n = 0;
        if (k == 0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.valueOf(systemName.substring(offset+1)).intValue();
            } catch (Exception e) {
                log.error("illegal character in number field of system name: " + systemName);
                return 0;
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.error("invalid CMRI system name: " + systemName);
                return 0;
            }
        } else {
            try {
                n = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.error("illegal character in bit number field of CMRI system name: " + systemName);
                return 0;
            }
        }
        return n;
    }

    /** 
     * Public static method to check and skip the System Prefix 
     * string on a system name.
     * 
     * @return offset of the 1st character past the prefix, or -1 if not valid
     * for this connection
     */
    public static int checkSystemPrefix(String systemName) {
        if (systemName.charAt(0) != 'C') return -1;
        return 1;
    }
    /**
     * Public static method to parse a C/MRI system name and return the Serial
     * Node Address Note: Returns '-1' if illegal systemName format or if the
     * node is not found. Nodes are numbered from 0 - 127. Does not check
     * whether that node is defined on current system.
     */
    public static int getNodeAddressFromSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset<1) return -1;
        
        if ((systemName.charAt(offset) != 'L') && (systemName.charAt(offset) != 'S') && (systemName.charAt(offset) != 'T')) {
            log.error("illegal character in header field of system name: " + systemName);
            return -1;
        }
        String s = "";
        boolean noB = true;
        for (int i = offset+1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset+1, i);
                noB = false;
            }
        }
        int ua;
        if (noB) {
            int num = Integer.valueOf(systemName.substring(2)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid CMRI system name: " + systemName);
                return -1;
            }
        } else {
            if (s.length() == 0) {
                log.error("no node address before 'B' in CMRI system name: " + systemName);
                return -1;
            } else {
                try {
                    ua = Integer.parseInt(s);
                } catch (Exception e) {
                    log.error("illegal character in CMRI system name: " + systemName);
                    return -1;
                }
            }
        }
        return ua;
    }

    /**
     * Public static method to convert one format C/MRI system name for the
     * alternate format. If the supplied system name does not have a valid
     * format, or if there is no representation in the alternate naming scheme,
     * an empty string is returned.
     */
    public static String convertSystemNameToAlternate(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset<1) return "";

        if (!validSystemNameFormat(systemName, systemName.charAt(1))) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset+1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset+1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            int num = Integer.valueOf(systemName.substring(offset+1)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = systemName.substring(0, offset+1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        } else {
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            if (bitNum > 999) {
                // bit number is out-of-range for a CLnnnxxx address
                return "";
            }
            altName = systemName.substring(0, offset+1) + Integer.toString((nAddress * 1000) + bitNum);
        }
        return altName;
    }

    /**
     * Public static method to normalize a C/MRI system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one C/MRI bit, by removing extra zeros inserted by the user.
     * <P>
     * If the supplied system name does not have a valid format, an empty string
     * is returned. Otherwise a normalized name is returned in the same format
     * as the input name.
     */
    public static String normalizeSystemName(String systemName) {
        int offset = checkSystemPrefix(systemName);
        if (offset<1) return "";

        if (!validSystemNameFormat(systemName, systemName.charAt(offset))) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset+1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset+1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            int num = Integer.valueOf(systemName.substring(2)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = systemName.substring(0, offset+1) + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = systemName.substring(0, offset+1) + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        }
        return nName;
    }

    /**
     * Public static method to validate C/MRI system name for configuration.
     * Returns 'true' if system name has a valid meaning in current
     * configuration, else returns 'false'.
     * Does validate node number and system prefix.
     */
    public static boolean validSystemNameConfig(String systemName, char type, SerialTrafficController tc) {
        if (!validSystemNameFormat(systemName, type)) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        SerialNode node = (SerialNode) getNodeFromSystemName(systemName, tc);
        if (node == null) {
            // The node indicated by this system address is not present
            return false;
        }
        int bit = SerialTrafficController.getBitFromSystemName(systemName);
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > (node.numOutputCards() * node.getNumBitsPerCard()))) {
                // The bit is not valid for this defined Serial node
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > (node.numInputCards() * node.getNumBitsPerCard()))) {
                // The bit is not valid for this defined Serial node
                return false;
            }
        } else {
            log.error("Invalid type specification in validSystemNameConfig call");
            return false;
        }
        // System name has passed all tests
        return true;
    }

    /**
     * Public static method to validate system name format returns 'true' if
     * system name has a valid format, else returns 'false'.
     * Does not check whether that node is defined on current system.
     */
    public static boolean validSystemNameFormat(String systemName, char type) {
        int offset = checkSystemPrefix(systemName);
        if (offset<1) {
            log.error("illegal system prefix in CMRI system name: " + systemName);
            return false;
        };

        if (systemName.charAt(offset) != type) {
            log.error("illegal type character in CMRI system name: " + systemName);
            return false;
        }
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset+1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset+1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a CLnnnxxx address
            int num;
            try {
                num = Integer.valueOf(systemName.substring(2)).intValue();
            } catch (Exception e) {
                log.error("illegal character in number field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 1) || (num >= 128000)) {
                log.error("number field out of range in CMRI system name: " + systemName);
                return false;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.error("bit number not in range 1 - 999 in CMRI system name: " + systemName);
                return false;
            }
        } else {
            if (s.length() == 0) {
                log.error("no node address before 'B' in CMRI system name: " + systemName);
                return false;
            }
            int num;
            try {
                num = Integer.valueOf(s).intValue();
            } catch (Exception e) {
                log.error("illegal character in node address field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 0) || (num >= 128)) {
                log.error("node address field out of range in CMRI system name: " + systemName);
                return false;
            }
            try {
                num = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.error("illegal character in bit number field of CMRI system name: " + systemName);
                return false;
            }
            if ((num < 1) || (num > 2048)) {
                log.error("bit number field out of range in CMRI system name: " + systemName);
                return false;
            }
        }
        return true;
    }

    /**
     * Public static method to parse a C/MRI system name and return the Serial
     * Node Note: Returns 'null' if illegal systemName format or if the node is
     * not found
     */
    public static AbstractNode getNodeFromSystemName(String systemName, SerialTrafficController tc) {
        // get the node address
        int ua;
        ua = SerialTrafficController.getNodeAddressFromSystemName(systemName);
        if (ua == -1) {
            return null;
        }
        return tc.getNodeFromAddress(ua);
    }

    /**
     * Public static method to construct a C/MRI system name from type
     * character, node address, and bit number
     * <P>
     * This routine returns a system name in the CLnnnxxx, CTnnnxxx, or CSnnnxxx
     * format if the bit number is 1 - 999. If the bit number is 1000 - 2048,
     * the system name is returned in the CLnnnBxxxx, CTnnnBxxxx, or CSnnnBxxxx
     * format. The returned name is normalized.
     * <P>
     * If the supplied character is not valid, or the node address is out of the
     * 0 - 127 range, or the bit number is out of the 1 - 2048 range, an error
     * message is logged and the null string "" is returned.
     */
    public static String makeSystemName(String type, int nAddress, int bitNum) {
        String nName = "";
        if ((!type.equals("S")) && (!type.equals("L")) && (!type.equals("T"))) {
            log.error("illegal type character proposed for system name");
            return nName;
        }
        if ((nAddress < 0) || (nAddress > 127)) {
            log.error("illegal node adddress proposed for system name");
            return nName;
        }
        if ((bitNum < 1) || (bitNum > 2048)) {
            log.error("illegal bit number proposed for system name");
            return nName;
        }
        if (bitNum < 1000) {
            nName = "C" + type + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            nName = "C" + type + Integer.toString(nAddress) + "B" + Integer.toString(bitNum);
        }
        return nName;
    }

    public SerialTrafficController() {
        super();

        // set node range
        init(0, 127);

        // entirely poll driven, so reduce interval
        mWaitBeforePoll = 5;  // default = 25

    }

    // The methods to implement the SerialInterface
    @Override
    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

    /**
     * Public method to set up for initialization of a Serial node
     */
    public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i = 0; i < getNumNodes(); i++) {
                if (getNode(i) == node) {
                    // found node - set up for initialization
                    setMustInit(i, true);
                    return;
                }
            }
        }
    }

    @Override
    protected AbstractMRMessage enterProgMode() {
        log.warn("enterProgMode doesnt make sense for C/MRI serial");
        return null;
    }

    @Override
    protected AbstractMRMessage enterNormalMode() {
        // can happen during error recovery, null is OK
        return null;
    }

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener) client).message((SerialMessage) m);
    }

    /**
     * Forward a SerialReply to all registered SerialInterface listeners.
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        ((SerialListener) client).reply((SerialReply) m);
    }

    SerialSensorManager mSensorManager = null;

    public void setSensorManager(SerialSensorManager m) {
        mSensorManager = m;
    }

    /**
     * Handles initialization, output and polling for C/MRI Serial Nodes from
     * within the running thread
     */
    @Override
    protected synchronized AbstractMRMessage pollMessage() {
        // ensure validity of call
        if (getNumNodes() <= 0) {
            return null;
        }

        int previousPollPointer = curSerialNodeIndex;
        updatePollPointer(); // service next node next

        // ensure that each node is initialized        
        if (getMustInit(curSerialNodeIndex)) {
            setMustInit(curSerialNodeIndex, false);
            AbstractMRMessage m = getNode(curSerialNodeIndex).createInitPacket();
            log.debug("send init message: " + m);
            m.setTimeout(500);  // wait for init to finish (milliseconds)
            return m;
        }
        // send Output packet if needed
        if (getNode(curSerialNodeIndex).mustSend()) {
            log.debug("request write command to send");
            getNode(curSerialNodeIndex).resetMustSend();
            AbstractMRMessage m = getNode(curSerialNodeIndex).createOutPacket();
            m.setTimeout(2);  // no need to wait for output to answer
            // reset poll pointer update, so next increment will poll from here
            curSerialNodeIndex = previousPollPointer;
            return m;
        }
        // poll for Sensor input
        if (getNode(curSerialNodeIndex).getSensorsActive()) {
            // Some sensors are active for this node, issue poll
            SerialMessage m = SerialMessage.getPoll(
                    getNode(curSerialNodeIndex).getNodeAddress());
            return m;
        } else {
            // no Sensors (inputs) are active for this node
            return null;
        }
    }

    /**
     * Update the curSerialNodeIndex so next node polled next time
     */
    private void updatePollPointer() {
        curSerialNodeIndex++;
        if (curSerialNodeIndex >= getNumNodes()) {
            curSerialNodeIndex = 0;
        }
    }

    @Override
    protected synchronized void handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // inform node, and if it resets then reinitialize        
        if (getNode(curSerialNodeIndex).handleTimeout(m, l)) {
            setMustInit(curSerialNodeIndex, true);
        }

    }

    @Override
    protected synchronized void resetTimeout(AbstractMRMessage m) {
        // don't use super behavior, as timeout to init, transmit message is normal

        // and inform node
        getNode(curSerialNodeIndex).resetTimeout(m);

    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        return mSensorManager;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    @Override
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, converted to JMRI multi-system support structure
     */
    @Override
    @Deprecated
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "temporary until mult-system; only set at startup")
    protected void setInstance() {
        log.debug("deprecated setInstance should not have been called");
    }

    @Override
    protected AbstractMRReply newReply() {
        return new SerialReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // our version of loadChars doesn't invoke this, so it shouldn't be called
        log.error("Not using endOfMessage, should not be called");
        return false;
    }

    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        int i;
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            if (char1 == 0x03) {
                break;           // check before DLE handling
            }
            if (char1 == 0x10) {
                char1 = readByteProtected(istream);
            }
            msg.setElement(i, char1 & 0xFF);
        }
    }

    @Override
    protected void waitForStartOfReply(DataInputStream istream) throws java.io.IOException {
        // loop looking for the start character
        while (readByteProtected(istream) != 0x02) {
        }
    }

    /**
     * Add header to the outgoing byte stream.
     *
     * @param msg The output byte stream
     * @return next location in the stream to fill
     */
    @Override
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        msg[0] = (byte) 0xFF;
        msg[1] = (byte) 0xFF;
        msg[2] = (byte) 0x02;  // STX
        return 3;
    }

    /**
     * Add trailer to the outgoing byte stream.
     *
     * @param msg    The output byte stream
     * @param offset the first byte not yet used
     */
    @Override
    protected void addTrailerToOutput(byte[] msg, int offset, AbstractMRMessage m) {
        msg[offset] = 0x03;  // etx
    }

    /**
     * Determine how much many bytes the entire message will take, including
     * space for header and trailer
     *
     * @param m The message to be sent
     * @return Number of bytes
     */
    @Override
    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        int cr = 4;
        return len + cr;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTrafficController.class.getName());
}
