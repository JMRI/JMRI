package jmri.jmrix.acela.acelamon;

import jmri.jmrix.acela.AcelaListener;
import jmri.jmrix.acela.AcelaMessage;
import jmri.jmrix.acela.AcelaReply;
import jmri.jmrix.acela.AcelaTrafficController;

/**
 * Frame displaying (and logging) Acela command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMonFrame extends jmri.jmrix.AbstractMonFrame implements AcelaListener {

    jmri.jmrix.acela.AcelaSystemConnectionMemo _memo = null;

    public AcelaMonFrame(jmri.jmrix.acela.AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return "Acela Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addAcelaListener(this);
    }

    public void dispose() {
        _memo.getTrafficController().removeAcelaListener(this);
        super.dispose();
    }

    public synchronized void message(AcelaMessage l) {  // receive a message and log it
        nextLine("cmd: \"" + l.toString() + "\"\n", "");
    }

    public synchronized void reply(AcelaReply l) {  // receive a reply message and log it
        l.setBinary(true);
        nextLine("rep: \"" + l.toString() + "\"\n", "");
    }
}

/* @(#)AcelaMonFrame.java */
