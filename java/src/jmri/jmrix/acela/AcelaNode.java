package jmri.jmrix.acela;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models an Acela node.
 * <p>
 * Nodes are numbered from 0. The first watchman node carries the first 8
 * sensors 0 to 7, etc.
 * <p>
 * The array of sensor states is used to update sensor known state only when
 * there's a change on the serial bus. This allows for the sensor state to be
 * updated within the program, keeping this updated state until the next change
 * on the serial bus. E.g. you can manually change a state via an icon, and not
 * have it change back the next time that node is polled.
 * <p>
 * Same applies to the outputs (Dash-8s and Signalmen)
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009 Based on CMRI serial
 * example, modified to establish Acela support.
 */
public class AcelaNode extends AbstractNode {

    /**
     * Maximum number of sensors/outputs any node of any type can carry.
     */
    static final int MAXSENSORBITS = 16;  // Used to initialize arrays
    static final int MAXOUTPUTBITS = 16;  // Used to initialize arrays
    static final int MAXNODE = 1024;
    private static int MAXDELAY = 65535;

    // class constants
    public static final byte AC = 0x00; // Acela Interface module (no inputs, no outputs)
    // Does not really return a code of 0x00
    public static final byte TB = 0x01; // TrainBrain (4 output bits and 4 input bits)
    public static final byte D8 = 0x02; // Dash-8 (8 output bits)
    public static final byte WM = 0x03; // Watchman (8 input bits)
    public static final byte SM = 0x04; // SignalMan (16 output bits)
    public static final byte SC = 0x05; // SmartCab (1 output bits. no input bits)
    public static final byte SW = 0x06; // SwitchMan (16 output bits. no input bits)
    public static final byte YM = 0x07; // YardMaster (16 output bits. no input bits)
    public static final byte SY = 0x08; // Sentry (no output bits. 16 input bits)
    public static final byte UN = 0x09; // Unidentified module -- should be FF
    public static final String moduleTypes = "ACTBD8WMSMSCSWYMSYUN";

    static final String[] nodeNames = new String[]{"0", "1", "2", "3", "4",
        "5", "6", "7", "8", "9",
        "10", "11", "12", "13", "14",
        "15", "16", "17", "18", "19"
    };

    public static String[] getNodeNames() {
        return nodeNames.clone();
    }

    static final String[] moduleNames = new String[]{"Acela",
        "TrainBrain",
        "Dash-8",
        "Watchman",
        "SignalMan",
        "SmartCab",
        "SwitchMan",
        "YardMaster",
        "Sentry"
    };

    public static String[] getModuleNames() {
        return moduleNames.clone();
    }

    static final String[] moduleTips = new String[]{"Acela",
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
    protected int[] outputSpecial = new int[MAXOUTPUTBITS]; // does the output circuit require special handling
    protected int[] outputSignalHeadType = new int[MAXOUTPUTBITS]; // type of SignalHead
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = -1;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORBITS];
    protected boolean[] sensorNeedInit = new boolean[MAXSENSORBITS];    // used to indicate if sensor needs to be configured
    protected boolean[] sensorHasBeenInit = new boolean[MAXSENSORBITS]; // used to indicate if sensor has been configured
    protected int[] sensorLastSetting = new int[MAXSENSORBITS];
    protected int[] sensorType = new int[MAXSENSORBITS];
    protected int[] sensorPolarity = new int[MAXSENSORBITS];
    protected int[] sensorThreshold = new int[MAXSENSORBITS];
    protected byte[] sensorConfigArray = new byte[MAXSENSORBITS]; // configuration values of the sensor circuits for this node
    protected int[] outputWired = new int[MAXOUTPUTBITS];
    protected int[] outputInit = new int[MAXOUTPUTBITS];
    protected int[] outputType = new int[MAXOUTPUTBITS];
    protected int[] outputLength = new int[MAXOUTPUTBITS];
    protected boolean[] outputNeedToSend = new boolean[MAXOUTPUTBITS];
    public static final String sensorTypes = "NFSBCGDT";
    public static final String sensorPolarities = "ACTINV";
    public static final String outputWireds = "NONC";
    public static final String outputInits = "OFFACT";
    public static final String outputTypes = "ONOFFPULSEBLINK";
    public static final int ONOFF = 0;
    public static final int PULSE = 1;
    public static final int BLINK = 2;
    public static final String outputSignalHeadTypes = "UKNOWNDOUBLETRIPLEBPOLARWIGWAG";
    public static final int UKNOWN = 0;
    public static final int DOUBLE = 1;
    public static final int TRIPLE = 2;
    public static final int BPOLAR = 3;
    public static final int WIGWAG = 4;
    // These can be removed in June 2010:
    public static final String outputONOFF = "ONOFF";   // used to dump/restore config file.
    public static final String outputLEN0 = "0";        // used to dump/restore config file.
    public static final String outputNO = "N0";         // used to dump/restore config file.
    protected int startingOutputAddress = -1;           // used to aid linear address search
    protected int endingOutputAddress = -1;             // used to aid linear address search
    protected int startingSensorAddress = -1;           // used to aid linear address search
    protected int endingSensorAddress = -1;             // used to aid linear address search

