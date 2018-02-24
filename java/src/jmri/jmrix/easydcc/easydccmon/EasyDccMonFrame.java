package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

/**
 * Frame displaying (and logging) EasyDCC command messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccMonFrame extends jmri.jmrix.AbstractMonFrame implements EasyDccListener {

    private EasyDccSystemConnectionMemo _memo = null;

    public EasyDccMonFrame(EasyDccSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    @Override
    protected String title() {
        return Bundle.getMessage("MonitorXTitle", "EasyDCC");
    }

    @Override
    protected void init() {
        // connect to TrafficController
        _memo.getTrafficController().addEasyDccListener(this);
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeEasyDccListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(EasyDccMessage l) {  // receive a message and log it
        nextLine("cmd: \"" + l.toString() + "\"\n", "");
    }

    @Override
    public synchronized void reply(EasyDccReply l) {  // receive a reply message and log it
        nextLine("rep: \"" + l.toString() + "\"\n", "");
    }

}
