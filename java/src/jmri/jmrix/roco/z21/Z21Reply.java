package jmri.jmrix.roco.z21;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.AbstractMRReply;
import jmri.DccLocoAddress;

/**
 * Class for replies in the z21/Z21 protocol.
 * <p>
 * Replies are of the format: 2 bytes length 2 bytes opcode n bytes data
 * <p>
 * numeric data is sent in little endian format.
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Paul Bender Copyright (C) 2014
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
     * @param a Array of bytes to send
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

    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    @Override
    public String toMonitorString() {
        switch(getOpCode()){
           case 0x0010:
               int serialNo = (getElement(4)&0xff) + ((getElement(5)&0xff) << 8)
                        + ((getElement(6)&0xff) << 16) + ((getElement(7)&0xff) << 24);
               return Bundle.getMessage("Z21ReplyStringSerialNo", serialNo);
           case 0x001A:
               int hwversion = getElement(4) + (getElement(5) << 8) +
                         (getElement(6) << 16 ) + (getElement(7) << 24 );
               float swversion = (getElementBCD(8)/100.0f)+
                               (getElementBCD(9))+
                               (getElementBCD(10)*100)+
                               (getElementBCD(11))*10000;
               return Bundle.getMessage("Z21ReplyStringVersion",java.lang.Integer.toHexString(hwversion), swversion);
           case 0x0040:
               return Bundle.getMessage("Z21XpressNetTunnelReply", getXNetReply().toMonitorString());
           case 0x0051:
                return Bundle.getMessage("Z21ReplyBroadcastFlags",Z21MessageUtils.interpretBroadcastFlags(_dataChars));
           case 0x0080:
               int groupIndex = getElement(4) & 0xff;
               int offset = (groupIndex * 10) + 1;
               String[] moduleStatus = new String[10];
               for(int i=0;i<10;i++){
                  moduleStatus[i]= Bundle.getMessage("RMModuleFeedbackStatus",offset + i,
                      Bundle.getMessage("RMModuleContactStatus",1, ((getElement(i+5)&0x01)==0x01)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",2, ((getElement(i+5)&0x02)==0x02)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",3, ((getElement(i+5)&0x04)==0x04)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",4, ((getElement(i+5)&0x08)==0x08)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",5, ((getElement(i+5)&0x10)==0x10)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",6, ((getElement(i+5)&0x20)==0x20)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",7, ((getElement(i+5)&0x40)==0x40)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")),
                      Bundle.getMessage("RMModuleContactStatus",8, ((getElement(i+5)&0x80)==0x80)? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff")));
               }
               return Bundle.getMessage("RMBusFeedbackStatus",groupIndex,
                      moduleStatus[0],moduleStatus[1],moduleStatus[2],
                      moduleStatus[3],moduleStatus[4],moduleStatus[5],
                      moduleStatus[6],moduleStatus[7],moduleStatus[8],
                      moduleStatus[9]);
           case 0x0084:
               int mainCurrent = getSystemDataMainCurrent();
               int progCurrent = getSystemDataProgCurrent();
               int filteredMainCurrent = getSystemDataFilteredMainCurrent();
               int temperature = getSystemDataTemperature();
               int supplyVolts = getSystemDataSupplyVoltage();
               int internalVolts = getSystemDataVCCVoltage();
               int state = getElement(16);
               int extendedState = getElement(17);
               // data bytes 14 and 15 (offset 18 and 19) are reserved.
               return Bundle.getMessage("Z21SystemStateReply",mainCurrent,
                      progCurrent,filteredMainCurrent,temperature,
                      supplyVolts,internalVolts,state,extendedState);
           case 0x00A0:
               return Bundle.getMessage("Z21LocoNetRxReply", getLocoNetMessage().toMonitorString());
           case 0x00A1:
               return Bundle.getMessage("Z21LocoNetTxReply", getLocoNetMessage().toMonitorString());
           case 0x00A2:
               return Bundle.getMessage("Z21LocoNetLanReply", getLocoNetMessage().toMonitorString());
           case 0x0088:
               int entries = getNumRailComDataEntries();
               StringBuilder datastring = new StringBuilder();
               for(int i = 0; i < entries ; i++) {
                   DccLocoAddress address = getRailComLocoAddress(i);
                   int rcvCount = getRailComRcvCount(i);
                   int errorCount = getRailComErrCount(i);
                   int speed = getRailComSpeed(i);
                   int options = getRailComOptions(i);
                   int qos = getRailComQos(i);
                   datastring.append(Bundle.getMessage("Z21_RAILCOM_DATA",address,rcvCount,errorCount,options,speed,qos));
                   datastring.append("\n");
               }
               return Bundle.getMessage("Z21_RAILCOM_DATACHANGED",entries,new String(datastring));
           case 0x00C4:
               int networkID = ( getElement(4)&0xFF) + ((getElement(5)&0xFF) << 8);
               int address = ( getElement(6)&0xFF) + ((getElement(7)&0xFF) << 8);
               int port = ( getElement(8) & 0xFF);
               int type = ( getElement(9) & 0xFF);
               int value1 = (getElement(10)&0xFF) + ((getElement(11)&0xFF) << 8);
               int value2 = (getElement(12)&0xFF) + ((getElement(13)&0xFF) << 8);
               String typeString = "";
               String value1String = "";
               String value2String = "";
               switch(type){
                    case 0x01:
                         typeString = "Input Status";
                         switch(value1){
                           case 0x0000:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_FREE_WITHOUT");
                                break;
                           case 0x0100:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_FREE_WITH");
                                break;
                           case 0x1000:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_BUSY_WITHOUT");
                                break;
                           case 0x1100:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_BUSY_WITH");
                                break;
                           case 0x1201:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_1");
                                break;
                           case 0x1202:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_2");
                                break;
                           case 0x1203:
                                value1String = Bundle.getMessage("Z21_CAN_INPUT_STATUS_OVERLOAD_3");
                                break;
                           default:
                                value1String = "<unknown>";
                         }
                         break;
                    case 0x11: 
                    case 0x12: 
                    case 0x13: 
                    case 0x14: 
                    case 0x15: 
                    case 0x16: 
                    case 0x17: 
                    case 0x18: 
                    case 0x19: 
                    case 0x1A: 
                    case 0x1B: 
                    case 0x1C: 
                    case 0x1D: 
                    case 0x1E: 
                    case 0x1F:
                         typeString = "Occupancy Info";
                         value1String = getCanDetectorLocoAddressString(value1);
                         value2String = getCanDetectorLocoAddressString(value2);
                         break;
                    default:
                         value1String = "" + value1;
                         value2String = "" + value2;
               }
                 
               return Bundle.getMessage("Z21CANDetectorReply",Integer.toHexString(networkID),address,port,typeString,value1String,value2String);

           default:
        }

        return toString();
    }

    // handle XpressNet replies tunneled in Z21 messages
    boolean isXPressNetTunnelMessage() {
        return (getOpCode() == 0x0040);
    }

    Z21XNetReply getXNetReply() {
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
    int getNumRailComDataEntries(){
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
    DccLocoAddress getRailComLocoAddress(int n){
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
    int getRailComRcvCount(int n){
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
    int getRailComErrCount(int n){
         int offset = 10+(n*13); // +10 to get past header, address,and rcv count.
         return Z21MessageUtils.integer16BitFromOffeset(_dataChars,offset);
    }

    /**
     * Get the speed value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the error counter for the specified entry.
     */
    int getRailComSpeed(int n){
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
    int getRailComOptions(int n){
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
    int getRailComQos(int n){
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
    int getSystemDataMainCurrent(){
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
    int getSystemDataProgCurrent(){
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
    int getSystemDataFilteredMainCurrent(){
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
    int getSystemDataTemperature(){
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
    int getSystemDataSupplyVoltage(){
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
    int getSystemDataVCCVoltage(){
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

    jmri.jmrix.loconet.LocoNetMessage getLocoNetMessage() {
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

    // address value is the 16 bits of the two bytes containing the
    // address.  The most significan two bits represent the direction.
    String getCanDetectorLocoAddressString(int addressValue) {
        if(!isCanDetectorMessage()) {
           return "";
        }
        String addressString;
        if(addressValue==0) {
           addressString="end of list";
        } else {
           addressString = "" + (getCanDetectorLocoAddress(addressValue)).toString();
           int direction = (0xC000&addressValue);
           switch (direction) {
              case 0x8000:
                 addressString += " direction forward";
                 break;
              case 0xC000:
                 addressString += " direction reverse";
                 break;
              default:
                 addressString += " direction unknown";
          }
       }
       return addressString;
   }

    // address value is the 16 bits of the two bytes containing the
    // address.  The most significant two bits represent the direction.
    DccLocoAddress getCanDetectorLocoAddress(int addressValue) {
        if(!isCanDetectorMessage()) {
           return null;
        }
        if(addressValue==0) {
           return null;
        } else {
           int locoAddress = (0x3FFF&addressValue);
           return new DccLocoAddress(locoAddress,locoAddress>=100);
        }
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

}
