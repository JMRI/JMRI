package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.CanSystemConnectionMemo;

public class CbusHubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public CbusHubAction() {
        super("CBUS Hub Control",
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            CbusHubPane.class.getName(),
            jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }

}
