//Sprogv4UpdateAction.java
package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogIIUpdateFrame object
 *
 * @author	Andrew crosland Copyright (C) 2004
 * @version	$Revision$
 */
public class Sprogv4UpdateAction extends SprogUpdateAction {

    /**
     *
     */
    private static final long serialVersionUID = 5840047276798366447L;

    public Sprogv4UpdateAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        Object[] options = {"Cancel", "Update"};
        if (1 == JOptionPane.showOptionDialog(null,
                "In order to proceed with a SPROG firmware update"
                + "You must have a valid .hex firmware update file\n"
                + "Are you certain you want to update the SPROG firmware?",
                "SPROG Firmware Update", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
            // create a SprogIIUpdateFrame
            // create a SprogUpdateFrame
            Sprogv4UpdateFrame f = new Sprogv4UpdateFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Sprogv4UpdateAction starting Sprogv4UpdateFrame: Exception: " + ex.toString());
            }
            f.setVisible(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Sprogv4UpdateAction.class.getName());

}


/* @(#)Sprogv4UpdateAction.java */
