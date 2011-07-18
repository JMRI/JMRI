// MergTrafficController.java

package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.*;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.cbus.CbusConstants;

/**
 * Traffic controller for the MERG varient of the GridConnect protocol.
 * <P>
 * MERG CAN-RS/CAN-USB uses messages transmitted
 * as an ASCII string of up to 24 characters of the form:
 *      :ShhhhNd0d1d2d3d4d5d6d7;
 * The S indicates a standard CAN frame
 * hhhh is the two byte header (11 useful bits), left justified on send to adapter
 * N or R indicates a normal or remote frame
 * d0 - d7 are the (up to) 8 data bytes
 *
 * @author          Andrew Crosland Copyright (C) 2008
 * @version			$Revision$
 */
public class MergTrafficController extends GcTrafficController {
    
    public MergTrafficController() {
        super();
    }

    /**
     * static function returning the CanTrafficController instance to use.
     * @return The registered SprogTrafficController instance for general use,
     *         if need be creating one.
     */
    static public TrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new MergTrafficController object");
            self = new MergTrafficController();
            _canid = CbusConstants.DEFAULT_STANDARD_ID;  // default value;
            // Get CAN ID from configuration option
            try {
                _canid = Integer.parseInt(jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.SerialDriverAdapter.instance().getCurrentOption2Setting());
            } catch (Exception e) {
                log.error("Cannot parse CAN ID - check your preference settings "+e);
                log.error("Now using default CAN ID");
            }
        }
        return self;
    }

    // New message for hardware protocol
    protected AbstractMRMessage newMessage() { 
        log.debug("New MergMessage created");
        MergMessage msg = new MergMessage();
        return msg;
    }

    /** 
     * Make a CanReply from a MergReply reply
     */
    public CanReply decodeFromHardware(AbstractMRReply m) {
        log.warn("Decoding from hardware");
	    MergReply gc = (MergReply)m;
        CanReply ret = gc.createReply();
        return ret;
    }

    /**
     * Encode a CanMessage for the hardware
     */
    public AbstractMRMessage encodeForHardware(CanMessage m) {
        //log.debug("Encoding for hardware");
	    MergMessage ret = new MergMessage(m);

        return ret;
    }

    // New reply from hardware
    protected AbstractMRReply newReply() { 
        log.debug("New MergReply created");
        MergReply reply = new MergReply();
        return reply;
    }
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergTrafficController.class.getName());
}


/* @(#)MergTrafficController.java */

