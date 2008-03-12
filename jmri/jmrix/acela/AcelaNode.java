// AcelaNode.java

package jmri.jmrix.acela;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;

/**
 * Models a Acela node.
 * <P>
 * Nodes are numbered from 0.
 * The first watchman node carries the first 8 sensors 0 to 7, etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 * <P>
 * Same applies to the outputs (Dash-8s and Signalmen)
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @version	$Revision: 1.3 $
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */
public class AcelaNode {

    /**
     * Maximum number of sensors/outputs any node of any type can carry.
     */
    static final int MAXSENSORBITS = 8;  // Used to initialize arrays
    static final int MAXOUTPUTBITS = 16;  // Used to initialize arrays

    private static int MAXNODE = 1024;
    private static int MAXDELAY = 65535;

    // class constants
    public static final byte TB = 0x01;	// TrainBrain (4 output bits and 4 input bits)
    public static final byte D8 = 0x02;	// Dash-8 (8 output bits)
    public static final byte WM = 0x03;	// Watchman (8 input bits)
    public static final byte SM = 0x04;	// SignalMan (16 output bits)
    public static final byte SC = 0x05;	// SmartCab (no output bits. no input bits)
    public static final byte UN = 0x00;	// Although this may be dangerous

    public static final String[] boardNames = new String[]{"<none>",
                                        "TrainBrain (4 output bits and 4 input bits)",
                                        "Dash-8 (8 output bits)",
                                        "Watchman (8 input bits)",
                                        "SignalMan (16 output bits)",
                                        "SmartCab (no output bits. no input bits)",
                                        "<undefined>"
                                        };
    // node definition instance variables (must persist between runs)
    public int nodeAddress = 0;                         // Node address, 0-1024 allowed
    protected int nodeType = UN;                        // See above
    protected int outputbitsPerCard = MAXOUTPUTBITS;    // See above
    protected int sensorbitsPerCard = MAXSENSORBITS;    // See above
    protected int transmissionDelay = 0;                // Delay between bytes on Receive (units of 10 microsec.)

