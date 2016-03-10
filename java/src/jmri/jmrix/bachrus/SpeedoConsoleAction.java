// SpeedoConsoleAction.java
package jmri.jmrix.bachrus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SpeedoConsoleFrame object
 *
 * @author	Andrew Crosland Copyright (C) 2010
 * @version	$Revision$
 */
public class SpeedoConsoleAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5455680125546606150L;

    public SpeedoConsoleAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        SpeedoConsoleFrame f = new SpeedoConsoleFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleAction.class.getName());
}


/* @(#)SpeedoConsoleAction.java */
