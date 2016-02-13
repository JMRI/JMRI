/**
 * TamsMonPane.java
 *
 * Description:	Swing action to create and register a MonFrame object
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version
 */
package jmri.jmrix.tams.swing.monitor;

import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import jmri.jmrix.tams.TamsListener;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsReply;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.swing.TamsPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TamsMonPane extends jmri.jmrix.AbstractMonPane implements TamsListener, TamsPanelInterface {

    /**
     *
     */
    private static final long serialVersionUID = -4164141037445448914L;

    public TamsMonPane() {
        super();
    }

    public String getHelpTarget() {
        return null;
    }

    public String getTitle() {
        return ResourceBundle.getBundle("jmri.jmrix.tams.TamsBundle").getString("CommandMonitor");
    }

    public void dispose() {
        // disconnect from the TamsTrafficController
        memo.getTrafficController().removeTamsListener(this);
        // and unwind swing
        super.dispose();
    }

    public void init() {
    }

    TamsSystemConnectionMemo memo;

    JCheckBox disablePollingCheckBox = new JCheckBox();

    public void initContext(Object context) {
        if (context instanceof TamsSystemConnectionMemo) {
            initComponents((TamsSystemConnectionMemo) context);
        }
    }

    public void initComponents(TamsSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the TamsTrafficController
        memo.getTrafficController().addTamsListener(this);
        disablePollingCheckBox.setSelected(memo.getTrafficController().getPollQueueDisabled());
    }

    public void initComponents() throws Exception {
        super.initComponents();
        JPanel check = new JPanel();
        disablePollingCheckBox.setText("Disable Polling");
        disablePollingCheckBox.setVisible(true);
        disablePollingCheckBox.setToolTipText("If checked, this will disable the polling messages");
        disablePollingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (memo != null) {
                    memo.getTrafficController().setPollQueueDisabled(disablePollingCheckBox.isSelected());
                }
            }
        });
        check.add(disablePollingCheckBox);
        add(check);
    }

    public synchronized void message(TamsMessage l) {  // receive a message and log it
        if (l.isBinary()) {
            nextLine("binary cmd: " + l.toString() + "\n", null);
        } else {
            nextLine("cmd: \"" + l.toString() + "\"\n", null);
        }
    }

    public synchronized void reply(TamsReply l) {  // receive a reply message and log it
        String raw = "";
        for (int i = 0; i < l.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i) & 0xFF, raw);
        }

        if (l.isUnsolicited()) {
            nextLine("msg: \"" + l.toString() + "\"\n", raw);
        } else {
            nextLine("rep: \"" + l.toString() + "\"\n", raw);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.tams.swing.TamsNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 1991332397617036846L;

        public Default() {
            super(ResourceBundle.getBundle("jmri.jmrix.tams.TamsBundle").getString("CommandMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    TamsMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TamsMonPane.class.getName());

}


/* @(#)MonAction.java */
