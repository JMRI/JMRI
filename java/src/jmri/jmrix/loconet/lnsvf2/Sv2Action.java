package jmri.jmrix.loconet.lnsvf2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locomon.LocoMonPane;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class Sv2Action extends LnNamedPaneAction {

    public Sv2Action() {
        super(Bundle.getMessage("MenuItemLocoNetMonitor"),
                new JmriJFrameInterface(),
                LocoMonPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}
