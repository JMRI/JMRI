package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Swing action to create and register a OperationsSetupFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class OperationsSettingsAction extends AbstractAction {

    public OperationsSettingsAction() {
        super(Bundle.getMessage("MenuSetup")); // NOI18N
    }

    static OperationsSettingsFrame operationsSettingsFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Only one OperationsSetupFrame")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (operationsSettingsFrame == null || !operationsSettingsFrame.isVisible()) {
            operationsSettingsFrame = new OperationsSettingsFrame();
            operationsSettingsFrame.initComponents();
        }
        operationsSettingsFrame.setExtendedState(Frame.NORMAL);
        operationsSettingsFrame.setVisible(true); // this also brings the frame into focus
    }
}


