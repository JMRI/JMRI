// ConnectionConfig.java
package jmri.jmrix.cmri.serial.networkdriver;

import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Definition of objects to handle configuring a layout connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2015
 * @version	$Revision: 28746 $
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "Network Interface";

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    public String name() {
        return NAME;
    }

    JButton b = new JButton("Configure C/MRI nodes");

    public void loadDetails(JPanel details) {

        b.addActionListener(new NodeConfigAction((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);

    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }
}
