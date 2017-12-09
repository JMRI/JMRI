package jmri.jmrix.grapevine.serialdriver;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrix.grapevine.nodeconfig.NodeConfigAction;

/**
 * Definition of objects to handle configuring a Grapevine layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007
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

    JButton b = new JButton("Configure nodes");

    @Override
    public void loadDetails(JPanel details) {

        jmri.jmrix.grapevine.GrapevineSystemConnectionMemo memo = (jmri.jmrix.grapevine.GrapevineSystemConnectionMemo)adapter.getSystemConnectionMemo();

        b.addActionListener(new NodeConfigAction(memo));
        if (!additionalItems.contains(b)) {
            additionalItems.add(b);
        }
        super.loadDetails(details);

    }

    @Override
    public String name() {
        return "Grapevine (ProTrak) Layout Bus";
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

    @Override
    protected void setInstance() {
        if(adapter == null) { 
           adapter = new SerialDriverAdapter();
        }
    }
}
