package jmri.jmrix.secsi;

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
 * sensors 1 to 999, node 2 1001 to 1999 etc.
 * <p>
 * The array of sensor states is used to update sensor known state only when
 * there's a change on the serial bus. This allows for the sensor state to be
 * updated within the program, keeping this updated state until the next change
 * on the serial bus. E.g. you can manually change a state via an icon, and not
 * have it change back the next time that node is polled.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 */
public class SerialNode extends AbstractNode {

    private SerialTrafficController tc = null;

    /**
     * Maximum number of sensors a node can carry.
     * <p>
     * Note this is less than a current SUSIC motherboard can have, but should
     * be sufficient for all reasonable layouts.
     * <p>
     * Must be less than, {@link SerialSensorManager#SENSORSPERNODE}
     */
    static final int MAXSENSORS = 16;
    static final int MAXTURNOUTS = 32;

    // class constants
    // board types
    public static final int DAUGHTER = 0;  // also default
    public static final int CABDRIVER = 1;

    private static final String[] boardNames = new String[]{
            Bundle.getMessage("BoardName1"),
            Bundle.getMessage("BoardName2")};

    public static String[] getBoardNames() {
        return boardNames.clone();
    }

    static final int[] outputBits = new int[]{32, 32};
    static final int[] inputBits = new int[]{16, 16};

    // node definition instance variables (must persist between runs)
    // Node address, 0-127 allowed
    protected int nodeType = DAUGHTER;          // See above

    // operational instance variables (should not be preserved between runs)
    protected boolean[] outputArray = new boolean[MAXTURNOUTS + 1]; // current values of the output bits for this node
    protected boolean[] outputBitChanged = new boolean[MAXTURNOUTS + 1];

    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS + 1];
    protected int[] sensorLastSetting = new int[MAXSENSORS + 1];
    protected int[] sensorTempSetting = new int[MAXSENSORS + 1];

    /**
     * Assumes a node address of 0, and a node type of 0 (IO24) If this
     * constructor is used, actual node address must be set using
     * setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode(SerialTrafficController _tc) {
        this(0, DAUGHTER, _tc);
    }

    /**
     * Create a new SerialNode and initialize default instance variables.
     *
     * @param address address of node on serial bus (0-255)
     * @param type a type constant from the class
     * @param _tc connected TafficController
     */
    public SerialNode(int address, int type, SerialTrafficController _tc) {
        // set address and type and check validity
        tc = _tc;
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
        for (int i = 0; i < MAXTURNOUTS + 1; i++) {
            outputArray[i] = false;
            outputBitChanged[i] = false;
        }
        // initialize other operational instance variables
        setMustSend();
        hasActiveSensors = false;
        // register this node
        tc.registerNode(this);
        log.debug("new serial node {}", this);
    }

    /**
     * Set an output bit on this node.
     *
     * @param bitNumber the bit on node to set (numbered from 1; not 0)
     * @param state 'true' for 0, 'false' for 1.
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // validate that this bit number is defined
        if (bitNumber > outputBits[nodeType]) { // logged only once
            warn("Output bit out-of-range for defined node: " + bitNumber);
            return;
        }
        // update the bit
        boolean oldBit = outputArray[bitNumber];
        outputArray[bitNumber] = state;

        // check for change, necessitating a send
        if (oldBit != outputArray[bitNumber]) {
            setMustSend();
            outputBitChanged[bitNumber] = true;
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
     * Public method to return node type.
     * Current types are: DAUGHTER, CABDRIVER
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Set node type.
     */
    public void setNodeType(int type) {
        nodeType = type;
        switch (nodeType) {
            default:
                log.error("Unexpected nodeType in setNodeType: {}", nodeType);
                // use DAUGHTER as default
                break;
            case DAUGHTER:
                break;
            case CABDRIVER:
                break;
        }
    }

    /**
     * Check for valid node address.
     */
    @Override
    protected boolean checkNodeAddress(int address) {
        return (address >= 0) && (address < 128);
    }

    /**
     * Create an Initialization packet (SerialMessage) for this
     * node.
     *
     * @return null as there are currently no SECSI boards that need
     * an init message
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Create a Transmit packet (SerialMessage).
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        log.debug("createOutPacket for nodeType {} with {} {};{} {};{} {};{} {}.",
                nodeType,
                outputBitChanged[0], outputArray[0],
                outputBitChanged[1], outputArray[1],
                outputBitChanged[2], outputArray[2],
                outputBitChanged[3], outputArray[3]);

        // Create a Serial message
        // For now, always write entire node
        SerialMessage m = new SerialMessage(1 + outputBits[getNodeType()] / 4); // m.size is usually 9 on Secsi
        log.debug("message m byte length = {}/4 = {}", (1 + outputBits[getNodeType()]), m.getNumDataElements());
        m.setElement(0, getNodeAddress()); // node address

        // Add output bytes
        int j = 0;
        // Note: bits are numbered from 1
        for (int i = 1; i < outputBits[nodeType]; i += 4) {
            int payload = 0;
            if (outputArray[i + 0]) {
                payload |= 1;
            }
            if (outputArray[i + 1]) {
                payload |= 2;
            }
            if (outputArray[i + 2]) {
                payload |= 4;
            }
            if (outputArray[i + 3]) {
                payload |= 8;
            }

            payload |= j << 4; // add Array num as bit 1
            m.setElement(j + 1, payload);
            j++;
        }
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
     * TODO For Secsi Simulator, needs more work to create correct reply.
     *
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        try {
            // get all input in one bit string
            int inputBits = (l.getElement(0) & 0xFF) + ((l.getElement(1) & 0xF) << 8);

            for (int i = 0; i <= lastUsedSensor; i++) {
                if (sensorArray[i] == null) {
                    continue; // skip ones that don't exist
                }
                boolean value = ((inputBits & 1) != 0);
                inputBits = inputBits >> 1;
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
            log.error("exception in markChanges: ", e);
        }
    }

    /**
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS.
     *
     * @param s Sensor object
     * @param i number of sensor's input bit on this node (0 to MAXSENSORS)
     */
    public void registerSensor(Sensor s, int i) {
        // validate the sensor ordinal
        if ((i < 0) || (i > (inputBits[nodeType] - 1)) || (i > MAXSENSORS)) {
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
                    tc.getSystemConnectionMemo().getSystemPrefix(), // multichar prefix
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
        try {
            if (m.getElement(1) != 0x50) {
                return false;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            log.debug("message does not contain element 1", e);
        }
        // see how many polls missed
        log.warn("Timeout to poll for addr {}: consecutive timeouts: {}", getNodeAddress(), timeout);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetTimeout(AbstractMRMessage m) {
        if (timeout > 0) {
            log.debug("Reset {} timeout count", timeout);
        }
        timeout = 0;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialNode.class);

}
