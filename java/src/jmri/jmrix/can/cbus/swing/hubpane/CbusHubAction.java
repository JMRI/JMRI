package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.CanSystemConnectionMemo;

public class CbusHubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public CbusHubAction() {
        super("CBUS Hub Control",
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            CbusHubPane.class.getName(),
            jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
    }

    // we have to locate the (only) CBUS CanSystemConnectionMemo
    // before we can call the super constructors.
    private static CanSystemConnectionMemo memo;
    
    static {        
        var memos = jmri.InstanceManager.getList(CanSystemConnectionMemo.class);
        for (CanSystemConnectionMemo check : memos) {
            String name = check.getUserName();
            if (name.equals("MERG")) {
                memo = check;
                break;
            }
        }
    }
}
