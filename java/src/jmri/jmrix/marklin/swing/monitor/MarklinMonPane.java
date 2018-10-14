package jmri.jmrix.marklin.swing.monitor;

import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.swing.MarklinPanelInterface;

/**
 * Swing action to create and register a MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class MarklinMonPane extends jmri.jmrix.AbstractMonPane implements MarklinListener, MarklinPanelInterface {

    public MarklinMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return Bundle.getMessage("MonitorXTitle", memo.getUserName());
        }
        return Bundle.getMessage("MarklinMonitorTitle");
    }

    @Override
    public void dispose() {
        // disconnect from the LnTrafficController
        memo.getTrafficController().removeMarklinListener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    MarklinSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof MarklinSystemConnectionMemo) {
            initComponents((MarklinSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the MarklinTrafficController
        memo.getTrafficController().addMarklinListener(this);
    }

    @Override
    public synchronized void message(MarklinMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            logMessage("binary cmd: ",l);
        } else {
            logMessage("cmd: ",l);
        }
    }

    @Override
    public synchronized void reply(MarklinReply l) {  // receive a reply message and log it
        if (l.isUnsolicited()) {
            logMessage("msg: ",l);
        } else {
            logMessage("rep: ",l);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.marklin.swing.MarklinNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MarklinMonitorTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    MarklinMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(MarklinSystemConnectionMemo.class));
        }
    }

}
