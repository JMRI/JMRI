package jmri.jmrix.cmri.serial.diagnostic;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a DiagnosticFrame object
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public class DiagnosticAction extends AbstractAction {

    CMRISystemConnectionMemo _memo = null;

    public DiagnosticAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public DiagnosticAction(CMRISystemConnectionMemo memo) {
        this("Run C/MRI Diagnostic",memo);
    }

    public void actionPerformed(ActionEvent e) {
        DiagnosticFrame f = new DiagnosticFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(DiagnosticAction.class.getName());
}
