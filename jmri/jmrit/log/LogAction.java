// LogAction.java

package jmri.jmrit.log;

import java.awt.event.*;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LogFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2007
 * @version     $Revision: 1.1 $
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
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LogAction.class.getName());
}

/* @(#)LogAction.java */
