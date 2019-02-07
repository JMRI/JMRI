package jmri.jmrix.grapevine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.grapevine.SerialTrafficController;
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
    public static final int NODE2002V6 = 0; // also default
    public static final int NODE2002V1 = 1;
    public static final int NODE2000 = 2;

    static private final String[] boardNames = new String[]{
            Bundle.getMessage("BoardName1"),
            Bundle.getMessage("BoardName2"),
            Bundle.getMessage("BoardName3")};

    static public String[] getBoardNames() {
        return boardNames.clone();
    }

    static final int[] outputBits = new int[]{424, 424, 424};
    static final int[] inputBits = new int[]{224, 224, 224};

    // node definition instance variables (must persist between runs)
    protected int nodeType = NODE2002V6;             // See above

    // operational instance variables  (should not be preserved between runs)
    protected byte[] outputArray = new byte[500]; // current values of the output bits for this node
    protected boolean[] outputByteChanged = new boolean[500];

    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS + 1];
    protected int[] sensorLastSetting = new int[MAXSENSORS + 1];
    protected int[] sensorTempSetting = new int[MAXSENSORS + 1];

    private SerialTrafficController tc = null;

    /**
     * Assumes a node address of 1, and a node type of 0 (NODE2002V6).
     * If this constructor is used, actual node address must be set using
     * 'setNodeAddress()', and actual node type using 'setNodeType()'
     */
    public SerialNode(SerialTrafficController tc) {
        this(1, tc);
    }

    public SerialNode(int address, SerialTrafficController tc) {
        this(address, NODE2002V6, tc);
    }

    /**
     * Create a new SerialNode and initialize default instance variables.
     *
     * @param address the address of node on serial bus (1-127)
     * @param type a type constant from the class
     * @param tc the TrafficController for this connection
     */
    public SerialNode(int address, int type, SerialTrafficController tc) {
        this.tc = tc;
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
        tc.registerNode(this);
        log.debug("new serial node {}", this);
    }

    /**
     * Set an output bit on this node.
     *
     * @param bitNumber the bit index. Bits are numbered from 1 (not 0)
     * @param state 'true' for 0, 'false' for 1.
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // locate in the outputArray
        int byteNumber = (bitNumber - 1) / 8;
        // validate that this byte number is defined
        if (bitNumber > outputBits[nodeType] - 1) {
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
     * bytes that need to be sent.
     */
    @Override
    public void resetMustSend() {
        for (int i = 0; i < (outputBits[nodeType] + 7) / 8; i++) {
            if (outputByteChanged[i]) {
                return;
            }
        }
        super.resetMustSend();
    }

    /**
     * Get node type.
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Set node type.
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public void setNodeType(int type) {
        nodeType = type;
        switch (nodeType) {
            default:
                log.error("Unexpected nodeType in setNodeType: {}", nodeType);
            // use NODE2002V6 as default
            case NODE2002V6:
            case NODE2002V1:
            case NODE2000:
                break;
        }
    }

    /**
     * Check for valid node address.
     */
    @Override
    protected boolean checkNodeAddress(int address) {
        return (address >= 1) && (address <= 127);
    }

    /**
     * Create Initialization packets (SerialMessage) for this node.
     * Initialization consists of multiple parts:
     * <ul>
     *   <li>Turn on the ASD input 0x71 to bank 0
     *   <li>After a wait, another ASD message 0x73 to bank 0
     * </ul>
     * (Eventually, it should also request input values, once we know what
     * message does that)
     * <p>
     * As an Ugly Hack to keep these separate, only the first is put in the
     * reply from this. The other(s) are sent via the usual output methods.
     */
    @Override
    public AbstractMRMessage createInitPacket() {

        // first, queue a timer to send 2nd message
        javax.swing.Timer timer = new javax.swing.Timer(250, null);
        java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SerialMessage m2 = new SerialMessage(4);
                int i = 0;

                // turn on 2nd parallel inputs
                m2.setElement(i++, getNodeAddress() | 0x80); // address
                m2.setElement(i++, 0x73);  // command
                m2.setElement(i++, getNodeAddress() | 0x80); // address
                m2.setElement(i++, 0x00);  // bank 0 = init
                m2.setParity(i - 4);
                log.debug("Node {} initpacket 2 sent to {} trafficController", getNodeAddress(),
                        tc.getSystemConnectionMemo().getSystemPrefix());
                tc.sendSerialMessage(m2, null);
            }
        };
        timer.addActionListener(l);
        timer.setRepeats(false);
        timer.setInitialDelay(250);
        timer.start();

        // Now, do the first message, and return it.
        SerialMessage m1 = new SerialMessage(4);
        int i = 0;

        // turn on ASD
        m1.setElement(i++, getNodeAddress() | 0x80);  // address
        m1.setElement(i++, 0x71);  // command
        m1.setElement(i++, getNodeAddress() | 0x80);  // address
        m1.setElement(i++, 0x00);  // bank 0 = init
        m1.setParity(i - 4);
        log.debug("Node {} initpacket 1 ready to send to {} trafficController", getNodeAddress(),
                tc.getSystemConnectionMemo().getSystemPrefix());
        return m1;
    }

    /**
     * Public method to create a Transmit packet (SerialMessage).
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        if (log.isDebugEnabled()) {
            log.debug("createOutPacket for nodeType "
                    + nodeType + " with "
                    + outputByteChanged[0] + " " + outputArray[0] + ";"
                    + outputByteChanged[1] + " " + outputArray[1] + ";"
                    + outputByteChanged[2] + " " + outputArray[2] + ";"
                    + outputByteChanged[3] + " " + outputArray[3] + ";");
        }

        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage();
        m.setElement(0, getNodeAddress()); // node address
        m.setElement(1, 17);
        // Add output bytes
        for (int i = 0; i < (outputBits[nodeType] + 7) / 8; i++) {
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

    // Define addressing offsets
    static final int offsetA = 100; // 'a' advanced occ sensors start at 100+1
    static final int offsetM = 200; // 'm' advanced movement sensors start at 200+1
    static final int offsetP = 0;   // 'p' parallel sensors start at 200+1
    static final int offsetS = 20;  // 's' serial occupancy sensors start at 200+1

    /**
     * Use the contents of a reply from the Grapevine to mark changes in the
     * sensors on the layout.
     *
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        // first, is it from a sensor?
        if (!(l.isFromParallelSensor() || l.isFromNewSerialSensor() || l.isFromOldSerialSensor())) {
            return; // not interesting message
        }
        // Yes, continue.
        // Want to get individual sensor bits, and xor them with the
        // past state and the inverted bit.

        if (l.isFromNewSerialSensor()) {
            // Serial sensor has only one bit. Extract value, then address
            boolean input = ((l.getElement(1) & 0x01) == 0);
            int card = ((l.getElement(1) & 0x60) >> 5); // number from 0
            if (card > 2) {
                log.warn("Did not expect card number {}, message {}", card, l.toString());
            }
            boolean motion = (l.getElement(1) & 0x10) != 0;
            int number = ((l.getElement(1) & 0x0E) >> 1) + 1;
            int sensor = card * 8 + (motion ? offsetM : offsetA) + number;
            // Update
            markBit(input, sensor);
        } else if (l.isFromOldSerialSensor()) {
            // Serial sensor brings in a nibble of four bits
            int byte1 = l.getElement(1);
            boolean altPort = ((byte1 & 0x40) != 0);
            boolean highNibble = ((byte1 & 0x10) != 0);
            boolean b0 = (byte1 & 0x01) == 0;
            boolean b1 = (byte1 & 0x02) == 0;
            boolean b2 = (byte1 & 0x04) == 0;
            boolean b3 = (byte1 & 0x08) == 0;
            int number = 1 + (highNibble ? 4 : 0) + (altPort ? 8 : 0) + offsetS;
            markBit(b0, number);
            markBit(b1, number + 1);
            markBit(b2, number + 2);
            markBit(b3, number + 3);
        } else {
            // Parallel sensor brings in a nibble of four bits
            int byte1 = l.getElement(1);
            boolean altPort = ((byte1 & 0x40) != 0);
            boolean highNibble = ((byte1 & 0x10) != 0);
            boolean b0 = (byte1 & 0x01) == 0;
            boolean b1 = (byte1 & 0x02) == 0;
            boolean b2 = (byte1 & 0x04) == 0;
            boolean b3 = (byte1 & 0x08) == 0;
            int number = 1 + (highNibble ? 4 : 0) + (altPort ? 8 : 0) + offsetP;
            markBit(b0, number);
            markBit(b1, number + 1);
            markBit(b2, number + 2);
            markBit(b3, number + 3);
        }
    }

    /**
     * Mark and act on a single input bit.
     *
     * @param input     true if sensor says active
     * @param sensorNum from 1 to lastUsedSensor+1 on this node
     */
    void markBit(boolean input, int sensorNum) {
        log.debug("Mark bit {} {} in node {}", sensorNum, input, getNodeAddress());
        if (sensorArray[sensorNum] == null) {
            log.debug("Try to create sensor {} on node {} since sensor doesn't exist", sensorNum, getNodeAddress());
            // try to make the sensor, which will also register it
            jmri.InstanceManager.sensorManagerInstance()
                    .provideSensor(tc.getSystemConnectionMemo().getSystemPrefix() + "S" + (getNodeAddress() * 1000 + sensorNum));
            if (sensorArray[sensorNum] == null) {
                log.error("Creating sensor {}S{} failed unexpectedly",
                        tc.getSystemConnectionMemo().getSystemPrefix(),
                        (getNodeAddress() * 1000 + sensorNum));
                log.debug("node should be " + this);
                return;
            }
        }

        boolean value = input ^ sensorArray[sensorNum].getInverted();

        try {
            if (value) {
                // bit set, considered ACTIVE
                if (sensorLastSetting[sensorNum] != Sensor.ACTIVE) {
                    sensorLastSetting[sensorNum] = Sensor.ACTIVE;
                    sensorArray[sensorNum].setKnownState(Sensor.ACTIVE);
                }
            } else {
                // bit reset, considered INACTIVE
                if (sensorLastSetting[sensorNum] != Sensor.INACTIVE) {
                    sensorLastSetting[sensorNum] = Sensor.INACTIVE;
                    sensorArray[sensorNum].setKnownState(Sensor.INACTIVE);
                }
            }
        } catch (JmriException e) {
            log.error("exception in markChanges: " + e);
        }
    }

    /**
     * Register a sensor on a node.
     * <p>
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS. E.g. the
     * integer argument is one less than the name of the sensor object.
     *
     * @param s Sensor object
     * @param i bit number corresponding, a 1-based value corresponding to the
     *          low digits of the system name
     */
    public void registerSensor(Sensor s, int i) {
        log.debug("Register sensor {} index {}", s.getSystemName(), i);
        // validate the sensor ordinal
        if ((i < 0) || (i > (inputBits[nodeType])) || (i > MAXSENSORS)) {
            log.error("Unexpected sensor ordinal in registerSensor: {}", Integer.toString(i));
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
                    tc.getSystemConnectionMemo().getSystemPrefix(),
                    (getNodeAddress() * SerialSensorManager.SENSORSPERNODE) + i,
                    new Exception("mult reg " + i + " S:" + s.getSystemName()));
        }
    }

    int timeout = 0;

    /**
     * {@inheritDoc}
     *
     * @return true if initialization required
     */
    @Override
    public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        timeout++;
        // normal to timeout in response to init, output
        if (m.getElement(1) != 0x50) {
            return false;
        }

        // see how many polls missed
        if (log.isDebugEnabled()) {
            log.warn("Timeout to poll for addr = {}: consecutive timeouts: {}", getNodeAddress(), timeout);
        }

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
     *
     * @param m GrapevineSerialMessage (ignored)
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
