/**
 * LIUSBXNetPacketizer.java
 */

package jmri.jmrix.lenz.liusbethernet;

import jmri.jmrix.lenz.XNetPacketizer;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;


/**
 * This is an extension of the XNetPacketizer to handle the device 
 * specific requirements of the LIUSBEthernet.
 * <P>
 * In particular, LIUSBEthernetXNetPacketizer counts the number of commands received.
 * @author		Paul Bender, Copyright (C) 2009
 * @version 	$Revision$
 *
 */
public class LIUSBEthernetXNetPacketizer extends jmri.jmrix.lenz.liusb.LIUSBXNetPacketizer {

	public LIUSBEthernetXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        	super(pCommandStation);
		if(log.isDebugEnabled()) log.debug("Loading LIUSB Ethernet Extension to XNetPacketizer");
    	}

    /**
     * Actually transmits the next message to the port
     */
     protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (log.isDebugEnabled()) log.debug("forwardToPort message: ["+m+"]");
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(m, mLastSender, this);
        javax.swing.SwingUtilities.invokeLater(r);

        // stream the bytes
        try {
            if (ostream != null) {
                while(m.getRetries()>=0) {
                    if(portReadyToSend(controller)) {
                        ostream.write((m+"\n\r").getBytes());
                        log.debug("written");
                        break;
                    } else if(m.getRetries()>=0) {
                        if (log.isDebugEnabled()) log.debug("Retry message: "+m.toString() +" attempts remaining: " + m.getRetries());
                        m.setRetries(m.getRetries() - 1);
                        try {
                            synchronized(xmtRunnable) {
                                xmtRunnable.wait(m.getTimeout());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // retain if needed later
                            log.error("retry wait interupted");
                        }
                    } else log.warn("sendMessage: port not ready for data sending: " +m.toString());
                }
            } else {  // ostream is null
                // no stream connected
                connectionWarn();
            }
        } catch (Exception e) {
                // TODO Currently there's no port recovery if an exception occurs
                // must restart JMRI to clear xmtException.
                xmtException = true;
            portWarn(e);
        }
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
     @Override
    protected void loadChars(jmri.jmrix.AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
        byte lastbyte=(byte)0xFF;
        if(log.isDebugEnabled()) log.debug("loading characters from port");
        for (i = 0; i < msg.maxSize(); i++) {
            byte char1 = readByteProtected(istream);
            // This is a test for the LIUSB device
            while((i==0) && ((char1 & 0xF0)==0xF0)) {
                if((char1&0xFF) !=0xF0 && (char1&0xFF)!=0xF2){
                   // save this so we can check for unsolicited
                   // messages.
                   lastbyte = char1;
                   //  toss this byte and read the next one
                   char1 = readByteProtected(istream);
                }

            }
            // LIUSB messages are preceeded by 0xFF 0xFE if they are
            // responses to messages we sent.  If they are unrequested
            // information, they are preceeded by 0xFF 0xFD.
            if(lastbyte==(byte)0xFD)
               msg.setUnsolicited();
            msg.setElement(i, char1 &0xFF);
            if (endOfMessage(msg)) {
                break;
            }
        }
    }

static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LIUSBEthernetXNetPacketizer.class.getName());
}

/* @(#)LIUSBEthernetXNetPacketizer.java */

