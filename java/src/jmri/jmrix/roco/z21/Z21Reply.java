package jmri.jmrix.roco.z21;

import jmri.jmrix.AbstractMRReply;
import jmri.DccLocoAddress;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class for replies in the z21/Z21 protocol.
 * <p>
 * Replies are of the format: 2 bytes length 2 bytes opcode n bytes data
 * <p>
 * numeric data is sent in little endian format.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Paul Bender Copyright (C) 2014
 */
public class Z21Reply extends AbstractMRReply {

    private static final String WRONG_REPLY_TYPE = "Wrong Reply Type";

    /**
     *  Create a new one.
     */
    public Z21Reply() {
        super();
        setBinary(true);
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send.
     * @param l length of reply.
     */
    public Z21Reply(byte[] a, int l) {
        super();
        _nDataChars = l;
        setBinary(true);
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = a[i];
        }
    }

    // keep track of length
    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = (char) v;
        _nDataChars = Math.max(_nDataChars, n + 1);
    }

    /**
     * Get an integer representation of a BCD value.
     *
     * @param n byte in message to convert
     * @return Integer value of BCD byte.
     */
    public Integer getElementBCD(int n) {
        return Integer.decode(Integer.toHexString(getElement(n)));
    }

    @Override
    public void setOpCode(int i) {
        _dataChars[2] = (char) (i & 0x00ff);
        _dataChars[3] = (char) ((i & 0xff00) >> 8);
        _nDataChars = Math.max(_nDataChars, 4);  //smallest reply is of length 4.
    }

    @Override
    public int getOpCode() {
        return (0xff&_dataChars[2]) + ((0xff&_dataChars[3]) << 8);
    }

    public void setLength(int i) {
        _dataChars[0] = (char) (i & 0x00ff);
        _dataChars[1] = (char) ((i & 0xff00) >> 8);
        _nDataChars = Math.max(_nDataChars, i);
    }

    public int getLength() {
        return (0xff & _dataChars[0] ) + ((0xff & _dataChars[1]) << 8);
    }

    @Override
    protected int skipPrefix(int index) {
        return 0;
    }

    private static List<Z21MessageFormatter> formatterList = new ArrayList<>();

    @Override
    public String toMonitorString() {
        if(formatterList.isEmpty()) {
            try {
                Reflections reflections = new Reflections("jmri.jmrix.roco.z21.messageformatters");
                Set<Class<? extends Z21MessageFormatter>> f = reflections.getSubTypesOf(Z21MessageFormatter.class);
                for (Class<?> c : f) {
                    log.debug("Found formatter: {}", f.getClass().getName());
                    Constructor<?> ctor = c.getConstructor();
                    formatterList.add((Z21MessageFormatter) ctor.newInstance());
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException e) {
                log.error("Error instantiating formatter", e);
            }
        }

        return formatterList.stream()
                .filter(f -> f.handlesMessage(this))
                .findFirst().map(f -> f.formatMessage(this))
                .orElse(this.toString());
    }

    // handle XpressNet replies tunneled in Z21 messages
    boolean isXPressNetTunnelMessage() {
        return (getOpCode() == 0x0040);
    }

    public Z21XNetReply getXNetReply() {
        Z21XNetReply xnr = null;
        if (isXPressNetTunnelMessage()) {
            int i = 4;
            xnr = new Z21XNetReply();
            for (; i < getLength(); i++) {
                xnr.setElement(i - 4, getElement(i));
            }
            if(( xnr.getElement(0) & 0x0F ) > ( xnr.getNumDataElements()+2) ){
               // there is at least one message from the Z21 that can be sent 
               // with fewer bytes than the XpressNet payload indicates it
               // should have.  Pad those messages with 0x00 bytes.
               for(i=i-4;i<((xnr.getElement(0)&0x0F)+2);i++){
                  xnr.setElement(i,0x00);
               }
            }
        }
        return xnr;
    }
   
    // handle RailCom data replies
    boolean isRailComDataChangedMessage(){
        return (getOpCode() == 0x0088);
    }

    /**
     * @return the number of RailCom entries in this message.
     *         the returned value is in the 0 to 19 range.
     */
    public int getNumRailComDataEntries(){
        if(!this.isRailComDataChangedMessage()){
           return 0; // this isn't a RailCom message, so there are no entries.
        }
        // if this is a RailCom message, the length field is
        // then the entries are n=(len-4)/13, per the Z21 protocol 
        // manual, section 8.1.  Also, 0<=n<=19
        return ((getLength() - 4)/13);
    } 

    /**
     * Get a locomotive address from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the locomotive address for the specified entry.
     */
    public DccLocoAddress getRailComLocoAddress(int n){
         int offset = 4+(n*13);  // +4 to get past header
         int address = Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
         return new DccLocoAddress(address,address>=100);
    }

    /**
     * Get the receive counter from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the receive counter for the specified entry.
     */
    public int getRailComRcvCount(int n){
         int offset = 6+(n*13); // +6 to get header and address.
         return ((0xff&getElement(offset+3))<<24) +
                       ((0xff&(getElement(offset+2))<<16) + 
                       ((0xff&getElement(offset+1))<<8) + 
                       (0xff&(getElement(offset))));
    }

    /**
     * Get the error counter from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the error counter for the specified entry.
     */
    public int getRailComErrCount(int n){
         int offset = 10+(n*13); // +10 to get past header, address,and rcv count.
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the speed value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the error counter for the specified entry.
     */
    public int getRailComSpeed(int n){
         int options = getRailComOptions(n);
         if(((options & 0x01) == 0x01) || ((options & 0x02) == 0x02)) { 
            int offset = 14+(n*13); //+14 to get past the options, 
                                    // and everything before the options.
            return (0xff&(getElement(offset)));
         } else {
            return 0;
         }
    }

    /**
     * Get the options value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the options for the specified entry.
     */
    public int getRailComOptions(int n){
         int offset = 13+(n*13); //+13 to get past the header, address, rcv 
                                 // counter, and reserved byte.
         return (0xff&(getElement(offset)));
    }

    /**
     * Get the Quality of Service value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the Quality of Service value for the specified entry.
     */
    public int getRailComQos(int n){
         if((getRailComOptions(n) & 0x04) == 0x04 ) { 
            int offset = 15+(n*13); //+15 to get past the speed, 
                                    // and everything before the speed.
            return (0xff&(getElement(offset)));
         } else {
            return 0; // if the QOS bit isn't set, there is no QOS attribute.
         }
    }

    // handle System data replies
    boolean isSystemDataChangedReply(){
        return (getOpCode() == 0x0084);
    }

    private void checkSystemDataChangeReply(){
        if(!isSystemDataChangedReply()){
            throw new IllegalArgumentException(WRONG_REPLY_TYPE);
        }
    }

    /**
     * Get the Main Track Current from the SystemStateDataChanged 
     * message.
     *
     * @return the current in mA.
     */
    public int getSystemDataMainCurrent(){
         checkSystemDataChangeReply();
         int offset = 4; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the Programming Track Current from the SystemStateDataChanged 
     * message.
     *
     * @return the current in mA.
     */
    public int getSystemDataProgCurrent(){
         checkSystemDataChangeReply();
         int offset = 6; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the Filtered Main Track Current from the SystemStateDataChanged 
     * message.
     *
     * @return the current in mA.
     */
    public int getSystemDataFilteredMainCurrent(){
         checkSystemDataChangeReply();
         int offset = 8; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the Temperature from the SystemStateDataChanged 
     * message.
     *
     * @return the current in degrees C.
     */
    public int getSystemDataTemperature(){
         checkSystemDataChangeReply();
         int offset = 10; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the Supply Voltage from the SystemStateDataChanged 
     * message.
     *
     * @return the current in mV.
     */
    public int getSystemDataSupplyVoltage(){
         checkSystemDataChangeReply();
         int offset = 12; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the VCC (and track) Voltage from the SystemStateDataChanged 
     * message.
     *
     * @return the current in mV.
     */
    public int getSystemDataVCCVoltage(){
         checkSystemDataChangeReply();
         int offset = 14; //skip the headers
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    // handle LocoNet replies tunneled in Z21 messages
    boolean isLocoNetTunnelMessage() {
        switch (getOpCode()){
          case 0xA0: // LAN_LOCONET_Z21_RX
          case 0xA1: // LAN_LOCONET_Z21_TX
          case 0xA2: // LAN_LOCONET_FROM_LAN
             return true;
          default:
             return false;
        }
    }

    boolean isLocoNetDispatchMessage() {
       return (getOpCode() == 0xA3);
    }

    boolean isLocoNetDetectorMessage() {
       return (getOpCode() == 0xA4);
    }

    public jmri.jmrix.loconet.LocoNetMessage getLocoNetMessage() {
        jmri.jmrix.loconet.LocoNetMessage lnr = null;
        if (isLocoNetTunnelMessage()) {
            int i = 4;
            lnr = new jmri.jmrix.loconet.LocoNetMessage(getLength()-4);
            for (; i < getLength(); i++) {
                lnr.setElement(i - 4, getElement(i));
            }
        }
        return lnr;
    }

    // handle RMBus data replies
    boolean isRMBusDataChangedReply(){
        return (getOpCode() == 0x0080);
    }

    // handle CAN Feedback/Railcom replies
    boolean isCanDetectorMessage() {
        return (getOpCode() == 0x00C4);
    }



    /**
     * @return the can Detector Message type or -1 if not a can detector message.
     */
   public int canDetectorMessageType() {
        if(isCanDetectorMessage()){
            return getElement(9) & 0xFF;
        }
        return -1;
   }

    /**
      * @return true if the reply is for a CAN detector and the type is 0x01
     */
   public boolean isCanSensorMessage(){
        return isCanDetectorMessage() && canDetectorMessageType() == 0x01;
   }

    /**
     * @return true if the reply is for a CAN detector and the type is 0x01
     */
    public boolean isCanReporterMessage(){
        int type = canDetectorMessageType();
        return isCanDetectorMessage() && type >= 0x11 && type<= 0x1f;
    }

    private static final Logger log = LoggerFactory.getLogger(Z21Reply.class);
}
