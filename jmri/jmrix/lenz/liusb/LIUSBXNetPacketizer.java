/**
 * LIUSBXNetPacketizer.java
 */

package jmri.jmrix.lenz.liusb;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;

import java.util.Vector;

import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * This is an extention of the XNetPacketizer to handle the device 
 * specific requirements of the LIUSB.
 * <P>
 * In particular, LIUSBXNetPacketizer adds functions to add and remove the 
 * 0xFF 0xFE or 0xFF 0xFD bytes that appear prior to any message read in.
 *
 * @author		Paul Bender  Copyright (C) 2005
 * @version 		$Revision: 1.0 $
 *
 */
public class LIUSBXNetPacketizer extends XNetPacketizer {

	public LIUSBXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        	super(pCommandStation);
		log.debug("Loading LIUSB Extention to XNetPacketizer");
    	}

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
     protected int addHeaderToOutput(byte[] msg, jmri.jmrix.AbstractMRMessage m)     {
	if(log.isDebugEnabled()) log.debug("Appending 0xFF 0xFE to start of outgoing message");
	msg[0]=(byte) 0xFF;
	msg[1]=(byte) 0xFE;
        return 2;
    }

    /**
     * Determine how much many bytes the entire
     * message will take, including space for header and trailer
     * @param m  The message to be sent
     * @return Number of bytes
     */
    protected int lengthOfByteStream(jmri.jmrix.AbstractMRMessage m) {
        int len = m.getNumDataElements() + 2;
        int cr = 0;
        if (! m.isBinary()) cr = 1;  // space for return
        return len+cr;
    }


        /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg message to fill
     * @param istream character source. 
     * @throws IOException when presented by the input source.
     */
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
	if(log.isDebugEnabled()) log.debug("loading characters from port");
        for (i = 0; i < msg.maxSize; i++) {
            byte char1 = istream.readByte();
            // This is a test for the LIUSB device
            while((i==0) && ((char1 & 0xF0)==0xF0)) {
                if((char1&0xFF) !=0xF0 && (char1&0xFF)!=0xF2){
                   //  toss this byte and read the next one
                   char1 = istream.readByte();
                }
            }
            msg.setElement(i, char1 &0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LIUSBXNetPacketizer.class.getName());
}

/* @(#)LIUSBXNetPacketizer.java */

