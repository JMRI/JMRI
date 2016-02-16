package jmri.jmrix.roco.z21;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a z21 XpressnetNet
 * connection.
 *
 * @author Paul Bender (C) 2015
 * @version $Revision$
 */
public class z21XNetThrottle extends jmri.jmrix.lenz.XNetThrottle {

    /**
     * Constructor
     */
    public z21XNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController controller) {
        super(memo,controller);
    }

    /**
     * Constructor
     */
    public z21XNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController controller) {
        super(memo,address,controller);
    }

    // Handle incoming messages for This throttle.
    @Override
    public void message(XNetReply l) {
        if (log.isDebugEnabled()) 
            log.debug("Throttle " + getDccAddress() + " - recieved message " + l.toString());
        if(l.getElement(1)==0xEF){
            //This is a Roco specific throttle information message.
            //Data Byte 0 and 1 contain the locomotive address
            int messageaddress=((l.getElement(1)&0x3F) << 8)+l.getElement(2);
            if(messageaddress==getDccAddress()){
               //The message is for this throttle.
               int b2= l.getElement(3); 
               int b3= l.getElement(4); 
               int b4= l.getElement(5); 
               int b5= l.getElement(6); 
               int b6= l.getElement(7); 
               int b7= l.getElement(8); 
               // byte 2 contains the speed step mode and availability 
               // information.
               parseSpeedandAvailability(b2); 
               // byte 3 contains the direction and the speed information
               parseSpeedandAvailability(b3);
               // byte 4 contains flags for whether or not the locomotive
               // is in a double header and for smart search.  These aren't used
               // here.

               // byte 4 and 5 contain function information for F0-F12
               parseFunctionInformation(b4,b5);
               // byte 6 and 7 contain function information for F13-F28
               parseFunctionHighInformation(b6,b7);
               
                // Always end by setting the state to idle
                // (z21 always responds with the same messge, regardless of
                // request).
                requestState = THROTTLEIDLE;
                // and send any queued messages.
                sendQueuedMessage();
           } 
        } else {
            // let the standard XPressNet Throttle have a chance to look 
            // at the message.
            super.message(l);
        }
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(z21XNetThrottle.class.getName());
}
