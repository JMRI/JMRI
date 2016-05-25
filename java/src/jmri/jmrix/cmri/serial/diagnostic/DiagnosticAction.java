// DiagnosticAction.java

package jmri.jmrix.cmri.serial.diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			DiagnosticFrame object
 *
 * @author                  Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 17977 $
 */
public class DiagnosticAction 	extends AbstractAction {

    public DiagnosticAction(String s) { super(s);}

    public DiagnosticAction() {
        this("Run C/MRI Diagnostic");
    }

    public void actionPerformed(ActionEvent e) {
        DiagnosticFrame f = new DiagnosticFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }

   static Logger log = LoggerFactory.getLogger(DiagnosticAction.class.getName());
}

/* @(#)DiagnosticAction.java */
