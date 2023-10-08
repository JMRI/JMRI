package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.util.swing.JmriJOptionPane;

/**
 * Swing action to create and register a SprogIIUpdateFrame object.
 *
 * @author Andrew crosland Copyright (C) 2004
 */
public class SprogIIUpdateAction extends SprogUpdateAction {

    public SprogIIUpdateAction(String s,SprogSystemConnectionMemo memo) {
        super(s,memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object[] options = {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonContinue")};
        if (1 == // array position 1 ButtonContinue
            JmriJOptionPane.showOptionDialog(null,
                Bundle.getMessage("SprogXUpdateDialogString"),
                Bundle.getMessage("SprogXFirmwareUpdate"), JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
            // create a SprogIIUpdateFrame
            SprogIIUpdateFrame f = new SprogIIUpdateFrame(_memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: {}", ex.toString());
            }
            f.setVisible(true);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogIIUpdateAction.class);

}
