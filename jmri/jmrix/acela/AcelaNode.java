// AcelaNode.java

package jmri.jmrix.acela;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;

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
 * @version	$Revision: 1.7 $
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */
public class AcelaNode extends AbstractNode {

    /**
     * Maximum number of sensors/outputs any node of any type can carry.
     */
    static final int MAXSENSORBITS = 16;  // Used to initialize arrays
    static final int MAXOUTPUTBITS = 16;  // Used to initialize arrays

    private static int MAXNODE = 1024;
    private static int MAXDELAY = 65535;

    // class constants
    public static final byte AC = 0x00;	// Acela Interface module (no inputs, no outputs)
                                        // Does not really return a code of 0x00
    public static final byte TB = 0x01;	// TrainBrain (4 output bits and 4 input bits)
    public static final byte D8 = 0x02;	// Dash-8 (8 output bits)
    public static final byte WM = 0x03;	// Watchman (8 input bits)
    public static final byte SM = 0x04;	// SignalMan (16 output bits)
    public static final byte SC = 0x05;	// SmartCab (1 output bits. no input bits)
    public static final byte SW = 0x06;	// SwitchMan (16 output bits. no input bits)
    public static final byte YM = 0x07;	// YardMaster (16 output bits. no input bits)
    public static final byte SY = 0x08;	// Sentry (no output bits. 16 input bits)
    public static final byte UN = 0x09;	// Unidentified module -- should be FF
    public static final String moduleTypes = "ACTBD8WMSMSCSWYMSYUN";

    public static final String[] nodeNames = new String[]{"0", "1", "2", "3", "4",
                                                          "5", "6", "7", "8", "9",
                                                        "10", "11", "12", "13", "14",
                                                        "15", "16", "17", "18", "19"
                                        };

    public static final String[] moduleNames = new String[]{"Acela",
                                        "TrainBrain",
                                        "Dash-8",
                                        "Watchman",
                                        "SignalMan",
                                        "SmartCab",
                                        "SwitchMan",
                                        "YardMaster",
                                        "Sentry"
                                        };

    public static final String[] moduleTips = new String[]{"Acela",
                                        "TrainBrain has 4 output circuits and 4 input circuits",
                                        "Dash-8 has 8 output circuits and no input circuits",
                                        "Watchman has no output circuits and 8 input circuits",
                                        "SignalMan has 16 output circuits and no input circuits",
                                        "SmartCab has 1 output circuit and no input circuits",
                                        "SwitchMan has 16 output circuits and no input circuits",
                                        "YardMaster has 16 output circuits and no input circuits",
                                        "Sentry has no output circuits and 16 input circuits"
                                        };

    // node definition instance variables (must persist between runs)
    protected int nodeType = UN;                        // See above
    protected int outputbitsPerCard = MAXOUTPUTBITS;    // See above
    protected int sensorbitsPerCard = MAXSENSORBITS;    // See above
    protected int transmissionDelay = 0;                // Delay between bytes on Receive (units of 10 microsec.)

    // operational instance variables  (should not be preserved between runs)
    protected boolean needInit = false;          // 'true' if this module needs to be initialized
                                                 //    used for sensors
    protected byte[] outputArray = new byte[MAXOUTPUTBITS]; // current values of the output bits for this node
    														  // BOB C: THis really could be a boolean array
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = -1;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORBITS];
    protected boolean[] sensorNeedInit = new boolean[MAXSENSORBITS];    // used to indicate if sensor needs to be configured
    protected boolean[] sensorHasBeenInit = new boolean[MAXSENSORBITS]; // used to indicate if sensor has been configured
    protected int[] sensorLastSetting = new int[MAXSENSORBITS];
    protected int[] sensorTempSetting = new int[MAXSENSORBITS];
    protected int[] sensorType = new int[MAXSENSORBITS];
    protected int[] sensorPolarity = new int[MAXSENSORBITS];
    protected int[] sensorThreshold = new int[MAXSENSORBITS];
    protected byte[] sensorConfigArray = new byte[MAXSENSORBITS]; // configuration values of the sensor circuits for this node
    protected int[] outputWired = new int[MAXOUTPUTBITS];
    protected int[] outputInit = new int[MAXOUTPUTBITS];
    public static final String sensorTypes = "NFSBCGDT";
    public static final String sensorPolarities = "ACTINV";
