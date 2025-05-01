package jmri.jmrix.openlcb.swing.hub;

import jmri.util.swing.sdi.JmriJFrameInterface;
import jmri.util.swing.WindowInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;

public class HubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public HubAction() {
        super("LCC Hub Control",
                new JmriJFrameInterface(),
                HubPane.class.getName(),
                memo);
    }

    public HubAction(String name, WindowInterface  iface) {
        super(name,
                iface,
                HubPane.class.getName(),
                memo);
    }

    // we have to locate the (only) LCC or OpenLCB CanSystemConnectionMemo
    // before we can call the super constructors.
    private static CanSystemConnectionMemo memo;
    
    static {        
        var memos = jmri.InstanceManager.getList(CanSystemConnectionMemo.class);
        for (CanSystemConnectionMemo check : memos) {
            if (check.provides(org.openlcb.OlcbInterface.class)) {
                memo = check;
                break;  // we're taking the first one
            }
        }
        // if not found above
        if (memo == null) {
            memo = jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class);
        }
    }
    
}
