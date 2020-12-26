package jmri.jmrix.loconet.locomon;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class LocoMonPaneAction extends LnNamedPaneAction {

    public LocoMonPaneAction() {
        super(Bundle.getMessage("MenuItemLocoNetMonitor"),
                new JmriJFrameInterface(),
                LocoMonPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}
