package jmri.jmrix.pricom.pockettester;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a pricom.pockettester.MonitorAction
 * object
 *
 * @see jmri.jmrix.pricom.pockettester.MonitorFrame
 *
 * @author	Bob Jacobsen Copyright (C) 2002,2004
 */
public abstract class MonitorAction extends AbstractAction {

    public MonitorAction(String s) {
        super(s);
    }

    public MonitorAction() {
        putValue(javax.swing.Action.NAME, Bundle.getMessage("ActionMonitor"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MonitorFrame f = new MonitorFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("starting MonitorFrame caught exception: " + ex.toString());
        }
        connect(f);
        f.setVisible(true);
    }

    abstract void connect(DataListener l);

    private final static Logger log = LoggerFactory.getLogger(MonitorAction.class);

}
