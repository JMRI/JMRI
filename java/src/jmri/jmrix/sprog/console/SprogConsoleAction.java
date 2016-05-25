// SprogConsoleAction.java
package jmri.jmrix.sprog.console;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogConsoleFrame object
 *
 * @author	Andrew Crosland Copyright (C) 2008
 * @version	$Revision$
 */
public class SprogConsoleAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3259877478074718099L;

    public SprogConsoleAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        SprogConsoleFrame f = new SprogConsoleFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SprogConsoleAction.class.getName());
}


/* @(#)SprogConsoleAction.java */
