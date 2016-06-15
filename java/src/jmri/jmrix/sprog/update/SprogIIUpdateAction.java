package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogIIUpdateFrame object
 *
 * @author	Andrew crosland Copyright (C) 2004
 */
public class SprogIIUpdateAction extends SprogUpdateAction {

    public SprogIIUpdateAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        Object[] options = {"Cancel", "Update"};
        if (1 == JOptionPane.showOptionDialog(null,
                "In order to proceed with a SPROG II firmware update"
                + "You must have a valid .hex firmware update file\n"
                + "Are you certain you want to update the SPROG II firmware?",
                "SPROG II Firmware Update", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
            // create a SprogIIUpdateFrame
            SprogIIUpdateFrame f = new SprogIIUpdateFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: " + ex.toString());
            }
            f.setVisible(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SprogIIUpdateAction.class.getName());

}
