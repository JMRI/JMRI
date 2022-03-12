package jmri.jmrix.loconet.clockmon;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class ClockMonPaneAction extends LnNamedPaneAction {

    public ClockMonPaneAction() {
        super(Bundle.getMessage("MenuItemClockMon"),
                new JmriJFrameInterface(),
                ClockMonPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}
