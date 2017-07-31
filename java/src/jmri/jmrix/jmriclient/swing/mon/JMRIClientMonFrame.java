package jmri.jmrix.jmriclient.swing.mon;

import jmri.jmrix.jmriclient.JMRIClientListener;
import jmri.jmrix.jmriclient.JMRIClientMessage;
import jmri.jmrix.jmriclient.JMRIClientReply;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;

/**
 * Frame displaying (and logging) JMRIClient command messages
 *
 * @author Bob Jacobsen Copyright (C) 2008
  */
public class JMRIClientMonFrame extends jmri.jmrix.AbstractMonFrame implements JMRIClientListener {

    protected JMRIClientTrafficController tc = null;

    public JMRIClientMonFrame(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        super();
        tc = memo.getJMRIClientTrafficController();
    }

    @Override
    protected String title() {
        return "JMRIClient Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        tc.addJMRIClientListener(this);
    }

    @Override
    public void dispose() {
        tc.removeJMRIClientListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(JMRIClientMessage l) {  // receive a message and log it

        nextLine("cmd: " + l.toString(), "");
    }

    @Override
    public synchronized void reply(JMRIClientReply l) {  // receive a reply message and log it
        nextLine("rep: " + l.toString(), "");
    }

}
