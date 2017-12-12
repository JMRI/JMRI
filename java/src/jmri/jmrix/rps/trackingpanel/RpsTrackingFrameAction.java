package jmri.jmrix.rps.trackingpanel;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Swing action to create and register a RpsTrackingFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class RpsTrackingFrameAction extends AbstractAction {

    RpsSystemConnectionMemo memo = null;

    public RpsTrackingFrameAction(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public RpsTrackingFrameAction(RpsSystemConnectionMemo _memo) {
        this("RPS Tracking Display",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RpsTrackingFrame f = new RpsTrackingFrame("RPS Tracking",memo);

        f.initComponents();
        f.setVisible(true);
    }
}
