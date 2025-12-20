package jmri.jmrix.nce.ncemon;

import java.awt.Dimension;

import jmri.jmrix.nce.*;
import jmri.jmrix.nce.swing.NcePanelInterface;
import jmri.util.swing.JmriJOptionPane;

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

    /**
     * The minimum frame size for font size 16
     */
    @Override
    public Dimension getMinimumDimension() {
        return new Dimension(700, 500);
    }

    @Override
    public void dispose() {
        // disconnect from the NceTrafficController
        try {
            memo.getNceTrafficController().removeNceListener(this);
        } catch (java.lang.NullPointerException e) {
            log.error("Error on dispose {}", e.toString());
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
            log.error("Unable to start the NCE Command monitor"); // NOI18N
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("DialogMonError"),
                    Bundle.getMessage("DialogMonErrorTitle"),
                    JmriJOptionPane.WARNING_MESSAGE);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NceMonPanel.class);

}
