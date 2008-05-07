 // SerialMonFrame.java

package jmri.jmrix.powerline.serialmon;

import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;

import jmri.jmrix.powerline.X10Sequence;

/**
 * Frame displaying (and logging) serial command messages
 * @author	    Bob Jacobsen   Copyright (C) 2001, 2006, 2007, 2008
 * @version         $Revision: 1.11 $
 */

public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    protected String title() { return "Powerline Device Command Monitor"; }

    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
        super.dispose();
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        nextLine(l.toMonitorString(),l.toString());
        return;
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        nextLine(l.toMonitorString(), l.toString());
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialMonFrame.class.getName());

}
