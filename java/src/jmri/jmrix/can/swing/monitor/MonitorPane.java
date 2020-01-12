package jmri.jmrix.can.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) CAN frames
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class MonitorPane extends jmri.jmrix.AbstractMonPane implements CanListener, CanPanelInterface {

    public MonitorPane() {
        super();
    }

    CanSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);
        try {
            initComponents();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("CanMonitorTitle");
    }

    @Override
    public void init() {
    }

    @Override
    public synchronized void message(CanMessage l) {  // receive a message and log it
        if (log.isDebugEnabled()) {
            log.debug("Message: " + l.toString());
        }
        logMessage("M: ",l);
    }

    @Override
    public synchronized void reply(CanReply l) {  // receive a reply and log it
        if (log.isDebugEnabled()) {
            log.debug("Reply: " + l.toString());
        }
        logMessage("R: ",l);
    }

    @Override
    public void dispose() {
        // disconnect from the LnTrafficController
        memo.getTrafficController().removeCanListener(this);
        // and unwind swing
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("CanMonitorTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    MonitorPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MonitorPane.class);

}
