package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to get SPROG firmware version
 *
 * @author	Andrew Crosland Copyright (C) 2004
 */
public class SprogVersionAction extends AbstractAction {

    private SprogSystemConnectionMemo _memo = null;

    public SprogVersionAction(String s,SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SprogVersionFrame
        SprogVersionFrame f = new SprogVersionFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SprogIIUpdateAction starting SprogIIUpdateFrame: Exception: " + ex.toString());
        }
//        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SprogVersionAction.class);

}
