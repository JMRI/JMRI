// LogAction.java

package jmri.jmrit.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LogFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2007
 * @version     $Revision$
 */
public class LogAction extends AbstractAction {

    public LogAction(String s) { 
	    super(s);
    }

    public LogAction() { this("Add Log Entry");}

    public void actionPerformed(ActionEvent e) {
        LogFrame f = new LogFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception in startup", ex);
        }
        f.setVisible(true);
    }
    static Logger log = LoggerFactory.getLogger(LogAction.class.getName());
}

/* @(#)LogAction.java */
