package jmri.jmrix.openlcb.swing.hub;

import jmri.jmrix.can.CanSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL)
public class HubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public HubAction() {
        super("Openlcb Hub Control",
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                HubPane.class.getName(),
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }

}
