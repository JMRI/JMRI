// SerialNode.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Models a serial C/MRI node, consisting of a (S)USIC and attached cards.
 * <P>
 * Nodes are numbered ala the UA number, from 1 to 63.
 * Node number 1 carries sensors 1 to 999, node 2 1001 to 1999 etc.
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 * <P>
 * The SMINI is defined as having 1 input and 2 outputs cards.<br>
 * USIC/SUSIC nodes can have 0-63 inputs and 0-63 output cards, but no
 * more than 64 total cards.   
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.11 $
 */
public class SerialNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     * Note this is more than a traditional motherboard,
     * and should perhaps be smaller.  But it only sizes
     * int arrays, and doesn't effect runtime, so we're
     * leaving it for now.
     * <P>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERUA}
     */
         
    static final int MAXSENSORS = 999;

    // class constants 
    public static final int SMINI = 1;          // SMINI node type
    public static final int USIC_SUSIC = 2;     // USIC/SUSIC node type 
    public static final byte INPUT_CARD = 1;    // USIC/SUSIC input card type for specifying location
    public static final byte OUTPUT_CARD = 2;   // USIC/SUSIC output card type for specifying location
    public static final byte NO_CARD = 0;       // USIC/SUSIC unused location
    // node definition instance variables
    public int nodeAddress = 0;                 // UA, Node address, 0-127 allowed
    protected int nodeType = SMINI;             // See above
    protected int bitsPerCard = 24;             // 24 for SMINI and USIC, 24 or 32 for SUSIC
    protected int transmissionDelay = 0;        // DL, delay between bytes on Receive (units of 10 microsec.)
    protected int num2LSearchLights = 0;        // SMINI only, 'NS' number of two lead bicolor signals
    protected byte[] locSearchLightBits = new byte[48]; // SMINI only, 0 = not searchlight LED, 
                                                //   1 = searchlight LED, 2*NS bits must be set to 1
    protected byte[] cardTypeLocation = new byte[64]; // USIC/SUSIC only, there must numInputCards bytes set to
    						//   INPUT_CARD, and numOutputCards set to OUTPUT_CARD, with 
                                                //   the remaining locations set to NO_CARD.  All 
                                                //   NO_CARD locations must be at the end of the array.  The
                                                //   array is indexed by card address.
         
    protected int LASTUSEDSENSOR = 1;  // grows as sensors defined

    public SerialNode() {
        sensorArray = new Sensor[MAXSENSORS+1];
        sensorLastSetting = new int[MAXSENSORS+1];

        for (int i = 0; i<MAXSENSORS+1; i++) {
            sensorArray[i] = null;
            sensorLastSetting[i] = Sensor.UNKNOWN;
        }
// temporary for debugging - set up SMINI node cards
        cardTypeLocation[0] = OUTPUT_CARD;
        cardTypeLocation[1] = OUTPUT_CARD;
        cardTypeLocation[2] = INPUT_CARD;
        for (int i=3;i<64;i++) {
            cardTypeLocation[i] = NO_CARD;
        }
// end temporary code
    }

    public int numInputCards() {
    	int result = 0;
    	for (int i=0; i<cardTypeLocation.length; i++) 
    		if (cardTypeLocation[i]==INPUT_CARD) result++;
    		
    	// check consistency
    	if (nodeType==SMINI && result!=1) 
    		warn("C/MRI SMINI node with "+result+" input cards");
    	if (nodeType==USIC_SUSIC && result>=64) 
    		warn("C/MRI USIC/SUSIC node with "+result+" input cards");
    		
    	return result;
    }

    public int numOutputCards() {
    	int result = 0;
    	for (int i=0; i<cardTypeLocation.length; i++) 
    		if (cardTypeLocation[i]==OUTPUT_CARD) result++;
    		
    	// check consistency
    	if (nodeType==SMINI && result!=1) 
    		warn("C/MRI SMINI node with "+result+" output cards");
    	if (nodeType==USIC_SUSIC && result>=64) 
    		warn("C/MRI USIC/SUSIC node with "+result+" output cards");
    		
    	return result;
    }
    
    public SerialNode getNodeFromAddress(int ua) {
        // What this routine should do is scan through defined serial nodes
        //   returning the one with the node address supplied
// Bob, should this routine be elsewhere?  In a 'manager' maybe?
        // For now, simply return any node  -- Note this didn't work--
        jmri.jmrix.cmri.serial.SerialNode n = new SerialNode();
    	return (n);
    }
    
    public int getNodeType() {
        return (nodeType);
    }
    
    public int getNumBitsPerCard() {
        return (bitsPerCard);
    }
    
    /** Local routine to test for OUTPUT_CARD type.
     *   Returns true if card with 'cardNum' is an output card.
     *   Returns false if card is not an output card, or if
     *       'cardNum' is out of range.
     */
    public boolean isOutputCard(int cardNum) {
        if (cardNum>63) {
            warn("C/MRI - isOutputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==SMINI) {
            if ( (cardNum==0) || (cardNum==1) ) return(true);
            else return (false);
        }
        return (cardTypeLocation[cardNum]==OUTPUT_CARD);
    }
        
    /** Local routine to test for INPUT_CARD type.
     *   Returns true if card with 'cardNum' is an input card.
     *   Returns false if card is not an input card, or if
     *       'cardNum' is out of range.
     */
    public boolean isInputCard(int cardNum) {
        if (cardNum>63) {
            warn("C/MRI - isInputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==SMINI) {
            if (cardNum==2) return(true);
            else return (false);
        }
        return (cardTypeLocation[cardNum]==INPUT_CARD);
    }
        
    /** Local routine to return 'Output Card Index'
     *   Returns the index this output card would have in an 
     *     array of output cards for this node.  Can be used 
     *     to locate this card's bytes in an output message.
     *     Array is ordered by increasing node address.
     */
    public int getOutputCardIndex(int cardNum) {
        if (nodeType==SMINI) {
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
        warn("C/MRI - input card to getOutputCardIndex is not an Output Card");
        return (0);
    }
        
    /** Local routine to return 'Input Card Index'
     *   Returns the index this input card would have in an 
     *     array of input cards for this node.  Can be used 
     *     to locate this card's bytes in an receive message.
     *     Array is ordered by increasing node address.
     */
    public int getInputCardIndex(int cardNum) {
        if (nodeType==SMINI) {
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
        warn("C/MRI - input card to getOutputCardIndex is not an Output Card");
        return (0);
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
        if (nodeType==SMINI) initBytes[0] = 77;  // 'M'
        else if (nodeType==USIC_SUSIC) {
            if (bitsPerCard==24) initBytes[0] = 78;  // 'N'
            else if (bitsPerCard==32) initBytes[0] = 88;  // 'X'
        }
// Here add code for other type of card
        // add Transmission Delay bytes (same for SMINI and USIC/SUSIC)
        int firstByte = transmissionDelay / 256;
        int secondByte = transmissionDelay - ( firstByte*256 );
        if (firstByte>255) firstByte = 255;
        initBytes[1] = (byte)firstByte;
        initBytes[2] = (byte)secondByte;

        // SMINI specific part of initialization byte array
        if (nodeType==SMINI) { 
            initBytes[3] = (byte)num2LSearchLights;
            if (num2LSearchLights>0) {
                // Set up searchlight LED bit codes
                for (int i=0,j=0;i<6;i++,j+=8) {
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
        // USIC/SUSIC specific part of initialization byte array
        else if (nodeType==USIC_SUSIC) {
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
// here add specific initialization for other type of card
        
        // count the number of DLE's to be inserted
        int nDLE = 0;
        for (int i=0; i<nInitBytes; i++) {
            if ( (initBytes[i]==2) || (initBytes[i]==3) || (initBytes[i]==16) ) 
                nDLE ++;
        }
        
        // create a Serial message and add initialization bytes
        SerialMessage m = new SerialMessage(nInitBytes + nDLE + 2);
        m.setElement(0,nodeAddress+65);  // node address
        m.setElement(1,73);     // 'I'
        // add initialization bytes
        int k = 2;
        for (int i=0; i<nInitBytes; i++) {
            // perform C/MRI required DLE processing
            if ( (initBytes[i]==2) || (initBytes[i]==3) || (initBytes[i]==16) ) {
                m.setElement(k,16);  // DLE
                k ++;
            }
            // add initialization byte
            m.setElement(k, initBytes[i]);
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
    
    Sensor[] sensorArray;
    int[] sensorLastSetting;

    /**
     * Use the contents of the poll reply to mark changes
     * @param l Reply to a poll operation
     */
    public void markChanges(SerialReply l) {
        try {
            for (int i=0; i<=LASTUSEDSENSOR; i++) {
                int loc = i/8;
                int bit = i%8;
                int value = (l.getElement(loc+2)>>bit)&0x01;  // byte 2 is first of data
                // if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value);
                if ( value == 1) {
                    // bit set, considered ACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.ACTIVE) ) {
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                    }
                } else {
                    // bit reset, considered INACTIVE
                    if ( sensorArray[i]!=null &&
                            ( sensorLastSetting[i] != Sensor.INACTIVE) ) {
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                    }
                }
            }
        } catch (JmriException e) { log.error("exception in markChanges: "+e); }
    }

    /**
     * The numbers here are 0 to MAXSENSORS-1, not 1 to MAXSENSORS.
     * @param s
     * @param i 0 to MAXSENSORS-1 number of sensor on unit
     */
    public void registerSensor(Sensor s, int i) {
        if (i<0 || i> (MAXSENSORS-1)) log.warn("Unexpected sensor ordinal: "+i);
        log.debug("registerSensor "+i);
        sensorArray[i] = s;
        if (LASTUSEDSENSOR<i) LASTUSEDSENSOR=i;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
