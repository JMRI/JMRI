// z21Message.java
package jmri.jmrix.roco.z21;

import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractMRMessage;

/**
 * Class for messages in the z21/Z21 protocol.
 * 
 * Messages have the following format:
 * 2 bytes data length.
 * 2 bytes op code.
 * n bytes data.
 *
 * All numeric values are stored in little endian format.
 *
 * Carries a sequence of characters, with accessors.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class z21Message extends AbstractMRMessage {

    public z21Message() {
        super();
        setBinary(true);
    }

    // create a new one
    public z21Message(int i) {
        this();
        if (i < 4) { // minimum length is 2 bytes of length, 2 bytes of opcode.
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
        setLength(i);
    }

    // from an XPressNet message (used for protocol tunneling)
    public z21Message(jmri.jmrix.lenz.XNetMessage m) {
        this(m.getNumDataElements()+4);
        this.setOpCode(0x0040);
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i+4] = m.getElement(i);
        }
    }

    /**
     * This ctor interprets the String as the exact
     * sequence to send, byte-for-byte.
     * @param m
     */
    public z21Message(String m, int l) {
        super(m);
        setBinary(true);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(m);
           if (b.length == 0)
           {
              // no such thing as a zero-length message
              _nDataChars=0;
              _dataChars = null;
              return;
           }
        _nDataChars = b.length;
        _dataChars = new int[_nDataChars];
        for (int i=0; i<b.length; i++) setElement(i, b[i]);
    }

    /**
     * This ctor interprets the byte array as
     * a sequence of characters to send.
     * @param a Array of bytes to send
     */
    public  z21Message(byte[] a, int l) {
        super(String.valueOf(a));
        setBinary(true);
    }

    public void setOpCode(int i) {
       _dataChars[2]=(i&0x00ff);
       _dataChars[3]=((i&0xff00)>>8);
    }

    public int getOpCode() {
        return (_dataChars[2]+(_dataChars[3]<<8));
    }

    public void setLength(int i) {
       _dataChars[0]=(i&0x00ff);
       _dataChars[1]=((i&0xff00)>>8);
    }

    public int getLength() {
        return (_dataChars[0]+(_dataChars[1]<<8));
    }

    /*
     * package protected method to get the _dataChars buffer as bytes.
     * @return byte array containing the low order bits of the  integer 
     *         values in _dataChars.
     */
    byte[] getBuffer() {
         byte byteData[]=new byte[_dataChars.length];
         for(int i=0;i<_dataChars.length;i++)
           byteData[i]=(byte)(0x00ff&_dataChars[i]); 
         return byteData; 
    }

    /*
     * canned messages
     */

   /*
    * @return z21 message for serial number request.
    */
   static z21Message getSerialNumberRequestMessage(){
       z21Message retval = new z21Message(4);
      retval.setElement(0,0x04);
      retval.setElement(1,0x00);
      retval.setElement(2,0x10);
      retval.setElement(3,0x00);
      return retval;
   }

   /*
    * @return z21 message for LAN_LOGOFF request.
    */
   static z21Message getLanLogoffRequestMessage(){
       z21Message retval = new z21Message(4);
      retval.setElement(0,0x04);
      retval.setElement(1,0x00);
      retval.setElement(2,0x30);
      retval.setElement(3,0x00);
      return retval;
   }

   public String toMonitorString() { return toString(); }

    static Logger log = LoggerFactory.getLogger(z21Message.class.getName());

}


/* @(#)z21Message.java */
