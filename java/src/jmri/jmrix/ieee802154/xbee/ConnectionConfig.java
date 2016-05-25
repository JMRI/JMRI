// ConnectionConfig.java
package jmri.jmrix.ieee802154.xbee;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.ieee802154.xbee.swing.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version	$Revision$
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    JButton b = new JButton("Configure XBee noddes");

    @Override
    public void loadDetails(JPanel details) {
        b.addActionListener(new NodeConfigAction());
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);
    }

    public String name() {
        return "XBee Network";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new XBeeAdapter();
        }
    }

}
