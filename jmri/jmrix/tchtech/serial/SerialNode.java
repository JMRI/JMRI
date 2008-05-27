// SerialNode.java

package jmri.jmrix.tchtech.serial;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;

/**
 * Models a serial TCH Technology Node, consisting of NIC and attached cards.
 * <P>
 * Nodes are numbered ala the NA number, from 1 to 63.
 * Node number 1 carries sensors 1 to 999, node 2 1001 to 1999 etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Tim Hatch for TCH Technology nodes
 * @version	$Revision: 1.3 $
 */
public class SerialNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     
     * <P>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERNA}
     */
    static final int MAXSENSORS = 999;
    
    static public final int MAXSEARCHLIGHTBYTES = 48;
    static public final int MAXCARDLOCATIONBYTES = 64;
    
   // class constants
    public static final int MICRO = 1;          // MICRO node type
    public static final int SNIC = 2;           // NICS node type
    public static final byte INPUT_CARD = 1;    // NICS input card type for specifying location
    public static final byte OUTPUT_CARD = 2;   // NICS output card type for specifying location
    public static final byte NO_CARD = 0;       // NICS unused location
    // node definition instance variables (must persist between runs)
    public int nodeAddress = 0;                 // NA, Node address, 0-255 allowed
    protected int nodeType = MICRO;             // See above
    protected int bitsPerCard = 16;             // 24 for MICRO and 32 for NICS
   // protected int transmissionDelay = 0;        // DL, delay between bytes on Receive (units of 10 microsec.)
         protected int pulseWidth = 500;	// Pulse width for pulsed turnout control (milliseconds)
    protected int num2LSearchLights = 0;        // MICRO Node only, 'NS' number of two lead bicolor signals
    protected byte[] locSearchLightBits = new byte[MAXSEARCHLIGHTBYTES]; // Micro Node only, 0 = not targetlight LED,
                                                //   1 = searchlight LED, 2*NS bits must be set to 1
    protected byte[] cardTypeLocation = new byte[MAXCARDLOCATIONBYTES]; // Varys on NICS. There must numInputCards bytes set to
    						//   INPUT_CARD, and numOutputCards set to OUTPUT_CARD, with
                                                //   the remaining locations set to NO_CARD.  All
                                                //   NO_CARD locations must be at the end of the array.  The
                                                //   array is indexed by card address.
    // operational instance variables  (should not be preserved between runs)
    protected boolean needSend = true;          // 'true' if something has changed in the outputByte array since
                                                //    the last send to the hardware node
    protected byte[] outputArray = new byte[256]; // current values of the output bits for this node
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS+1];
    protected int[] sensorLastSetting = new int[MAXSENSORS+1];
    protected int[] sensorTempSetting = new int[MAXSENSORS+1];
   /**
     * Assumes a node address of 0, and a node type of MICRO
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode() {
        this (0,MICRO);
    }

    /**
     * Creates a new SerialNode and initialize default instance variables
     *   address - Address of node on TCH Technology serial bus (0-255)
     *   type - MICRO Node, NICS,
     */
    public SerialNode(int address, int type) {
        // set address and type and check validity
        setNodeAddress (address);
        setNodeType (type);
        // set default values for other instance variables
        bitsPerCard = 16;
        //transmissionDelay = 0;
        num2LSearchLights = 0;
        for (int i = 0; i<MAXSEARCHLIGHTBYTES; i++) {
            locSearchLightBits[i] = 0;
        }
        // note: setNodeType initialized cardTypeLocation[];
        // clear the Sensor arrays
        for (int i = 0; i<MAXSENSORS+1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
            sensorTempSetting[i] = Sensor.UNKNOWN;
        }
        // clear all output bits
        for (int i = 0; i<256; i++) {
            outputArray[i] = 0;
        }
        // initialize other operational instance variables
        needSend = true;
        hasActiveSensors = false;
        // register this node
        SerialTrafficController.instance().registerSerialNode(this);
    }

    public int getNum2LSearchLights() {
    	return num2LSearchLights;
    }
    	
    public void setNum2LSearchLights(int n) {
    	num2LSearchLights = n;
    }
    	
    public byte[] getLocSearchLightBits() {
    	return locSearchLightBits;
    }
    	
    public void setLocSearchLightBits(int num, int value) {
    	locSearchLightBits[num] = (byte)(value&0xFF);
    }
    	
    public byte[] getCardTypeLocation() {
    	return cardTypeLocation;
    }

    public void setCardTypeLocation(int num, int value) {
        // Validate the input
        if ( (num < 0) || (num >= MAXCARDLOCATIONBYTES) ) {
            log.error("setCardTypeLocation - invalid num (index) - "+num);
            return;
        }
        int val = value & 0xFF;
        if ( (val!=NO_CARD) && (val!=INPUT_CARD) && (val!=OUTPUT_CARD) ) {
            log.error("setCardTypeLocation - invalid value - "+val);
            return;
        }
        // Set the card type
    	cardTypeLocation[num] = (byte)(val);
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
        if (byteNumber > (numOutputCards()*(bitsPerCard/8)) ) {
            warn("TCH Tech NIC - Output bit out-of-range for defined node");
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
        }
    }
    	
    /**
     * Public method get the current state of an output bit.
     *    Note:  returns 'true' for 0, 'false' for 1
     *           bits are numbered from 1 (not 0)
     */
    public boolean getOutputBit(int bitNumber) {
        // locate in the outputArray
        int byteNumber = (bitNumber-1)/8;
        // validate that this byte number is defined
        if (byteNumber > (numOutputCards()*(bitsPerCard/8)) ) {
            warn("TCH Tech NIC - Output bit out-of-range for defined node");
        }
        if (byteNumber >= 256) byteNumber = 255;
        // update the byte
        byte bit = (byte) (1<<((bitNumber-1) % 8));
		byte testByte = outputArray[byteNumber];
		testByte &= bit;
		if (testByte == 0) {
			return (true);
		}
		else {
			return (false);
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
     */
    public void resetMustSend() { needSend = false; }
    /**
     * Public to set state of needSend flag.
     */
    public void setMustSend() { needSend = true; }

    /**
     * Public method to return number of input cards.
     */
    public int numInputCards() {
    	int result = 0;
    	for (int i=0; i<cardTypeLocation.length; i++)
    		if (cardTypeLocation[i]==INPUT_CARD) result++;

    	// check consistency
    	if (nodeType==MICRO && result!=1)
    		warn("NIC node with "+result+" input cards");
    	if (nodeType==SNIC && result>=MAXCARDLOCATIONBYTES)
    		warn("NIC node with "+result+" input cards");

    	return result;
    }

    /**
     * Public method to return number of output cards.
     */
    public int numOutputCards() {
    	int result = 0;
    	for (int i=0; i<cardTypeLocation.length; i++)
    		if (cardTypeLocation[i]==OUTPUT_CARD) result++;

    	// check consistency
    	if (nodeType==MICRO && result!=2)
    		warn("MICRO node with "+result+" output cards");
    	if (nodeType==SNIC && result>=MAXCARDLOCATIONBYTES)
    		warn("NIC node with "+result+" output cards");

    	return result;
    }

    /**
     * Public method to return node type
     *   Current types are:
     *      MICRO Node, NICS,
     */
    public int getNodeType() {
        return (nodeType);
    }

   /**
     * Public method to set node type
     *   Current types are:
     *      MICRO, NICS,
     *   For MICRO Node, also sets cardTypeLocation[] and bitsPerCard
     *   For NICS, also clears cardTypeLocation
     */
    public void setNodeType(int type) {
        if (type == MICRO) {
            nodeType = type;
            bitsPerCard = 16;
            // set cardTypeLocation for SMINI
            cardTypeLocation[0] = OUTPUT_CARD;
            cardTypeLocation[1] = OUTPUT_CARD;
            cardTypeLocation[2] = INPUT_CARD;
            for (int i=3;i<MAXCARDLOCATIONBYTES;i++) {
                cardTypeLocation[i] = NO_CARD;
            }
        }
        else if (type ==SNIC) {
            nodeType = type;
            // clear cardTypeLocations
            for (int i=0;i<MAXCARDLOCATIONBYTES;i++) {
                cardTypeLocation[i] = NO_CARD;
            }
        }
// here recognize other node types
        else {
            log.error("Bad node type - "+Integer.toString(type) );
        }
    }

    /**
     * Public method to return number of bits per card.
     */
    public int getNumBitsPerCard() {
        return (bitsPerCard);
    }

    /**
     * Public method to set number of bits per card.
     */
    public void setNumBitsPerCard(int bits) {
        if ( (bits==24) || (bits==32) || (bits==16) ) {
            bitsPerCard = bits;
        }
        else {
            log.warn("unexpected number of bits per Tab: "+Integer.toString(bits));
            bitsPerCard = bits;
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
     *   address - node address set in dip switches (0 - 255)
     */
    public void setNodeAddress(int address) {
        if ( (address >= 0) && (address < 256) ) {
            nodeAddress = address;
        }
        else {
            log.error("illegal node address: "+Integer.toString(address));
            nodeAddress = 0;
        }
    }

    /**
     * Public method to return transmission delay.
     */
   // public int getTransmissionDelay() {
       // return (transmissionDelay);
    //}

    /**
     * Public method to set transmission delay.
     *   delay - delay between bytes on receive (units of 10 microsec.)
     *   Note: two bytes are used, so range is 0-65,535.  If delay
     *          is out of range, it is restricted to the allowable range
     */
   // public void setTransmissionDelay(int delay) {
       // if ( (delay < 0) || (delay > 65535) ) {
           // log.warn("transmission delay out of 0-65535 range: "+
                                           // Integer.toString(delay));
            //if (delay < 0) delay = 0;
            //if (delay > 65535) delay = 65535;
        //}
       // transmissionDelay = delay;
    //}

    /**
     * Public method to return pulse width.
	 *    Used with pulsed turnout control.
     */
    public int getPulseWidth() {
        return (pulseWidth);
    }

    /**
     * Public method to set pulse width.
     *   width - width of pulse used for pulse controlled turnout control (millisec.)
     *   Note: Pulse width must be between 100 and 10000 milliseconds.  If width
     *          is out of range, it is restricted to the allowable range
     */
    public void setPulseWidth(int width) {
        if ( (width < 100) || (width > 10000) ) {
            log.warn("pulse width out of 100 - 10000 range: "+
                                            Integer.toString(width));
            if (width < 100) width = 100;
            if (width > 10000) width = 10000;
        }
        pulseWidth = width;
    }

    /**
     * Public method to set the type of one card.
     *   address - address recognized for this card by the node hardware.
     *               for NICS address set in card's dip switches (0 - 63)
     *   type - INPUT_CARD, OUTPUT_CARD, or NO_CARD
     */
     public void setCardTypeByAddress (int address, int type) {
        // validate address
        if ( (address < 0) || (address > 63) ) {
            log.error("illegal card address: "+Integer.toString(address));
            return;
        }
        // validate type
        if ( (type != OUTPUT_CARD) && (type != INPUT_CARD) && (type != NO_CARD) ) {
            log.error("illegal card type: "+Integer.toString(type));
            cardTypeLocation[address] = NO_CARD;
            return;
        }
        // check node type/location restrictions
        if ( (nodeType==MICRO) && ( ( (address>2) && (type!=NO_CARD) ) ||
                                ( (address==2) && (type!=INPUT_CARD) ) ||
                                ( (address<2) && (type!=OUTPUT_CARD) ) ) ) {
            log.error("illegal card type/address specification for MICRO");
            return;
        }
// here add type/location restrictions for other types of card
        cardTypeLocation[address] = (byte) type;
    }

    /** Public method to test for OUTPUT_CARD type.
     *   Returns true if card with 'cardNum' is an output card.
     *   Returns false if card is not an output card, or if
     *       'cardNum' is out of range.
     */
    public boolean isOutputCard(int cardNum) {
        if (cardNum>63) {
            warn("TCH Tech NIC - isOutputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==MICRO) {
            if ( (cardNum==0) || (cardNum==1) ) return(true);
            else return (false);
        }
        return (cardTypeLocation[cardNum]==OUTPUT_CARD);
    }

    /** Public method to test for INPUT_CARD type.
     *   Returns true if card with 'cardNum' is an input card.
     *   Returns false if card is not an input card, or if
     *       'cardNum' is out of range.
     */
    public boolean isInputCard(int cardNum) {
        if (cardNum>63) {
            warn("TCH Tech NIC - isInputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==MICRO) {
            if (cardNum==2) return(true);
            else return (false);
        }
        return (cardTypeLocation[cardNum]==INPUT_CARD);
    }

    /** Public method to return 'Output Card Index'
     *   Returns the index this output card would have in an
     *     array of output cards for this node.  Can be used
     *     to locate this card's bytes in an output message.
     *     Array is ordered by increasing node address.
     */
    public int getOutputCardIndex(int cardNum) {
        if (nodeType==MICRO) {
            if ( (cardNum==0) || (cardNum==1) ) return(cardNum);
        }
        else {
            int index = 0;
            for (int i=0; i<cardTypeLocation.length; i++) {
                if (cardTypeLocation[i]==OUTPUT_CARD) {
                    if (i==cardNum)
                        return(index);
                    else
                        index ++;
                }
            }
        }
        // Here if error - cardNum is not an
        warn("NIC - input card to getOutputCardIndex is not an Output Card");
        return (0);
    }

    /** Public method to return 'Input Card Index'
     *   Returns the index this input card would have in an
     *     array of input cards for this node.  Can be used
     *     to locate this card's bytes in an receive message.
     *     Array is ordered by increasing node address.
     */
    public int getInputCardIndex(int cardNum) {
        if (nodeType==MICRO) {
            if (cardNum==2) return(0);
        }
        else {
            int index = 0;
            for (int i=0; i<cardTypeLocation.length; i++) {
                if (cardTypeLocation[i]==INPUT_CARD) {
                    if (i==cardNum)
                        return(index);
                    else
                        index ++;
                }
            }
        }
        // Here if error - cardNum is not an
        warn("NIC - input card to getOutputCardIndex is not an Output Card");
        return (0);
    }

    /**
     * Public Method to set location of TargetLightBits (MICRO only)
     *   bit - bitNumber of the low bit of an oscillating search light bit pair
     *   Notes:  Bits are numbered from 0
     *           Two bits are set by each call - bit and bit + 1.
     *           If either bit is already set, an error is logged and no
     *               bits are set.
     */
    public void set2LeadSearchLight(int bit) {
        // check for MICRO
// if other types of TCH Technology nodes allow oscillating search lights, modify this method
        if (nodeType!=MICRO) {
            log.error("Invalid setting of Targetlights bits - not NICS node");
            return;
        }
        // validate bit number range
        if ( (bit<0) || (bit>46) ) {
            log.error("Invalid bit number when setting NIC Targetlights bits: "+
                                            Integer.toString(bit));
            return;
        }
        // validate that bits are not already set
        if ( (locSearchLightBits[bit] != 0) || (locSearchLightBits[bit+1] != 0) ) {
            log.error("bit number for NIC Targetlights bits already set: "+
                                            Integer.toString(bit));
            return;
        }
        // set the bits
        locSearchLightBits[bit] = 1;
        locSearchLightBits[bit+1] = 1;
        num2LSearchLights ++;
    }

     /**
     * Public Method to clear location of TargetLightBits (MICRO only)
     *   bit - bitNumber of the low bit of an oscillating search light bit pair
     *   Notes:  Bits are numbered from 0
     *           Two bits are cleared by each call - bit and bit + 1.
     *           If either bit is already clear, an error is logged and no
     *               bits are set.
     */
    public void clear2LeadSearchLight(int bit) {
        // check for MICRO
// if other types of TCH Technology nodes allow oscillating search lights, modify this method
        if (nodeType!=MICRO) {
            log.error("Invalid setting of Targetlights bits - not NICS node");
            return;
        }
        // validate bit number range
        if ( (bit<0) || (bit>46) ) {
            log.error("Invalid bit number when setting NICS Targetlights bits: "+
                                            Integer.toString(bit));
            return;
        }
        // validate that bits are not already clear
        if ( (locSearchLightBits[bit] != 1) || (locSearchLightBits[bit+1] != 1) ) {
            log.error("bit number for NICS Targetlights bits already clear: "+
                                            Integer.toString(bit));
            return;
        }
        // set the bits
        locSearchLightBits[bit] = 0;
        locSearchLightBits[bit+1] = 0;
        num2LSearchLights --;
    }

   /**
     * Public Method to query TargetLightBits by bit number (MICRO Node only)
     *   bit - bitNumber of the either bit of an oscillating search light bit pair
     *   Note: returns 'true' if bit is an oscillating TargetLightBit, otherwise
     *          'false' is returned
     */
     public boolean isSearchLightBit (int bit) {
        // check for MIRCO
// if other types of TCH Technology nodes allow oscillating Target lights, modify this method
        if (nodeType!=MICRO) {
            log.error("Invalid query of Targetlights bits - not NIC node");
            return (false);
        }
        // validate bit number range
        if ( (bit<0) || (bit>47) ) {
            log.error("Invalid bit number in query of NIC Targetlights bits: "+
                                            Integer.toString(bit));
            return (false);
        }
        if (locSearchLightBits[bit] == 1) {
            return (true);
        }
        return (false);
    }

    /**
     * Public Method to create an Initialization packet (SerialMessage) for this node
     */
    public SerialMessage createInitPacket() {
        // Assemble initialization byte array from node information
        int nInitBytes = 4;
        byte[] initBytes = new byte [20];
        int code = 0;
        // set node definition parameter
        if (nodeType==MICRO) initBytes[0] = 77;  // 77 = 'M'  109 = "m"
        else if (nodeType==SNIC) {
           // if (bitsPerCard==24) initBytes[0] = 78;  // 78 = 'N'  110 = "n"
            //else
            if (bitsPerCard==32) initBytes[0] = 0X20;  // 0x20 for 32 bit board 
        }
// Here add code for other type of card
        // add Transmission Delay bytes (same for MIRCO and NICS)
        //int firstByte = transmissionDelay / 256;
        //int secondByte = transmissionDelay - ( firstByte*256 );
        //if (firstByte>255) firstByte = 255;
        //initBytes[1] = (byte)firstByte;
        //initBytes[2] = (byte)secondByte;

        // MICRO specific part of initialization byte array
        if (nodeType==MICRO) {
            initBytes[3] = (byte)num2LSearchLights;
            if (num2LSearchLights>0) {
                // Set up searchlight LED bit codes
                for (int i=0,j=0;i<4;i++,j+=8) {
                    code = locSearchLightBits[j];
                    code = code + (locSearchLightBits[j+1]*2);
                    code = code + (locSearchLightBits[j+2]*4);
                    code = code + (locSearchLightBits[j+3]*8);
                    code = code + (locSearchLightBits[j+4]*16);
                    code = code + (locSearchLightBits[j+5]*32);
                    code = code + (locSearchLightBits[j+6]*64);
                    code = code + (locSearchLightBits[j+7]*128);
                    initBytes[nInitBytes] = (byte)code;
                    nInitBytes ++;
                }
            }
        }
        // SNIC specific part of initialization byte array
        else if (nodeType==SNIC) {
            int numCards = numInputCards() + numOutputCards();
            int numFours = numCards/4;
            if ( (numCards-(numFours*4)) > 0) numFours ++;  // Round up if not even multiple
            initBytes[3] = (byte)numFours;
            for (int i=0,j=0;i<numFours;i++,j+=4) {
                code = cardTypeLocation[j];
                code = code + (cardTypeLocation[j+1] * 4);
                code = code + (cardTypeLocation[j+2] * 16);
                code = code + (cardTypeLocation[j+3] * 64);
                initBytes[nInitBytes] = (byte)code;
                nInitBytes ++;
            }
        }
// here add specific Attribute for other type of card

       // create a Serial message and add Attribute bytes
       SerialMessage m = new SerialMessage(nInitBytes + 2);
       // add Attribute bytes
       m.setElement(0,nodeAddress);  // node address
       m.setElement(1,201);     // 'I'
       // copy the data bytes into the buffer
       int k = 2;
       for (int i=0; i<nInitBytes; i++) {
            m.setElement(k, initBytes[i]);
            k ++;
       }
       return m;  
    } 
     /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public SerialMessage createOutPacket() {
        
        int nOutBytes = numOutputCards() * (bitsPerCard/8);
        // Create a Serial message and add Attribute bytes
        SerialMessage m = new SerialMessage(nOutBytes + 2);
        m.setElement(0,nodeAddress); // node address
        m.setElement(1,0x53);         //Send
        // Add output bytes to buffer
        int k = 2;
        for (int i=0; i<nOutBytes; i++) {
            m.setElement(k,outputArray[i]);  // DLE
            k ++;
        }
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
    public void markChanges(SerialReply l) {
        try {
            for (int i=0; i<=lastUsedSensor; i++) {
                if (sensorArray[i] == null) continue; // skip ones that don't exist
                int loc = i/8;
                int bit = i%8;
                boolean value = (((l.getElement(loc+2)>>bit)&0x01) == 1) ^ sensorArray[i].getInverted();  // byte 2 is first of data

                //// if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value+
                ////                    " tempSetting is "+((sensorTempSetting[i] == Sensor.ACTIVE)?"active ":"inactive ")+
                ////                    "lastSetting is "+((sensorLastSetting[i] == Sensor.ACTIVE)?"active ":"inactive ")
                ////                    );
                
                if ( value ) {
                    //// considered ACTIVE
                    if (   ( (sensorTempSetting[i] == Sensor.ACTIVE) || 
                               (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                           ( sensorLastSetting[i] != Sensor.ACTIVE) ) { // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                        //// log.debug("set active");
                    }
                    // save for next time
                    //sensorTempSetting[i] = Sensor.ACTIVE;
                } else {
                    //// considered INACTIVE
                    if (  ( (sensorTempSetting[i] == Sensor.INACTIVE)  || 
                                (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                          ( sensorLastSetting[i] != Sensor.INACTIVE) ) {  // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                        //// log.debug("set inactive");
                    }
                    //// save for next time
                    //sensorTempSetting[i] = Sensor.INACTIVE;
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
        //// validate the sensor ordinal
        if ( (i<0) || (i> ((numInputCards()*bitsPerCard) - 1)) || (i>MAXSENSORS) ) {
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
            //// multiple registration of the same sensor
            log.warn("multiple registration of same sensor: HS"+
                    Integer.toString((nodeAddress*SerialSensorManager.SENSORSPERNA) + i + 1) );
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
        if (log.isDebugEnabled()) log.warn("Timeout to poll for NA="+nodeAddress+": consecutive timeouts: "+timeout);
        
        if (timeout>5) { // enough, reinit
            // reset timeout count to zero to give polls another try
            timeout = 0;
            // reset poll and send control so will retry initialization
            setMustSend();
            
            // force sensors to UNKNOWN, including callbacks; might take some time
            for (int i=0; i<=lastUsedSensor; i++) {
                if (sensorArray[i] != null) {
                    sensorLastSetting[i] = Sensor.UNKNOWN;
                    sensorTempSetting[i] = Sensor.UNKNOWN;
                    try {
                        sensorArray[i].setKnownState(Sensor.UNKNOWN);
                    } catch (jmri.JmriException e) {
                        log.error("unexpected exception setting sensor i="+i+" on node "+nodeAddress+"e: "+e);
                    }
                }
            }
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
