package jmri.jmrix.oaktree;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models a serial node.
 * <p>
 * Nodes are numbered ala their address, from 0 to 255. Node number 1 carries
 * sensors 1 to 999, node 2 carries 1001 to 1999 etc.
 * <p>
 * The array of sensor states is used to update sensor known state only when
 * there's a change on the serial bus. This allows for the sensor state to be
 * updated within the program, keeping this updated state until the next change
 * on the serial bus. E.g. you can manually change a state via an icon, and not
 * have it change back the next time that node is polled.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2008
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialNode extends AbstractNode {

    /**
     * Maximum number of sensors a node can carry.
     * <p>
     * Note this is less than a current SUSIC motherboard can have, but should
     * be sufficient for all reasonable layouts.
     * <p>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERNODE}
     */
    static final int MAXSENSORS = 999;

    // class constants
    // board types
    public static final int IO24 = 0;  // also default
    public static final int IO48 = 1;
    public static final int O48 = 2;

    private static final String[] boardNames = new String[]{"IO24", "IO48", "O48"}; // NOI18N

    public static String[] getBoardNames() {
        return boardNames.clone();
    }

    static final int[] outputBytes = new int[]{2, 4, 6};
    static final int[] inputBytes = new int[]{1, 2, 0};

    // node definition instance variables (must persist between runs)
    protected int nodeType = IO24;             // See above

    // operational instance variables  (should not be preserved between runs)
    protected byte[] outputArray = new byte[256]; // current values of the output bits for this node
    protected boolean[] outputByteChanged = new boolean[256];

    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS + 1];
    protected int[] sensorLastSetting = new int[MAXSENSORS + 1];
    protected int[] sensorTempSetting = new int[MAXSENSORS + 1];

    OakTreeSystemConnectionMemo _memo = null;

    /**
     * Create a new SerialNode without a name supplied.
     * Assumes a node address of 0, and a node type of 0 (IO24). If this
     * constructor is used, actual node address must be set using
     * setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode(OakTreeSystemConnectionMemo memo) {
        this(0, IO24, memo);
    }

    /**
     * Create a new SerialNode and initialize default instance variables
     * address - Address of node on serial bus (0-255) type - a type constant
     * from the class
     */
    public SerialNode(int address, int type, OakTreeSystemConnectionMemo memo) {
        _memo = memo;
        // set address and type and check validity
        setNodeAddress(address);
        setNodeType(type);
        // set default values for other instance variables
        // clear the Sensor arrays
        for (int i = 0; i < MAXSENSORS + 1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
        }
        // clear all output bits
        for (int i = 0; i < 256; i++) {
            outputArray[i] = 0;
            outputByteChanged[i] = false;
        }
        // initialize other operational instance variables
        setMustSend();
        hasActiveSensors = false;
        // register this node
        _memo.getTrafficController().registerNode(this);
    }

    /**
     * Set an output bit.
     *
     * @param bitNumber bit id, numbered from 1 (not 0)
     * @param state 'true' for 0, 'false' for 1
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // locate in the outputArray
        int byteNumber = (bitNumber - 1) / 8;
        // validate that this byte number is defined
        if (byteNumber > outputBytes[nodeType]) { // logged only once
            warn("Output bit out-of-range for defined node: " + bitNumber);
        }
        if (byteNumber >= 256) {
            byteNumber = 255;
        }
        // update the byte
        byte bit = (byte) (1 << ((bitNumber - 1) % 8));
        byte oldByte = outputArray[byteNumber];
        if (state) {
            outputArray[byteNumber] &= (~bit);
        } else {
            outputArray[byteNumber] |= bit;
        }
        // check for change, necessitating a send
        if (oldByte != outputArray[byteNumber]) {
            setMustSend();
            outputByteChanged[byteNumber] = true;
        }
    }

    /**
     * Get state of Sensors.
     *
     * @return 'true' if at least one sensor is active for this node
     */
    @Override
    public boolean getSensorsActive() {
        return hasActiveSensors;
    }

    /**
     * Reset state of needSend flag. Can only reset if there are no
     * bytes that need to be sent
     */
    @Override
    public void resetMustSend() {
        for (int i = 0; i < outputBytes[nodeType]; i++) {
            if (outputByteChanged[i]) {
                return;
            }
        }
        super.resetMustSend();
    }

    /**
     * Get Node type. Current types are: IO24, I048, O48.
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Set Node type.
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public void setNodeType(int type) {
        nodeType = type;
        switch (nodeType) {
            default:
                log.error("Unexpected nodeType in setNodeType: {}", nodeType);
            // use IO-48 as default
            case IO48:
            case IO24:
            case O48:
                break;
        }
    }

    /**
     * Check for valid node address.
     */
    @Override
    protected boolean checkNodeAddress(int address) {
        return (address >= 0) && (address < 256);
    }

    /**
     * Create an Initialization packet (SerialMessage) for this
     * node. There are currently no Oak Tree boards that need an init message,
     * so this returns null.
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Create an Transmit packet (SerialMessage).
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        if (log.isDebugEnabled()) {
            log.debug("createOutPacket for nodeType {} with {} {};{} {};{} {};{} {}.",
                    nodeType,
                    outputByteChanged[0], outputArray[0],
                    outputByteChanged[1], outputArray[1],
                    outputByteChanged[2], outputArray[2],
                    outputByteChanged[3], outputArray[3]);
        }

        // create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage(1);
        m.setElement(0, getNodeAddress()); // node address
        m.setElement(1, 17);
        // Add output bytes
        for (int i = 0; i < outputBytes[nodeType]; i++) {
            if (outputByteChanged[i]) {
                outputByteChanged[i] = false;
                m.setElement(2, i);
                m.setElement(3, outputArray[i]);
                return m;
            }
        }

        // return result packet for start of card, since need
        // to do something!
        m.setElement(2, 0);
        m.setElement(3, outputArray[0]);
        return m;
    }

    boolean warned = false;

    void warn(String s) {
        if (warned) {
            return;
        }
        warned = true;
        log.warn(s);
    }

    /**
     * Use the contents of the poll reply to mark changes.
     *
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        try {
            for (int i = 0; i <= lastUsedSensor; i++) {
                if (sensorArray[i] == null) {
                    continue; // skip ones that don't exist
                }
                int loc = i / 8;
                int bit = i % 8;
                boolean value = (((l.getElement(loc + 2) >> bit) & 0x01) == 1) ^ sensorArray[i].getInverted(); // byte 2 is first of data
                log.debug("markChanges loc={} bit={} is {}", loc, bit, value);
                if (value) {
                    // bit set, considered ACTIVE
                    if (((sensorTempSetting[i] == Sensor.ACTIVE)
                            || (sensorTempSetting[i] == Sensor.UNKNOWN))
                            && (sensorLastSetting[i] != Sensor.ACTIVE)) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.ACTIVE;
                } else {
                    // bit reset, considered INACTIVE
                    if (((sensorTempSetting[i] == Sensor.INACTIVE)
                            || (sensorTempSetting[i] == Sensor.UNKNOWN))
                            && (sensorLastSetting[i] != Sensor.INACTIVE)) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.INACTIVE;
                }
            }
        } catch (JmriException e) {
            log.error("exception in markChanges: " + e);
        }
    }

    /**
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS.
     *
     * @param s sensor object
     * @param i number of sensor's input bit on this node (0 to MAXSENSORS)
     */
    public void registerSensor(Sensor s, int i) {
        // validate the sensor ordinal
        if ((i < 0) || (i > (inputBytes[nodeType] * 8 - 1)) || (i > MAXSENSORS)) {
            log.error("Unexpected sensor ordinal in registerSensor: {}", Integer.toString(i + 1));
            return;
        }
        hasActiveSensors = true;
        if (sensorArray[i] == null) {
            sensorArray[i] = s;
            if (lastUsedSensor < i) {
                lastUsedSensor = i;
            }
        } else {
            // multiple registration of the same sensor
            log.warn("multiple registration of same sensor: {}S{}",
                    InstanceManager.getDefault(OakTreeSystemConnectionMemo.class).getSystemPrefix(), // multichar prefix
                    Integer.toString((getNodeAddress() * SerialSensorManager.SENSORSPERNODE) + i + 1));
        }
    }

    int timeout = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        timeout++;
        // normal to timeout in response to init, output
        if (m.getElement(1) != 0x50) {
            return false;
        }

        // see how many polls missed
        log.warn("Timeout to poll for addr={}: consecutive timeouts: {}", getNodeAddress(), timeout);

        if (timeout > 5) { // enough, reinit
            // reset timeout count to zero to give polls another try
            timeout = 0;
            // reset poll and send control so will retry initialization
            setMustSend();
            return true;   // tells caller to force init
        } else {
            return false;
        }
    }

    @Override
    public void resetTimeout(AbstractMRMessage m) {
        if (timeout > 0) {
            log.debug("Reset {} timeout count", timeout);
        }
        timeout = 0;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialNode.class);

}
