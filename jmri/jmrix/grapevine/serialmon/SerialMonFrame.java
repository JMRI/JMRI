// SerialMonFrame.java

package jmri.jmrix.grapevine.serialmon;

import jmri.jmrix.grapevine.SerialListener;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2001, 2006, 2007
 * @version         $Revision: 1.1 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Grapevine Serial Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) log.debug("Message: "+l.toString());
        // check for valid length
        if (l.getNumDataElements() < 4) {
            nextLine("Truncated message of length "+l.getNumDataElements()+": "+l.toString()+"\n",
                            l.toString());
            return;
        } else
            nextLine("Message "+l.format()+"\n", l.toString());
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        if (log.isDebugEnabled()) log.debug("Reply: "+l.toString());
        // check for valid length
        if (l.getNumDataElements() < 4) {
            nextLine("Truncated reply of length "+l.getNumDataElements()+": "+l.toString()+"\n",
                            l.toString());
            return;
        } else
            nextLine("    Reply "+l.format()+"\n", l.toString());
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}
