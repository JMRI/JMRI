package jmri.jmrix.nce.ncemon;

import javax.swing.JOptionPane;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.swing.NcePanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a MonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author kcameron Copyright (C) 2011 copied from SerialMonPane.java
 * @author Daniel Boudreau Copyright (C) 2012 added human readable format
 */
public class NceMonPanel extends jmri.jmrix.AbstractMonPane implements NceListener, NcePanelInterface {

    public NceMonPanel() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append("Command Monitor");
        return x.toString();
    }

    @Override
    public void dispose() {
        // disconnect from the NceTrafficController
        try {
            memo.getNceTrafficController().removeNceListener(this);
        } catch (java.lang.NullPointerException e) {
            log.error("Error on dispose " + e.toString());
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    NceSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(NceSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the NceTrafficController
        try {
            memo.getNceTrafficController().addNceListener(this);
        } catch (java.lang.NullPointerException e) {
            log.error("Unable to start the NCE Command monitor");
            JOptionPane.showMessageDialog(null, "An Error has occurred that prevents the NCE Command Monitor from being loaded.\nPlease check the System Console for more information", "No Connection", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public synchronized void message(NceMessage m) {  // receive a message and log it
        if (m.isBinary()) {
            logMessage(m);
        } else {
            logMessage("cmd: ",m);
        }
    }

    @Override
    public synchronized void reply(NceReply r) {  // receive a reply message and log it
        if (r.isUnsolicited()) {
            logMessage("msg: ",r);
        } else {
            logMessage(r);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {

        public Default() {
            super("Nce Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NceMonPanel.class.getName(),
                    jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceMonPanel.class);

}
