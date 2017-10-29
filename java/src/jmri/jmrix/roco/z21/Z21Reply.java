package jmri.jmrix.roco.z21;

import jmri.jmrix.AbstractMRReply;

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

    public String toMonitorString() {
        switch(getOpCode()){
           case 0x0010:
               int serialNo = getElement(4) + (getElement(5) << 8) +
                     (getElement(6) << 16) + (getElement(7) << 24);
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
           default:
        }

        return toString();
    }

    // handle XpressNet replies tunneled in Z21 messages
    boolean isXPressNetTunnelMessage() {
        return (getOpCode() == 0x0040);
    }

    jmri.jmrix.lenz.XNetReply getXNetReply() {
        jmri.jmrix.lenz.XNetReply xnr = null;
        if (isXPressNetTunnelMessage()) {
            int i = 4;
            xnr = new jmri.jmrix.lenz.XNetReply();
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
        return (((getLength() - 4)/13));
    } 

    /**
     * Get a locomotive address from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the locomotive address for the specified entry.
     */
    jmri.DccLocoAddress getRailComLocoAddress(int n){
         int offset = 4+(n*13);
         int address = ((0xff&getElement(offset))<<8)+(0xff&(getElement(offset+1)));
         return new jmri.DccLocoAddress(address,address>=100);
    }

    /**
     * Get the receive counter from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the receive counter for the specified entry.
     */
    int getRailComRcvCount(int n){
         int offset = 6+(n*13); // +2 to get past the address.
         int rcvcount = ((0xff&getElement(offset))<<24) +
                       ((0xff&(getElement(offset+1))<<16) + 
                       ((0xff&getElement(offset+2))<<8) + 
                       (0xff&(getElement(offset+3))));
         return rcvcount;
    }

    /**
     * Get the error counter from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the error counter for the specified entry.
     */
    int getRailComErrCount(int n){
         int offset = 10+(n*13); // +6 to get past the address and rcv count.
         int errorcount = ((0xff&getElement(offset))<<24) +
                       ((0xff&(getElement(offset+1))<<16) + 
                       ((0xff&getElement(offset+2))<<8) + 
                       (0xff&(getElement(offset+3))));
         return errorcount;
    }

    /**
     * Get the speed value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the error counter for the specified entry.
     */
    int getRailComSpeed(int n){
         int offset = 14+(n*13); //+10 to get past the address and counters.
         return (0xff&(getElement(offset)));
    }

    /**
     * Get the options value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the options for the specified entry.
     */
    int getRailComOptions(int n){
         int offset = 15+(n*13); //+10 to get past the address,counter,speed.
         return (0xff&(getElement(offset)));
    }

    /**
     * Get the Temperature value from an entry in a railcom message.
     *
     * @param n the entry to get the address from.
     * @return the temperature for the specified entry.
     */
    int getRailComTemp(int n){
         int offset = 16+(n*13); //+10 to get past the other data.
         return (0xff&(getElement(offset)));
    }

}
