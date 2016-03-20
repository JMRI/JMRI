package jmri.jmrix.roco.z21.swing.mon;

import jmri.jmrix.roco.z21.Z21Listener;
import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21Reply;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;

/**
 * Frame displaying (and logging) Z21 messages
 *
 * @author Paul Bender Copyright (C) 2013
 */
public class Z21MonFrame extends jmri.jmrix.AbstractMonFrame implements Z21Listener {

    private Z21SystemConnectionMemo _memo = null;

    public Z21MonFrame() {
        super();
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(Z21SystemConnectionMemo.class).get(0);
    }

    public Z21MonFrame(Z21SystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return "Z21 Traffic";
    }

    // ieee802.15.4 Listener methods
    public void message(Z21Message m) {
        nextLine(m.toMonitorString() + "\n", m.toString() + "\n");
    }

    public void reply(Z21Reply m) {
        nextLine(m.toMonitorString() + "\n", m.toString() + "\n");
    }

    public void dispose() {
        _memo.getTrafficController().removez21Listener(this);
        // unwind swing
        super.dispose();
    }

    protected void init() {
        // connect to the TrafficController
        _memo.getTrafficController().addz21Listener(this);
    }

}
