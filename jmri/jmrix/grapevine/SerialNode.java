// SerialNode.java

package jmri.jmrix.grapevine;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;

/**
 * Models a serial node.
 * <P>
 * Nodes are numbered ala their address, from 0 to 255.
 * Node number 1 carries sensors 1 to 999, node 2 1001 to 1999 etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision: 1.2 $
 */
public class SerialNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     * Note this is less than a current SUSIC motherboard can have,
     * but should be sufficient for all reasonable layouts.
     * <P>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERNODE}
     */
    static final int MAXSENSORS = 999;
    
    // class constants
    
    // board types
    public static final int NODE2002V6 = 0;  // also default
    public static final int NODE2002V1 = 1;
    public static final int NODE2000 = 2;

    public static final String[] boardNames = new String[]{"2002 node, version 6 or later",
                                                           "2002 node, pre version 6", 
                                                           "2000 (original) node"};
    public static final int[] outputBits = new int[]{12,12,12};
    public static final int[] inputBits = new int[]{250,250,250};
    
    // node definition instance variables (must persist between runs)
    public int nodeAddress = 0;                 // Node address, 1-127 allowed
    protected int nodeType = NODE2002V6;             // See above

    // operational instance variables  (should not be preserved between runs)
    protected boolean needSend = true;          // 'true' if something has changed in the outputByte array since
                                                //    the last send to the hardware node
    protected byte[] outputArray = new byte[256]; // current values of the output bits for this node
    protected boolean[] outputByteChanged = new boolean[256];
    
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS+1];
    protected int[] sensorLastSetting = new int[MAXSENSORS+1];
    protected int[] sensorTempSetting = new int[MAXSENSORS+1];

    /**
     * Assumes a node address of 0, and a node type of 0 (NODE2002V6)
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode() {
        this (1,NODE2002V6);
    }

    /**
     * Creates a new SerialNode and initialize default instance variables
     *   address - Address of node on serial bus (0-255)
     *   type - a type constant from the class
     */
    public SerialNode(int address, int type) {
        // set address and type and check validity
        setNodeAddress (address);
        setNodeType (type);
        // set default values for other instance variables
        // clear the Sensor arrays
        for (int i = 0; i<MAXSENSORS+1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
        }
        // clear all output bits
        for (int i = 0; i<256; i++) {
            outputArray[i] = 0;
            outputByteChanged[i] = false;
        }
        // initialize other operational instance variables
        needSend = true;
        hasActiveSensors = false;
        // register this node
        SerialTrafficController.instance().registerSerialNode(this);
    }

    	    	
    /**
     * Public method setting an output bit.
     *    Note:  state = 'true' for 0, 'false' for 1
     *           bits are numbered from 1 (not 0)
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // locate in the outputArray
        int byteNumber = (bitNumber-1)/8;
        // validate that this byte number is defined
        if (bitNumber > outputBits[nodeType]-1 ) {
            warn("Output bit out-of-range for defined node: "+bitNumber);
        }
        if (byteNumber >= 256) byteNumber = 255;
        // update the byte
        byte bit = (byte) (1<<((bitNumber-1) % 8));
        byte oldByte = outputArray[byteNumber];
        if (state) outputArray[byteNumber] &= (~bit);
        else outputArray[byteNumber] |= bit;
        // check for change, necessitating a send
        if (oldByte != outputArray[byteNumber]) {
            needSend = true;
            outputByteChanged[byteNumber] = true;
        }
    }

    /**
     * Public method to return state of Sensors.
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean sensorsActive() { return hasActiveSensors; }

    /**
     * Public method to return state of needSend flag.
     */
    public boolean mustSend() { return needSend; }

    /**
     * Public to reset state of needSend flag.
     * Can only reset if there are no bytes that need to be
     * sent
     */
    public void resetMustSend() { 
        for (int i = 0; i < (outputBits[nodeType]+7)/8; i++) {
            if (outputByteChanged[i]) return;
        }
        needSend = false; 
    }
    /**
     * Public to set state of needSend flag.
     */
    public void setMustSend() { needSend = true; }

    /**
     * Public method to return node type
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Public method to set node type.
     */
    public void setNodeType(int type) {
        nodeType = type;
        switch (nodeType) {
            default:
                log.error("Unexpected nodeType in setNodeType: "+nodeType);
                // use NODE2002V6 as default
            case NODE2002V6:
            case NODE2002V1:
            case NODE2000:
                break;
        }
    }

    /**
     * Public method to return the node address.
     */
    public int getNodeAddress() {
        return (nodeAddress);
    }

    /**
     * Public method to set the node address.
     * @param address Node address from 1 to 127 inclusive
     */
    public void setNodeAddress(int address) {
        if ( (address >= 1) && (address <= 127) ) {
            nodeAddress = address;
        }
        else {
            log.error("illegal node address: "+Integer.toString(address));
            nodeAddress = 1;
        }
    }


    /**
     * Public Method to create an Initialization packet (SerialMessage) for this node.
     * There are currently no Grapevine boards that need an init message, so this
     * returns null.
     */
    public SerialMessage createInitPacket() {
        return null;
    }
    
    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public SerialMessage createOutPacket() {
        if (log.isDebugEnabled()) log.debug("createOutPacket for nodeType "
            +nodeType+" with "
            +outputByteChanged[0]+" "+outputArray[0]+";"
            +outputByteChanged[1]+" "+outputArray[1]+";"
            +outputByteChanged[2]+" "+outputArray[2]+";"
            +outputByteChanged[3]+" "+outputArray[3]+";");
            
        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage();
        m.setElement(0,nodeAddress); // node address
        m.setElement(1,17);          
        // Add output bytes
        for (int i = 0; i < (outputBits[nodeType]+7)/8; i++) {
            if (outputByteChanged[i]) {
                outputByteChanged[i] = false;
                m.setElement(2, i);
                m.setElement(3, outputArray[i]);
                return m;
            }
        }
        
        // return result packet for start of card, since need
        // to do something!
        m.setElement(2,0);
        m.setElement(3,outputArray[0]);
        return m;
    }

    boolean warned = false;

    void warn(String s) {
    	if (warned) return;
    	warned = true;
    	log.warn(s);
    }

    /**
     * Use the contents of a reply from the Grapevine to mark changes
     * in the sensors on the layout.
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        // first, is it from a sensor?
        if ( !(l.isFromParallelSensor() || l.isFromSerialSensor()) ) return;  // not interesting message

        // Yes, continue.
        // Want to get individual sensor bits, and xor them with the 
        // past state and the inverted bit.
    
        if (l.isFromSerialSensor()) {
            // Serial sensor has only one bit. Extract value, then address
            boolean input = ((l.getElement(1)&0x01)!=0);
            int number = ((l.getElement(1)&0x7E)>>1) +1;
            // Update
            markBit(input, number);
        } else {
            // Skip if missing high bits
            if ((l.getElement(1)&0xC0) != 0x40) return;
            
            // Parallel sensor brings in a nibble of four bits
            int byte1 = l.getElement(1);
            boolean oldSerial  = ( (byte1&0x20) != 0);
            boolean highNibble = ( (byte1&0x10) != 0);
            boolean b0 = (byte1 & 0x01) == 0;
            boolean b1 = (byte1 & 0x02) == 0;
            boolean b2 = (byte1 & 0x04) == 0;
            boolean b3 = (byte1 & 0x08) == 0;
            int number = 1 + (highNibble ? 4 : 0) + (!oldSerial ? 100 : 0);
            markBit(b0, number);
            markBit(b1, number+1);
            markBit(b2, number+2);
            markBit(b3, number+3);
        }
        
/*         try { */
/*             for (int i=0; i<=lastUsedSensor; i++) { */
/*                 if (sensorArray[i] == null) continue; // skip ones that don't exist */
/*                 int loc = i/8; */
/*                 int bit = i%8; */
/*                 boolean value = (((l.getElement(loc+2)>>bit)&0x01) == 1) ^ sensorArray[i].getInverted();  // byte 2 is first of data */
/*                 // if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value); */
/*                 if ( value ) { */
/*                     // bit set, considered ACTIVE */
/*                     if (    ( (sensorTempSetting[i] == Sensor.ACTIVE) ||  */
/*                                 (sensorTempSetting[i] == Sensor.UNKNOWN) ) && */
/*                             ( sensorLastSetting[i] != Sensor.ACTIVE) ) { */
/*                         sensorLastSetting[i] = Sensor.ACTIVE; */
/*                         sensorArray[i].setKnownState(Sensor.ACTIVE); */
/*                     } */
/*                     // save for next time */
/*                     sensorTempSetting[i] = Sensor.ACTIVE; */
/*                 } else { */
/*                     // bit reset, considered INACTIVE */
/*                     if (    ( (sensorTempSetting[i] == Sensor.INACTIVE)  ||  */
/*                                 (sensorTempSetting[i] == Sensor.UNKNOWN) ) && */
/*                             ( sensorLastSetting[i] != Sensor.INACTIVE) ) { */
/*                         sensorLastSetting[i] = Sensor.INACTIVE; */
/*                         sensorArray[i].setKnownState(Sensor.INACTIVE); */
/*                     } */
/*                     // save for next time */
/*                     sensorTempSetting[i] = Sensor.INACTIVE; */
/*                 } */
/*             } */
/*         } catch (JmriException e) { log.error("exception in markChanges: "+e); } */
    }

    /**
     * Mark and act on a single input bit.
     * @param input True if sensor says active
     * @param sensorNum from 0 to lastUsedSensor on this node
     */
    void markBit(boolean input, int sensorNum) {
        if (sensorArray[sensorNum] == null) {
            log.info("Try to create sensor "+sensorNum+" on node "+getNodeAddress()+", since sensor doesn't exist");
            // try to make the sensor
            sensorArray[sensorNum] = jmri.InstanceManager
                                        .sensorManagerInstance()
                                        .provideSensor("GS"+(getNodeAddress()*1000+sensorNum));
        }
        
        boolean value = input ^ sensorArray[sensorNum].getInverted(); 

        try {
            if ( value ) {
                // bit set, considered ACTIVE
                if ( sensorLastSetting[sensorNum] != Sensor.ACTIVE ) {
                    sensorLastSetting[sensorNum] = Sensor.ACTIVE;
                    sensorArray[sensorNum].setKnownState(Sensor.ACTIVE);
                }
            } else {
                // bit reset, considered INACTIVE
                if ( sensorLastSetting[sensorNum] != Sensor.INACTIVE ) {
                    sensorLastSetting[sensorNum] = Sensor.INACTIVE;
                    sensorArray[sensorNum].setKnownState(Sensor.INACTIVE);
                }
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }
    
    /**
     * The numbers here are 0 to MAXSENSORS, not 1 to MAXSENSORS.
     * @param s - Sensor object
     * @param i - 0 to MAXSENSORS number of sensor's input bit on this node
     */
    public void registerSensor(Sensor s, int i) {
        // validate the sensor ordinal
        if ( (i<0) || (i> (inputBits[nodeType] - 1)) || (i>MAXSENSORS) ) {
            log.error("Unexpected sensor ordinal in registerSensor: "+Integer.toString(i+1));
            return;
        }
        hasActiveSensors = true;
        if (sensorArray[i] == null) {
            sensorArray[i] = s;
            if (lastUsedSensor<i) {
                lastUsedSensor = i;
            }
        }
        else {
            // multiple registration of the same sensor
            log.warn("multiple registration of same sensor: CS"+
                    Integer.toString((nodeAddress*SerialSensorManager.SENSORSPERNODE) + i + 1) );
        }
    }

    int timeout = 0;
    /**
     *
     * @return true if initialization required
     */
    boolean handleTimeout(AbstractMRMessage m) {
        timeout++;
        // normal to timeout in response to init, output
        if (m.getElement(1)!=0x50) return false;
        
        // see how many polls missed
        if (log.isDebugEnabled()) log.warn("Timeout to poll for addr="+nodeAddress+": consecutive timeouts: "+timeout);
        
        if (timeout>5) { // enough, reinit
            // reset timeout count to zero to give polls another try
            timeout = 0;
            // reset poll and send control so will retry initialization
            setMustSend();
            return true;   // tells caller to force init
        } 
        else return false;
    }
    void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