    /**
     * Create a new AcelaNode instance on the TrafficController associated
     * with the default AcelaSystemConnectionMemo.
     * <p>
     * Assumes a node address of 0, and a node type of NO_CARD. If this
     * constructor is used, actual node address must be set using
     * {@link jmri.jmrix.AbstractNode#setNodeAddress(int)} and actual
     * node type using {@link #setNodeType(int)}
     */
    public AcelaNode() {
        this(0, UN, InstanceManager.getDefault(AcelaSystemConnectionMemo.class).getTrafficController());
    }

    /**
     * Create a new AcelaNode instance and initialize default instance variables.
     *
     * @param address the address of first bit on Acela bus (0-1023) type - D8, SM, WM
     * @param type a type constant from the class
     * @param tc the TrafficControllerfor this connection
     */
    public AcelaNode(int address, int type, AcelaTrafficController tc) {
        // set address and type and check validity
        setNodeAddress(address);
        setNodeType(type);

        // set default values for other instance variables
        transmissionDelay = 0;

        // clear the Sensor arrays
        for (int i = 0; i < MAXSENSORBITS; i++) {
            sensorArray[i] = null;
            sensorNeedInit[i] = false;
            sensorHasBeenInit[i] = false;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorType[i] = 2; // Car Gap
            sensorPolarity[i] = 1; // Inverse
            sensorThreshold[i] = 4; // Normal -- 0010 0
            sensorConfigArray[i] = 0x00; // Normal
        }

        // clear all output bits
        for (int i = 0; i < MAXOUTPUTBITS; i++) {
            outputArray[i] = 0;
            outputSpecial[i] = 0;
            outputSignalHeadType[i] = 0;
            outputInit[i] = 0;  // Off
            outputWired[i] = 0; // NO (Normally Open)
            outputType[i] = 0; // ONOFF
            outputLength[i] = 10; // 10 tenths of a second
            outputNeedToSend[i] = false;
        }

        // initialize other operational instance variables
        resetMustSend();
        needInit = false;
        hasActiveSensors = false;

        // register this node
        tc.registerAcelaNode(this);
    }

    public void initNode() {
        if (outputbitsPerCard > 0) {
            // check to see if we can use bulk mode
            boolean bulk_message = true;
            int c = 0;
            while (c < outputbitsPerCard) {
                if ((outputType[c] != AcelaNode.ONOFF)
                        || (outputSpecial[c] != 0)) {
                    bulk_message = false;
                }
                c++;
            }

            // Initialize all output circuits
            for (int i = 0; i < MAXOUTPUTBITS; i++) {
                outputArray[i] = (byte) outputInit[i];
                if (!bulk_message) {
                    outputNeedToSend[i] = true;
                }
                //  outputWired is applied as the command is being constructed so all GUI views on as on and off as off.
            }
            setMustSend();
        }
        if (sensorbitsPerCard > 0) {
            // Initialize all sensor circuits
            for (int i = 0; i < MAXSENSORBITS; i++) {
                sensorConfigArray[i] = (byte) ((byte) (sensorThreshold[i] << 3) + (byte) (sensorType[i] << 1) + (byte) (sensorPolarity[i]));
                sensorNeedInit[i] = true;
            }
            hasActiveSensors = true;
        }
    }

    /**
     * Set starting output address for range.
     * Used to help linear address search.
     */
    public void setStartingOutputAddress(int startingAddress) {
        startingOutputAddress = startingAddress;
    }

    /**
     * Get starting output address for range.
     * Used to help linear address search.
     */
    public int getStartingOutputAddress() {
        return startingOutputAddress;
    }

    /**
     * Set ending output address for range.
     * Used to help linear address search.
     */
    public void setEndingOutputAddress(int endingAddress) {
        endingOutputAddress = endingAddress;
    }

    /**
     * Get ending output address for range.
     * Used to help linear address search.
     */
    public int getEndingOutputAddress() {
        return endingOutputAddress;
    }

