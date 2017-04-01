package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccTrafficController;

/**
 * Frame displaying (and logging) EasyDcc command messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class EasyDccMonFrame extends jmri.jmrix.AbstractMonFrame implements EasyDccListener {

    public EasyDccMonFrame() {
        super();
    }

    @Override
    protected String title() {
        return "EasyDcc Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        EasyDccTrafficController.instance().addEasyDccListener(this);
    }

    @Override
    public void dispose() {
        EasyDccTrafficController.instance().removeEasyDccListener(this);
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
