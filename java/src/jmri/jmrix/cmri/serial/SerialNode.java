// SerialNode.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.cmri.serial.serialmon.SerialFilterFrame;

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
 * <P>
 * A CPNODE (Control Point Node) is defined as having 2 inputs and 2 outputs //c2
 * on the node board and 0-128 bits of input or output (in 8 bit increments)
 * for added I/O extender cards IOX16,IOX32.  
 *
 * A PINODE (Raspberry Pi Node) is defined as having 3 inputs and 3 outputs //c2
 *on the node board and 0-128 bits of input or output (in 8 bit increments)
 * for added I/O extender cards IOX16,IOX32.  
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author      Chuck Catania, cpNode Extensions 2013, 2014, 2015, 2016
 * @version	$Revision: 17977 $
 */
public class SerialNode extends AbstractNode {

    /**
     * Maximum number of sensors a node can carry.
     * <P>
     * Note this is less than a current SUSIC motherboard can have,
     * but should be sufficient for all reasonable layouts.
     * <P>
     * Must be less than, and is general one less than,
     * {@link SerialSensorManager#SENSORSPERUA}
     */
    static final int MAXSENSORS = 999;
    
    static public final int MAXSEARCHLIGHTBYTES = 48;
    static public final int MAXCARDLOCATIONBYTES = 64;
    
    // class constants
    public static final int SMINI = 1;          // SMINI node type
    public static final int USIC_SUSIC = 2;     // USIC/SUSIC node type
    public static final int CPNODE = 3;         // cpNode Control Point (Arduino) node type  c2
    public static final int PINODE = 4;         // Raspberry Pi node type  c2
    
    public static final byte INPUT_CARD = 1;    // USIC/SUSIC input card type for specifying location
    public static final byte OUTPUT_CARD = 2;   // USIC/SUSIC output card type for specifying location
    public static final byte NO_CARD = 0;       // USIC/SUSIC unused location    

    // node definition instance variables (must persist between runs)
    protected int nodeType = SMINI;             // See above
    protected int bitsPerCard = 24;             // 24 for SMINI and USIC, 24 or 32 for SUSIC  8 for CPNODE
    protected int transmissionDelay = 0;        // DL, delay between bytes on Receive (units of 10 microsec.)
    protected int pulseWidth = 500;		// Pulse width for pulsed turnout control (milliseconds)
    protected int num2LSearchLights = 0;        // SMINI only, 'NS' number of two lead bicolor signals
    protected byte[] locSearchLightBits = new byte[MAXSEARCHLIGHTBYTES]; // SMINI only, 0 = not searchlight LED,
                                                //   1 = searchlight LED, 2*NS bits must be set to 1
    protected byte[] cardTypeLocation = new byte[MAXCARDLOCATIONBYTES]; // Varys on USIC/SUSIC. There must numInputCards bytes set to
    						//   INPUT_CARD, and numOutputCards set to OUTPUT_CARD, with
                                                //   the remaining locations set to NO_CARD.  All
                                                //   NO_CARD locations must be at the end of the array.  The
                                                //   array is indexed by card address.
    // cpNode/PiNode variables
    public static final int INITMSGLEN = 12;
    public static final int NUMCMRINETOPTS = 16;
    public static final int NUMCPNODEOPTS = 16;
    protected int cmrinetOptions[] = new int[NUMCMRINETOPTS];  // CMRInet options stored as 16 binary digits 
    protected int cpnodeOptions[] = new int[NUMCPNODEOPTS];  // cpNode options stored as 16 binary digits 
    
    protected String cmriNodeDesc = ""; // CMRI node name for display    
    protected int pollListPosition = 0;
    
    public int pollStatus = 1;
    public static final int POLLSTATUS_ERROR    = 0;
    public static final int POLLSTATUS_IDLE     = 1;
    public static final int POLLSTATUS_POLLING  = 2;
    public static final int POLLSTATUS_TIMEOUT  = 3;
    public static final int POLLSTATUS_INIT     = 4;
    
