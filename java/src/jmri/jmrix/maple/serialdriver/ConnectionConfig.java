// ConnectionConfig.java
package jmri.jmrix.maple.serialdriver;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.maple.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a layout connection via an
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
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

    JButton b = new JButton("Configure Maple Nodes");

    public void loadDetails(JPanel details) {
        // have to embed the usual one in a new JPanel
        b.addActionListener(new NodeConfigAction());
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);

    }

    public String name() {
        return "Serial";
    }

    protected void setInstance() {
        adapter = SerialDriverAdapter.instance();
    }
}
