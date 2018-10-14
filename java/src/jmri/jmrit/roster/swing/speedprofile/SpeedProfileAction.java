package jmri.jmrit.roster.swing.speedprofile;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register the Add Entry Exit Pair
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SpeedProfileAction extends JmriAbstractAction {

    public SpeedProfileAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SpeedProfileAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SpeedProfileAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SpeedProfileFrame f = new SpeedProfileFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used by this classes actionPerformed, not migrated to new form yet

    private final static Logger log = LoggerFactory.getLogger(SpeedProfileAction.class);
}
