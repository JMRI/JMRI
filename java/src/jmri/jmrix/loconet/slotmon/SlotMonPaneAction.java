package jmri.jmrix.loconet.slotmon;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class SlotMonPaneAction extends LnNamedPaneAction {

    public SlotMonPaneAction() {
        super(Bundle.getMessage("MenuItemSlotMonitor"),
                new JmriJFrameInterface(),
                SlotMonPane.class.getName(),
                InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
    }

}
