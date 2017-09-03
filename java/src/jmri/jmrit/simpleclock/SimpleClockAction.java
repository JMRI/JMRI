package jmri.jmrit.simpleclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SimpleClockFrame object
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public class SimpleClockAction extends AbstractAction {

    public SimpleClockAction(String s) {
        super(s);
    }

    public SimpleClockAction() {
        super("Fast Clock Setup");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SimpleClockFrame f = new SimpleClockFrame();
        try {
            f.initComponents();
        } catch (Exception E) {
            log.error("Exception in Simple Clock: " + e);
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleClockAction.class);
}
