// ConnectionConfig.java
package jmri.jmrix.powerline.insteon2412s;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author	Ken Cameron	Copyright (C) 2011
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

    public void loadDetails(JPanel details) {
        // have to embed the usual one in a new JPanel

        JPanel p = new JPanel();
        super.loadDetails(p);

        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(p);
    }

    public String name() {
        return "Insteon 2412s";
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new SpecificDriverAdapter();
        }
    }

}