    // CMRInet options stored in XML
    public static final int optbitNet_AUTOPOLL  = 0;
    public static final int optbitNet_USECMRIX  = 1;
    public static final int optbitNet_USEBCC    = 2;
    public static final int optbitNet_BIT8      = 8;
    public static final int optbitNet_BIT15     = 15;

    // cpNode/PiNode options in initialization message
    public static final int optbitNode_USECMRIX = 0;
    public static final int optbitNode_SENDEOT  = 1;
    public static final int optbitNode_USEBCC   = 2;
    public static final int optbitNode_BIT8     = 8;
    public static final int optbitNode_BIT15    = 15;
    
    // operational instance variables  (should not be preserved between runs)
    protected byte[] outputArray = new byte[256]; // current values of the output bits for this node
    protected boolean hasActiveSensors = false; // 'true' if there are active Sensors for this node
    protected int lastUsedSensor = 0;           // grows as sensors defined
    protected Sensor[] sensorArray = new Sensor[MAXSENSORS+1];
    protected int[] sensorLastSetting = new int[MAXSENSORS+1];
    protected int[] sensorTempSetting = new int[MAXSENSORS+1];
    
    protected boolean monitorNodePackets = true;
    protected boolean[] monitorPacketBits = new boolean[SerialFilterFrame.numMonPkts];

   /**
     * Assumes a node address of 0, and a node type of SMINI
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress, and actual node type using 'setNodeType'
     */
    public SerialNode() {
        this (0,SMINI);
    }
    
    /**
     * Creates a new SerialNode and initialize default instance variables
     *   address - Address of node on CMRI serial bus (0-127)
     *   type - SMINI, USIC_SUSIC, CPNODE, PINODE c2
     */
    public SerialNode(int address, int type) {
        // set address and type and check validity
        setNodeAddress (address);
        setNodeType (type);
        // set default values for other instance variables
        bitsPerCard = 24;
        transmissionDelay = 0;
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
        for (int i = 0; i<SerialFilterFrame.numMonPkts; i++)
            monitorPacketBits[i] = true;
        
        // initialize other operational instance variables
        setMustSend();
        hasActiveSensors = false;
        // register this node
        SerialTrafficController.instance().registerNode(this);
    }

    public int getNum2LSearchLights() {
    	return num2LSearchLights;
    }
    	
    public void setNum2LSearchLights(int n) {
    	num2LSearchLights = n;
    }
    	
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public byte[] getLocSearchLightBits() {
    	return locSearchLightBits;
    }
    	
