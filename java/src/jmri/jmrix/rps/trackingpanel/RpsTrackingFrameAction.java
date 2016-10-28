package jmri.jmrix.rps.trackingpanel;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RpsTrackingFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class RpsTrackingFrameAction extends AbstractAction {

    public RpsTrackingFrameAction(String s) {
        super(s);
    }

    public RpsTrackingFrameAction() {
        this("RPS Tracking Display");
    }

    public void actionPerformed(ActionEvent e) {
        RpsTrackingFrame f = new RpsTrackingFrame("RPS Tracking");

        f.initComponents();
        f.setVisible(true);
    }
}
