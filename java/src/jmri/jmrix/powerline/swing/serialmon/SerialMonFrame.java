 // SerialMonFrame.java
package jmri.jmrix.powerline.swing.serialmon;

import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) serial command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version $Revision$
 */
@Deprecated
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    /**
     *
     */
    private static final long serialVersionUID = -919574536509743467L;

    public SerialMonFrame(SerialTrafficController tc) {
        super();
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    protected String title() {
        return "Powerline Device Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        tc.addSerialListener(this);
    }

    public void dispose() {
        tc.removeSerialListener(this);
        super.dispose();
    }

    public synchronized void message(SerialMessage l) {  // receive a message and log it
        nextLine(l.toMonitorString(), l.toString());
        return;
    }

    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        nextLine(l.toMonitorString(), l.toString());
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonFrame.class.getName());

}
