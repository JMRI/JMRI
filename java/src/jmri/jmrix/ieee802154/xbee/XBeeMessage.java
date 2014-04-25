// XBeeMessage.java

package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.XBeeRequest;

/**
 * This is a wrapper class for an XBeeAPI XBeeRequest.
 * <P>
 *
 * @author Paul Bender Copyright (C) 2013
 * @version   $Revision$
 */

public class XBeeMessage extends jmri.jmrix.ieee802154.IEEE802154Message {

    private XBeeRequest xbm = null;

    /** Suppress the default ctor, as the
     * length must always be specified
     */
    protected XBeeMessage() {}
    
    public XBeeMessage(int l) {
        super(l);
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public XBeeMessage(String m, int l) {
        super(m,l);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  XBeeMessage(byte[] a, int l) {
        super(String.valueOf(a),l);
    }

    /**
     * This ctor interprets the parameter as an
     * XBeeRequest message.
     * This is the message form that will generally be used by
     * the implementation.
     * @param request an XBeeRequest of bytes to send
     */
    public  XBeeMessage(XBeeRequest request) {
        _nDataChars = request.getFrameData().length;
        _dataChars = request.getFrameData();
        xbm=request;
    }

    public XBeeRequest getXBeeRequest() { return xbm; }
    public void setXBeeRequest(XBeeRequest request) {xbm=request;}

    public String toMonitorString() { 
              if(xbm!=null)
                 return xbm.toString();                    
              else return toString(); 
    }

    public String toString() {
           String s="";
           int packet[]=xbm.getFrameData();
           for(int i=0;i<packet.length;i++)
               s=s+jmri.util.StringUtil.twoHexFromInt(packet[i]);
           return s; }

        /**
         * check whether the message has a valid parity
         */
        @Override
        public boolean checkParity() {
           int len = getNumDataElements();
           int chksum = 0x00;  /* the seed */
           int loop;

           for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
           }
           return ((chksum&0xFF) == getElement(len-1));
        }

       @Override
       public void setParity() {
          int len = getNumDataElements();
          int chksum = 0x00;  /* the seed */
          int loop;

          for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
          }
          setElement(len-1, chksum&0xFF);
       }

      // a few canned messages
      public static XBeeMessage getHardwareVersionRequest(){
          return new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("HV"));
      }

      public static XBeeMessage getFirmwareVersionRequest(){
          return new XBeeMessage(new com.rapplogic.xbee.api.AtCommand("VR"));
      }

      /*
       * Get an XBee Message requesting an digital output pin be turned on or off.
       * @param address XBee Address of the node.  This can be either 
                16 bit or 64 bit.
       * @param pin the DIO Pin on the XBee to use.
       * @param on boolean value stating whether or not the pin should be turned
       *        on (true) or off (false)
       */
      public static XBeeMessage getRemoteDoutMessage(com.rapplogic.xbee.api.XBeeAddress address, int pin, boolean on) {
          int onValue[]={0x5};
          int offValue[]={0x4};
          if(address instanceof com.rapplogic.xbee.api.XBeeAddress16)
             return new XBeeMessage( new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress16)address,"D" + pin, on?onValue:offValue));
          else return new XBeeMessage( new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress64)address,"D" + pin, on?onValue:offValue));
      }

      /*
       * Get an XBee Message requesting the status of a digital IO pin.
       * @param address XBee Address of the node.  This can be either 
                16 bit or 64 bit.
       * @param pin the DIO Pin on the XBee to use.
       */
      public static XBeeMessage getRemoteDoutMessage(com.rapplogic.xbee.api.XBeeAddress address, int pin) {
          if(address instanceof com.rapplogic.xbee.api.XBeeAddress16)
             return new XBeeMessage( new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress16)address,"D" + pin));
          else return new XBeeMessage( new com.rapplogic.xbee.api.RemoteAtRequest((com.rapplogic.xbee.api.XBeeAddress64)address,"D" + pin));
      }

      public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress address, int[] payload){
         if (address instanceof com.rapplogic.xbee.api.XBeeAddress16)
            return getRemoteTransmissionRequest((com.rapplogic.xbee.api.XBeeAddress16) address, payload);
         else return getRemoteTransmissionRequest((com.rapplogic.xbee.api.XBeeAddress64) address, payload);
      }

      public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress16 address, int[] payload) {
          return new XBeeMessage(new com.rapplogic.xbee.api.wpan.TxRequest16(address,payload));
   }

      public static XBeeMessage getRemoteTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress64 address, int[] payload) {
          return new XBeeMessage(new com.rapplogic.xbee.api.wpan.TxRequest64(address,payload));
   }

      public static XBeeMessage getZNetTransmissionRequest(com.rapplogic.xbee.api.XBeeAddress64 address, int[] payload) {
          return new XBeeMessage(new com.rapplogic.xbee.api.zigbee.ZNetTxRequest(address,payload));
   }

}
/* @(#)XBeeMessage.java */
