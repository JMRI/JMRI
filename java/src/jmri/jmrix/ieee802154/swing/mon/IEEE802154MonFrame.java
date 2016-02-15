// XNetMonFrame.java
package jmri.jmrix.ieee802154.swing.mon;

import jmri.jmrix.ieee802154.IEEE802154Listener;
import jmri.jmrix.ieee802154.IEEE802154Message;
import jmri.jmrix.ieee802154.IEEE802154Reply;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;

/**
 * Frame displaying (and logging) IEEE 802.15.4 messages
 *
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class IEEE802154MonFrame extends jmri.jmrix.AbstractMonFrame implements IEEE802154Listener {

    /**
     *
     */
    private static final long serialVersionUID = -6640585503142002255L;
    private IEEE802154SystemConnectionMemo _memo = null;

    public IEEE802154MonFrame() {
        super();
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(IEEE802154SystemConnectionMemo.class).get(0);
    }

    public IEEE802154MonFrame(IEEE802154SystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return "IEEE 802.15.4 Traffic";
    }

    // ieee802.15.4 Listener methods
    public void message(IEEE802154Message m) {
        nextLine(m.toMonitorString() + "\n", m.toString() + "\n");
    }

    public void reply(IEEE802154Reply m) {
        nextLine(m.toMonitorString() + "\n", m.toString() + "\n");
    }

    public void dispose() {
        _memo.getTrafficController().removeIEEE802154Listener(this);
        // unwind swing
        super.dispose();
    }

    protected void init() {
        // connect to the TrafficController
        _memo.getTrafficController().addIEEE802154Listener(this);
    }

}