    // operational instance variables  (should not be preserved between runs)
    protected boolean needInit = false;          // 'true' if this module needs to be initialized
                                                 //    used for sensors
    protected boolean needSend = false;          // 'true' if something has changed in the outputByte array since
                                                 //    the last send to the hardware node
    protected byte[] outputArray = new byte[MAXOUTPUTBITS]; // current values of the output bits for this node
    														  // BOB C: THis really could be a boolean array
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = -1;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORBITS];
    protected boolean[] sensorInit = new boolean[MAXSENSORBITS];    // used to indicate if sensor needs to be configured
    protected int[] sensorLastSetting = new int[MAXSENSORBITS];
    protected int[] sensorTempSetting = new int[MAXSENSORBITS];

    protected int startingOutputAddress = -1;           // used to aid linear address search
    protected int endingOutputAddress = -1;           // used to aid linear address search
    protected int startingSensorAddress = -1;           // used to aid linear address search
    protected int endingSensorAddress = -1;           // used to aid linear address search

    /**
     * Assumes a node address of 0, and a node type of NO_CARD
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress, and actual node type using 'setNodeType'
     */
    public AcelaNode() {
        this (0, UN);
    }

    /**
     * Creates a new AcelaNode and initialize default instance variables
     *   address - Address of first bit on Acela bus (0-1023)
     *   type - D8, SM, WM
     */
    public AcelaNode(int address, int type) {
        // set address and type and check validity
        setNodeAddress (address);
        setNodeType (type);

        // set default values for other instance variables
        transmissionDelay = 0;

        // clear the Sensor arrays
        for (int i = 0; i<MAXSENSORBITS; i++) {
            sensorArray[i] = null;
            sensorInit[i] = false;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
        }

        // clear all output bits
        for (int i = 0; i<MAXOUTPUTBITS; i++) {
            outputArray[i] = 0;
        }

        // initialize other operational instance variables
        needSend = false;
        needInit = false;
        hasActiveSensors = false;

        // register this node
        AcelaTrafficController.instance().registerAcelaNode(this);
    }

    /**
     * Public method setting starting output addresss
     *    Used to help linear address search
     */
    public void setStartingOutputAddress(int startingAddress) {
        startingOutputAddress = startingAddress;
    }	

    /**
     * Public method getting starting output addresss
     *    Used to help linear address search
     */
    public int getStartingOutputAddress() {
        return startingOutputAddress;
    }	

    /**
     * Public method setting ending output addresss
     *    Used to help linear address search
     */
    public void setEndingOutputAddress(int endingAddress) {
        endingOutputAddress = endingAddress;
    }	

    /**
     * Public method getting ending output addresss
     *    Used to help linear address search
     */
    public int getEndingOutputAddress() {
        return endingOutputAddress;
    }	

    /**
     * Public method setting starting sensor addresss
     *    Used to help linear address search
     */
    public void setStartingSensorAddress(int startingAddress) {
        startingSensorAddress = startingAddress;
    }	

    /**
     * Public method getting starting sensor addresss
     *    Used to help linear address search
     */
    public int getStartingSensorAddress() {
        return startingSensorAddress;
    }	

    /**
     * Public method setting ending sensor addresss
     *    Used to help linear address search
     */
    public void setEndingSensorAddress(int endingAddress) {
        endingSensorAddress = endingAddress;
    }	

    /**
     * Public method getting ending sensor addresss
     *    Used to help linear address search
     */
    public int getEndingSensorAddress() {
        return endingSensorAddress;
    }	

    /**
     * Public method setting an output bit.
     *    Note:  state = 'true' for 0, 'false' for 1
     */
    public void setOutputBit(int bitNumber, boolean state) {
	// Save old state
        byte oldbyte = 0;
       	int newbitNumber = 0;
        newbitNumber = bitNumber - startingOutputAddress;
        oldbyte = outputArray[newbitNumber];
            	
        if (state) {
        	outputArray[newbitNumber] = 1;
        } else {
        	outputArray[newbitNumber] = 0;
        }

        // check for change, necessitating a send
        if (oldbyte != outputArray[newbitNumber]) {
            needSend = true;
        }
    }
    	
    /**
     * Public method get the current state of an output bit.
     *    Note:  returns 'true' for 0, 'false' for 1
     *           bits are numbered from 0 for Acela
     */
    public boolean getOutputBit(int bitNumber) {
       	int newbitNumber = 0;
        newbitNumber = bitNumber - startingOutputAddress;
        byte testByte = outputArray[newbitNumber];
        if (testByte == 0) {
        	return (false);
        } else {
        	return (true);
        }
    }

    /**
     * Public method to return state of Sensors.
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean sensorsActive() { 
    	return hasActiveSensors;
    }

    /**
     * Public method to return state of needSend flag.
     */
    public boolean mustSend() { 
    	return needSend;
    }

    /**
     * Public to reset state of needSend flag.
     */
    public void resetMustSend() { 
    	needSend = false; 
    }

    /**
     * Public to set state of needSend flag.
     */
    public void setMustSend() { 
    	needSend = true;
    }

    /**
     * Public method to return node type
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Public method to set node type
     */
    public void setNodeType(int type) {
        nodeType = type;
        // set default values for other instance variables
        switch (type) {
            case TB: {
        	outputbitsPerCard = 4;
        	sensorbitsPerCard = 4;
                break;
            }
            case D8: {         
        	outputbitsPerCard = 8;
        	sensorbitsPerCard = 0;
                break;
            }
            case WM: {
        	outputbitsPerCard = 0;
        	sensorbitsPerCard = 8;
                break;
            }
            case SM: {
        	outputbitsPerCard = 16;
        	sensorbitsPerCard = 0;
                break;
            }
            case SC: {
        	outputbitsPerCard = 1;
        	sensorbitsPerCard = 0;
                break;
            }
            case UN: {
        	outputbitsPerCard = 0;
        	sensorbitsPerCard = 0;
                break;
            }
            default: {
        	outputbitsPerCard = 0;
        	sensorbitsPerCard = 0;
        	log.error("Bad node type - "+Integer.toString(type) );
            }
        }
    }

    /**
     * Public method to return number of bits per card.
     */
    public int getNumOutputBitsPerCard() {
        return (outputbitsPerCard);
    }
    public int getNumSensorBitsPerCard() {
        return (sensorbitsPerCard);
    }


    /**
     * Public method to return the node address.
     */
    public int getNodeAddress() {
        return (nodeAddress);
    }

    /**
     * Public method to set the node address.
     */
    public void setNodeAddress(int address) {
        if ( (address >= 0) && (address < MAXNODE) ) {
            nodeAddress = address;
        }
        else {
            log.error("illegal node address: "+Integer.toString(address));
            nodeAddress = -1;
        }
    }

    /**
     * Public method to return the number of sensor bits per node.
     */
    public int getSensorBitsPerCard() {
        return (sensorbitsPerCard);
    }

    /**
     * Public method to return transmission delay.
     */
    public int getTransmissionDelay() {
        return (transmissionDelay);
    }

    /**
     * Public method to set transmission delay.
     *   delay - delay between bytes on receive (units of 10 microsec.)
     *   Note: two bytes are used, so range is 0-65,535.  If delay
     *          is out of range, it is restricted to the allowable range
     */
    public void setTransmissionDelay(int delay) {
        if ( (delay < 0) || (delay > MAXDELAY) ) {
            log.warn("transmission delay out of 0-65535 range: "+
                                            Integer.toString(delay));
            if (delay < 0) delay = 0;
            if (delay > MAXDELAY) delay = MAXDELAY;
        }
        transmissionDelay = delay;
    }

    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public AcelaMessage createOutPacket(int nodeindex) {
    	byte addr = 0x00;
        Integer tempint = new Integer(startingOutputAddress);
        addr = tempint.byteValue();

        if (nodeType == TB) {         
            int tempsettings = outputArray[3] * 8 + outputArray[2] * 4 + outputArray[1] * 2 + outputArray[0] * 1;
            byte newsettings = (byte) (tempsettings);
            AcelaMessage m = new AcelaMessage(4);
            m.setElement(0, 0x07);
            m.setElement(1, 0x00);
            m.setElement(2, addr);
            m.setElement(3, newsettings);
            m.setBinary(true);
            return m;
    	}
    	if (nodeType == D8) {         
            int tempsettings = outputArray[3] * 8 + outputArray[2] * 4 + outputArray[1] * 2 + outputArray[0] * 1;
            tempsettings = outputArray[7] * 128 + outputArray[6] * 64 + outputArray[5] * 32 + outputArray[4] * 16 + tempsettings;
            byte newsettings = (byte) (tempsettings);
            AcelaMessage m = new AcelaMessage(4);
            m.setElement(0, 0x08);
            m.setElement(1, 0x00);
            m.setElement(2, addr);
            m.setElement(3, newsettings);
            m.setBinary(true);
            return m;
    	}
    	if (nodeType == WM) {
            AcelaMessage m = new AcelaMessage(3);
            m.setElement(0, 0x01);
            m.setElement(1, 0x00);
            m.setElement(2, 0x00);
            m.setBinary(true);
            return m;
    	}
    	if (nodeType == SC) {
            AcelaMessage m = new AcelaMessage(3);
            m.setElement(0, 0x01);
            m.setElement(1, 0x00);
            m.setElement(2, 0x00);
            m.setBinary(true);
            return m;
    	}
    	if (nodeType == SM) {
            int tempsettings = outputArray[3] * 8 + outputArray[2] * 4 + outputArray[1] * 2 + outputArray[0] * 1;
            tempsettings = outputArray[7] * 128 + outputArray[6] * 64 + outputArray[5] * 32 + outputArray[4] * 16 + tempsettings;
            byte newsettings = (byte) (tempsettings);
            int tempsettings2 = outputArray[11] * 8 + outputArray[10] * 4 + outputArray[9] * 2 + outputArray[8] * 1;
            tempsettings2 = outputArray[15] * 128 + outputArray[14] * 64 + outputArray[13] * 32 + outputArray[12] * 16 + tempsettings2;
            byte newsettings2 = (byte) (tempsettings2);
            AcelaMessage m = new AcelaMessage(5);
            m.setElement(0, 0x09);
            m.setElement(1, 0x00);
            m.setElement(2, addr);
            m.setElement(3, newsettings2);
            m.setElement(4, newsettings);
            m.setBinary(true);
            return m;
    	}

        AcelaMessage m = new AcelaMessage(3);
        m.setElement(0, 0x01);
        m.setElement(1, 0x00);
        m.setElement(2, 0x00);
        m.setBinary(true);
        return m;
    }

    boolean warned = false;

    void warn(String s) {
    	if (warned) return;
    	warned = true;
    	log.warn(s);
    }

    /**
     * Use the contents of the poll reply to mark changes
     * @param l Reply to a poll operation
     */
    public void markChanges(AcelaReply l, int startingAbsoluteSensorAddress, int endingAbsoluteSensorAddress) {
        try {
            for (int i=0; i<sensorbitsPerCard; i++) {
                if (sensorArray[i] == null) continue; // skip ones that don't exist
                int relnode = 0;
                int k = 0;
                while (k < startingAbsoluteSensorAddress) {
                    k = k + 8;  // Bob C: This 8 needs to be calculated.
                    relnode = relnode + 1;
                }
//              int relnode = this.nodeAddress - 11;
                int rawvalue = l.getElement(relnode);
                int relvalue = rawvalue;
                for (int j=0; j < i; j ++) {
                	relvalue = relvalue >> 1;
                }
                boolean nooldstate = false;
                byte oldstate = 0x00;
                if (sensorLastSetting[i] == Sensor.ACTIVE) {
                	oldstate = 0x01;
                } else {
                	if (sensorLastSetting[i] == Sensor.INACTIVE) {
                		oldstate = 0x00;
                	} else {
                		nooldstate = true;
                	}
                }
                int newerstate = relvalue &0x01;
                byte newstate = (byte) (newerstate);
                
                if ((nooldstate) || (oldstate != newstate)) {
                	if (newstate == 0x00) {
                		sensorLastSetting[i] = Sensor.INACTIVE;
                		sensorArray[i].setKnownState(sensorLastSetting[i]);
                	} else {
                		sensorLastSetting[i] = Sensor.ACTIVE;
                		sensorArray[i].setKnownState(sensorLastSetting[i]);
                	}
                }
                

            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }


    /**
     * The numbers here are 0 to MAXSENSORBITS, not 1 to MAXSENSORBITS.
     * @param s - Sensor object
     * @param rawaddr - 0 to MAXSENSORBITS number of sensor's input bit on this node
     */

    public void registerSensor(Sensor s, int rawaddr) {
        // validate the sensor ordinal
        if ( (rawaddr<0) || (rawaddr>=MAXNODE) ) {
            log.error("Unexpected sensor ordinal in registerSensor: "+Integer.toString(rawaddr));
            return;
        }

        int addr = -1;
        addr = rawaddr - startingSensorAddress;

       	hasActiveSensors = true;
        AcelaTrafficController.instance().setAcelaSensorsState(true);
        if (sensorArray[addr] == null) {
            sensorArray[addr] = s;
            sensorInit[addr] = true;
        }
        else {
            // multiple registration of the same sensor
            log.warn("multiple registration of same sensor: CS"+ rawaddr );
        }
    }


    int timeout = 0;

    /**
     *
     * @return true if initialization required
     */
    boolean handleTimeout(AbstractMRMessage m) {
        timeout++;
        if (log.isDebugEnabled()) log.warn("Timeout to poll for UA="+nodeAddress+": consecutive timeouts: "+timeout);
        return false;   // tells caller to NOT force init
    }

    void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaNode.class.getName());
}

/* @(#)AcelaNode.java */