package jmri.jmrix.tams.swing.monitor;

import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import jmri.jmrix.tams.TamsListener;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsReply;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.swing.TamsPanelInterface;

/**
 * Swing action to create and register a MonFrame object.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsMonPane extends jmri.jmrix.AbstractMonPane implements TamsListener, TamsPanelInterface {

    private TamsMessage tm; // Keeping a local copy of the latest TamsMessage for helping with decoding

    public TamsMonPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("CommandMonitor");
    }

    @Override
    public void dispose() {
        // disconnect from the TamsTrafficController
        memo.getTrafficController().removeTamsListener(this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public void init() {
    }

    TamsSystemConnectionMemo memo;

    JCheckBox disablePollingCheckBox = new JCheckBox();

    @Override
    public void initContext(Object context) {
        if (context instanceof TamsSystemConnectionMemo) {
            initComponents((TamsSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(TamsSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the TamsTrafficController
        memo.getTrafficController().addTamsListener(this);
        disablePollingCheckBox.setSelected(memo.getTrafficController().getPollQueueDisabled());
    }

    @Override
    public void initComponents() {
        super.initComponents();
        JPanel check = new JPanel();
        disablePollingCheckBox.setText(Bundle.getMessage("DisablePollingBoxLabel"));
        disablePollingCheckBox.setVisible(true);
        disablePollingCheckBox.setToolTipText(Bundle.getMessage("DisablePollingToolTip"));
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

    @Override
    public synchronized void message(TamsMessage tm) {  // receive a message and log it
        if (tm.isBinary()) {
            logMessage("Binary cmd: ", tm);
        } else {
            logMessage("ASCII cmd: ", tm);
        }
    }

    @Override
    public synchronized void reply(TamsReply l) {  // receive a reply message and log it
        String raw = "";
        for (int i = 0; i < l.getNumDataElements(); i++) {
            if (i > 0) {
                raw += " ";
            }
            raw = jmri.util.StringUtil.appendTwoHexFromInt(l.getElement(i) & 0xFF, raw);
        }
        if (l.isUnsolicited()) {
            logMessage("msg: ", l);
        } else {
            if (tm.isBinary()){
                logMessage("Binary rep: ", l);
            } else {
                logMessage("ASCII rep: " , l);
            }
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.tams.swing.TamsNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("CommandMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    TamsMonPane.class.getName(),
                    jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

}
