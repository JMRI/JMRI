package jmri.jmrix.sprog.sprogmon;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) Sprog command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SprogMonFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener {

    private SprogSystemConnectionMemo _memo = null;

    public SprogMonFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("MonitorXTitle", "SPROG");
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getSprogTrafficController().addSprogListener(this);
    }

    @Override
    public void dispose() {
        _memo.getSprogTrafficController().removeSprogListener(this);
        super.dispose();
    }

    @Override
    public synchronized void notifyMessage(SprogMessage l) { // receive a message and log it
        nextLine("cmd: \"" + l.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");

    }

    @Override
    public synchronized void notifyReply(SprogReply l) { // receive a reply and log it
        nextLine("rep: \"" + l.toString() + "\"\n", "");
        log.debug("reply heard");
    }

    private final static Logger log = LoggerFactory.getLogger(SprogMonFrame.class);

}
