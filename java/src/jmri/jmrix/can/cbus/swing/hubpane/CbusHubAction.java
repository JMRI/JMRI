package jmri.jmrix.can.cbus.swing.hubpane;

import jmri.jmrix.can.CanSystemConnectionMemo;

public class CbusHubAction extends jmri.jmrix.can.swing.CanNamedPaneAction {

    public CbusHubAction() {
        super("CBUS Hub Control",
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            CbusHubPane.class.getName(),
            memo);
    }

    // we have to locate the (only) CBUS CanSystemConnectionMemo
    // before we can call the super constructors.
    private static CanSystemConnectionMemo memo;
    
    static {        
        var memos = jmri.InstanceManager.getList(CanSystemConnectionMemo.class);
        for (CanSystemConnectionMemo check : memos) {
            if (check.provides(jmri.jmrix.can.cbus.CbusPreferences.class)) {
                memo = check;
                break;
            }
        }
        // if not found above
        if (memo == null) {
            memo = jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class);
        }
        System.err.println("memo "+memo.getUserName());
    }
}
