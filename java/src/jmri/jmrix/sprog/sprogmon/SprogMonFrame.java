package jmri.jmrix.sprog.sprogmon;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

/**
 * Frame displaying (and logging) Sprog command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogMonFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener {

    private SprogSystemConnectionMemo _memo = null;;

    public SprogMonFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return "Sprog Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        _memo.getSprogTrafficController().addSprogListener(this);
    }

    public void dispose() {
        _memo.getSprogTrafficController().removeSprogListener(this);
        super.dispose();
    }

    public synchronized void notifyMessage(SprogMessage l) {  // receive a message and log it
        nextLine("cmd: \"" + l.toString() + "\"\n", "");

    }

    public synchronized void notifyReply(SprogReply l) {  // receive a message and log it
        nextLine("rep: \"" + l.toString() + "\"\n", "");

    }

}
