package jmri.jmrix.grapevine.serialmon;

import jmri.jmrix.grapevine.SerialListener;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) serial command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements SerialListener {

    public SerialMonFrame() {
        super();
    }

    @Override
    protected String title() {
        return "Grapevine Serial Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        SerialTrafficController.instance().addSerialListener(this);
    }

    /**
     * Define system-specific help item
     */
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.grapevine.serialmon.SerialMonFrame", true);  // NOI18N
    }

    @Override
    public void dispose() {
        SerialTrafficController.instance().removeSerialListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SerialMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) {
            log.debug("Message: " + l.toString());
        }
        nextLine("M: " + l.format() + "\n", l.toString());
    }

    @Override
    public synchronized void reply(SerialReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) {
            log.debug("Reply: " + l.toString());
        }
        nextLine("R: " + l.format() + "\n", l.toString());
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonFrame.class);

}
