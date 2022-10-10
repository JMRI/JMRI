package jmri.jmrix.openlcb.swing.hub;

import jmri.jmrix.can.CanSystemConnectionMemo;

public class HubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public HubAction() {
        super("Openlcb Hub Control",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                HubPane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }

}
