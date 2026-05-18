package jmri.jmrix.dccpp.swing.exrail;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

/**
 * Action to open the DCC-EX EXRAIL Automations window.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailAction extends DCCppSystemConnectionAction {

    private DCCppExrailFrame f = null;

    public DCCppExrailAction(String name, DCCppSystemConnectionMemo memo) {
        super(name, memo);
    }

    public DCCppExrailAction(DCCppSystemConnectionMemo memo) {
        super(Bundle.getMessage("ExrailFrameTitle"), memo);
    }

    public DCCppExrailAction() {
        this(InstanceManager.getNullableDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
            if (memo == null) {
                log.error("connection memo was null!");
                return;
            }
            f = new DCCppExrailFrame(memo);
            DCCppTrafficController tc = memo.getDCCppTrafficController();
            tc.addDCCppListener(DCCppInterface.FEEDBACK, f);
            f.initComponents();
            tc.sendDCCppMessage(DCCppMessage.makeAutomationIDsMsg(), f);
            f.setVisible(true);
        }
        f.setExtendedState(Frame.NORMAL);
        f.toFront();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailAction.class);
}
