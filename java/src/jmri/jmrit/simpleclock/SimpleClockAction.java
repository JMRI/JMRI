package jmri.jmrit.simpleclock;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a SimpleClockFrame object
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2024
 */
public class SimpleClockAction extends JmriAbstractAction {

    public SimpleClockAction(String s) {
        super(s);
    }

    public SimpleClockAction() {
        super("Fast Clock Setup");
    }

    public SimpleClockAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SimpleClockAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SimpleClockFrame f = new SimpleClockFrame();
        try {
            f.initComponents();
        } catch (Exception E) {
            log.error("Exception in Simple Clock: {}", e);
        }
        f.setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used here

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleClockAction.class);
}