    public void setLocSearchLightBits(int num, int value) {
    	locSearchLightBits[num] = (byte)(value&0xFF);
    }
    	
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
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
            warn("CMRInet - Output bit out-of-range for defined node");
        }
        if (byteNumber >= 256) byteNumber = 255;
        // update the byte
        byte bit = (byte) (1<<((bitNumber-1) % 8));
        byte oldByte = outputArray[byteNumber];
        if (state) outputArray[byteNumber] &= (~bit);
        else outputArray[byteNumber] |= bit;
        // check for change, necessitating a send
        if (oldByte != outputArray[byteNumber]) {
            setMustSend();
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
            warn("CMRInet - Output bit out-of-range for defined node");
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
    public boolean getSensorsActive() { return hasActiveSensors; }

    /**
     * Public method to set state of Sensors.
     * Used to disable polling for test purposes only.
     */
    public void setSensorsActive(boolean flag) { hasActiveSensors = flag; }
    
    /**
     * Public method to return number of input cards.
     */
    public int numInputCards()   //c2
    {
    	int result = 0;
        int nodeID = this.getNodeAddress();
    	for (int i=0; i<cardTypeLocation.length; i++)
    	  if (cardTypeLocation[i]==INPUT_CARD) result++;

        // check consistency
        switch (nodeType)
        {
          case SMINI:      if (result!=1)
                           {
                            warn("CMRInet SMINI "+"(Node "+nodeID+") with "+result+" INPUT cards");
                           }
          break;
          case USIC_SUSIC: if(result>=MAXCARDLOCATIONBYTES)
                            warn("CMRInet USIC/SUSIC node with "+result+" INPUT cards");
          break;
          case CPNODE:     if(result<2)  //c2
                            warn("CMRInet CPNODE node with "+result+" INPUT cards");
          break;
          case PINODE:    if(result<3)  //c2
                            warn("CMRInet PINODE node with "+result+" INPUT cards");
          break;
          default: ;
          break;
        }

    	return result;
    }

    /**
     * Public method to return number of output cards.
     */
    public int numOutputCards()  //c2
    {
    	int result = 0;
        int nodeID = this.getNodeAddress();
    	for (int i=0; i<cardTypeLocation.length; i++)
    	  if (cardTypeLocation[i]==OUTPUT_CARD) result++;

    	// check consistency
         switch (nodeType)
         {
           case SMINI:     if (result!=2) 
                           {
                            warn("CMRInet SMINI "+"(Node "+nodeID+") with "+result+" OUTPUT cards");
                           }
           break;
           case USIC_SUSIC: 
            if(result>=MAXCARDLOCATIONBYTES)
             warn("CMRInet USIC/SUSIC node with "+result+" OUTPUT cards");
           break;
           case CPNODE:     //c2
           if(result<2)
             warn("CMRInet CPNODE node with "+result+" OUTPUT cards");
           break;
           case PINODE:     //c2
           if(result<3)
             warn("CMRInet PINODE node with "+result+" OUTPUT cards");
           break;
           default: ;
         }
         
    	 return result;
    }

    /**
     * Public method to return node type
     *   Current types are:
     *      SMINI, USIC_SUSIC,
     */
    public int getNodeType() {
        return (nodeType);
    }

    /**
     * Public method to set node type
     *   Current types are:
     *      SMINI, USIC_SUSIC, CPNODE
     *   For SMINI, also sets cardTypeLocation[] and bitsPerCard
     *   For USIC_SUSIC, also clears cardTypeLocation
     *   For CPNODE, sets the on board port configuration and IO extenders
     */
    public void setNodeType(int type)   //c2
    {
        switch(type)
        {
          case SMINI: 
            nodeType = type;
            bitsPerCard = 24;
            // set cardTypeLocation for SMINI
            cardTypeLocation[0] = OUTPUT_CARD;
            cardTypeLocation[1] = OUTPUT_CARD;
            cardTypeLocation[2] = INPUT_CARD;
            for (int i=3;i<MAXCARDLOCATIONBYTES;i++)
            {
             cardTypeLocation[i] = NO_CARD;
            }
          break;
          case USIC_SUSIC:
            nodeType = type;
            // clear cardTypeLocations
            for (int i=0;i<MAXCARDLOCATIONBYTES;i++)
            {
             cardTypeLocation[i] = NO_CARD;
            }
          break;
          case CPNODE:  //c2
            nodeType = type;
            bitsPerCard = 8;
            
            // set cardTypeLocation for CPNODE.  First four bytes are onboard
            cardTypeLocation[0] = INPUT_CARD;
            cardTypeLocation[1] = INPUT_CARD;
            cardTypeLocation[2] = OUTPUT_CARD;
            cardTypeLocation[3] = OUTPUT_CARD;
            for (int i=4;i<MAXCARDLOCATIONBYTES;i++) 
            {
             cardTypeLocation[i] = NO_CARD;
            }
          break;
            
          case PINODE:  //c2
            nodeType = type;
            bitsPerCard = 8;
            
            // set cardTypeLocation for PINODE.  First six bytes are onboard
            cardTypeLocation[0] = INPUT_CARD;
            cardTypeLocation[1] = INPUT_CARD;
            cardTypeLocation[2] = INPUT_CARD;
            cardTypeLocation[3] = OUTPUT_CARD;
            cardTypeLocation[4] = OUTPUT_CARD;
            cardTypeLocation[5] = OUTPUT_CARD;
            for (int i=6;i<MAXCARDLOCATIONBYTES;i++) 
            {
             cardTypeLocation[i] = NO_CARD;
            }
          break;
            
// here recognize other node types
          default: log.error("Bad node type - "+Integer.toString(type) );
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
        if ( (bits==24) || (bits==32) || (bits==8) ) {
            bitsPerCard = bits;
        }
        else {
            log.warn("unexpected number of bits per card: "+Integer.toString(bits));
            bitsPerCard = bits;
        }
    }

    /**  
     * return CMRInet options.  
     */
    public int getCMRInetOpts(int optionbit) { return (cmrinetOptions[optionbit]); }
    public void setCMRInetOpts(int optionbit,int val) { cmrinetOptions[optionbit] = (byte)val; }
    public boolean isCMRInetBit(int optionbit) { return (cmrinetOptions[optionbit] == 1); }

    /** 
     * return cpNode Initialization options.  
     */
    public int getcpnodeOpts(int optionbit) { return (cpnodeOptions[optionbit]); }
    public void setcpnodeOpts(int optionbit,int val) { cpnodeOptions[optionbit] = (byte)val; }
    public boolean iscpnodeBit(int optionbit) { return (cpnodeOptions[optionbit] == 1); }

    /**
     * get and set specific option bits.
     * 
     */
    
    /**
     * Network Option Bits
     * 
     */
    public boolean getOptNet_AUTOPOLL() { return (cmrinetOptions[optbitNet_AUTOPOLL] == 1); }
    public boolean getOptNet_USECMRIX() { return (cmrinetOptions[optbitNet_USECMRIX] == 1); }
    public boolean getOptNet_USEBCC()     { return (cmrinetOptions[optbitNet_USEBCC] == 1); }
    public boolean getOptNet_BIT8()     { return (cmrinetOptions[optbitNet_BIT8] == 1); }
    public boolean getOptNet_BIT15()    { return (cmrinetOptions[optbitNet_BIT15] == 1); }

    public void setOptNet_AUTOPOLL(int val) { cmrinetOptions[optbitNet_AUTOPOLL] = (byte)val; }
    public void setOptNet_USECMRIX(int val) { cmrinetOptions[optbitNet_USECMRIX] = (byte)val; }
    public void setOptNet_USEBCC(int val)     { cmrinetOptions[optbitNet_USEBCC] = (byte)val; }
    public void setOptNet_BIT8(int val)     { cmrinetOptions[optbitNet_BIT8] = (byte)val; }
    public void setOptNet_BIT15(int val)    { cmrinetOptions[optbitNet_BIT15] = (byte)val; }
    
    public int getOptNet_byte0() {return cmrinetOptions[0];}
    public int getOptNet_byte1() {return cmrinetOptions[1];}

    /**
     * Node Option Bits  
     * 
     */
    public boolean getOptNode_SENDEOT()  { return (cpnodeOptions[optbitNode_SENDEOT] == 1); }
    public boolean getOptNode_USECMRIX() { return (cpnodeOptions[optbitNode_USECMRIX] == 1); }
    public boolean getOptNode_USEBCC()   { return (cpnodeOptions[optbitNode_USEBCC] == 1); }
    public boolean getOptNode_BIT8()     { return (cpnodeOptions[optbitNode_BIT8] == 1); }
    public boolean getOptNode_BIT15()    { return (cpnodeOptions[optbitNode_BIT15] == 1); }

    public void setOptNode_SENDEOT(int val)  { cpnodeOptions[optbitNode_SENDEOT] = (byte)val; }
    public void setOptNode_USECMRIX(int val) { cpnodeOptions[optbitNode_USECMRIX] = (byte)val; }
    public void setOptNode_USEBCC(int val)   { cpnodeOptions[optbitNode_USEBCC] = (byte)val; }
    public void setOptNode_BIT8(int val)     { cpnodeOptions[optbitNode_BIT8] = (byte)val; }
    public void setOptNode_BIT15(int val)    { cpnodeOptions[optbitNode_BIT15] = (byte)val; }
    
    public int getOptNode_byte0() {return cpnodeOptions[0];}
    public int getOptNode_byte1() {return cpnodeOptions[1];}
    
    /**
     * node description 
     * 
     */
    public String getcmriNodeDesc() { return cmriNodeDesc; }
    public void setcmriNodeDesc(String nodeDesc) { cmriNodeDesc = nodeDesc; }
    
    /**
     * cpNode poll list position
     * 
     */
    public int getPollListPosition() { return pollListPosition; }
    public void setPollListPosition(int pos)  { pollListPosition = pos; }

    /**
     * cpNode polling status
     * 
     */
    public int getPollStatus() { return pollStatus; }
    public void setPollStatus(int status) { pollStatus = status; }

    /**
     * checking cpNode polling enabled state
     * 
     */
    public boolean getPollingEnabled() { return (cmrinetOptions[optbitNet_AUTOPOLL] == 1); }
    public void setPollingEnabled(boolean isEnabled)
    {  
      if(isEnabled)
        cmrinetOptions[optbitNet_AUTOPOLL] = 1;
      else
        cmrinetOptions[optbitNet_AUTOPOLL] = 0;
    }
   
   /**
    * 
    * Set/Get packet monitoring for the node 
    */
   public boolean getMonitorNodePackets()  { return monitorNodePackets; }
   public void setMonitorNodePackets(boolean onoff) { monitorNodePackets = onoff; }
   
   /**
    * Set/Get the specific packet monitoring enable bit
   */
   public void setMonitorPacketBit(int pktTypeBit, boolean onoff)
   { 
       monitorPacketBits[pktTypeBit] = onoff; 
   }
   
   public boolean getMonitorPacketBit(int pktTypeBit)
   { 
       return monitorPacketBits[pktTypeBit]; 
   }
   
    /**
     * Check valid node address, must match value in dip switches (0 - 127)
     * 
     */
    protected boolean checkNodeAddress(int address) { return (address >= 0) && (address < 128); }

    /**
     * Public method to return transmission delay
     * 
     */
    public int getTransmissionDelay() { return (transmissionDelay); }

    /**
     * Public method to set transmission delay.
     *   delay - delay between bytes on receive (units of 10 microsec.)
     *   Note: two bytes are used, so range is 0-65,535.  If delay
     *          is out of range, it is restricted to the allowable range
     */
    public void setTransmissionDelay(int delay) {
        if ( (delay < 0) || (delay > 65535) ) {
            log.warn("transmission delay out of 0-65535 range: "+
                                            Integer.toString(delay));
            if (delay < 0) delay = 0;
            if (delay > 65535) delay = 65535;
        }
        transmissionDelay = delay;
    }

    /**
     * Public method to return pulse width.
     * Used with pulsed turnout control (e.g. twin coil).
     */
    public int getPulseWidth() { return (pulseWidth); }

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
     *               for USIC_SUSIC address set in card's dip switches (0 - 63)
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
        if ( (nodeType==SMINI) && ( ( (address>2) && (type!=NO_CARD) ) ||
                                ( (address==2) && (type!=INPUT_CARD) ) ||
                                ( (address<2) && (type!=OUTPUT_CARD) ) ) ) {
            log.error("illegal card type/address specification for SMINI");
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
        if (cardNum>63)
        {
            warn("CMRInet - isOutputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==SMINI)
        {
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
    public boolean isInputCard(int cardNum)
    {
        if (cardNum>63)
        {
            warn("CMRInet - isInputCard - cardNum out of range");
            return (false);
        }
        if (nodeType==SMINI)
        {
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
        warn("CMRInet - input card to getOutputCardIndex is not an Output Card");
        return (0);
    }

    /** Public method to return 'Input Card Index'
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
        warn("CMRInet - input card to getOutputCardIndex is not an Output Card");
        return (0);
    }

    /**
     * Public Method to set location of SearchLightBits (SMINI only)
     *   bit - bitNumber of the low bit of an oscillating search light bit pair
     *   Notes:  Bits are numbered from 0
     *           Two bits are set by each call - bit and bit + 1.
     *           If either bit is already set, an error is logged and no
     *               bits are set.
     */
    public void set2LeadSearchLight(int bit) {
        // check for SMINI
// if other types of CMRI nodes allow oscillating search lights, modify this method
        if (nodeType!=SMINI) {
            log.error("Invalid setting of Searchlights bits - not SMINI node");
            return;
        }
        // validate bit number range
        if ( (bit<0) || (bit>46) ) {
            log.error("Invalid bit number when setting SMINI Searchlights bits: "+
                                            Integer.toString(bit));
            return;
        }
        // validate that bits are not already set
        if ( (locSearchLightBits[bit] != 0) || (locSearchLightBits[bit+1] != 0) ) {
            log.error("bit number for SMINI Searchlights bits already set: "+
                                            Integer.toString(bit));
            return;
        }
        // set the bits
        locSearchLightBits[bit] = 1;
        locSearchLightBits[bit+1] = 1;
        num2LSearchLights ++;
    }

    /**
     * Public Method to clear location of SearchLightBits (SMINI only)
     *   bit - bitNumber of the low bit of an oscillating search light bit pair
     *   Notes:  Bits are numbered from 0
     *           Two bits are cleared by each call - bit and bit + 1.
     *           If either bit is already clear, an error is logged and no
     *               bits are set.
     */
    public void clear2LeadSearchLight(int bit) {
        // check for SMINI
// if other types of CMRI nodes allow oscillating search lights, modify this method
        if (nodeType!=SMINI) {
            log.error("Invalid setting of Searchlights bits - not SMINI node");
            return;
        }
        // validate bit number range
        if ( (bit<0) || (bit>46) ) {
            log.error("Invalid bit number when setting SMINI Searchlights bits: "+
                                            Integer.toString(bit));
            return;
        }
        // validate that bits are not already clear
        if ( (locSearchLightBits[bit] != 1) || (locSearchLightBits[bit+1] != 1) ) {
            log.error("bit number for SMINI Searchlights bits already clear: "+
                                            Integer.toString(bit));
            return;
        }
        // set the bits
        locSearchLightBits[bit] = 0;
        locSearchLightBits[bit+1] = 0;
        num2LSearchLights --;
    }

    /**
     * Public Method to query SearchLightBits by bit number (SMINI only)
     *   bit - bitNumber of the either bit of an oscillating search light bit pair
     *   Note: returns 'true' if bit is an oscillating SearchLightBit, otherwise
     *          'false' is returned
     */
     public boolean isSearchLightBit (int bit) {
        // check for SMINI
// if other types of CMRI nodes allow oscillating search lights, modify this method
        if (nodeType!=SMINI) {
            log.error("Invalid query of Searchlights bits - not SMINI node");
            return (false);
        }
        // validate bit number range
        if ( (bit<0) || (bit>47) ) {
            log.error("Invalid bit number in query of SMINI Searchlights bits: "+
                                            Integer.toString(bit));
            return (false);
        }
        if (locSearchLightBits[bit] == 1) {
            return (true);
        }
        return (false);
    }


    /**
     * Public Method to create an Initialization packet (SerialMessage) 
     * for this node
     */
    public AbstractMRMessage createInitPacket() {
        // Assemble initialization byte array from node information
        int nInitBytes = 4;
        byte[] initBytes = new byte [32];  //c2
        int code = 0;
        // set node definition parameter
        switch(nodeType)
        {
            case SMINI:       initBytes[0] = 77;  // 'M'
            break;
         
            case USIC_SUSIC:  if (bitsPerCard==24) initBytes[0] = 78;   // 'N'
                               else 
                                if (bitsPerCard==32) initBytes[0] = 88; // 'X'
            break;
            case CPNODE:      initBytes[0] = 67;  // 'C'   c2
            break;
            case PINODE:      initBytes[0] = 80;  // 'P'   c2
            break;
            
            default: ;
        }
// Here add code for other type of card
         
        /**
         * add Transmission Delay bytes (same for SMINI and USIC/SUSIC)
         */
         int firstByte = transmissionDelay / 256;
         int secondByte = transmissionDelay - ( firstByte*256 );
         if (firstByte>255) firstByte = 255;
         initBytes[1] = (byte)firstByte;
         initBytes[2] = (byte)secondByte;

        // SMINI specific part of initialization byte array
        switch (nodeType)  //c2
        {
            case SMINI:
                        initBytes[3] = (byte)num2LSearchLights;
                        if (num2LSearchLights>0)
                        {
                        // Set up searchlight LED bit codes
                            for (int i=0,j=0;i<6;i++,j+=8)
                            {
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
            break;
        
        // USIC/SUSIC specific part of initialization byte array
            case USIC_SUSIC:        
                            int numCards = numInputCards() + numOutputCards();
                            int numFours = numCards/4;
                            if ( (numCards-(numFours*4)) > 0) numFours ++;  // Round up if not even multiple
                            initBytes[3] = (byte)numFours;
                            for (int i=0,j=0;i<numFours;i++,j+=4)
                            {
                              code = cardTypeLocation[j];
                              code = code + (cardTypeLocation[j+1] * 4);
                              code = code + (cardTypeLocation[j+2] * 16);
                              code = code + (cardTypeLocation[j+3] * 64);
                              initBytes[nInitBytes] = (byte)code;
                              nInitBytes ++;
                            }
            break;
                
        /* CPNODE specific part of initialization byte array
         * The I message has the node configuration options following the
         * DL bytes, followed by the defined number of I/O cards.
         *    0   1   2    3        4        5     6     7 - 12
         *  <NDP><dH><dL><cpOPTS1><cpOPTS2><cpNI><cpNO> <rfe 8>
         */
            case CPNODE:
                          nInitBytes = 3;
                           // ------------------------- 
                           // Pack the two option bytes 
                           // ------------------------- 
                           for (int i=0,j=0;i<2;i++,j+=8)
                           {
                              code = cpnodeOptions[j];
                              code = code + (cpnodeOptions[j+1]*2);   
                              code = code + (cpnodeOptions[j+2]*4);  
                              code = code + (cpnodeOptions[j+3]*8);  
                              code = code + (cpnodeOptions[j+4]*16);   
                              code = code + (cpnodeOptions[j+5]*32);  
                              code = code + (cpnodeOptions[j+6]*64);  
                              code = code + (cpnodeOptions[j+7]*128); 
                              initBytes[nInitBytes] = (byte)code;
                              nInitBytes++;
                           }
                           // ------------------------------------- 
                           // Configured input and output byte count
                           // ------------------------------------- 
                           initBytes[nInitBytes++] = (byte)numInputCards();
                           initBytes[nInitBytes++] = (byte)numOutputCards();
                         
                           // --------------------------
                           // future to be defined bytes
                           // --------------------------
                           for (int i=nInitBytes; i<INITMSGLEN+1; i++)
                           {
                            initBytes[i] = (byte)0xFF;
                            nInitBytes++;
                           }

            break;
            
         /* PINODE specific part of initialization byte array
         * The I message has the node configuration options following the
         * DL bytes, followed by the defined number of I/O cards.
         *    0   1   2    3        4        5     6     7 - 12
         *  <NDP><dH><dL><cpOPTS1><cpOPTS2><cpNI><cpNO> <rfe 8>
         */
           case PINODE:
                          nInitBytes = 3;
                           // ------------------------- 
                           // Pack the two option bytes 
                           // ------------------------- 
                           for (int i=0,j=0;i<2;i++,j+=8)
                           {
                              code = cpnodeOptions[j];
                              code = code + (cpnodeOptions[j+1]*2);   
                              code = code + (cpnodeOptions[j+2]*4);  
                              code = code + (cpnodeOptions[j+3]*8);  
                              code = code + (cpnodeOptions[j+4]*16);   
                              code = code + (cpnodeOptions[j+5]*32);  
                              code = code + (cpnodeOptions[j+6]*64);  
                              code = code + (cpnodeOptions[j+7]*128); 
                              initBytes[nInitBytes] = (byte)code;
                              nInitBytes++;
                           }
                           // ------------------------------------- 
                           // Configured input and output byte count
                           // ------------------------------------- 
                           initBytes[nInitBytes++] = (byte)numInputCards();
                           initBytes[nInitBytes++] = (byte)numOutputCards();
                         
                           // --------------------------
                           // future to be defined bytes
                           // --------------------------
                           for (int i=nInitBytes; i<INITMSGLEN+1; i++)
                           {
                            initBytes[i] = (byte)0xFF;
                            nInitBytes++;
                           }

            break;
            
            default:  log.error("Invalid node type ("+nodeType+") in SerialNode Init Message");
                
        }            
// here add specific initialization for other type of card

        // count the number of DLE's to be inserted
        int nDLE = 0;
        for (int i=1; i<nInitBytes; i++) {
            if ( (initBytes[i]==2) || (initBytes[i]==3) || (initBytes[i]==16) )
                nDLE ++;
        }

        // create a Serial message and add initialization bytes
        SerialMessage m = new SerialMessage(nInitBytes + nDLE + 2);
        m.setElement(0,getNodeAddress()+65);  // node address
        m.setElement(1,73);     // 'I'
        // add initialization bytes
        int k = 2;
        for (int i=0; i<nInitBytes; i++) {
            // perform CMRInet required DLE processing
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

    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
        public AbstractMRMessage createOutPacket() {
        // Count the number of DLE's to be inserted
        int nOutBytes = numOutputCards() * (bitsPerCard/8);
        int nDLE = 0;
        for (int i=0; i<nOutBytes; i++) {
            if ( (outputArray[i]==2) || (outputArray[i]==3) ||(outputArray[i]==16) )
                nDLE ++;
        }
        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage(nOutBytes + nDLE + 2);
        m.setElement(0,getNodeAddress()+65); // node address
        m.setElement(1,84);             // 'T'
        // Add output bytes
        int k = 2;
        for (int i=0; i<nOutBytes; i++) {
            // perform CMRInet required DLE processing
            if ( (outputArray[i]==2) || (outputArray[i]==3) ||(outputArray[i]==16) ) {
                m.setElement(k,16);  // DLE
                k ++;
            }
            // add output byte
            m.setElement(k, outputArray[i]);
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

                // if (log.isDebugEnabled()) log.debug("markChanges loc="+loc+" bit="+bit+" is "+value+
                //                    " tempSetting is "+((sensorTempSetting[i] == Sensor.ACTIVE)?"active ":"inactive ")+
                //                    "lastSetting is "+((sensorLastSetting[i] == Sensor.ACTIVE)?"active ":"inactive ")
                //                    );
                
                if ( value ) {
                    // considered ACTIVE
                    if (   ( (sensorTempSetting[i] == Sensor.ACTIVE) || 
                                (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                           ( sensorLastSetting[i] != Sensor.ACTIVE) ) { // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.ACTIVE;
                        sensorArray[i].setKnownState(Sensor.ACTIVE);
                        // log.debug("set active");
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.ACTIVE;
                } else {
                    // considered INACTIVE
                    if (  ( (sensorTempSetting[i] == Sensor.INACTIVE)  || 
                                (sensorTempSetting[i] == Sensor.UNKNOWN) ) &&
                          ( sensorLastSetting[i] != Sensor.INACTIVE) ) {  // see comment at top; allows persistent local changes
                        sensorLastSetting[i] = Sensor.INACTIVE;
                        sensorArray[i].setKnownState(Sensor.INACTIVE);
                        // log.debug("set inactive");
                    }
                    // save for next time
                    sensorTempSetting[i] = Sensor.INACTIVE;
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
        if ( (i<0) || (i> ((numInputCards()*bitsPerCard) - 1)) || (i>MAXSENSORS) ) {
            log.error("Node "+Integer.toString(getNodeAddress())+" Unexpected sensor ordinal in registerSensor: "+Integer.toString(i+1));
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
                    Integer.toString((getNodeAddress()*SerialSensorManager.SENSORSPERUA) + i + 1) );
        }
    }

    int timeout = 0;
    /**
     *
     * @return true if initialization required
     */
    public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l) {
        timeout++;
        // normal to timeout in response to init, output
        if (m.getElement(1)!=0x50) return false;
        
        // see how many polls missed
        if (log.isDebugEnabled()) log.warn("Timeout to poll for UA="+getNodeAddress()+": consecutive timeouts: "+timeout);
        
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
                        log.error("unexpected exception setting sensor i="+i+" on node "+getNodeAddress()+"e: "+e);
                    }
                }
            }
            return true;   // tells caller to force init
        } 
        else
        { 
            return false; 
        } 
    }
    
    public void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