//    public static final String sensorThresholds = "MINLOWNORHIGMAX";
    public static final String outputWireds = "NONC";
    public static final String outputInits = "OFFACT";

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
            sensorNeedInit[i] = false;
            sensorHasBeenInit[i] = false;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
            sensorType[i] = 2; // Car Gap
            sensorPolarity[i] = 1; // Inverse
            sensorThreshold[i] = 4; // Normal -- 0010 0
            sensorConfigArray[i] = 0x00; // Normal
        }

        // clear all output bits
        for (int i = 0; i<MAXOUTPUTBITS; i++) {
            outputArray[i] = 0;
            outputInit[i] = 0;  // Off
            outputWired[i] = 0; // NO (Normally Open)
        }

        // initialize other operational instance variables
        resetMustSend();
        needInit = false;
        hasActiveSensors = false;

        // register this node
        AcelaTrafficController.instance().registerAcelaNode(this);
    }

    public void initNode() {
        if (outputbitsPerCard >0) {
            // Initialize all output circuits
            for (int i = 0; i<MAXOUTPUTBITS; i++) {
                outputArray[i] = (byte) outputInit[i];
                //  outputWired is applied as the command is being constructed so all GUI views on as on and off as off.
            }
            setMustSend();
        }
        if (sensorbitsPerCard >0) {
            // Initialize all sensor circuits
            for (int i = 0; i<MAXSENSORBITS; i++) {
                sensorConfigArray[i] = (byte) ((byte) (sensorThreshold[i] << 3) + (byte) (sensorType[i] << 1) + (byte) (sensorPolarity[i]));
//                sensorConfigArray[i] = (byte) ((byte) (sensorType[i] << 1));
                sensorNeedInit[i] = true;
            }
            hasActiveSensors = true;
        }
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
            setMustSend();
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
     * Public method to set and return Output configuration values
     */
    public int getOutputWired(int circuitnum) {
        return outputWired[circuitnum];
    }

    public String getOutputWiredString(int circuitnum) {
        int sensortype = outputWired[circuitnum];
        String value = outputWireds.substring(sensortype*2, sensortype*2+2);
        return value;
    }

    public void setOutputWired(int circuitnum, int type) {
        outputWired[circuitnum] = type;
    }
    
    public void setOutputWiredString(int circuitnum, String stringtype) {
        int type = outputWireds.lastIndexOf(stringtype) / 2;
        outputWired[circuitnum] = type;
    }
    
    public int getOutputInit(int circuitnum) {
        return outputInit[circuitnum];
    }

    public String getOutputInitString(int circuitnum) {
        int sensortype = outputInit[circuitnum];
        String value = outputInits.substring(sensortype*3, sensortype*3+3);
        return value;
    }

    public void setOutputInit(int circuitnum, int type) {
        outputInit[circuitnum] = type;
    }
    
    public void setOutputInitString(int circuitnum, String stringtype) {
        int type = outputInits.lastIndexOf(stringtype) / 3;
        outputInit[circuitnum] = type;
    }
    
    /**
     * Public method to set and return Sensor configuration values
     */
    public int getSensorType(int circuitnum) {
        return sensorType[circuitnum];
    }

    public String getSensorTypeString(int circuitnum) {
        int sensortype = sensorType[circuitnum];
        String value = sensorTypes.substring(sensortype*2, sensortype*2+2);
        return value;
    }

    public void setSensorType(int circuitnum, int type) {
        sensorType[circuitnum] = type;
    }
    
    public void setSensorTypeString(int circuitnum, String stringtype) {
        int type = sensorTypes.lastIndexOf(stringtype) / 2;
        sensorType[circuitnum] = type;
    }
    
    public int getSensorPolarity(int circuitnum) {
        return sensorPolarity[circuitnum];
    }

    public String getSensorPolarityString(int circuitnum) {
        int sensorpolarity = sensorPolarity[circuitnum];
        String value = sensorPolarities.substring(sensorpolarity*3, sensorpolarity*3+3);
        return value;
    }

    public void setSensorPolarity(int circuitnum, int polarity) {
        sensorPolarity[circuitnum] = polarity;
    }
    
    public void setSensorPolarityString(int circuitnum, String stringpolarity) {
        int polarity = sensorPolarities.lastIndexOf(stringpolarity) / 3;
        sensorPolarity[circuitnum] = polarity;
    }
    
    public int getSensorThreshold(int circuitnum) {
        return sensorThreshold[circuitnum];
    }
/*
    public String getSensorThresholdString(int circuitnum) {
        int sensorthreshold = sensorThreshold[circuitnum];
        String value = sensorThresholds.substring(sensorthreshold*3, sensorthreshold*3+3);
        return value;
    }
*/
    public void setSensorThreshold(int circuitnum, int threshold) {
        sensorThreshold[circuitnum] = threshold;
    }
/*    
    public void setSensorThresholdString(int circuitnum, String stringthreshold) {
        int threshold = sensorThresholds.lastIndexOf(stringthreshold) / 3;
        sensorThreshold[circuitnum] = threshold;
    }
*/    
    /**
     * Public method to return node type
     */
    public int getNodeType() {
        return (nodeType);
    }

    public String getNodeTypeString() {
        String value = moduleTypes.substring(nodeType*2, nodeType*2+2);
        return value;
    }

    /**
     * Public method to set node type
     */
    public void setNodeTypeString(String stringtype) {
        int type = moduleTypes.lastIndexOf(stringtype) / 2;
        setNodeType (type);
    }
    
    public void setNodeType(int type) {
        nodeType = type;
        // set default values for other instance variables
        switch (type) {
            case AC: {
        	outputbitsPerCard = 0;
        	sensorbitsPerCard = 0;
                break;
            }
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
            case SW: {
        	outputbitsPerCard = 16;
        	sensorbitsPerCard = 0;
                break;
            }
            case YM: {
        	outputbitsPerCard = 16;
        	sensorbitsPerCard = 0;
                break;
            }
            case SY: {
        	outputbitsPerCard = 0;
        	sensorbitsPerCard = 16;
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
     * Public method to set the node address.
     */
    public boolean checkNodeAddress(int address) {
        return ( (address >= 0) && (address < MAXNODE) );
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
     * Create an initialization packet if needed
     */
    public AbstractMRMessage createInitPacket() { return null; }
    
    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public AbstractMRMessage createOutPacket() {
    	byte addr = 0x00;
        Integer tempint = new Integer(startingOutputAddress);
        addr = tempint.byteValue();

        if (nodeType == TB) {         
            int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
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
//            int tempsettings = outputArray[3] * 8 + outputArray[2] * 4 + outputArray[1] * 2 + outputArray[0] * 1;
//            tempsettings = outputArray[7] * 128 + outputArray[6] * 64 + outputArray[5] * 32 + outputArray[4] * 16 + tempsettings;
            int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
            tempsettings = (outputArray[7] ^ outputWired[7]) * 128 + (outputArray[6] ^ outputWired[6]) * 64 + (outputArray[5] ^ outputWired[5]) * 32 + (outputArray[4] ^ outputWired[4]) * 16 + tempsettings;
            byte newsettings = (byte) (tempsettings);
            AcelaMessage m = new AcelaMessage(4);
            m.setElement(0, 0x08);
            m.setElement(1, 0x00);
            m.setElement(2, addr);
            m.setElement(3, newsettings);
            m.setBinary(true);
            return m;
    	}
    	if ((nodeType == WM) || (nodeType == SY)) {
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
    	if ((nodeType == SM) || (nodeType == SW) || (nodeType == YM)) {
//            int tempsettings = outputArray[3] * 8 + outputArray[2] * 4 + outputArray[1] * 2 + outputArray[0] * 1;
//            tempsettings = outputArray[7] * 128 + outputArray[6] * 64 + outputArray[5] * 32 + outputArray[4] * 16 + tempsettings;
//            byte newsettings = (byte) (tempsettings);
//            int tempsettings2 = outputArray[11] * 8 + outputArray[10] * 4 + outputArray[9] * 2 + outputArray[8] * 1;
//            tempsettings2 = outputArray[15] * 128 + outputArray[14] * 64 + outputArray[13] * 32 + outputArray[12] * 16 + tempsettings2;
//            byte newsettings2 = (byte) (tempsettings2);
            int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
            tempsettings = (outputArray[7] ^ outputWired[7]) * 128 + (outputArray[6] ^ outputWired[6]) * 64 + (outputArray[5] ^ outputWired[5]) * 32 + (outputArray[4] ^ outputWired[4]) * 16 + tempsettings;
            byte newsettings = (byte) (tempsettings);
            int tempsettings2 = (outputArray[11] ^ outputWired[11]) * 8 + (outputArray[10] ^ outputWired[10]) * 4 + (outputArray[9] ^ outputWired[9]) * 2 + (outputArray[8] ^ outputWired[8]) * 1;
            tempsettings2 = (outputArray[15] ^ outputWired[15]) * 128 + (outputArray[14] ^ outputWired[14]) * 64 + (outputArray[13] ^ outputWired[13]) * 32 + (outputArray[12] ^ outputWired[12]) * 16 + tempsettings2;
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
    public void markChanges(AcelaReply l) {
        int numSensorstoProcess = sensorbitsPerCard;
  
        // We are going to get back 8 bits per byte from the poll.
        // We have three types of sensor modules:
        // TB with 4 sensor inputs, WM with 8 sensor inputs, SY with 16 sensor inputs
        // The TB causes two cases: either the bits we want start at bit 0 or bit 4.
        // The sensor bits we want for a TB will always be within one byte.
        // The sensor bits we want for a WM could be within one byte if we start at 0,
        //    or spread across two bytes if we start at 4.
        // The sensor bits we want for a SY could be within two byte if we start at 0,
        //    or spread across three bytes if we start at 4.
        int firstByteNum = startingSensorAddress / 8;
        int firstBitAt = startingSensorAddress % 8; // mod operator
        int numBytes = 1;   // For TB there are only 4 sensors so always 1 byte

        if (nodeType == WM) {
            if (firstBitAt != 0) {
                numBytes = 2;   //  8 bits, but straddling two bytes
            }
        }
        
        if (nodeType == SY) {
            if (firstBitAt == 0) {
                numBytes = 2;  // 16 bits, aligned in two bytes
            } else {
                numBytes = 3;  // 16 bits, straddling three bytes
            }
        }

        //  Maybe unnecessary, but trying to minimize reads to getElement 
        int rawvalue = l.getElement(firstByteNum);

        int usingByteNum = 0;
        
        try {
            for (int i=0; i<sensorbitsPerCard; i++) {
                if (sensorArray[i] == null) continue; // skip ones that don't exist

                //  Maybe unnecessary, but trying to minimize reads to getElement 
                int relvalue = rawvalue;

                //  Need a temporary counter within the byte  
                int tempi = i;

                //  If necessary, shift by four before we start  
                if (usingByteNum == 0) {
                    if (firstBitAt == 4) {
                        for (int j=0; j < firstBitAt; j++) {
                        	relvalue = relvalue >> 1;
                        }
                    }
                }

                //  If necessary, get next byte  
                if (firstBitAt == 4) {
                    if (i == 4) {
                        usingByteNum++;
                        //  Maybe unnecessary, but trying to minimize reads to getElement 
                        rawvalue = l.getElement(usingByteNum + firstByteNum);
                        relvalue = rawvalue;
                    }
                    if (i >= 4) {
                        tempi = i - 4;  // tempi needs to shift down by 4
                    }
                    if (i == 12) {  // Will only get here if there are 16 sensors
                        usingByteNum++;
                        //  Maybe unnecessary, but trying to minimize reads to getElement 
                        rawvalue = l.getElement(usingByteNum + firstByteNum);
                        relvalue = rawvalue;
                    }
                    if (i >= 12) {  // Will only get here if there are 16 sensors
                        tempi = i - 12;  // tempi needs to shift down by 12
                    }
                } else {
                    if (i == 8) {  // Will only get here if there are 16 sensors
                        usingByteNum++;
                        //  Maybe unnecessary, but trying to minimize reads to getElement 
                        rawvalue = l.getElement(usingByteNum + firstByteNum);
                        relvalue = rawvalue;
                    }
                    if (i >= 8) {  // Will only get here if there are 16 sensors
                        tempi = i - 8;  // tempi needs to shift down by 8
                    }
                }
                
                //  Finally we can isolate the bit from the poll
                for (int j=0; j < tempi; j ++) {
                	relvalue = relvalue >> 1;
                }

                // Now that we have the relvalue need to compare to last time
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
        if (startingSensorAddress < 0) {
            log.info("Trying to register sensor too early: AS"+ rawaddr );
        } else {

            if (sensorArray[addr] == null) {
                sensorArray[addr] = s;
                if (!sensorHasBeenInit[addr]) {
                    sensorNeedInit[addr] = true;
                }
            }
            else {
                // multiple registration of the same sensor
                log.warn("Multiple registration of same sensor: CS"+ rawaddr );
            }
        }
    }


    int timeout = 0;

    /**
     *
     * @return true if initialization required
     */
    public boolean handleTimeout(AbstractMRMessage m) {
        timeout++;
        if (log.isDebugEnabled()) log.warn("Timeout to poll for UA="+nodeAddress+": consecutive timeouts: "+timeout);
        return false;   // tells caller to NOT force init
    }

    public void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaNode.class.getName());
}

/* @(#)AcelaNode.java */