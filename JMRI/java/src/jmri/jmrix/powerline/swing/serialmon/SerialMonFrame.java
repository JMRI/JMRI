package jmri.jmrix.powerline.swing.serialmon;

import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Frame displaying (and logging) serial command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
@Deprecated
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame(SerialTrafficController tc) {
        super();
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    @Override
    protected String title() {
        return "Powerline Device Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        tc.addSerialListener(this);
    }

    /**
     * Define system-specific help item
     */
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.powerline.serialmon.SerialMonFrame", true);  // NOI18N
    }

    @Override
    public void dispose() {
        tc.removeSerialListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        nextLine(l.toMonitorString(), l.toString());
        return;
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply message and log it
        nextLine(l.toMonitorString(), l.toString());
    }

}