    /**
     * Set starting sensor address for range.
     * Used to help linear address search.
     */
    public void setStartingSensorAddress(int startingAddress) {
        startingSensorAddress = startingAddress;
    }

    /**
     * Get starting sensor addresses for range.
     * Used to help linear address search.
     */
    public int getStartingSensorAddress() {
        return startingSensorAddress;
    }

    /**
     * Set ending sensor addresses for range.
     * Used to help linear address search.
     */
    public void setEndingSensorAddress(int endingAddress) {
        endingSensorAddress = endingAddress;
    }

    /**
     * Get ending sensor addresses for range.
     * Used to help linear address search.
     */
    public int getEndingSensorAddress() {
        return endingSensorAddress;
    }

    /**
     * Set an output bit on this node.
     *
     * @param bitNumber the bit to set
     * @param state bit state to set: 'true' for 0, 'false' for 1
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
        boolean bulk_message = true;
        int c = 0;
        while (c < outputbitsPerCard) {
            if ((outputType[c] != AcelaNode.ONOFF)
                    || (outputSpecial[c] != 0)) {
                bulk_message = false;
            }
            c++;
        }
        if (bulk_message) {
            // check for change, necessitating a send
            if (oldbyte != outputArray[newbitNumber]) {
                setMustSend();
            }
        } else {
            outputNeedToSend[newbitNumber] = true;
            setMustSend();
        }
    }

    /**
     * Get the current state of an output bit.
     *
     * @param bitNumber the bit. Bits are numbered from 0 for Acela
     * @return 'true' for 0, 'false' for 1
     */
    public boolean getOutputBit(int bitNumber) {
        int newbitNumber = 0;
        newbitNumber = bitNumber - startingOutputAddress;
        byte testByte = outputArray[newbitNumber];
        return  (testByte != 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSensorsActive() {
        return hasActiveSensors;
    }

    /**
     * Get Output configuration values.
     */
    public int getOutputWired(int circuitnum) {
        return outputWired[circuitnum];
    }

    public String getOutputWiredString(int circuitnum) {
        int sensortype = outputWired[circuitnum];
        return outputWireds.substring(sensortype * 2, sensortype * 2 + 2);
    }

    /**
     * Set Output configuration values.
     */
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
        return outputInits.substring(sensortype * 3, sensortype * 3 + 3);
    }

    public void setOutputInit(int circuitnum, int type) {
        outputInit[circuitnum] = type;
    }

    public void setOutputInitString(int circuitnum, String stringtype) {
        int type = outputInits.lastIndexOf(stringtype) / 3;
        outputInit[circuitnum] = type;
    }

    public int getOutputType(int circuitnum) {
        return outputType[circuitnum];
    }

    public String getOutputTypeString(int circuitnum) {
        int outputtype = outputType[circuitnum];
        return outputTypes.substring(outputtype * 5, outputtype * 5 + 5);
    }

    public void setOutputType(int circuitnum, int type) {
        outputType[circuitnum] = type;
    }

    public void setOutputTypeString(int circuitnum, String stringtype) {
        int type = outputTypes.lastIndexOf(stringtype) / 5;
        outputType[circuitnum] = type;
    }

    public int getOutputLength(int circuitnum) {
        return outputLength[circuitnum];
    }

    public void setOutputLength(int circuitnum, int newlength) {
        outputLength[circuitnum] = newlength;
    }

    public int getOutputSpecial(int circuitnum) {
        int newbitNumber = circuitnum - startingOutputAddress;
        return outputSpecial[newbitNumber];
    }

    public void setOutputSpecial(int circuitnum, int type) {
        int newbitNumber = circuitnum - startingOutputAddress;
        outputSpecial[newbitNumber] = type;
    }

    public int getOutputSignalHeadType(int circuitnum) {
        int newbitNumber = circuitnum - startingOutputAddress;
        return outputSignalHeadType[newbitNumber];
    }

    public String getOutputSignalHeadTypeString(int circuitnum) {
        int newbitNumber = circuitnum - startingOutputAddress;
        int outputsignalheadtype = outputSignalHeadType[newbitNumber];
        return outputSignalHeadTypes.substring(outputsignalheadtype * 6, outputsignalheadtype * 6 + 6);
    }

    public void setOutputSignalHeadType(int circuitnum, int type) {
        int newbitNumber = circuitnum - startingOutputAddress;
        outputSignalHeadType[newbitNumber] = type;
    }

    public void setOutputSignalHeadTypeString(int circuitnum, String stringtype) {
        int newbitNumber = circuitnum - startingOutputAddress;
        int type = outputSignalHeadTypes.lastIndexOf(stringtype) / 6;
        outputSignalHeadType[newbitNumber] = type;
    }

    /**
     * Public method to set and return Sensor configuration values.
     */
    public int getSensorType(int circuitnum) {
        return sensorType[circuitnum];
    }

    public String getSensorTypeString(int circuitnum) {
        int sensortype = sensorType[circuitnum];
        return sensorTypes.substring(sensortype * 2, sensortype * 2 + 2);
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
        return sensorPolarities.substring(sensorpolarity * 3, sensorpolarity * 3 + 3);
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

    public void setSensorThreshold(int circuitnum, int threshold) {
        sensorThreshold[circuitnum] = threshold;
    }

    /**
     * Public method to return node type.
     */
    public int getNodeType() {
        return (nodeType);
    }

    public String getNodeTypeString() {
        return moduleTypes.substring(nodeType * 2, nodeType * 2 + 2);
    }

    /**
     * Public method to set node type.
     */
    public void setNodeTypeString(String stringtype) {
        int type = moduleTypes.lastIndexOf(stringtype) / 2;
        setNodeType(type);
    }

    public void setNodeType(int type) {
        nodeType = type;
        // set default values for other instance variables
        switch (type) {
            case AC:
            case UN:
                outputbitsPerCard = 0;
                sensorbitsPerCard = 0;
                break;
            case TB:
                outputbitsPerCard = 4;
                sensorbitsPerCard = 4;
                break;
            case D8:
                outputbitsPerCard = 8;
                sensorbitsPerCard = 0;
                break;
            case WM:
                outputbitsPerCard = 0;
                sensorbitsPerCard = 8;
                break;
            case SC:
                outputbitsPerCard = 1;
                sensorbitsPerCard = 0;
                break;
            case SM:
            case SW:
            case YM:
                outputbitsPerCard = 16;
                sensorbitsPerCard = 0;
                break;
            case SY:
                outputbitsPerCard = 0;
                sensorbitsPerCard = 16;
                break;
            default:
                outputbitsPerCard = 0;
                sensorbitsPerCard = 0;
                log.error("Bad node type - {}", Integer.toString(type));
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
     * {@inheritDoc}
     */
    @Override
    public boolean checkNodeAddress(int address) {
        return ((address >= 0) && (address < MAXNODE));
    }

    /**
     * Get the number of sensor bits per node.
     *
     * @return sensorbitsPerCard
     */
    public int getSensorBitsPerCard() {
        return (sensorbitsPerCard);
    }

    /**
     * Get the transmission delay on thsi node.
     */
    public int getTransmissionDelay() {
        return (transmissionDelay);
    }

    /**
     * Set transmission delay.
     * <p>
     * Note: two bytes are used, so range is 0-65,535. If delay is out of
     * range, it is restricted to the allowable range.
     *
     * @param delay a delay between bytes on receive (units of 10 microsec.)
     */
    public void setTransmissionDelay(int delay) {
        if ((delay < 0) || (delay > MAXDELAY)) {
            log.warn("transmission delay {} out of 0-65535 range",
                    Integer.toString(delay));
            if (delay < 0) {
                delay = 0;
            }
            if (delay > MAXDELAY) {
                delay = MAXDELAY;
            }
        }
        transmissionDelay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Create a Transmit packet (SerialMessage) to send current state.
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        //  Set up variables that will be used at the end to send the msg.
        int cmdlen = 3;         // Message length == 3, 4, or 5
        byte cmdcode = 0x03;    // Message command: default == activate output
        byte addrhi = 0x00;     // Used to address more than 255 nodes
        byte addrlo = 0x00;     // Address of node
        byte settinghi = 0x00;  // Used when setting 16 outputs
        byte settinglo = 0x00;  // Used to set output state

        // If we can, we want to send one bulk message for the entire node
        // We can only send a bulk message if all of the output circuits have
        // outputType of ONOFF
        boolean bulk_message = true;
        int c = 0;
        while (c < outputbitsPerCard) {
            if ((outputType[c] != AcelaNode.ONOFF) || (outputSpecial[c] != 0)) {
                bulk_message = false;
            }
            c++;
        }

        //  For now, we are not going to support more than 255 nodes
        //  so we leave addrhi at 0x00.
        // We need to see if there is a second output circuit for this
        // node that we need to send.  If there is then we need to set
        // the mustsend flag for the node because the Traffic Controller
        // reset it before calling this routine.
        if (!bulk_message) {
            c = 0;
            boolean foundfirst = false;
            boolean foundanother = false;
            while (c < outputbitsPerCard) {
                if (outputNeedToSend[c] && foundfirst) {
                    foundanother = true;
                }
                if (outputNeedToSend[c] && !foundfirst) {
                    foundfirst = true;
                }
                c++;
            }
            if (foundanother) {
                setMustSend();
            }
        }

        //  If we are going to do a bulk command then the address will be
        //  the starting address.  If we are not going to do a bulk command
        //  then the address will start from the starting address.
        Integer tempint = startingOutputAddress;
        addrlo = tempint.byteValue();

        // For each nodetype set up variables that will end up in the msg
        if (bulk_message) {
            if (nodeType == TB) {
                cmdlen = 4;
                cmdcode = 0x07;  // Set 4 outputs: bit on means output on
                int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
                settinglo = (byte) (tempsettings);
            }
            if (nodeType == D8) {
                cmdlen = 4;
                cmdcode = 0x08;  // Set 8 outputs: bit on means output on
                int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
                tempsettings = (outputArray[7] ^ outputWired[7]) * 128 + (outputArray[6] ^ outputWired[6]) * 64 + (outputArray[5] ^ outputWired[5]) * 32 + (outputArray[4] ^ outputWired[4]) * 16 + tempsettings;
                settinglo = (byte) (tempsettings);
            }
            if ((nodeType == WM) || (nodeType == SY)) {
                //cmdlen = 3;
                cmdcode = 0x01;  //  This really is an error case since these
                //  nodes do not have outputs
            }
            if (nodeType == SC) {
                //cmdlen = 3;
                cmdcode = 0x01;  //  This really is an error case since these
                //  nodes do not have outputs
            }
            if ((nodeType == SM) || (nodeType == SW) || (nodeType == YM)) {
                cmdlen = 5;
                cmdcode = 0x09;  // Set 16 outputs: bit on means output on
                int tempsettings = (outputArray[3] ^ outputWired[3]) * 8 + (outputArray[2] ^ outputWired[2]) * 4 + (outputArray[1] ^ outputWired[1]) * 2 + (outputArray[0] ^ outputWired[0]) * 1;
                tempsettings = (outputArray[7] ^ outputWired[7]) * 128 + (outputArray[6] ^ outputWired[6]) * 64 + (outputArray[5] ^ outputWired[5]) * 32 + (outputArray[4] ^ outputWired[4]) * 16 + tempsettings;
                settinglo = (byte) (tempsettings);
                int tempsettings2 = (outputArray[11] ^ outputWired[11]) * 8 + (outputArray[10] ^ outputWired[10]) * 4 + (outputArray[9] ^ outputWired[9]) * 2 + (outputArray[8] ^ outputWired[8]) * 1;
                tempsettings2 = (outputArray[15] ^ outputWired[15]) * 128 + (outputArray[14] ^ outputWired[14]) * 64 + (outputArray[13] ^ outputWired[13]) * 32 + (outputArray[12] ^ outputWired[12]) * 16 + tempsettings2;
                settinghi = (byte) (tempsettings2);
            }
        } else {  // Not bulk message
            // For now, we will simply send the first output circuit that
            // we encounter.  In theory, this could starve a later output
            // circuit on this node.  If someone complains then we should
            // implement a more complicated algorithm.

            c = 0;
            boolean foundsomething = false;
            while ((c < outputbitsPerCard) && !foundsomething) {
                if (outputNeedToSend[c]) {
                    // Need to adjust addr to address the actual output
                    // circuit rather than the starting output address
                    // That it currently points to.
                    Integer tempaddr = c + addrlo;
                    addrlo = tempaddr.byteValue();

                    // Reset the needtosend flag for this output circuit
                    outputNeedToSend[c] = false;

                    // Reset the foundfirst flag
                    foundsomething = true;

                    // We are here because at least one output circuit on
                    // this node is not set to a type of ONOFF
                    //  -- but some of the output circuits may still be
                    // of type ONOFF.
                    if (outputSpecial[c] == 0) {
                        if (outputType[c] == AcelaNode.ONOFF) {
                            // outputArray[c] tells us to to turn the output on
                            // or off.
                            // outputWired[c] tells us whether the relay is 
                            // wired backwards.
                            // command 0x01 is activate
                            // command 0x02 is deactivate
                            int tempcommand = (outputArray[c] ^ outputWired[c]);
                            if (tempcommand == 0) {
                                tempcommand = 2;
                            }
                            cmdcode = (byte) (tempcommand);
                            cmdlen = 3;
                        }

                        if (outputType[c] == AcelaNode.BLINK) {
                            // outputArray[c] tells us to to turn the output on
                            // or off.
                            // outputWired[c] tells us whether the output
                            // should start on or start off.
                            // outputLength[c] tells us how long in tenths of
                            // a second to blink.
                            // output will continue to blink until turned off.
                            // command 0x02 is deactivate
                            // command 0x05 is blink
                            // command 0x06 is reverse blink
                            int tempcommand = outputArray[c];
                            if ((tempcommand == 1) && (outputWired[c] == 1)) {
                                tempcommand = 5;
                            }
                            if ((tempcommand == 1) && (outputWired[c] == 0)) {
                                tempcommand = 6;
                            }
                            if (tempcommand == 0) {
                                tempcommand = 2;
                            }
                            cmdcode = (byte) (tempcommand);
                            if (cmdcode == 0x02) {
                                cmdlen = 3;
                            } else {
                                cmdlen = 4;
                                settinglo = (byte) outputLength[c];
                            }
                        }

                        if (outputType[c] == AcelaNode.PULSE) {
                            // outputArray[c] tells us to to turn the output on
                            // or off.
                            // outputWired[c] tells us whether the output
                            // should start on or start off.
                            // outputLength[c] tells us how long in tenths of
                            // a second to pulse the output.
                            // output will actually return to off state after
                            // the pulse duratiom -- but we will never know.
                            // Program will need to fake this out.
                            // command 0x02 is deactivate
                            // command 0x03 is to pulse on
                            // command 0x04 is to pulse off
                            int tempcommand = outputArray[c];
                            if ((tempcommand == 1) && (outputWired[c] == 1)) {
                                tempcommand = 4;
                            }
                            if ((tempcommand == 1) && (outputWired[c] == 0)) {
                                tempcommand = 3;
                            }
                            if (tempcommand == 0) {
                                tempcommand = 2;
                            }
                            cmdcode = (byte) (tempcommand);
                            if (cmdcode == 0x02) {
                                cmdlen = 3;
                            } else {
                                cmdlen = 4;
                                settinglo = (byte) outputLength[c];
                            }
                        }
                    } else {
                        switch (outputSignalHeadType[c]) {
                            case DOUBLE: {
                                switch (outputSpecial[c]) {
                                    case 1:
                                        cmdcode = 0x0c;
                                        settinglo = 0x01;
                                        break; // Red
                                    case 2:
                                        cmdcode = 0x0c;
                                        settinglo = 0x02;
                                        break; // Flashing red
                                    case 3: // Yellow
                                    case 4: // Flashing Yellow
                                    case 6: // Flashing Green
                                        cmdcode = 0x0c;
                                        settinglo = 0x08;
                                        break;
                                    case 5:
                                        cmdcode = 0x0c;
                                        settinglo = 0x04;
                                        break; // Green
                                    case 7:
                                        cmdcode = 0x0c;
                                        settinglo = 0x00;
                                        break; // Dark
                                    default:
                                        cmdcode = 0x0c;
                                        settinglo = 0x03;
                                        break; // Flashing red
                                }
                                break;
                            }
                            case TRIPLE: {
                                switch (outputSpecial[c]) {
                                    case 1:
                                        cmdcode = 0x0d;
                                        settinglo = 0x01;
                                        break; // Red
                                    case 2:
                                        cmdcode = 0x0d;
                                        settinglo = 0x02;
                                        break; // Flashing red
                                    case 3:
                                        cmdcode = 0x0d;
                                        settinglo = 0x04;
                                        break; // Yellow
                                    case 4:
                                        cmdcode = 0x0d;
                                        settinglo = 0x08;
                                        break; // Flashing Yellow
                                    case 5:
                                        cmdcode = 0x0d;
                                        settinglo = 0x10;
                                        break; // Green
                                    case 6:
                                        cmdcode = 0x0d;
                                        settinglo = 0x20;
                                        break; // Flashing Green
                                    case 7:
                                        cmdcode = 0x0d;
                                        settinglo = 0x00;
                                        break; // Dark
                                    default:
                                        cmdcode = 0x0d;
                                        settinglo = 0x03;
                                        break; // Flashing red
                                }
                                break;
                            }
                            case BPOLAR: {
                                switch (outputSpecial[c]) {
                                    case 1:
                                        cmdcode = 0x0c;
                                        settinglo = 0x01;
                                        break; // Red
                                    case 2:
                                        cmdcode = 0x0c;
                                        settinglo = 0x02;
                                        break; // Flashing red
                                    case 3:
                                        cmdcode = 0x0c;
                                        settinglo = 0x10;
                                        break; // Yellow
                                    case 4:
                                        cmdcode = 0x0c;
                                        settinglo = 0x20;
                                        break; // Flashing Yellow
                                    case 5:
                                        cmdcode = 0x0c;
                                        settinglo = 0x04;
                                        break; // Green
                                    case 6:
                                        cmdcode = 0x0c;
                                        settinglo = 0x08;
                                        break; // Flashing Green
                                    case 7:
                                        cmdcode = 0x0c;
                                        settinglo = 0x00;
                                        break; // Dark
                                    default:
                                        cmdcode = 0x0c;
                                        settinglo = 0x03;
                                        break; // Flashing red
                                }
                                break;
                            }
                            case WIGWAG: {
                                switch (outputSpecial[c]) {
                                    case 1: // Red
                                    case 2: // Flashing red
                                    case 3:
                                    case 4: // Flashing Yellow
                                    case 5: // Green
                                    case 6: // Flashing Green
                                        cmdcode = 0x0c;
                                        settinglo = 0x0B;
                                        break;
                                    case 7: // Dark
                                        cmdcode = 0x0c;
                                        settinglo = 0x00;
                                        break;
                                    default: // Flashing red
                                        cmdcode = 0x0c;
                                        settinglo = 0x0F;
                                        break;
                                }
                                break;
                            }
                            default: {
                                switch (outputSpecial[c]) {
                                    case 1:
                                        cmdcode = 0x0d;
                                        settinglo = 0x01;
                                        break; // Red
                                    case 3:
                                        cmdcode = 0x0d;
                                        settinglo = 0x04;
                                        break; // Yellow
                                    case 4:
                                        cmdcode = 0x0d;
                                        settinglo = 0x08;
                                        break; // Flashing Yellow
                                    case 5:
                                        cmdcode = 0x0d;
                                        settinglo = 0x10;
                                        break; // Green
                                    case 6:
                                        cmdcode = 0x0d;
                                        settinglo = 0x30;
                                        break; // Flashing Green
                                    case 7:
                                        cmdcode = 0x0d;
                                        settinglo = 0x00;
                                        break; // Dark
                                    case 2: // Flashing red
                                    default:
                                        cmdcode = 0x0d;
                                        settinglo = 0x03;
                                        break;
                                }
                            }
                        }
                        cmdlen = 4;
                    }
                }
                c++;
            }
        }

        AcelaMessage m = new AcelaMessage(cmdlen);
        m.setElement(0, cmdcode);
        m.setElement(1, addrhi);
        m.setElement(2, addrlo);
        if (cmdlen > 3) {
            if (cmdlen > 4) {
                m.setElement(3, settinghi);
            } else {
                m.setElement(3, settinglo);
            }
        }
        if (cmdlen > 4) {
            m.setElement(4, settinglo);
        }
        m.setBinary(true);
        return m;
    }
    boolean warned = false;

    /**
     * Use the contents of the poll reply to mark changes.
     *
     * @param l Reply to a poll operation
     */
    public void markChanges(AcelaReply l) {

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
        //int numBytes = 1;   // For TB there are only 4 sensors so always 1 byte

        log.debug("Sensor Parsing for module type: {}", moduleNames[nodeType]);
        log.debug("Sensor Parsing has startingSensorAddress: {}", startingSensorAddress);
        log.debug("Sensor Parsing has firstByteNum: {}", firstByteNum);
        log.debug("Sensor Parsing has firstBitAt: {}", firstBitAt);

        //  Using rawvalue may be unnecessary, but trying to minimize reads to getElement 
        int rawvalue = l.getElement(firstByteNum);
        log.debug("Sensor Parsing has first rawvalue: {}", Integer.toHexString(rawvalue));

        int usingByteNum = 0;

        try {
            for (int i = 0; i < sensorbitsPerCard; i++) {
                if (sensorArray[i] == null) {
                    log.debug("Sensor Parsing for Sensor: {} + {} was skipped", startingSensorAddress, i);
                    continue;
                } // skip ones that don't exist

                log.debug("Sensor Parsing for Sensor: {} + {}", startingSensorAddress, i);

                int relvalue = rawvalue;

                //  Need a temporary counter within the byte so we can shift 
                int tempi = i;

                //  If necessary, shift by four before we start  
                if (usingByteNum == 0) {
                    if (firstBitAt == 4) {
                        for (int j = 0; j < firstBitAt; j++) {
                            relvalue = relvalue >> 1;
                        }
                        log.debug("Sensor Parsing for Sensor: {} + {} shifted by 4: {}", startingSensorAddress, i, Integer.toHexString(relvalue));
                    }
                }

                //  If necessary, get next byte  
                if (firstBitAt == 4) {
                    if (i >= 12) {  // Will only get here if there are 16 sensors
                        usingByteNum = 2;
                        //  Using rawvalue may be unnecessary, but trying to minimize reads to getElement 
                        rawvalue = l.getElement(usingByteNum + firstByteNum);
                        log.debug("Sensor Parsing (1stat4) has third rawvalue: {}", Integer.toHexString(rawvalue));
                        relvalue = rawvalue;
                        tempi = i - 12;  // tempi needs to shift down by 12
                    } else {
                        if (i >= 4) {
                            usingByteNum = 1;
                            //  Using rawvalue may be unnecessary, but trying to minimize reads to getElement 
                            rawvalue = l.getElement(usingByteNum + firstByteNum);
                            log.debug("Sensor Parsing (1stat4) has second rawvalue: {}", Integer.toHexString(rawvalue));
                            relvalue = rawvalue;
                            tempi = i - 4;  // tempi needs to shift down by 4
                        }
                    }
                } else {
                    if (i >= 8) {  // Will only get here if there are 16 sensors
                        usingByteNum = 1;
                        //  Using rawvalue may be unnecessary, but trying to minimize reads to getElement 
                        rawvalue = l.getElement(usingByteNum + firstByteNum);
                        log.debug("Sensor Parsing has second rawvalue: {}", Integer.toHexString(rawvalue));
                        relvalue = rawvalue;
                        tempi = i - 8;  // tempi needs to shift down by 8
                    }
                }

                log.debug("Sensor Parsing for Sensor: {} + {} has tempi: {}", startingSensorAddress, i, tempi);

                //  Finally we can isolate the bit from the poll result
                for (int j = 0; j < tempi; j++) {
                    relvalue = relvalue >> 1;
                }

                log.debug("Sensor Parsing for Sensor: {} + {} has relvalue: {}", startingSensorAddress, i, Integer.toHexString(relvalue));

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
                int newerstate = relvalue & 0x01;
                byte newstate = (byte) (newerstate);

                if ((nooldstate) || (oldstate != newstate)) {
                    if (nooldstate) {
                        log.debug("Sensor Parsing for Sensor: {} + {} had no old state.", startingSensorAddress, i);
                    }
                    if (newstate == 0x00) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(sensorLastSetting[i]);
                    } else {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(sensorLastSetting[i]);
                    }
                    log.debug("Sensor Parsing for Sensor: {} + {} changed state: {} rawvalue: {}", startingSensorAddress, i, sensorLastSetting[i], Integer.toHexString(rawvalue));
                } else {
                    log.debug("Sensor Parsing for Sensor: {} + {} did NOT change state: {} rawvalue: {}", startingSensorAddress, i, sensorLastSetting[i], Integer.toHexString(rawvalue));
                }
            }
        } catch (JmriException e) {
            log.error("exception in markChanges: " + e);
        }
    }

    /**
     * Register a sensor on an Acela node.
     * The numbers here are 0 to MAXSENSORBITS, not 1 to MAXSENSORBITS.
     *
     * @param s       Sensor object
     * @param rawaddr index number of sensor's input bit on this
     *                node, valid range from 0 to MAXSENSORBITS
     */
    public void registerSensor(Sensor s, int rawaddr) {
        // validate the sensor ordinal
        if ((rawaddr < 0) || (rawaddr >= MAXNODE)) {
            log.error("Unexpected sensor ordinal in registerSensor: {}", Integer.toString(rawaddr));
            return;
        }

        int addr = -1;
        addr = rawaddr - startingSensorAddress;

        hasActiveSensors = true;
        InstanceManager.getDefault(AcelaSystemConnectionMemo.class).getTrafficController().setAcelaSensorsState(true);
        if (startingSensorAddress < 0) {
            log.info("Trying to register sensor too early: {}S{}",
                    InstanceManager.getDefault(AcelaSystemConnectionMemo.class).getSystemPrefix(), // multichar prefix
                    rawaddr);
        } else {

            if (sensorArray[addr] == null) {
                sensorArray[addr] = s;
                if (!sensorHasBeenInit[addr]) {
                    sensorNeedInit[addr] = true;
                }
            } else {
                // multiple registration of the same sensor
                log.warn("Multiple registration of same sensor: {}S{}",
                        InstanceManager.getDefault(AcelaSystemConnectionMemo.class).getSystemPrefix(), // multichar prefix
                        rawaddr);
            }
        }
    }
    int timeout = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        timeout++;
        if (log.isDebugEnabled()) {
            log.warn("Timeout to poll for UA={}: consecutive timeouts: {}", nodeAddress, timeout);
        }
        return false;   // tells caller to NOT force init
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

    private final static Logger log = LoggerFactory.getLogger(AcelaNode.class);

}
