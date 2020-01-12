package jmri.jmrix.acela.acelamon;

import jmri.jmrix.acela.AcelaListener;
import jmri.jmrix.acela.AcelaMessage;
import jmri.jmrix.acela.AcelaReply;

/**
 * Frame displaying (and logging) Acela command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMonFrame extends jmri.jmrix.AbstractMonFrame implements AcelaListener {

    jmri.jmrix.acela.AcelaSystemConnectionMemo _memo = null;

    public AcelaMonFrame(jmri.jmrix.acela.AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("MonitorXTitle", "Acela");
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addAcelaListener(this);
    }

    /**
     * Define system-specific help item
     */
    @Override
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.acela.acelamon.AcelaMonFrame", true); // NOI18N
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeAcelaListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(AcelaMessage l) {  // receive a message and log it
        nextLine("cmd: \"" + l.toString() + "\"\n", "");
    }

    @Override
    public synchronized void reply(AcelaReply l) {  // receive a reply message and log it
        l.setBinary(true);
        nextLine("rep: \"" + l.toString() + "\"\n", "");
    }

}
