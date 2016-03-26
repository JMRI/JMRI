// EasyDccMonFrame.java
package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccTrafficController;

/**
 * Frame displaying (and logging) EasyDcc command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class EasyDccMonFrame extends jmri.jmrix.AbstractMonFrame implements EasyDccListener {

    /**
     *
     */
    private static final long serialVersionUID = 7982331955917888988L;

    public EasyDccMonFrame() {
        super();
    }

    protected String title() {
        return "EasyDcc Command Monitor";
    }

    protected void init() {
        // connect to TrafficController
        EasyDccTrafficController.instance().addEasyDccListener(this);
    }

    public void dispose() {
        EasyDccTrafficController.instance().removeEasyDccListener(this);
        super.dispose();
    }

    public synchronized void message(EasyDccMessage l) {  // receive a message and log it
        nextLine("cmd: \"" + l.toString() + "\"\n", "");
    }

    public synchronized void reply(EasyDccReply l) {  // receive a reply message and log it
        nextLine("rep: \"" + l.toString() + "\"\n", "");
    }

}
