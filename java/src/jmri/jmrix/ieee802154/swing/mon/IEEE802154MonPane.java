package jmri.jmrix.ieee802154.swing.mon;

import jmri.jmrix.ieee802154.IEEE802154Listener;
import jmri.jmrix.ieee802154.IEEE802154Message;
import jmri.jmrix.ieee802154.IEEE802154Reply;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;

/**
 * Pane displaying (and logging) IEEE 802.15.4 messages
 *
 * @author Paul Bender Copyright (C) 2013,2018
 */
public class IEEE802154MonPane extends jmri.jmrix.AbstractMonPane implements IEEE802154Listener {

    private IEEE802154SystemConnectionMemo _memo = null;

    public IEEE802154MonPane() {
        super();
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MonFrameTitle");
    }

    // ieee802.15.4 Listener methods
    @Override
    public void message(IEEE802154Message m) {
        logMessage(m);
    }

    @Override
    public void reply(IEEE802154Reply m) {
        logMessage(m);
    }

    @Override
    public void dispose() {
        _memo.getTrafficController().removeIEEE802154Listener(this);
        // unwind swing
        super.dispose();
    }

    @Override
    protected void init() {
    }

    @Override
    public void initContext(Object context) {
       if (context instanceof IEEE802154SystemConnectionMemo) {
            _memo = (IEEE802154SystemConnectionMemo) context;
            // connect to the TrafficController
            _memo.getTrafficController().addIEEE802154Listener(this);
       }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MonFrameTitle"), IEEE802154MonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(IEEE802154SystemConnectionMemo.class));
        }
    }

}
