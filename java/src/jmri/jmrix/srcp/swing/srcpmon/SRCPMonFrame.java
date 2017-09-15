package jmri.jmrix.srcp.swing.srcpmon;

import jmri.jmrix.srcp.SRCPListener;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPReply;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.SRCPTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) SRCP command messages
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class SRCPMonFrame extends jmri.jmrix.AbstractMonFrame implements SRCPListener {

    private SRCPSystemConnectionMemo _memo = null;
    private SRCPTrafficController tc = null;

    public SRCPMonFrame(SRCPSystemConnectionMemo memo) {
        super();
        _memo = memo;
        tc = _memo.getTrafficController();
    }

    @Override
    protected String title() {
        return "SRCP Command Monitor";
    }

    @Override
    protected void init() {
        // connect to TrafficController
        tc.addSRCPListener(this);
    }

    @Override
    public void dispose() {
        SRCPTrafficController.instance().removeSRCPListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(SRCPMessage l) {  // receive a message and log it

        nextLine("cmd: " + l.toString(), "");
    }

    @Override
    public synchronized void reply(SRCPReply l) {  // receive a reply message and log it
        nextLine("reply: " + l.toString() + "\n", "");
    }

    @Override
    public synchronized void reply(jmri.jmrix.srcp.parser.SimpleNode n) {  // receive a reply message and log it
        if (log.isDebugEnabled()) {
            log.debug("reply called with simpleNode " + n.jjtGetValue());
        }
        reply(new SRCPReply(n));
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPMonFrame.class);

}
