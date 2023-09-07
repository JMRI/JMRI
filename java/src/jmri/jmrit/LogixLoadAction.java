package jmri.jmrit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Provide an action to allow Logixs to be loaded disabled when panel file is
 * loaded
 *
 * @author Dave Duchamp Copyright (C) 2007
 */
public class LogixLoadAction extends AbstractAction {

    public LogixLoadAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JPanel _who;

    @Override
    public void actionPerformed(ActionEvent e) {
        // Set option to force Logixs to be loaded disabled

        Object[] options = {"Disable",
            "Enable"}; // TODO I18N

        int retval = JmriJOptionPane.showOptionDialog(_who, Bundle.getMessage("LogixDisabledMessage"), Bundle.getMessage("DebugOption"),
                JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (retval != 0) { // not array position 0 for Disable or Dialog closed
            InstanceManager.getDefault(jmri.LogixManager.class).setLoadDisabled(false);
            log.info("Requested load Logixs enabled via Debug menu.");
        } else {
            InstanceManager.getDefault(jmri.LogixManager.class).setLoadDisabled(true);
            log.info("Requested load Logixs diabled via Debug menu.");
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixLoadAction.class);

}
