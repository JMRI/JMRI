// DiagnosticAction.java

package jmri.jmrix.cmri.serial.diagnostic;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			DiagnosticFrame object
 *
 * @author                  Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 1.10 $
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

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DiagnosticAction.class.getName());
}

/* @(#)DiagnosticAction.java */
