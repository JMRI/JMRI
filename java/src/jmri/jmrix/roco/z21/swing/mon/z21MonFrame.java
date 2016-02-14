// XNetMonFrame.java
package jmri.jmrix.roco.z21.swing.mon;

import jmri.jmrix.roco.z21.z21Listener;
import jmri.jmrix.roco.z21.z21Message;
import jmri.jmrix.roco.z21.z21Reply;
import jmri.jmrix.roco.z21.z21SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying (and logging) Z21 messages
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class z21MonFrame extends jmri.jmrix.AbstractMonFrame implements z21Listener {

    /**
     *
     */
    private static final long serialVersionUID = 8216921338328955386L;
    private z21SystemConnectionMemo _memo = null;

    public z21MonFrame() {
        super();
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(z21SystemConnectionMemo.class).get(0);
    }

    public z21MonFrame(z21SystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return "Z21 Traffic";
    }

    // ieee802.15.4 Listener methods
    public void message(z21Message m) {
        nextLine(m.toMonitorString() + "\n", m.toString() + "\n");
    }

    public void reply(z21Reply m) {
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

    private final static Logger log = LoggerFactory.getLogger(z21MonFrame.class.getName());

}
