package jmri.jmrix.dccpp.swing.exrail;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;

/**
 * Action to open the DCC-EX EXRAIL Automations window.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailAction extends DCCppSystemConnectionAction {

    private DCCppExrailFrame frame = null;

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
        if (frame == null || !frame.isVisible()) {
            DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
            if (memo == null) {
                log.error("connection memo was null!");
                return;
            }
            frame = new DCCppExrailFrame(memo);
            frame.initComponents();
            frame.setVisible(true);
        }
        frame.setExtendedState(Frame.NORMAL);
        frame.toFront();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppExrailAction.class);
}
