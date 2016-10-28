package jmri.jmrix.rps.swing.debugger;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a DisplayFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class DebuggerAction extends AbstractAction {

    public DebuggerAction(String s) {
        super(s);
    }

    public DebuggerAction() {
        this("RPS Debugger Window");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
        DebuggerFrame f = new DebuggerFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("starting frame: Exception: " + ex.toString());
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(DebuggerAction.class.getName());

}
