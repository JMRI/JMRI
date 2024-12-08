package jmri.jmrix.openlcb.swing.hub;

import jmri.util.swing.sdi.JmriJFrameInterface;
import jmri.util.swing.WindowInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;

public class HubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public HubAction() {
        super("LCC Hub Control",
                new JmriJFrameInterface(),
                HubPane.class.getName(),
                jmri.InstanceManager.getNullableDefault(CanSystemConnectionMemo.class));
    }

    public HubAction(String name, WindowInterface  iface) {
        super(name,
                iface,
                HubPane.class.getName(),
                jmri.InstanceManager.getNullableDefault(CanSystemConnectionMemo.class));
    }

}
