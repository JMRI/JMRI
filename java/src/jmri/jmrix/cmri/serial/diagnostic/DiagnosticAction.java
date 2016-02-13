// DiagnosticAction.java
package jmri.jmrix.cmri.serial.diagnostic;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a DiagnosticFrame object
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @version	$Revision$
 */
public class DiagnosticAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1241617418393868830L;

    public DiagnosticAction(String s) {
        super(s);
    }

    public DiagnosticAction() {
        this("Run C/MRI Diagnostic");
    }

    public void actionPerformed(ActionEvent e) {
        DiagnosticFrame f = new DiagnosticFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(DiagnosticAction.class.getName());
}

/* @(#)DiagnosticAction.java */
