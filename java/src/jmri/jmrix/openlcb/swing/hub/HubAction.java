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
            String name = check.getUserName();
            if (name.equals("LCC") || name.equals("OpenLCB")) {
                memo = check;
                break;
            }
        }
    }
    
}
