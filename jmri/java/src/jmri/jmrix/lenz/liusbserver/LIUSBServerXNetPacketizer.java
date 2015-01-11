/**
 * LIUSBXNetPacketizer.java
 */

package jmri.jmrix.lenz.liusbserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.lenz.XNetPacketizer;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;


/**
 * This is an extension of the XNetPacketizer to handle the device 
 * specific requirements of the LIUSB Server.
 * <P>
 * In particular, LIUSBServerXNetPacketizer counts the number of commands received.
 * @author		Paul Bender, Copyright (C) 2009
 * @version 	$Revision$
 *
 */
public class LIUSBServerXNetPacketizer extends XNetPacketizer {

	public LIUSBServerXNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        	super(pCommandStation);
		if(log.isDebugEnabled()) log.debug("Loading LIUSB Server Extension to XNetPacketizer");
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
                        ostream.write((m+"\n\r").getBytes(java.nio.charset.Charset.forName("UTF-8")));
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
                // start the recovery process if an exception occurs.
                portWarn(e);
                controller.recover();
        }
     }




static Logger log = LoggerFactory.getLogger(LIUSBServerXNetPacketizer.class.getName());
}

/* @(#)XnTcpXNetPacketizer.java */

